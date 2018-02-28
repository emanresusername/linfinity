package my.will.be.done.linfinity.cli

import my.will.be.done.linfinity.model._

object Clinfinity extends App {
  def display(row: Row, conf: Conf): Unit = {
    for {
      chunk ← row.chunks
    } {
      chunk match {
        case Right(Seq(one)) ⇒
          val lin = one.lin
          val color = lin.color
          print(
            fansi.Color.True(color.red, color.green, color.blue)(
              lin.display.toString))
        case Right(Seq(_, _*)) ⇒
          print(conf.collideDisplay)
        case Left(length) ⇒
          print(conf.blankDisplay.toString * length)
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
