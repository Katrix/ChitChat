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

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import io.github.katrix_.chitchat.chat.ChannelChitChat;
import io.github.katrix_.chitchat.chat.ChitChatChannels;
import io.github.katrix_.chitchat.lib.LibCommandKey;
import io.github.katrix_.chitchat.lib.LibPerm;

public class CmdChannelModifyName extends CommandBase {

	public static final CmdChannelModifyName INSTACE = new CmdChannelModifyName(CmdChannelProperties.INSTACE);

	private CmdChannelModifyName(CommandBase parent) {
		super(parent);
	}

	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		String nameNew = args.<String>getOne(LibCommandKey.CHANNEL_NAME_NEW).get();
		ChannelChitChat channel = args.<ChannelChitChat>getOne(LibCommandKey.CHANNEL_NAME).get();
		if(!channelNameNotUsed(nameNew, src) || !permissionChannel(channel.getName(), src, LibPerm.CHANNEL_NAME)
				|| !permissionChannel(nameNew, src, LibPerm.CHANNEL_NAME))
			return CommandResult.empty();

		String nameOld = channel.getName();
		channel.setName(nameNew);
		ChitChatChannels.remapChannelName(channel, nameOld);

		src.sendMessage(Text.of(TextColors.GREEN, "Name of " + nameOld + " changed to " + nameNew));
		return CommandResult.success();
	}

	@Override
	public CommandSpec getCommand() {
		return CommandSpec.builder().description(Text.of("Change the name of a channel")).permission(LibPerm.CHANNEL_NAME)
				.arguments(new CommandElementChannel(LibCommandKey.CHANNEL_NAME), GenericArguments.string(LibCommandKey.CHANNEL_NAME_NEW))
				.executor(this).build();
	}

	@Override
	public String[] getAliases() {
		return new String[] {"name"};
	}
}
