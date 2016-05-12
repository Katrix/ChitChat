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
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;

import io.github.katrix_.chitchat.chat.ChannelChitChat;
import io.github.katrix_.chitchat.helper.LogHelper;
import io.github.katrix_.chitchat.lib.LibCommandKey;
import io.github.katrix_.chitchat.lib.LibPerm;

public class CmdChannelModifyPrefix extends CommandBase {

	public static final CmdChannelModifyPrefix INSTANCE = new CmdChannelModifyPrefix(CmdChannelProperties.INSTANCE);

	private CmdChannelModifyPrefix(CommandBase parent) {
		super(parent);
	}

	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		Optional<ChannelChitChat> optChannel = args.<ChannelChitChat>getOne(LibCommandKey.CHANNEL_NAME);
		if(channelExists(src, optChannel)) {
			ChannelChitChat channel = optChannel.get();
			Text prefix = TextSerializers.FORMATTING_CODE.deserialize(args.<String>getOne(LibCommandKey.CHANNEL_PREFIX).orElse(channel.getName()));
			if(permissionChannel(channel.getQueryName(), src, LibPerm.CHANNEL_PREFIX)) {
				if(channel.setPrefix(prefix)) {
					src.sendMessage(Text.of(TextColors.GREEN, "Prefix of " + channel.getName() + " changed to: ", TextColors.RESET,
							prefix));
					LogHelper.info("Prefix of " + channel.getName() + " changed to: " + prefix);
				}
				else {
					src.sendMessage(Text.of(TextColors.RED, "Prefix of " + channel.getName() + " changed to: ", TextColors.RESET,
							prefix, TextColors.RED,
							"\n However, this change did not save properly to the database"));
					LogHelper.error("Failed to write new prefix " + prefix + " of channel " + channel.getName() + " to storage");
				}
				return CommandResult.success();
			}

		}
		return CommandResult.empty();
	}

	@Override
	public CommandSpec getCommand() {
		return CommandSpec.builder().description(Text.of("Change the prefix of a channel"))
				.extendedDescription(Text.of("If left empty, it will set the \nprefix to the same as the channel name"))
				.permission(LibPerm.CHANNEL_PREFIX).arguments(new CommandElementChannel(LibCommandKey.CHANNEL_NAME),
						GenericArguments.optional(GenericArguments.string(LibCommandKey.CHANNEL_PREFIX)))
				.executor(this).build();
	}

	@Override
	public String[] getAliases() {
		return new String[] {"prefix"};
	}
}
