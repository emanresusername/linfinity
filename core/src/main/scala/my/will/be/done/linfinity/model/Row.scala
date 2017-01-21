package my.will.be.done.linfinity.model

import Row.Lindex
import scala.util.Random

case class Row(
    lindexes: Seq[Lindex],
    width: Int
) {
  def mapLindexes(mapper: Lindex ⇒ Lindex): Row = {
    copy(
      lindexes = lindexes.map(mapper)
    )
  }

  def mapLins(mapper: Lin ⇒ Lin): Row = {
    mapLindexes { lindex ⇒
      lindex.copy(lin = mapper(lindex.lin))
    }
  }

  def flatmapLins(mapper: Lin ⇒ Seq[Lin]): Row = {
    copy(
      lindexes = for {
        lindex ← lindexes
        lin    ← mapper(lindex.lin)
      } yield {
        lindex.copy(lin = lin)
      }
    )
  }

  def moveLins: Row = {
    mapLindexes(_.moveLin)
  }

  def bounceOutOfBoundsLins: Row = {
    mapLindexes { lindex ⇒
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
    flatmapLins(_.nextgen)
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
        _ ← 1 to conf.initialNumLins
      } yield {
        Lindex(
          lin = Lin.random(conf.linDisplays, conf.chances),
          index = Random.nextInt(width)
        )
      }
    )
  }
}
