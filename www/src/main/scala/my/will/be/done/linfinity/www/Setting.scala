package my.will.be.done.linfinity.www

import my.will.be.done.linfinity.model.{Setting ⇒ SealedSetting, Chances, Conf}
import com.thoughtworks.binding.Binding.Var
import scala.language.implicitConversions
import enumeratum._, EnumEntry._

sealed class Setting[V](val default: V, val description: String)
    extends EnumEntry
    with Hyphencase {

  def this(setting: SealedSetting[V]) = {
    this(setting.default, setting.description)
  }

  val variable = Var(default)

  def get: V = variable.get
}

object Setting extends Enum[Setting[_]] {
  implicit def settingToVar[V](setting: Setting[V]): Var[V] = {
    setting.variable
  }

  val values = findValues

  case object Width            extends Setting(SealedSetting.Width)
  case object BlankDisplay     extends Setting(SealedSetting.BlankDisplay)
  case object CollideDisplay   extends Setting(SealedSetting.CollideDisplay)
  case object InitialNumLins   extends Setting(SealedSetting.InitialNumLins)
  case object RowDelay         extends Setting(SealedSetting.RowDelay)
  case object LinDisplays      extends Setting(SealedSetting.LinDisplays)
  case object Split            extends Setting(SealedSetting.SplitChance)
  case object Merge            extends Setting(SealedSetting.MergeChance)
  case object Die              extends Setting(SealedSetting.DieChance)
  case object Mutate           extends Setting(SealedSetting.MutateChance)
  case object RowHistory       extends Setting(25, "how many rows will stay on screen")
  case object ReverseDirection extends Setting(false, "false: top to bottom. true: bottom to top")
  case object ShowDescriptions
      extends Setting(true, "show explanations for the moused over settings")
  case object ShowLineages extends Setting(true, "show info on the currently living lineages")

  def conf: Conf = {
    Conf(
      width = Width.get,
      blankDisplay = BlankDisplay.get,
      collideDisplay = CollideDisplay.get,
      initialNumLins = InitialNumLins.get,
      rowDelay = RowDelay.get,
      chances = Chances(
        split = Split.get,
        merge = Merge.get,
        die = Die.get,
        mutate = Mutate.get
      ),
      linDisplays = LinDisplays.get
    )
  }
}
