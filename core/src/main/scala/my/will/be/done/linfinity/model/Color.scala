package my.will.be.done.linfinity.model

import scala.util.Random

case class Color(red: Int, green: Int, blue: Int)

object Color {
  def random: Color = {
    def randomValue = Random.nextInt(256)
    Color(
      red = randomValue,
      green = randomValue,
      blue = randomValue
    )
  }
}
