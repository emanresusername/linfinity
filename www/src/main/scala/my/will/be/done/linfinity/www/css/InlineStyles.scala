package my.will.be.done.linfinity.www

import scalacss.Defaults._

object InlineStyles extends StyleSheet.Inline {
  import dsl._

  val mainPanelCommon = style(
    borderColor.black,
    borderStyle.solid,
    borderWidth(1.px),
    padding(1.em)
  )

  val confPanel = style(
    mainPanelCommon,
    display.flex,
    flexDirection.column,
    justifyContent.spaceAround,
    borderTopLeftRadius(1.em),
    borderBottomLeftRadius(1.em),
    width(30.vw)
  )

  val rowsPanel = style(
    mainPanelCommon,
    display.flex,
    flexDirection.column,
    justifyContent.center,
    borderTopRightRadius(1.em),
    borderBottomRightRadius(1.em)
  )

  val mainContainer = style(
    display.flex,
    height(95.vh),
    width(95.vw)
  )

  val settingsContainer = style(
    display.flex,
    flexDirection.column,
    justifyContent.spaceBetween,
    alignItems.flexEnd,
    flex := "10"
  )

  val settingBorderRadius = style(
    borderRadius(5.px, 15.px, 15.px, 5.px)
  )

  val setting = style(
    marginTop(1.pt),
    marginBottom(1.pt),
    settingBorderRadius,
    &.hover(
      boxShadow := "2px 0 2px 1px rgba(0, 50, 100, 0.52)"
    )
  )

  val settingInput = style(
    settingBorderRadius,
    borderStyle.double,
    paddingLeft(1.ex)
  )

  val settingLabel = style(
    fontVariantCaps.allSmallCaps
  )

  val controlButtons = style(
    display.flex,
    justifyContent.spaceAround
  )

  val controlButton = style(
    backgroundColor(c"#eee"),
    padding(3.px, 1.em, 3.px, 1.em),
    borderRadius(1.ex),
    borderStyle.inset,
    cursor.pointer,
    borderColor.lightgray,
    borderWidth(2.pt),
    &.hover(
      borderStyle.outset,
      backgroundColor(c"#ddd")
    )
  )

  val linStatuses = style(
    display.flex,
    justifyContent.spaceAround,
    flexWrap.wrap
  )

  val infoPanel = style(
    flex := "1.5",
    borderTopStyle.double,
    borderTopColor(c"#aaf"),
    borderTopWidth(2.px),
    marginTop(4.px),
    paddingTop(4.px),
    overflowY.scroll
  )

  val lineages = style(
    overflowY.scroll,
    flex := "5"
  )
}
