package my.will.be.done.linfinity.www

import chrome.windows.bindings.{Window, CreateOptions}, Window.CreateType.PANEL
import chrome.windows.Windows
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import org.scalajs.dom.{window, document}
import chrome.runtime.Runtime
import scala.scalajs.js.timers.setInterval
import scala.concurrent.duration._

object Webextinfinity extends js.JSApp {
  val SaveSettingsInterval = 10.seconds
  def createWindowOptions: CreateOptions = {
    val width  = 1000
    val height = 600
    val options = CreateOptions(
      url = js.Array("html/linfinity.html"),
      width = width,
      height = height,
      `type` = PANEL
    ).asInstanceOf[js.Dictionary[_]]

    // TODO: `focused` isn't supported in firefox
    options.delete("focused")
    options.asInstanceOf[CreateOptions]
  }

  def main(): Unit = {
    for {
      backgroundPage ← Runtime.getBackgroundPage
    } {
      if (window == backgroundPage) {
        BrowserAction.onClicked.addListener { tab ⇒
          Windows.create(createWindowOptions)
        }
      } else {
        Store.loadSettings.onComplete {
          case attempt ⇒
            attempt.failed.foreach { cause ⇒
              println(s"failed to load settings: $cause")
            }
            Ui(document.body, document.head)
            setInterval(SaveSettingsInterval)(Store.saveSettings)
        }
      }
    }
  }
}
