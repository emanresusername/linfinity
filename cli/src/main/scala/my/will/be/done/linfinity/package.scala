package my.will.be.done.linfinity

import my.will.be.done.linfinity.model._, Setting._
import my.will.be.done.linfinity.cli.BuildInfo
import scala.concurrent.duration.Duration
import com.softwaremill.quicklens._

package object cli {
  val name: String = {
    Seq(
      BuildInfo.organization.split("\\.").last,
      BuildInfo.name
    ).mkString("-")
  }
  val OptionParser = new scopt.OptionParser[Conf](name) {

    head(name, BuildInfo.version)
    def settingText[V](setting: Setting[V]): String = {
      s"${setting.description}. Defaults to ${setting.default}"
    }

    implicit val charRead: scopt.Read[Char] =
      scopt.Read.reads {
        _.getBytes match {
          case Array(char) => char.toChar
          case s =>
            throw new IllegalArgumentException("'" + s + "' is not a char.")
        }
      }

    opt[Int]('w', "width")
      .action { (x, c) =>
        c.copy(width = x)
      }
      .text(settingText(Width))
    opt[Char]('b', "blank-display")
      .action { (x, c) =>
        c.copy(blankDisplay = x)
      }
      .text(settingText(BlankDisplay))
    opt[Char]('c', "collide-display")
      .action { (x, c) =>
        c.copy(collideDisplay = x)
      }
      .text(settingText(CollideDisplay))
    opt[Int]('i', "initial-num-lins")
      .action { (x, c) =>
        c.copy(initialNumLins = x)
      }
      .text(settingText(InitialNumLins))
    opt[Duration]('r', "row-delay")
      .action { (x, c) =>
        c.copy(rowDelay = x)
      }
      .text(settingText(RowDelay))
    opt[Double]('s', "split-chance")
      .action { (x, c) =>
        c.modify(_.chances.split).setTo(x)
      }
      .text(settingText(SplitChance))
    opt[Double]('d', "die-chance")
      .action { (x, c) =>
        c.modify(_.chances.die).setTo(x)
      }
      .text(settingText(DieChance))
    opt[Double]('u', "mutate-chance")
      .action { (x, c) =>
        c.modify(_.chances.mutate).setTo(x)
      }
      .text(settingText(MutateChance))
    opt[Double]('e', "merge-chance")
      .action { (x, c) =>
        c.modify(_.chances.merge).setTo(x)
      }
      .text(settingText(MergeChance))
    opt[String]('l', "lin-displays")
      .action { (x, c) =>
        c.copy(linDisplays = x)
      }
      .text(settingText(LinDisplays))
    help("help")
    version("version")
  }
}
