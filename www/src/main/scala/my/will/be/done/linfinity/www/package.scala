package my.will.be.done.linfinity.www

import org.scalajs.dom.raw.HTMLStyleElement
import org.scalajs.dom.raw.Node
import scalacss.Defaults._

package object css {
  def render(node: Node): Unit = {
    for {
      styles ← Seq(InlineStyles, StandaloneStyles)
      styleElement = styles.render[HTMLStyleElement]
    } {
      node.appendChild(styleElement)
    }
  }
}
