package net.katsstuff.chitchat

import java.util.Locale

import org.spongepowered.api.command.CommandException
import org.spongepowered.api.entity.living.player.{Player, User}
import org.spongepowered.api.text.Text
import org.spongepowered.api.text.action.TextActions

import io.github.katrix.katlib.helper.Implicits._
import shapeless.Typeable

package object command {

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

  def sendMessageFailed(implicit locale: Locale): CommandException =
    new CommandException(CCResource.getText("command.error.sendMessageFailed"))
}
