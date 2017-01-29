package my.will.be.done.linfinity.model

import Row.{Lindex, BlanksOrLindexs}
import scala.util.Random
import com.softwaremill.quicklens._

case class Row(
    lindexes: Seq[Lindex],
    width: Int
) {
  def moveLins: Row = {
    this.modify(_.lindexes.each).using(_.moveLin)
  }

  def bounceOutOfBoundsLins: Row = {
    this.modify(_.lindexes.each).using { lindex ⇒
      val lin         = lindex.lin
      val index       = lindex.index
      val speed       = lin.speed
      val bounceRight = index <= 0 && speed < 0
      val bounceLeft  = index >= width - 1 && speed > 0
      if (bounceRight || bounceLeft) {
        lindex.copy(lin = lin.bounce)
      } else {
        lindex
      }
    }
  }

  def collideLins: Row = {
    val newLindexes = lindexMap.foldLeft(Seq.empty[Lindex]) {
      case (collided, (_, lindexGroup)) ⇒
        if (lindexGroup.length < 2) {
          collided ++ lindexGroup
        } else {
          collided ++ (for {
            lindex ← lindexGroup
            lin    ← lindex.lin.collide
          } yield {
            lindex.copy(lin = lin)
          })
        }
    }

    copy(lindexes = newLindexes)
  }

  def propagateLins: Row = {
    copy(
      lindexes = (for {
        lindex ← lindexes
        lin    ← lindex.lin.nextgen
      } yield {
        lindex.copy(lin = lin)
      })
    )
  }

  def next: Row = {
    bounceOutOfBoundsLins.moveLins.collideLins.propagateLins
  }

  def lindexMap: Map[Int, Seq[Lindex]] = {
    lindexes.groupBy(_.index)
  }

  def indexes: Range = {
    0 until width
  }

  def chunks: Iterator[BlanksOrLindexs] = {
    val map = lindexMap
    Iterator
      .iterate[((Int, Option[Seq[Lindex]]), Seq[Int])]((0, None) -> indexes) {
        case (_, indexsToSpan) =>
          val (blankIndexs, indexsAfterBlanks) = indexsToSpan.span(map.get(_).isEmpty)
          val blankLength                      = blankIndexs.length
          indexsAfterBlanks match {
            case Seq(indexWithLins, remainingIndexs @ _ *) =>
              ((blankLength, map.get(indexWithLins)), remainingIndexs)
            case Nil =>
              ((blankLength, None), Nil)
          }
      }
      .drop(1)
      .takeWhile {
        case ((blankLength, _), remainingIndexs) =>
          blankLength > 0 || remainingIndexs.nonEmpty
      }
      .flatMap {
        case ((blankLength, lindexes), _) ⇒
          (if (blankLength > 0) {
             Seq(Left(blankLength))
           } else {
             Nil
           }) ++ lindexes.map(Right(_))
      }
  }
}

object Row {
  type BlanksOrLindexs = Either[Int, Seq[Lindex]]

  case class Lindex(
      lin: Lin,
      index: Int
  ) {
    def moveLin: Lindex = {
      copy(
        index = index + lin.speed
      )
    }
  }

  def apply(conf: Conf): Row = {
    val width = conf.width
    Row(
      width = width,
      lindexes = for {
        lineage ← 1 to conf.initialNumLins
      } yield {
        Lindex(
          lin = Lin.random(conf.linDisplays, conf.chances, lineage),
          index = Random.nextInt(width)
        )
      }
    )
  }
}
