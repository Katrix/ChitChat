package net.katsstuff.chitchat.chat.channel

import scala.collection.mutable
import scala.ref.WeakReference
import scala.reflect.ClassTag
import scala.util.Try

import org.spongepowered.api.text.Text
import org.spongepowered.api.text.channel.{MessageChannel, MessageReceiver}

import io.github.katrix.katlib.serializer.ConfigSerializerBase.{ConfigNode, ConfigSerializer}
import net.katsstuff.chitchat.chat.HandlerOnly

case class ExtraData(displayName: String, data: Any)

trait Channel {
  type Self <: Channel

  def messageChannel: MessageChannel

  def name: String
  def name_=(newName: String)(implicit perm: HandlerOnly): Self
  def prefix: Text
  def prefix_=(newPrefix: Text)(implicit perm: HandlerOnly): Self
  def description: Text
  def description_=(newDescription: Text)(implicit perm: HandlerOnly): Self

  def members: Set[WeakReference[MessageReceiver]]
  def members_=(newMembers: Set[WeakReference[MessageReceiver]])(implicit perm: HandlerOnly): Self

  def addMember(receiver:    MessageReceiver)(implicit perm: HandlerOnly): Self = this.members = members + WeakReference(receiver)
  def removeMember(receiver: MessageReceiver)(implicit perm: HandlerOnly): Self = this.members = members.filter(!_.get.contains(receiver))

  def typeName: String
  def extraData: Map[String, ExtraData] = Map()
  def handleExtraData(data: String): Either[Text, Self]
}

object Channel {

  //Yuck!
  //We need to allow custom channel types that are provided at runtime. As such we can't use implicits to resolve the serializers.

  private val nameToSerializer = new mutable.HashMap[String, ConfigSerializer[_ <: Channel]]
  private val classToName      = new mutable.HashMap[Class[_ <: Channel], String]

  def registerChannelType[A <: Channel: ClassTag: ConfigSerializer](name: String): Unit = {
    val serializer = implicitly[ConfigSerializer[A]]
    val clazz      = implicitly[ClassTag[A]].runtimeClass.asInstanceOf[Class[A]]

    nameToSerializer.put(name, serializer)
    classToName.put(clazz, name)
  }

  implicit object ChannelSerializer extends ConfigSerializer[Channel] {
    //Super hacky
    override def write(obj: Channel, node: ConfigNode): ConfigNode = {
      val withType = node.getNode("type").write(classToName(obj.getClass)).getParent
      nameToSerializer(classToName(obj.getClass)).asInstanceOf[ConfigSerializer[obj.Self]].write(obj.asInstanceOf[obj.Self], withType)
    }

    override def read(node: ConfigNode): Try[Channel] =
      node.getNode("type").read[String].flatMap(tpe => nameToSerializer(tpe).read(node))
  }
}
