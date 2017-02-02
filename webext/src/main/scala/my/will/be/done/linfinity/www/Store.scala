package my.will.be.done.linfinity.www

import scala.concurrent.{Future, duration}, duration.Duration
import chrome.storage.Storage.local
import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined
import scala.util.{Try, Success, Failure}
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

@ScalaJSDefined
class StorableDuration(val length: Long, val unit: String) extends js.Object

object Store {

  def toAny[V](setting: Setting[V]): Try[js.Any] = {
    setting.get match {
      case value: String ⇒
        Success(value)
      case value: Long ⇒
        Success(value)
      case value: Int ⇒
        Success(value)
      case value: Double ⇒
        Success(value)
      case value: Float ⇒
        Success(value)
      case value: Short ⇒
        Success(value)
      case value: Char ⇒
        Success(value)
      case value: Duration ⇒
        Success(new StorableDuration(value.length, value.unit.name))
      case value ⇒
        Failure(new Exception(s"cannot store value: $value"))
    }
  }

  def fromAny[V](any: js.Any): Try[V] = {
    ((any: Any) match {
      case value: String ⇒
        Success(value)
      case value: Long ⇒
        Success(value)
      case value: Int ⇒
        Success(value)
      case value: Double ⇒
        Success(value)
      case value: Float ⇒
        Success(value)
      case value: Short ⇒
        Success(value)
      case value: Char ⇒
        Success(value)
      case value: StorableDuration ⇒
        Success(Duration(value.length, value.unit))
      case value ⇒
        Failure(new Exception(s"cannot load value: $value"))
    }).map(_.asInstanceOf[V])
  }

  def saveSettings: Future[Unit] = {
    local.set((for {
      setting ← Setting.values
      any     ← toAny(setting).toOption
    } yield {
      setting.entryName → any
    }).toMap)
  }

  def loadSettings: Future[Unit] = {
    val keys = js.Array(Setting.values.map(_.entryName): _*)
    for {
      storedMap ← local.get(keys)
    } yield {
      for {
        (entryName, any) ← storedMap
        setting          ← Setting.withNameOption(entryName)
        value            ← fromAny(any).toOption
      } yield {
        setting := value
      }
    }
  }
}
