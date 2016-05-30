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
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.command.spec.CommandSpec.Builder;
import org.spongepowered.api.text.Text;

import com.google.common.collect.ImmutableList;

import io.github.katrix.chitchat.lib.LibPerm;

public class CmdChannel extends CommandBase {

	public static final CmdChannel INSTANCE = new CmdChannel();

	private CmdChannel() {
		super(null);
	}

	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		return CommandResult.success();
	}

	@Override
	public CommandSpec getCommand() {
		Builder builder = CommandSpec.builder()
				.permission(LibPerm.CHANNEL)
				.description(Text.of("Do stuff with channels"));
		registerSubcommands(builder);
		return builder.build();
	}

	@Override
	public String[] getAliases() {
		return new String[] {"channel", "room"};
	}

	@Override
	public ImmutableList<CommandBase> getChildren() {
		//@formatter:off
		return ImmutableList.of(
				CmdChannelCreate.INSTANCE,
				CmdChannelRemove.INSTANCE,
				CmdChannelJoin.INSTANCE,
				CmdChannelList.INSTANCE,
				CmdChannelProperties.INSTANCE,
				CmdChannelReload.INSTANCE,
				CmdChannelMove.INSTANCE
				);
		//@formatter:on
	}
}
