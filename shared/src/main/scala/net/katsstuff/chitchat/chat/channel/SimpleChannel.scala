package net.katsstuff.chitchat.chat.channel

import scala.collection.JavaConverters._
import scala.ref.WeakReference
import scala.util.Try

import org.spongepowered.api.text.Text
import org.spongepowered.api.text.channel.{MessageChannel, MessageReceiver}

import io.github.katrix.katlib.serializer.ConfigSerializerBase.{ConfigNode, ConfigSerializer}
import net.katsstuff.chitchat.chat.RenamePermission

case class SimpleChannel(name: String, prefix: Text, description: Text, members: Set[WeakReference[MessageReceiver]]) extends Channel {
  type Self = SimpleChannel

  lazy val messageChannel: MessageChannel = MessageChannel.fixed(members.flatMap(_.get).asJava)

  override def rename(newName:               String)(permission: RenamePermission): Self = copy(name = newName)
  override def prefix_=(newPrefix:           Text): Self = copy(prefix = newPrefix)
  override def description_=(newDescription: Text): Self = copy(description = newDescription)
  override def members_=(newMembers:         Set[WeakReference[MessageReceiver]]): Self = copy(members = newMembers)
}

object SimpleChannel {

  //We want to ignore the members field

  implicit object SimpleChannelSerializer extends ConfigSerializer[SimpleChannel] {
    override def write(obj: SimpleChannel, node: ConfigNode): ConfigNode =
      node.getNode("name").write(obj.name).getParent
        .getNode("prefix").write(obj.prefix).getParent
        .getNode("description").write(obj.description)

    override def read(node: ConfigNode): Try[SimpleChannel] =
      for {
        name        <- node.getNode("name").read[String]
        prefix      <- node.getNode("prefix").read[Text]
        description <- node.getNode("description").read[Text]
      } yield SimpleChannel(name, prefix, description, Set())
  }

}
