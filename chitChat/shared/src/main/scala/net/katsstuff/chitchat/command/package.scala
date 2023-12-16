package net.katsstuff.chitchat

import java.util.Locale

import org.jetbrains.annotations.PropertyKey
import org.spongepowered.api.command.{CommandException, CommandSource}
import org.spongepowered.api.entity.living.player.{Player, User}
import org.spongepowered.api.text.Text
import org.spongepowered.api.text.action.TextActions

import net.katsstuff.chitchat.chat.ChannelHandler
import net.katsstuff.chitchat.chat.channel.Channel
import net.katsstuff.katlib.KatPlugin
import net.katsstuff.katlib.helper.Implicits._
import net.katsstuff.katlib.command.KatLibCommands
import net.katsstuff.katlib.i18n.Localized
import shapeless.Typeable

package object command extends KatLibCommands {

  //TODO: Remove this
  override implicit def plugin: KatPlugin = ???

  def LocalizedDescription(
      @PropertyKey(resourceBundle = CCResource.ResourceLocation) key: String
  ): CommandSource => Option[Text] =
    Description(src => Localized(src)(implicit locale => CCResource.getText(key)))

  def channelHandler(implicit plugin: ChitChatPlugin): ChannelHandler = plugin.channelHandler

  implicit val channelHasName: HasName[Channel] = HasName.instance[Channel](_.name)

  def channelParameter(implicit plugin: ChitChatPlugin): Parameter[Set[Channel]] =
    Parameter.mkNamed("channel", channelHandler.allChannels.values)

  val userTypeable:   Typeable[User]   = Typeable[User]
  val playerTypeable: Typeable[Player] = Typeable[Player]

  def button(label: Text)(command: String): Text =
    t"[$label]".toBuilder.onClick(TextActions.runCommand(command)).build()

  def manualButton(label: Text)(command: String): Text =
    t"[[$label]]".toBuilder.onClick(TextActions.suggestCommand(command)).build()

  def channelNotFound(implicit locale: Locale): CommandException =
    new CommandException(CCResource.getText("command.error.channelNotFound"))

  def missingPermissionChannel(implicit locale: Locale): CommandException =
    new CommandException(CCResource.getText("command.error.noPermissionChannel"))

  def sendMessageFailed(implicit locale: Locale): String = CCResource.get("command.error.sendMessageFailed")
}
