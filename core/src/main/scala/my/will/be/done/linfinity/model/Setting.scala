package my.will.be.done.linfinity.model

import scala.concurrent.duration._

sealed abstract class Setting[V](val default: V, val description: String)

object Setting {
  case object Width extends Setting(80, "the number of characters in each row")
  case object BlankDisplay
      extends Setting(
        '_',
        "the characters that fill the space between/around the lins")
  case object CollideDisplay
      extends Setting('X',
                      "the character that shows when 2 or more lins collide")
  case object InitialNumLins
      extends Setting(3, "the number of lins to (re)start the game with")
  case object RowDelay
      extends Setting[Duration](
        100.millis,
        "how long to pause between rows. the lower the duration the faster the game")
  case object SplitChance
      extends Setting(0.005d, "chance a lin will split in two on each row")
  case object MutateChance
      extends Setting(
        0.0075d,
        "chance a lin will mutate (change color or character, etc) on each row")
  case object MergeChance
      extends Setting(
        0.25d,
        "chance a lin will merge into another lin it collides with effectively dying. if both colliding lins merge, they both die")
  case object DieChance
      extends Setting(
        0.002d,
        "chance a lin will die each row. function of the age of the lin")
  case object LinDisplays
      extends Setting("0123456789", "the characters that can represent a lin")
}
