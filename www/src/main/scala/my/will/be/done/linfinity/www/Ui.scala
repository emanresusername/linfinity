package my.will.be.done.linfinity.www

import scala.scalajs.js.annotation.JSExport
import com.thoughtworks.binding.dom
import com.thoughtworks.binding.Binding
import com.thoughtworks.binding.Binding._
import org.scalajs.dom.raw.Node
import org.scalajs.dom.html
import my.will.be.done.linfinity.model._
import my.will.be.done.linfinity.www.Setting._
import scala.scalajs.js.timers.{setInterval, clearInterval}
import scala.concurrent.duration._
import org.scalajs.dom.raw.{HTMLInputElement, Event}
import com.softwaremill.quicklens._

trait Ui {
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
  def settingToggle(label: String, setting: Setting[Boolean]) = {
    val settingVar = setting.variable
    <div onmouseover={mouseoverInfo(setting)}>
      <label>{label}</label>
      <input type="checkbox"
    onchange={inputEventHandler(settingVar := _.checked)}
    checked={settingVar.bind}
      ></input>
      </div>
  }

  def mouseoverInfo[V](setting: Setting[V]): Event ⇒ Unit = { event: Event ⇒
    info := s"${setting.description.capitalize}. Default value: ${setting.default}"
  }

  @dom def confPanel: Binding[Node] = {
    <div class={InlineStyles.settingsContainer.htmlClass}>
      {settingToggle("show lineages", ShowLineages).bind}
      <div onmouseover={mouseoverInfo(Width)}>
      <label>width: </label>
      {widthInputElem(Width).bind}
      </div>
      <div onmouseover={mouseoverInfo(BlankDisplay)}>
      <label>blank character: </label>
      {charInputElem(BlankDisplay).bind}
      </div>
      <div onmouseover={mouseoverInfo(CollideDisplay)}>
      <label>collide character: </label>
      {charInputElem(CollideDisplay).bind}
      </div>
      <div onmouseover={mouseoverInfo(InitialNumLins)}>
      <label>initial number of lins: </label>
      {intInputElem(InitialNumLins).bind}
      </div>
      <div onmouseover={mouseoverInfo(RowDelay)}>
      <label>row delay: </label>
      {rowDelayInput.bind}
      </div>
      <div onmouseover={mouseoverInfo(Split)}>
      <label>split chance: </label>
      {chanceInputElem(Split).bind}
      </div>
      <div onmouseover={mouseoverInfo(Merge)}>
      <label>merge chance: </label>
      {chanceInputElem(Merge).bind}
      </div>
      <div onmouseover={mouseoverInfo(Die)}>
      <label>die chance: </label>
      {chanceInputElem(Die).bind}
      </div>
      <div onmouseover={mouseoverInfo(Mutate)}>
      <label>mutate chance: </label>
      {chanceInputElem(Mutate).bind}
      </div>
      <div onmouseover={mouseoverInfo(LinDisplays)}>
      <label>lin characters: </label>
      {stringInputElem(LinDisplays).bind}
      </div>
      <div onmouseover={mouseoverInfo(RowHistory)}>
      <label>row history: </label>
      {intInputElem(RowHistory).bind}
      </div>
      {settingToggle("reverse direction", ReverseDirection).bind}
      {settingToggle("show setting desciptions", ShowDescriptions).bind}
      </div>
  }

  def cssColor(lin: Lin): String = {
    val color = lin.color
    s"color: rgb(${color.red}, ${color.green}, ${color.blue})"
  }

  @dom
  def display(row: Row): Binding[Node] = {
    <div class={InlineStyles.row.htmlClass}>{
        Constants(row.chunks.toStream:_*).map {
          case Right(one) if one.length == 1 ⇒
            val lin = one.head.lin
              <span style={cssColor(lin)}>{
                lin.display.toString
              }</span>
          case Right(more) if more.length > 1 ⇒
            <span>{
              CollideDisplay.bind.toString
            }</span>
          case Left(length) ⇒
            <span>{
              BlankDisplay.bind.toString * length
            }</span>
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
    if (ReverseDirection.get) {
      history.+=:(row)
    } else {
      history += row
    }
  }

  @dom
  def trimHistory: Unit = {
    val currentNumRows = rowHistory.length.bind
    val allowedNumRows = RowHistory.bind
    if (currentNumRows > allowedNumRows) {
      val trimLength = currentNumRows - allowedNumRows
      val rows       = rowHistory.get
      if (ReverseDirection.bind) {
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
    val rowDelay    = RowDelay.bind
    val rowInterval = setInterval(rowDelay.toMillis)(onRowInterval)
    val changeHandler = inputEventHandler { input ⇒
      val newDuration = Duration(input.value)
      if (newDuration != rowDelay) {
        clearInterval(rowInterval)
        RowDelay := newDuration
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
  def statusPanel = {
    <div class={InlineStyles.statusPanel.htmlClass}>
      <div class={InlineStyles.controlButtons.htmlClass}>
        <div onclick={event: Event ⇒
          nextRow := (for {
            row ← nextRow.get
          } yield {
            val lindexes = for {
              (lindex, index) ← row.lindexes.zipWithIndex
            } yield {
              lindex.modify(_.lin.lineage).setTo(index)
            }
            row.modify(_.lindexes).setTo(lindexes)
          })
        }>Reset Lineages</div>
      </div>
      {
        nextRow.bind match {
          case Some(row) ⇒
            val lineages = row.lindexes.map(_.lin).groupBy(_.lineage)
              <div class={InlineStyles.lineages.htmlClass}> {
                Constants(lineages.toSeq:_*).map {
                  case (lineage, lins) ⇒
                    lineageStatus(lineage, lins).bind
                }
              } </div>
          case None ⇒
            <!-- -->
        }
      }
    </div>
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
    if (ShowDescriptions.bind) {
      <div class={InlineStyles.infoPanel.htmlClass}>{info.bind}</div>
    } else {
      <!-- -->
    }
  }

  @dom
  def render: Binding[Node] = {
    <div class={InlineStyles.mainContainer.htmlClass}>
      <div class={InlineStyles.confPanel.htmlClass}>
       { confPanel.bind }
       { controlButtons.bind }
       { infoPanel.bind }
      </div>
      {
        if(ShowLineages.bind && !isStopped.bind) {
          statusPanel.bind
        } else {
          <!-- -->
        }
      }
      { rowsPanel.bind }
    </div>
  }
}

@JSExport
object Ui extends Ui {
  def apply(uiContainer: Node, stylesContainer: Node): Unit = {
    css.render(stylesContainer)
    dom.render(uiContainer, render)
  }
}
