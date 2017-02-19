package net.katsstuff.chitchat.chat.channel

import scala.ref.WeakReference
import scala.util.Try

import org.spongepowered.api.text.Text
import org.spongepowered.api.text.channel.{MessageChannel, MessageReceiver}

import io.github.katrix.katlib.helper.Implicits._
import io.github.katrix.katlib.serializer.ConfigSerializerBase.{ConfigNode, ConfigSerializer}
import io.github.katrix.katlib.serializer.TypeSerializerImpl
import net.katsstuff.chitchat.chat.HandlerOnly
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializers

case class SimpleChannel(name: String, prefix: Text, description: Text, members: Set[WeakReference[MessageReceiver]]) extends Channel {
  type Self = SimpleChannel

  lazy val messageChannel: MessageChannel = new PrefixedMessageChannel(prefix, members)

  override def name_=(newName:               String)(implicit perm: HandlerOnly): Self = copy(name = newName)
  override def prefix_=(newPrefix:           Text)(implicit perm: HandlerOnly): Self = copy(prefix = newPrefix)
  override def description_=(newDescription: Text)(implicit perm: HandlerOnly): Self = copy(description = newDescription)
  override def members_=(newMembers:         Set[WeakReference[MessageReceiver]])(implicit perm: HandlerOnly): Self = copy(members = newMembers)
  override def handleExtraData(data:         String): Either[Text, Self] = Left(t"This channel does not support extra data")
  override def typeName: String = "Simple"
  override def toString: String =
    s"SimpleChannel($name, $prefix, $description, ${members.flatMap(_.get)})"
}

object SimpleChannel {

  //We want to ignore the members field

  implicit private val textSerializer =
    TypeSerializerImpl.fromTypeSerializer(TypeSerializers.getDefaultSerializers.get(typeToken[Text]), classOf[Text])

  implicit object SimpleChannelSerializer extends ConfigSerializer[SimpleChannel] {
    // @formatter:off
    override def write(obj: SimpleChannel, node: ConfigNode): ConfigNode =
      node.getNode("name").write(obj.name).getParent
        .getNode("prefix").write(obj.prefix).getParent
        .getNode("description").write(obj.description)
    // @formatter:on

    override def read(node: ConfigNode): Try[SimpleChannel] =
      for {
        name        <- node.getNode("name").read[String]
        prefix      <- node.getNode("prefix").read[Text]
        description <- node.getNode("description").read[Text]
      } yield SimpleChannel(name, prefix, description, Set())
  }

}
