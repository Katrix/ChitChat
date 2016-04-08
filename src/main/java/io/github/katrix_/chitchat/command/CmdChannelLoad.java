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
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import io.github.katrix_.chitchat.io.SQLStorage;
import io.github.katrix_.chitchat.lib.LibPerm;

public class CmdChannelLoad extends CommandBase {

	public static final CmdChannelLoad INSTANCE = new CmdChannelLoad(CmdChannel.INSTANCE);

	private CmdChannelLoad(CommandBase parent) {
		super(parent);
	}

	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		if(SQLStorage.reloadChannels()) {
			src.sendMessage(Text.of(TextColors.GREEN, "Loaded channels from disk successfully."));
			return CommandResult.success();
		}
		else {
			src.sendMessage(Text.of(TextColors.RED, "Something went wrong when loading channels from disk"));
			return CommandResult.empty();
		}
	}

	@Override
	public CommandSpec getCommand() {
		return CommandSpec.builder().description(Text.of("NOT IMPLEMENTED YET")).permission(LibPerm.CHANNEL_LOAD).executor(this).build();
	}

	@Override
	public String[] getAliases() {
		return new String[] {"load"};
	}
}
