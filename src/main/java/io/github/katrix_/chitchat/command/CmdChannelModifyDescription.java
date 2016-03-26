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

public class CmdChannelModifyDescription extends CommandBase {

	public static final CmdChannelModifyDescription INSTACE = new CmdChannelModifyDescription(CmdChannelProperties.INSTACE);

	private CmdChannelModifyDescription(CommandBase parent) {
		super(parent);
	}

	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		String description = args.<String>getOne(LibCommandKey.CHANNEL_DESCRIPTION).orElse("");
		Optional<ChannelChitChat> optChannel = args.<ChannelChitChat>getOne(LibCommandKey.CHANNEL_NAME);
		if(!channelExists(src, optChannel)) return CommandResult.empty();
		ChannelChitChat channel = optChannel.get();
		if(!permissionChannel(channel.getName(), src, LibPerm.CHANNEL_DESCRIPTION)) return CommandResult.empty();

		if(channel.setDescription(description)) {
			src.sendMessage(Text.of(TextColors.GREEN, "Description of " + channel.getName() + " changed to: ", TextColors.RESET,
					TextSerializers.FORMATTING_CODE.deserialize(description)));
			LogHelper.info("Description of " + channel.getName() + " changed to: " + description);
		}
		else {
			src.sendMessage(Text.of(TextColors.RED, "Description of " + channel.getName() + " changed to: ", TextColors.RESET,
					TextSerializers.FORMATTING_CODE.deserialize(description), TextColors.RED,
					"\n However, this change did not save properly to the database"));
			LogHelper.error("Failed to write new description " + description + " of channel " + channel.getName() + " to the database");
		}
		return CommandResult.success();
	}

	@Override
	public CommandSpec getCommand() {
		return CommandSpec.builder().description(Text.of("Change the description of a channel"))
				.extendedDescription(Text.of("If left empty, it will delete the \ncurrently set description")).permission(LibPerm.CHANNEL_DESCRIPTION)
				.arguments(new CommandElementChannel(LibCommandKey.CHANNEL_NAME),
						GenericArguments.optional(GenericArguments.remainingJoinedStrings(LibCommandKey.CHANNEL_DESCRIPTION)))
				.executor(this).build();
	}

	@Override
	public String[] getAliases() {
		return new String[] {"description"};
	}
}
