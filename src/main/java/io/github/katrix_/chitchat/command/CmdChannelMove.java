/**
 * This file is part of ChitChat, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2016 Katrix
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package io.github.katrix_.chitchat.command;

import java.util.Optional;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import io.github.katrix_.chitchat.chat.ChannelChitChat;
import io.github.katrix_.chitchat.chat.ChitChatPlayers;
import io.github.katrix_.chitchat.lib.LibCommandKey;
import io.github.katrix_.chitchat.lib.LibPerm;

public class CmdChannelMove extends CommandBase {

	public static final CmdChannelMove INSTANCE = new CmdChannelMove(CmdChannel.INSTANCE);

	protected CmdChannelMove(CommandBase parent) {
		super(parent);
	}

	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		Optional<User> optUser = args.getOne(LibCommandKey.USER);
		Optional<ChannelChitChat> optChannel = args.getOne(LibCommandKey.CHANNEL_NAME);
		if(channelExists(src, optChannel)) {
			if(!optUser.isPresent()) {
				src.sendMessage(Text.of(TextColors.RED, "No user by that name found"));
				return CommandResult.empty();
			}
			ChannelChitChat channel = optChannel.get();
			User user = optUser.get();
			if(permissionChannel(channel.getName(), src, LibPerm.CHANNEL_MOVE)) {
				Optional<Player> optPlayer = user.getPlayer();
				if(optPlayer.isPresent()) {
					Player player = optPlayer.get();
					ChitChatPlayers.getOrCreate(player).setChannel(channel);
					src.sendMessage(Text.of(TextColors.GREEN, player.getName() + " was move into channel " + channel.getName()));
					player.sendMessage(Text.of(TextColors.YELLOW, "You were moved into " + channel.getName() + " by " + src.getName()));
				}
				else {
					if(src.hasPermission(LibPerm.CHANNEL_MOVE_OFFLINE)) {
						ChitChatPlayers.getFromUuid(user.getUniqueId()).setChannel(channel);
						src.sendMessage(Text.of(TextColors.GREEN, "Next time " + user.getName() + " joins, they will be in " + channel.getName()));
					}
					else {
						src.sendMessage(Text.of(TextColors.RED, "You do not have permission to move offline players"));
						return CommandResult.empty();
					}
				}

				return CommandResult.success();
			}
		}
		return CommandResult.empty();
	}

	@Override
	public CommandSpec getCommand() {
		return CommandSpec.builder().description(Text.of("Move a player into a channel"))
				.extendedDescription(Text.of("You can also use this command on offline players")).permission(LibPerm.CHANNEL_MOVE)
				.arguments(GenericArguments.user(LibCommandKey.USER), new CommandElementChannel(LibCommandKey.CHANNEL_NAME)).executor(this).build();
	}


	@Override
	public String[] getAliases() {
		return new String[] {"move", "moveplayer"};
	}
}
