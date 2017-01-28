package my.will.be.done.linfinity.www

import org.scalajs.dom.document
import com.thoughtworks.binding.dom
import com.thoughtworks.binding.Binding
import com.thoughtworks.binding.Binding._
import org.scalajs.dom.raw.Node
import scala.scalajs.js
import org.scalajs.dom.html
import my.will.be.done.linfinity.model._
import my.will.be.done.linfinity.www.Setting._
import my.will.be.done.linfinity.util.Duration
import scala.scalajs.js.timers.{setInterval, clearInterval}
import scala.concurrent.duration._
import org.scalajs.dom.raw.{HTMLInputElement, Event, HTMLStyleElement}
import scalacss.Defaults._
import com.softwaremill.quicklens._
import my.will.be.done.linfinity.www.Setting.conf

object Jsinfinity extends js.JSApp {
  val ContainerId = "linfinity"

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

  def inputEventHandler(handler: HTMLInputElement ⇒ Unit): Event ⇒ Unit = { event: Event ⇒
    event.currentTarget match {
      case input: HTMLInputElement ⇒
        handler(input)
    }
  }

  @dom
  def reverseDirectionInput: Binding[HTMLInputElement] = {
    import Setting.isReversed
    <input type="checkbox"
    onchange={inputEventHandler(isReversed := _.checked)}
    checked={isReversed.bind}
      ></input>
  }

  @dom
  def showDescriptionsInput: Binding[HTMLInputElement] = {
    import Setting.showInfo
    <input type="checkbox"
    onchange={inputEventHandler{showInfo := _.checked}}
    checked={showInfo.bind}
      ></input>
  }

  def mouseoverInfo[V](setting: Setting[V]): Event ⇒ Unit = { event: Event ⇒
    info := s"${setting.description.capitalize}. Default value: ${setting.default}"
  }

  @dom def confPanel: Binding[Node] = {
    import Setting._
    <div class={InlineStyles.settingsContainer.htmlClass}>
      <div onmouseover={mouseoverInfo(width)}>
      <label>width: </label>
      {widthInputElem(width).bind}
      </div>
      <div onmouseover={mouseoverInfo(blankDisplay)}>
      <label>blank character: </label>
      {charInputElem(blankDisplay).bind}
      </div>
      <div onmouseover={mouseoverInfo(collideDisplay)}>
      <label>collide character: </label>
      {charInputElem(collideDisplay).bind}
      </div>
      <div onmouseover={mouseoverInfo(initialNumLins)}>
      <label>initial number of lins: </label>
      {intInputElem(initialNumLins).bind}
      </div>
      <div onmouseover={mouseoverInfo(rowDelay)}>
      <label>row delay: </label>
      {rowDelayInput.bind}
      </div>
      <div onmouseover={mouseoverInfo(split)}>
      <label>split chance: </label>
      {chanceInputElem(split).bind}
      </div>
      <div onmouseover={mouseoverInfo(merge)}>
      <label>merge chance: </label>
      {chanceInputElem(merge).bind}
      </div>
      <div onmouseover={mouseoverInfo(die)}>
      <label>die chance: </label>
      {chanceInputElem(die).bind}
      </div>
      <div onmouseover={mouseoverInfo(mutate)}>
      <label>mutate chance: </label>
      {chanceInputElem(mutate).bind}
      </div>
      <div onmouseover={mouseoverInfo(linDisplays)}>
      <label>lin characters: </label>
      {stringInputElem(linDisplays).bind}
      </div>
      <div onmouseover={mouseoverInfo(Setting.rowHistory)}>
      <label>row history: </label>
      {intInputElem(Setting.rowHistory).bind}
      </div>
      <div onmouseover={mouseoverInfo(isReversed)}>
      <label>reverse direction: </label>
      {reverseDirectionInput.bind}
      </div>
      <div onmouseover={mouseoverInfo(showInfo)}>
      <label>show setting descriptions: </label>
      {showDescriptionsInput.bind}
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
                Setting.collideDisplay.bind.toString
              }</span>
            case _ ⇒
              <span>{
                Setting.blankDisplay.bind.toString
              }</span>
          }
        }
      }</div>
  }

  def updateLinChances: Unit = {
    nextRow := (
      for {
        row ← nextRow.get
      } yield {
        row.modify(_.lindexes.each.lin.chances).setTo(conf.chances)
      }
    )
  }

  def updateRowWidth: Unit = {
    nextRow := (
      for {
        row ← nextRow.get
      } yield {
        row.copy(width = conf.width)
      }
    )
  }

  val nextRow    = Var[Option[Row]](None)
  val rowHistory = Vars.empty[Row]
  val isPaused   = Var(false)
  val isStopped  = Var(true)
  val info       = Var("")

  def addToHistory(row: Row): Unit = {
    val history = rowHistory.get
    if (Setting.isReversed.get) {
      history.+=:(row)
    } else {
      history += row
    }
  }

  @dom
  def trimHistory: Unit = {
    val currentNumRows = rowHistory.length.bind
    val allowedNumRows = Setting.rowHistory.bind
    if (currentNumRows > allowedNumRows) {
      val trimLength = currentNumRows - allowedNumRows
      val rows       = rowHistory.get
      if (Setting.isReversed.bind) {
        rows.trimEnd(trimLength)
      } else {
        rows.trimStart(trimLength)
      }
    }
  }

  def onRowInterval(): Unit = {
    for {
      row ← nextRow.get
      if (!isPaused.get)
    } {
      if (row.lindexes.nonEmpty) {
        nextRow := Option(row.next)
        val rows = rowHistory.get
        addToHistory(row)
        trimHistory
      } else {
        isStopped := true
      }
    }
  }

  @dom
  def rowDelayInput: Binding[HTMLInputElement] = {
    val rowDelay    = Setting.rowDelay.bind
    val rowInterval = setInterval(rowDelay)(onRowInterval)
    val changeHandler = inputEventHandler { input ⇒
      val newDuration = Duration(input.value)
      if (newDuration != rowDelay) {
        clearInterval(rowInterval)
        Setting.rowDelay := newDuration
      }
    }

    <input value={rowDelay.toString} onchange={changeHandler}/>
  }

  def restartRows: Unit = {
    rowHistory.get.clear
    nextRow := Option(Row(conf))
    isStopped := false
    isPaused := false
  }

  def stop: Unit = {
    isStopped := true
    rowHistory.get.clear
    nextRow := None
  }

  @dom
  def controlButtons: Binding[Node] = {
    val stopped = isStopped.bind
    <div class={InlineStyles.controlButtons.htmlClass}>
      <div onclick={event: Event ⇒ restartRows}>
      {if (stopped) "Start" else "Restart"}
      </div>
      {
        if(stopped) {
          <!-- stopped -->
        } else {
          <div onclick={event: Event ⇒ isPaused := !isPaused.get}>
            {if (isPaused.bind) "Unpause" else "Pause"}
          </div>
        }
      }
      {
        if(stopped) {
          <!-- stopped -->
        } else {
          <div onclick={event: Event ⇒ stop}>
            Stop
          </div>
        }
      }
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
    nextRow.bind match {
      case None ⇒
        <!-- -->
      case Some(row) ⇒
        val lineages = row.lindexes.map(_.lin).groupBy(_.lineage)
        <div class={InlineStyles.lineages.htmlClass}>{
            Constants(lineages.toSeq:_*).map {
              case (lineage, lins) ⇒
                lineageStatus(lineage, lins).bind
            }
          }</div>
    }
  }

  @dom
  def rowsPanel: Binding[Node] = {
    if (rowHistory.length.bind > 0) {
      <div class={InlineStyles.rowsPanel.htmlClass}>
        {
          rowHistory.map {row ⇒
            display(row).bind
          }
        }
        </div>
    } else {
      <!-- -->
    }
  }

  @dom
  def infoPanel: Binding[Node] = {
    if (showInfo.bind) {
      <div class={InlineStyles.infoPanel.htmlClass}>{info.bind}</div>
    } else {
      <!-- -->
    }
  }

  @dom
  def render: Binding[Node] = {
    <div class={InlineStyles.mainContainer.htmlClass}>
      <div class={InlineStyles.confPanel.htmlClass}>
       { statusPanel.bind }
       { confPanel.bind }
       { controlButtons.bind }
       { infoPanel.bind }
      </div>
      { rowsPanel.bind }
    </div>
  }

  def main(): Unit = {
    val head = document.head
    head.appendChild(InlineStyles.render[HTMLStyleElement])
    head.appendChild(StandaloneStyles.render[HTMLStyleElement])
    dom.render(document.getElementById(ContainerId), render)
  }
}
