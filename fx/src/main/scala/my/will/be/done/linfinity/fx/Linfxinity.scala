package my.will.be.done.linfinity.fx

import javafx.application.Application
import javafx.stage._
import javafx.scene._
import javafx.scene.control._
import com.thoughtworks.binding._

final class Linfxinity extends Application {

  @fxml override def start(primaryStage: Stage): Unit = {
    val scene: Binding[Scene] = <Scene><Label>Hello, World!</Label></Scene>
    fxml.show(primaryStage, scene)
  }

}

object Linfxinity extends App {
  Application.launch(classOf[Linfxinity], args: _*)
}
