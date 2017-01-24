package my.will.be.done.linfinity

import org.scalajs.dom.document
import org.scalajs.dom.window.console
import com.thoughtworks.binding.dom
import com.thoughtworks.binding.Binding
import com.thoughtworks.binding.Binding._
import org.scalajs.dom.raw.Node
import scala.scalajs.js
import org.scalajs.dom.html
import my.will.be.done.linfinity.model._
import scala.scalajs.js.timers.{setInterval, clearInterval}
import scala.concurrent.duration._
import org.scalajs.dom.raw.{HTMLInputElement, Event}
import scalacss.Defaults._

object Jsinfinity extends js.JSApp {
  val ContainerId = "linfinity"
  object Settings {
    val width          = Var(50)
    val blankDisplay   = Var('_')
    val collideDisplay = Var('X')
    val initialNumLins = Var(3)
    val rowDelay       = Var(100.millis)
    val linDisplays    = Var("0123456789")
    val split          = Var(0.005)
    val merge          = Var(0.25)
    val die            = Var(0.002)
    val mutate         = Var(0.0075)
    val rowHistory     = Var(25)
  }

  def conf: Conf = {
    import Settings._
    Conf(
      width = width.get,
      blankDisplay = blankDisplay.get,
      collideDisplay = collideDisplay.get,
      initialNumLins = initialNumLins.get,
      rowDelay = rowDelay.get,
      chances = Chances(
        split = split.get,
        merge = merge.get,
        die = die.get,
        mutate = mutate.get
      ),
      linDisplays = linDisplays.get
    )
  }

  @dom
  def inputElem[V](value: Var[V], deserializeValue: String ⇒ V, serializeValue: V ⇒ String = {
    v: V ⇒
      v.toString
  }, additionalHandling: HTMLInputElement ⇒ Unit = _ ⇒ Unit): Binding[HTMLInputElement] = {
    <input
    value={serializeValue(value.bind)}
    onchange={inputEventHandler { input ⇒
                value := deserializeValue(input.value)
                additionalHandling(input)
              } }
      ></input>
  }

  def intInputElem(value: Var[Int]): Binding[HTMLInputElement] = {
    inputElem(value, _.toInt)
  }

  def widthInputElem(value: Var[Int]): Binding[HTMLInputElement] = {
    inputElem(value, _.toInt, additionalHandling = { _ ⇒
      updateRowWidth
    })
  }

  def stringInputElem(value: Var[String]): Binding[HTMLInputElement] = {
    inputElem[String](value, identity, identity)
  }

  def charInputElem(value: Var[Char]): Binding[HTMLInputElement] = {
    inputElem(value, _.head)
  }

  def chanceInputElem(value: Var[Double]): Binding[HTMLInputElement] = {
    inputElem(value, _.toDouble, additionalHandling = { _ ⇒
      updateLinChances
    })
  }

  val DurationRegex = """\s*(\d+)\s*(\w+)\s*""".r
  def durationFromString(string: String): FiniteDuration = {
    string match {
      case DurationRegex(length, unit) ⇒
        Duration(length.toLong, unit)
    }
  }

  def inputEventHandler(handler: HTMLInputElement ⇒ Unit): Event ⇒ Unit = { event: Event ⇒
    event.currentTarget match {
      case input: HTMLInputElement ⇒
        handler(input)
    }
  }

  @dom def confPanel: Binding[Node] = {
    import Settings._
    <div class={InlineStyles.settingsContainer.htmlClass}>
      <div>
      <label>width: </label>
      {widthInputElem(width).bind}
      </div>
      <div>
      <label>blank character: </label>
      {charInputElem(blankDisplay).bind}
      </div>
      <div>
      <label>collide character: </label>
      {charInputElem(collideDisplay).bind}
      </div>
      <div>
      <label>initial number of lins: </label>
      {intInputElem(initialNumLins).bind}
      </div>
      <div>
      <label>row delay: </label>
      {rowDelayInput.bind}
      </div>
      <div>
      <label>split chance: </label>
      {chanceInputElem(split).bind}
      </div>
      <div>
      <label>merge chance: </label>
      {chanceInputElem(merge).bind}
      </div>
      <div>
      <label>die chance: </label>
      {chanceInputElem(die).bind}
      </div>
      <div>
      <label>mutate chance: </label>
      {chanceInputElem(mutate).bind}
      </div>
      <div>
      <label>lin characters: </label>
      {stringInputElem(linDisplays).bind}
      </div>
      <div>
      <label>row history: </label>
      {intInputElem(rowHistory).bind}
      </div>
      </div>
  }

  def cssColor(lin: Lin): String = {
    val color = lin.color
    Seq(color.red, color.green, color.blue).foldLeft("color: #") {
      case (string, value) ⇒
        string ++ value.toHexString
    }
  }

  @dom
  def display(row: Row): Binding[Node] = {
    val lindexes = row.lindexes.groupBy(_.index).mapValues(_.map(_.lin))
    <div>{
        Constants(0 until row.width:_*).map { index ⇒
          lindexes.get(index) match {
            case Some(one) if one.length == 1 ⇒
              val lin = one.head
                <span style={cssColor(lin)}>{
                  lin.display.toString
                }</span>
            case Some(more) if more.length > 1 ⇒
              <span>{
                Settings.collideDisplay.bind.toString
              }</span>
            case _ ⇒
              <span>{
                Settings.blankDisplay.bind.toString
              }</span>
          }
        }
      }</div>
  }

  def updateLinChances: Unit = {
    val rows       = this.rows.get
    val updatedRow = rows.head.mapLins(_.copy(chances = conf.chances))
    rows.update(0, updatedRow)
  }

  def updateRowWidth: Unit = {
    val rows = this.rows.get
    rows.update(0, rows.head.copy(width = conf.width))
  }

  val rows      = Vars.empty[Row]
  val isPaused  = Var(false)
  val isStopped = Var(true)

  def addNextRow(): Unit = {
    if (!isPaused.get) {
      val rows = this.rows.get
      if (rows.nonEmpty) {
        val lastRow = rows.head
        if (lastRow.lindexes.nonEmpty) {
          rows.+=:(lastRow.next)
          val currentNumRows = rows.length
          val allowedNumRows = Settings.rowHistory.get
          if (currentNumRows > allowedNumRows) {
            rows.trimEnd(currentNumRows - allowedNumRows)
          }
        } else {
          isStopped := true
        }
      }
    }
  }

  @dom
  def rowDelayInput: Binding[HTMLInputElement] = {
    val rowDelay    = Settings.rowDelay.bind
    val rowInterval = setInterval(rowDelay)(addNextRow)
    val changeHandler = inputEventHandler { input ⇒
      val newDuration = durationFromString(input.value)
      if (newDuration != rowDelay) {
        clearInterval(rowInterval)
        Settings.rowDelay := newDuration
      }
    }

    <input value={rowDelay.toString} onchange={changeHandler}/>
  }

  def restartRows: Unit = {
    val rows = this.rows.get
    rows.clear
    rows += Row(conf)
    isStopped := false
    isPaused := false
  }

  @dom
  def controlButtons: Binding[Node] = {
    <div class={InlineStyles.controlButtons.htmlClass}>
      <div onclick={event: Event ⇒ restartRows}>
      {if (isStopped.bind) "Start" else "Restart"}
      </div>
      <div onclick={event: Event ⇒ isPaused := !isPaused.get}>
      {if (isPaused.bind) "Unpause" else "Pause"}
      </div>
      </div>
  }

  @dom
  def linStatus(lin: Lin): Binding[Node] = {
    val style = Seq(
      cssColor(lin)
    ).mkString(";")
    <div style={style}>{lin.display.toString}</div>
  }

  @dom
  def lineageStatus(lineage: Int, lins: Seq[Lin]): Binding[Node] = {
    <div>
      <div>Lineage {lineage.toString}:</div>
      <div class={InlineStyles.linStatuses.htmlClass}>{
        Constants(lins.sortBy(-_.age):_*).map { lin ⇒
          linStatus(lin).bind
        }
      }</div>
      </div>
  }

  @dom
  def statusPanel: Binding[Node] = {
    if (rows.length.bind > 0) {
      val lineages = rows.bind.head.lindexes.map(_.lin).groupBy(_.lineage)
      <div>{
          Constants(lineages.toSeq:_*).map {
            case (lineage, lins) ⇒
              lineageStatus(lineage, lins).bind
          }
        }</div>
    } else {
      <!-- no rows yet -->
    }
  }

  @dom
  def rowsPanel: Binding[Node] = {
    if (rows.length.bind > 0) {
      <div class={InlineStyles.rowsPanel.htmlClass}>
        {
          rows.map {row ⇒
            display(row).bind
          }
        }
        </div>
    } else {
      <!-- no rows yet -->
    }
  }

  @dom
  def render: Binding[Node] = {
    <div>
      <style>{InlineStyles.render[String]}</style>
      <style>{StandaloneStyles.render[String]}</style>
      <div class={InlineStyles.mainContainer.htmlClass}>
      <div class={InlineStyles.confPanel.htmlClass}>
      { statusPanel.bind }
      { confPanel.bind }
      { controlButtons.bind }
      </div>
      { rowsPanel.bind }
      </div>
      </div>
  }

  def main(): Unit = {
    dom.render(document.getElementById(ContainerId), render)
  }
}
