package my.will.be.done.linfinity.www

import chrome.app.runtime.bindings.LaunchData
import chrome.app.window.bindings.{BoundsSpecification, CreateWindowOptions}
import chrome.app.window.Window
import chrome.utils.ChromeApp
import org.scalajs.dom.Event
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

object SystemMonitor extends ChromeApp {

  override def onLaunched(launchData: LaunchData): Unit = {
    val options = {
      val width        = 1000
      val height       = 600
      val boundsBorder = 25
      CreateWindowOptions(
        id = "linfinity",
        innerBounds = BoundsSpecification(
          minWidth = width,
          minHeight = height
        ),
        outerBounds = BoundsSpecification(
          minWidth = width + boundsBorder,
          minHeight = height + boundsBorder
        )
      )
    }
    Window.create("html/linfinity.html", options).foreach { window =>
      window.contentWindow.onload = { e: Event ⇒
        val document = window.contentWindow.document
        Ui(document.body, document.head)
      }
    }
  }
}
