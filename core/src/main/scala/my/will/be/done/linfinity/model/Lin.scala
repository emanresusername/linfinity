package my.will.be.done.linfinity.model

import scala.util.Random

case class Lin(
    display: Char,
    displays: Seq[Char],
    color: Color,
    speed: Int,
    chances: Chances,
    lineage: Int,
    age: Int = 0
) extends Point(display, color) {
  import Lin._

  def bounce: Lin = {
    copy(
      speed = -speed
    )
  }

  def collide: Seq[Lin] = {
    Random.nextDouble match {
      case merge if merge < chances.merge ⇒
        Nil
      case _ ⇒
        Seq(bounce)
    }
  }

  def nextgen: Seq[Lin] = {
    val older = copy(age = age + 1)
    Random.nextDouble match {
      case split if split < chances.split ⇒
        Seq(older, older.bounce.copy(age = 0))
      case mutate if mutate < chances.mutate ⇒
        Seq(
          older.copy(
            color = cointoss(color, Color.random),
            display = cointoss(display, randomDisplay(displays))
          ))
      case die if die < chances.die * Math.log(age) ⇒
        Nil
      case _ ⇒
        Seq(older)
    }
  }
}

object Lin {
  def randomDisplay(displays: Seq[Char]): Char = {
    displays(Random.nextInt(displays.length))
  }

  def cointoss[T](heads: T, tails: T): T = {
    if (Random.nextBoolean) heads else tails
  }

  def random(displays: Seq[Char], chances: Chances, lineage: Int): Lin = {
    Lin(
      chances = chances,
      displays = displays,
      display = randomDisplay(displays),
      color = Color.random,
      speed = cointoss(1, -1),
      lineage = lineage
    )
  }
}
