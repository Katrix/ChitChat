package net.katsstuff.chitchat

import org.spongepowered.api.command.CommandException
import org.spongepowered.api.entity.living.player.{Player, User}
import org.spongepowered.api.text.Text
import org.spongepowered.api.text.action.TextActions
import org.spongepowered.api.text.format.TextColors._

import io.github.katrix.katlib.helper.Implicits._
import shapeless.Typeable

package object command {

  val userTypeable:   Typeable[User]   = Typeable[User]
  val playerTypeable: Typeable[Player] = Typeable[Player]

  def button(label: Text)(command: String): Text =
    t"[$label]".toBuilder.onClick(TextActions.suggestCommand(command)).build()

  def nonUserError: CommandException =
    new CommandException(t"${RED}You need to be a user to be able to use this command")
  def channelNotFound: CommandException = new CommandException(t"${RED}No channel with that name found")
  def missingPermissionChannel: CommandException =
    new CommandException(t"${RED}You don't have the permission to do that with this channel")
}
