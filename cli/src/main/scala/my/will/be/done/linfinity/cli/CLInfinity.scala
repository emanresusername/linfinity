package my.will.be.done.linfinity.cli

import my.will.be.done.linfinity.model._

object CLInfinity extends App {
  def display(row: Row, conf: Conf): Unit = {
    val lindexes = row.lindexes.groupBy(_.index).mapValues(_.map(_.lin))
    for {
      index ← 0 until row.width
    } {
      lindexes.get(index) match {
        case Some(Seq(one)) ⇒
          val color = one.color
          print(fansi.Color.True(color.red, color.green, color.blue)(one.display.toString))
        case Some(Seq(_, _ *)) ⇒
          print(conf.collideDisplay)
        case _ ⇒
          print(conf.blankDisplay)
      }
    }
    println
  }

  OptionParser.parse(args, Conf) match {
    case Some(conf) ⇒
      for {
        row ← Iterator.iterate(Row(conf))(_.next).takeWhile(_.lindexes.nonEmpty)
      } {
        display(row, conf)
        Thread.sleep(conf.rowDelay.toMillis)
      }
    case None ⇒
  }
}
