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

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializer;
import org.spongepowered.api.text.serializer.TextSerializers;

import io.github.katrix.chitchat.chat.ChannelChitChat;
import io.github.katrix.chitchat.lib.LibCommandKey;
import io.github.katrix.chitchat.lib.LibPerm;

public class CmdChannelCreate extends CommandBase {

	public static final CmdChannelCreate INSTANCE = new CmdChannelCreate(CmdChannel.INSTANCE);

	private CmdChannelCreate(CommandBase parent) {
		super(parent);
	}

	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		TextSerializer serializer = TextSerializers.FORMATTING_CODE;
		String name = args.<String>getOne(LibCommandKey.CHANNEL_NAME).orElse(""); //If name is somehow ever not present this could get ugly
		Text prefix = serializer.deserialize(args.<String>getOne(LibCommandKey.CHANNEL_PREFIX).orElse(name));
		Text description = serializer.deserialize(args.<String>getOne(LibCommandKey.CHANNEL_DESCRIPTION).orElse(""));

		if(sourceIsPlayer(src) && channelNameNotUsed(name, (Player)src)) {
			ChannelChitChat userChannel = getChannelUser((User)src);
			if(permissionChannel(userChannel.getQueryName().then(DataQuery.of(name)), src, LibPerm.CHANNEL_CREATE)) {
				userChannel.createChannel(name, prefix, description);
				src.sendMessage(Text.of(TextColors.GREEN, "Created channel " + name));
				return CommandResult.success();
			}
		}

		return CommandResult.empty();
	}

	@Override
	public CommandSpec getCommand() {
		return CommandSpec.builder()
				.description(Text.of("Create a new channel"))
				.extendedDescription(Text.of("If the prefix is empty, the channel \nname will be used"))
				.permission(LibPerm.CHANNEL_CREATE)
				.arguments(GenericArguments.string(LibCommandKey.CHANNEL_NAME),
						GenericArguments.optional(GenericArguments.string(LibCommandKey.CHANNEL_PREFIX)),
						GenericArguments.optional(GenericArguments.remainingJoinedStrings(LibCommandKey.CHANNEL_DESCRIPTION)))
				.executor(this)
				.build();
	}

	@Override
	public String[] getAliases() {
		return new String[] {"create", "addUser", "createchannel", "createroom"};
	}
}
