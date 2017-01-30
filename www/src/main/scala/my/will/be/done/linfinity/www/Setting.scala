package my.will.be.done.linfinity.www

import my.will.be.done.linfinity.model.{Setting ⇒ SealedSetting, Chances, Conf}, SealedSetting._
import com.thoughtworks.binding.Binding.Var
import scala.language.implicitConversions

case class Setting[V](default: V, description: String, value: Var[V]) {
  def get: V = value.get
}

object Setting {
  def apply[V](setting: SealedSetting[V]): Setting[V] = {
    Setting(setting.default, setting.description)
  }

  def apply[V](default: V, description: String): Setting[V] = {
    Setting(default, description, Var(default))
  }

  implicit def settingToVar[V](setting: Setting[V]): Var[V] = {
    setting.value
  }

  val width            = Setting(Width)
  val blankDisplay     = Setting(BlankDisplay)
  val collideDisplay   = Setting(CollideDisplay)
  val initialNumLins   = Setting(InitialNumLins)
  val rowDelay         = Setting(RowDelay)
  val linDisplays      = Setting(LinDisplays)
  val split            = Setting(SplitChance)
  val merge            = Setting(MergeChance)
  val die              = Setting(DieChance)
  val mutate           = Setting(MutateChance)
  val rowHistory       = Setting(25, "how many rows will stay on screen")
  val reverseDirection = Setting(false, "false: top to bottom. true: bottom to top")
  val showDescriptions = Setting(true, "show explanations for the moused over settings")
  val showLineages     = Setting(true, "show info on the currently living lineages")

  def conf: Conf = {
    Conf(
      width = width.get,
      blankDisplay = blankDisplay.get,
      collideDisplay = collideDisplay.get,
      initialNumLins = initialNumLins.get,
      rowDelay = rowDelay.get,
      chances = Chances(
        split = split.get,
        merge = merge.get,
        die = die.get,
        mutate = mutate.get
      ),
      linDisplays = linDisplays.get
    )
  }
}
