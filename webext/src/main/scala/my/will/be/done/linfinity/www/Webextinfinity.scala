package my.will.be.done.linfinity.www

import chrome.windows.bindings.{ Window, CreateOptions }, Window.CreateType.PANEL
import chrome.windows.Windows
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import org.scalajs.dom.document

object Webextinfinity extends js.JSApp {
  def createWindowOptions: CreateOptions = {
    val width        = 1000
    val height       = 600
    CreateOptions(
      url = js.Array("html/linfinity.html"),
      width = width,
      height = height,
      `type` = PANEL
    )
  }

  def main(): Unit = {
    for {
      window ← Windows.getCurrent()
    } {
      if (window.id == 1) {
        BrowserAction.onClicked.addListener { tab ⇒
          Windows.create(createWindowOptions)
        }
      } else {
        Ui(document.body, document.head)
      }
    }
  }
}
