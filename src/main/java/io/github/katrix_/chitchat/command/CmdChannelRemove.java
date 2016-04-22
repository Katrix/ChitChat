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
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import io.github.katrix_.chitchat.chat.ChannelChitChat;
import io.github.katrix_.chitchat.chat.ChitChatChannels;
import io.github.katrix_.chitchat.helper.LogHelper;
import io.github.katrix_.chitchat.io.SQLStorage;
import io.github.katrix_.chitchat.lib.LibCommandKey;
import io.github.katrix_.chitchat.lib.LibPerm;

public class CmdChannelRemove extends CommandBase {

	public static final CmdChannelRemove INSTANCE = new CmdChannelRemove(CmdChannel.INSTANCE);

	private CmdChannelRemove(CommandBase parent) {
		super(parent);
	}

	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		Optional<ChannelChitChat> optChannel = args.<ChannelChitChat>getOne(LibCommandKey.CHANNEL_NAME);
		if(channelExists(src, optChannel)) {
			ChannelChitChat channel = optChannel.get();
			if(channel.equals(ChitChatChannels.getGlobalChannel())) {
				src.sendMessage(Text.of(TextColors.RED, "You can't remove the global channel"));
				return CommandResult.empty();
			}

			if(permissionChannel(channel.getName(), src, LibPerm.CHANNEL_PREFIX)) {
				ChitChatChannels.removeChannel(channel);
				if(SQLStorage.deleteChannel(channel)) {
					src.sendMessage(Text.of(TextColors.GREEN, "Removed channel " + channel.getName()));
				}
				else {
					src.sendMessage(Text.of(TextColors.RED, "Failed to delete the channel " + channel.getName()
							+ " from the database. It will be gone for now, but it will be back when the server restarts."));
					LogHelper.error("Failed to delete the channel " + channel.getName() + " from the database");
				}
				return CommandResult.success();
			}
		}
		return CommandResult.empty();
	}

	@Override
	public CommandSpec getCommand() {
		return CommandSpec.builder().description(Text.of("Remove an existing channel"))
				.extendedDescription(Text.of("If any players is in the channel \nat the time, they will be moved to the \nglobal channel"))
				.permission(LibPerm.CHANNEL_REMOVE).arguments(new CommandElementChannel(LibCommandKey.CHANNEL_NAME)).executor(this).build();
	}

	@Override
	public String[] getAliases() {
		return new String[] {"remove", "removechannel", "delete", "deletechannel", "removeroom", "deleteroom"};
	}
}
