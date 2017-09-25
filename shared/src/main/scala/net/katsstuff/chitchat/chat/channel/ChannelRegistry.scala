package net.katsstuff.chitchat.chat.channel

import scala.collection.mutable

import org.spongepowered.api.text.Text
import org.spongepowered.api.text.format.TextColors._

import io.github.katrix.katlib.helper.Implicits._

/**
  * A class which keeps track of the different types of channels,
  * and allows constructing them by name.
  */
class ChannelRegistry {

  private val channels = new mutable.HashMap[String, (String, Text, Text, String) => Either[Text, Channel]]

  /**
    * Registers a new channel type
    * @param name The name of the channel type as typed into chat
    * @param constructor The constructor for the channel type.
    *                    If incorrect information is supplied,
    *                    the function can return Left with the error message
    */
  def registerChannelType(name: String, constructor: (String, Text, Text, String) => Either[Text, Channel]): Unit =
    channels.put(name, constructor)

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
}
