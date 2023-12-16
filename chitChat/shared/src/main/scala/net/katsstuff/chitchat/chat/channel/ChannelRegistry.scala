package net.katsstuff.chitchat.chat.channel

import scala.reflect.ClassTag

import org.spongepowered.api.text.Text
import org.spongepowered.api.text.format.TextColors._

import io.circe._
import io.circe.syntax._
import net.katsstuff.katlib.helper.Implicits._

/**
  * A class which keeps track of the different types of channels,
  * and allows constructing them by name.
  */
//Yuck!
//We need to allow custom channel types that are provided at runtime. As such we can't use implicits to resolve the serializers.
case class ChannelRegistry(
    channels: Map[String, ChannelRegistry.Constructor[_ <: Channel]],
    nameToEncoder: Map[String, Encoder[_ <: Channel]],
    nameToDecoder: Map[String, Decoder[_ <: Channel]],
    classToName: Map[Class[_ <: Channel], String]
) {

  /**
    * Registers a new channel type
    * @param name The name of the channel type as typed into chat
    * @param constructor The constructor for the channel type.
    *                    If incorrect information is supplied,
    *                    the function can return Left with the error message
    */
  def registerChannelType[A <: Channel](
      name: String,
      constructor: ChannelRegistry.Constructor[A]
  )(implicit tag: ClassTag[A], encoder: Encoder[A], decoder: Decoder[A]): ChannelRegistry =
    copy(
      channels = channels.updated(name, constructor: ChannelRegistry.Constructor[Channel]),
      nameToEncoder = nameToEncoder.updated(name, encoder),
      nameToDecoder = nameToDecoder.updated(name, decoder),
      classToName = classToName.updated(tag.runtimeClass.asInstanceOf[Class[A]], name)
    )

  /**
    * Creates a channel by it's name, and parameters passed to the
    * constructor.
    * @return If the creation failed, returns the error message on the left
    *         side, else returns the channel on the right side.
    */
  def createChannel(
      channelType: String,
      name: String,
      prefix: Text,
      description: Text,
      extra: String
  ): Either[Text, Channel] =
    channels
      .get(channelType)
      .toRight(t"${RED}No channel type by that name")
      .flatMap(f => f(name, prefix, description, extra))

  implicit val channelEncoder: Encoder[Channel] = Encoder.instance[Channel] { obj =>
    val name = classToName(obj.getClass)
    Json.obj(
      "name" -> name.asJson,
      "data" -> nameToEncoder(name).asInstanceOf[Encoder[obj.Self]](obj.asInstanceOf[obj.Self])
    )
  }

  implicit val channelDecoder: Decoder[Channel] = Decoder.instance[Channel] { hcursor =>
    for {
      name <- hcursor.get[String]("name")
      channel <- hcursor.get("data")(nameToDecoder(name))
    } yield channel
  }
}
object ChannelRegistry {

  //Name, prefix, description, extra
  type Constructor[A <: Channel] = (String, Text, Text, String) => Either[Text, A]
}
