package net.katsstuff.chitchat.command

import java.lang.Iterable

import scala.collection.JavaConverters._

import org.spongepowered.api.command.CommandSource
import org.spongepowered.api.command.args.PatternMatchingCommandElement
import org.spongepowered.api.text.Text

import net.katsstuff.chitchat.chat.ChannelHandler

class ChannelCommandArgument(key: Option[Text])(implicit handler: ChannelHandler) extends PatternMatchingCommandElement(key.orNull) {

  def this(key: Text)(implicit handler: ChannelHandler) = this(Some(key))

  override def getValue(choice:   String):        AnyRef           = handler.getChannel(choice).getOrElse(throw new IllegalArgumentException)
  override def getChoices(source: CommandSource): Iterable[String] = handler.allChannels.keys.asJava
}
