package my.will.be.done.linfinity.www

import scala.scalajs.js
import org.scalajs.dom.document

object Jsinfinity extends js.JSApp {
  val ContainerId = "linfinity"

  def main(): Unit = {
    Ui(document.getElementById(ContainerId))
  }
}
