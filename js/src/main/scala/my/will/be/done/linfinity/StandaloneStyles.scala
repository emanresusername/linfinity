package my.will.be.done.linfinity

import scalacss.Defaults._
import InlineStyles._

object StandaloneStyles extends StyleSheet.Standalone {
  import dsl._

  s".${settingsContainer.htmlClass}>div" - (
    setting,
    &("label") - (
      settingLabel
    ),
    &("input") - (
      settingInput
    )
  )

  s".${controlButtons.htmlClass}>div" - (
    controlButton
  )
}
