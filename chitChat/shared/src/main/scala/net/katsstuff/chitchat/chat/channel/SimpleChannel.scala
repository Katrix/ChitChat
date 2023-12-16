package net.katsstuff.chitchat.chat.channel

import scala.ref.WeakReference

import org.spongepowered.api.text.Text
import org.spongepowered.api.text.channel.MessageReceiver

import io.circe._
import io.circe.syntax._
import net.katsstuff.chitchat.chat.LocalizedMessageChannel
import net.katsstuff.katlib.helper.Implicits._

case class SimpleChannel(name: String, prefix: Text, description: Text, members: Set[WeakReference[MessageReceiver]])
    extends Channel {
  type Self = SimpleChannel

  lazy val messageChannel: LocalizedMessageChannel = new LocalizedMessageChannel(new PrefixedMessageChannel(prefix, members))

  override def name_=(newName: String):                                    Self = copy(name = newName)
  override def prefix_=(newPrefix: Text):                                  Self = copy(prefix = newPrefix)
  override def description_=(newDescription: Text):                        Self = copy(description = newDescription)
  override def members_=(newMembers: Set[WeakReference[MessageReceiver]]): Self = copy(members = newMembers)

  override def handleExtraData(data: String): Either[Text, Self] = Left(t"This channel does not support extra data")

  override def typeName: String = "Simple"

  override def toString: String =
    s"SimpleChannel($name, $prefix, $description, ${members.flatMap(_.get)})"
}

object SimpleChannel {

  //We want to ignore the members field
  implicit val encoder: Encoder[SimpleChannel] = Encoder.instance[SimpleChannel] { obj =>
    Json.obj(
      "name" -> obj.name.asJson,
      "prefix" -> obj.prefix.asJson,
      "description" -> obj.description.asJson
    )
  }

  implicit val decoder: Decoder[SimpleChannel] = Decoder.instance[SimpleChannel] { hcursor =>
    for {
      name <- hcursor.get[String]("name")
      prefix <- hcursor.get[Text]("prefix")
      description <- hcursor.get[Text]("description")
    } yield SimpleChannel(name, prefix, description, Set.empty)
  }
}
