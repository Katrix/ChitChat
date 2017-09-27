package net.katsstuff.chitchat.chat

import java.util.Locale

import org.spongepowered.api.command.CommandSource
import org.spongepowered.api.text.channel.impl.DelegateMessageChannel
import org.spongepowered.api.text.channel.{ChatTypeMessageReceiver, MessageChannel}
import org.spongepowered.api.text.chat.{ChatType, ChatTypes}
import org.spongepowered.api.text.translation.locale.Locales

import io.github.katrix.katlib.i18n.Resource
import scala.collection.JavaConverters._

import org.spongepowered.api.text.Text

class LocalizedMessageChannel(channel: MessageChannel) extends DelegateMessageChannel(channel) {

  def sendLocalized(key: String, prefix: Text = Text.EMPTY, sender: Option[Any] = None, `type`: ChatType = ChatTypes.SYSTEM)
    (implicit resource: Resource): Unit = sendLocalizedParams(key, prefix, sender, `type`)()(resource)

  def sendLocalizedParams(key: String, prefix: Text = Text.EMPTY, sender: Option[Any] = None, `type`: ChatType = ChatTypes.SYSTEM)(
      params: (String, AnyRef)*
  )(implicit resource: Resource): Unit = {
    for (member <- getMembers.asScala) {
      implicit val locale: Locale = member match {
        case src: CommandSource => src.getLocale
        case _                  => Locales.EN_US
      }

      val original   = if (params.isEmpty) resource.getText(key) else resource.getText(key, params.toMap)
      val withPrefix = prefix.concat(original)

      member match {
        case receiver: ChatTypeMessageReceiver =>
          transformMessage(sender, member, withPrefix, `type`).ifPresent(text => receiver.sendMessage(`type`, text))
        case _ => transformMessage(sender, member, withPrefix, `type`).ifPresent(text => member.sendMessage(text))
      }
    }
  }
}
