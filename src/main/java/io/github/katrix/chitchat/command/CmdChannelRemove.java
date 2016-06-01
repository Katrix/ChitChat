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
package io.github.katrix.chitchat.command;

import java.util.Optional;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import io.github.katrix.chitchat.ChitChat;
import io.github.katrix.chitchat.chat.channels.Channel;
import io.github.katrix.chitchat.lib.LibCommandKey;
import io.github.katrix.chitchat.lib.LibPerm;
import io.github.katrix.chitchat.helper.LogHelper;

public class CmdChannelRemove extends CommandBase {

	public static final CmdChannelRemove INSTANCE = new CmdChannelRemove();

	private CmdChannelRemove() {
		super(CmdChannel.INSTANCE);
	}

	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		Optional<Channel> optChannel = args.getOne(LibCommandKey.CHANNEL_NAME);
		if(sourceIsPlayer(src) && channelExists(src, optChannel)) {
			@SuppressWarnings("OptionalGetWithoutIsPresent")
			Channel channel = optChannel.get();

			if(permissionChannel(channel.getQueryName(), src, LibPerm.CHANNEL_PREFIX)) {
				getChannelUser((User)src).removeChild(channel);

				if(ChitChat.getStorage().saveRootChannel()) {
					src.sendMessage(Text.of(TextColors.GREEN, "Removed channel " + channel.getName()));
				}
				else {
					src.sendMessage(Text.of(TextColors.RED, "Failed to delete the channel " + channel.getName()
							+ " from the database. It will be gone for now, but it will be back when the server restarts."));
					LogHelper.error("Failed to delete the channel " + channel.getName() + " from storage");
				}
				return CommandResult.success();
			}
		}
		return CommandResult.empty();
	}

	@Override
	public CommandSpec getCommand() {
		return CommandSpec.builder()
				.description(Text.of("Remove an existing channel"))
				.extendedDescription(Text.of("If any players is in the channel \nat the time, they will be moved to the \nglobal channel"))
				.permission(LibPerm.CHANNEL_REMOVE)
				.arguments(new CommandElementChannel(LibCommandKey.CHANNEL_NAME))
				.executor(this)
				.build();
	}

	@Override
	public String[] getAliases() {
		return new String[] {"removeUser", "removechannel", "delete", "deletechannel", "removeroom", "deleteroom"};
	}
}