package my.will.be.done.linfinity.model

import scala.concurrent.duration.Duration

case class Conf(
    width: Int,
    blankDisplay: Char,
    collideDisplay: Char,
    initialNumLins: Int,
    rowDelay: Duration,
    chances: Chances,
    linDisplays: Seq[Char]
)

object Conf
    extends Conf(
      width = Setting.Width.default,
      blankDisplay = Setting.BlankDisplay.default,
      collideDisplay = Setting.CollideDisplay.default,
      initialNumLins = Setting.InitialNumLins.default,
      rowDelay = Setting.RowDelay.default,
      chances = Chances(
        split = Setting.SplitChance.default,
        merge = Setting.MergeChance.default,
        mutate = Setting.MutateChance.default,
        die = Setting.DieChance.default
      ),
      linDisplays = Setting.LinDisplays.default
    )
