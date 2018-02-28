package my.will.be.done.linfinity.www

import scala.scalajs.js.annotation.JSExportTopLevel
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
import my.will.be.done.linfinity.www.Setting.conf

trait Ui {
  @dom
  def inputElem[V](value: Var[V],
                   deserializeValue: String ⇒ V,
                   serializeValue: V ⇒ String = { v: V ⇒
                     v.toString
                   },
                   additionalHandling: HTMLInputElement ⇒ Unit = _ ⇒ Unit)
    : Binding[HTMLInputElement] = {
    <input
    value={serializeValue(value.bind)}
    onchange={inputEventHandler { input ⇒
                value value_= deserializeValue(input.value)
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

  def inputEventHandler(handler: HTMLInputElement ⇒ Unit): Event ⇒ Unit = {
    event: Event ⇒
      event.currentTarget match {
        case input: HTMLInputElement ⇒
          handler(input)
      }
  }

  @dom
  def settingToggle(label: String, setting: Setting[Boolean]) = {
    val settingVar = setting.value
    <div onmouseover={mouseoverInfo(setting)}>
      <label>{label}</label>
      <input type="checkbox"
    onchange={inputEventHandler(settingVar value_= _.checked)}
    checked={settingVar.bind}
      ></input>
      </div>
  }

  def mouseoverInfo[V](setting: Setting[V]): Event ⇒ Unit = { event: Event ⇒
    info value_= s"${setting.description.capitalize}. Default value: ${setting.default}"
  }

  @dom def confPanel: Binding[Node] = {
    import Setting._
    <div class={InlineStyles.settingsContainer.htmlClass}>
      {settingToggle("show lineages", showLineages).bind}
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
      {settingToggle("reverse direction", reverseDirection).bind}
      {settingToggle("show setting desciptions", showDescriptions).bind}
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
              Setting.collideDisplay.bind.toString
            }</span>
          case Left(length) ⇒
            <span>{
              Setting.blankDisplay.bind.toString * length
            }</span>
        }
      }</div>
  }

  def updateLinChances: Unit = {
    nextRow value_= (
      for {
        row ← nextRow.value
      } yield {
        row.modify(_.lindexes.each.lin.chances).setTo(conf.chances)
      }
    )
  }

  def updateRowWidth: Unit = {
    nextRow value_= (
      for {
        row ← nextRow.value
      } yield {
        row.copy(width = conf.width)
      }
    )
  }

  val nextRow = Var[Option[Row]](None)
  val rowHistory = Vars.empty[Row]
  val isPaused = Var(false)
  val isStopped = Var(true)
  val info = Var("")

  def addToHistory(row: Row): Unit = {
    val history = rowHistory.value
    if (Setting.reverseDirection.get) {
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
      val rows = rowHistory.value
      if (Setting.reverseDirection.bind) {
        rows.trimEnd(trimLength)
      } else {
        rows.trimStart(trimLength)
      }
    }
  }

  def onRowInterval(): Unit = {
    for {
      row ← nextRow.value
      if (!isPaused.value)
    } {
      if (row.lindexes.nonEmpty) {
        nextRow value_= Option(row.next)
        val rows = rowHistory.value
        addToHistory(row)
        trimHistory
      } else {
        isStopped value_= true
      }
    }
  }

  @dom
  def rowDelayInput: Binding[HTMLInputElement] = {
    val rowDelay = Setting.rowDelay.bind
    val rowInterval = setInterval(rowDelay.toMillis)(onRowInterval)
    val changeHandler = inputEventHandler { input ⇒
      val newDuration = Duration(input.value)
      if (newDuration != rowDelay) {
        clearInterval(rowInterval)
        Setting.rowDelay value_= newDuration
      }
    }

    <input value={rowDelay.toString} onchange={changeHandler}/>
  }

  def restartRows: Unit = {
    rowHistory.value.clear
    nextRow value_= Option(Row(conf))
    isStopped value_= false
    isPaused value_= false
  }

  def stop: Unit = {
    isStopped value_= true
    rowHistory.value.clear
    nextRow value_= None
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
          <div onclick={event: Event ⇒ isPaused value_= !isPaused.value}>
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
          nextRow value_= (for {
            row ← nextRow.value
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
    if (showDescriptions.bind) {
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
        if(showLineages.bind && !isStopped.bind) {
          statusPanel.bind
        } else {
          <!-- -->
        }
      }
      { rowsPanel.bind }
    </div>
  }
}

@JSExportTopLevel("Ui")
object Ui extends Ui {
  def apply(uiContainer: Node, stylesContainer: Node): Unit = {
    css.render(stylesContainer)
    dom.render(uiContainer, render)
  }
}
