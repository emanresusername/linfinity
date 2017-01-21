package my.will.be.done.linfinity.model

import scala.concurrent.duration.FiniteDuration

case class Conf(
    width: Int,
    blankDisplay: Char,
    collideDisplay: Char,
    initialNumLins: Int,
    rowDelay: FiniteDuration,
    chances: Chances,
    linDisplays: Seq[Char]
)
