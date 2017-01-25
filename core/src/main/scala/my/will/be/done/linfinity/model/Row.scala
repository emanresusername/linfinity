package my.will.be.done.linfinity.model

import Row.Lindex
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
    val newLindexes = lindexes.groupBy(_.index).foldLeft(Seq.empty[Lindex]) {
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
}

object Row {
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
