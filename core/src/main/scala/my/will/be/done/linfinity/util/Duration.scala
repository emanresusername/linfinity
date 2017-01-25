package my.will.be.done.linfinity.util

import scala.concurrent.duration, duration.FiniteDuration

object Duration {
  val DurationRegex = """\s*(\d+)\s*(\w+)\s*""".r
  def apply(string: String): FiniteDuration = {
    string match {
      case DurationRegex(length, unit) ⇒
        duration.Duration(length.toLong, unit)
    }
  }
}
