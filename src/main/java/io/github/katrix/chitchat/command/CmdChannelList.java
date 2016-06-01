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

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.service.pagination.PaginationService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;

import io.github.katrix.chitchat.chat.channels.Channel;
import io.github.katrix.chitchat.lib.LibPerm;

public class CmdChannelList extends CommandBase {

	public static final CmdChannelList INSTANCE = new CmdChannelList();

	private CmdChannelList() {
		super(CmdChannel.INSTANCE);
	}

	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		if(sourceIsPlayer(src)) {
			Channel parentChannel = getChannelUser((User)src);
			PaginationList.Builder pages = Sponge.getGame().getServiceManager().provideUnchecked(PaginationService.class).builder();
			pages.title(Text.of(TextColors.RED, "Child channels: " + parentChannel.getName()));
			Collection<Channel> children = parentChannel.getChildren();
			List<Text> list = children.stream()
					.filter(c -> src.hasPermission(LibPerm.CHANNEL_JOIN + "." + c.getQueryName()))
					.map(c -> Text.builder()
							.color(TextColors.GREEN)
							.onHover(TextActions.showText(Text.of("Click to join channel")))
							.onClick(TextActions.runCommand("/channel join " + c.getName()))
							.append(Text.of(c.getName(), " - ", c.getDescription()))
							.build())
					.collect(Collectors.toList());

			list.sort(null);

			pages.contents(list);
			pages.sendTo(src);
			return CommandResult.success();
		}

		return CommandResult.empty();
	}

	@Override
	public CommandSpec getCommand() {
		return CommandSpec.builder()
				.description(Text.of("Lists all the currently existing channels"))
				.permission(LibPerm.CHANNEL_LIST)
				.executor(this)
				.build();
	}

	@Override
	public String[] getAliases() {
		return new String[] {"list", "listchannels", "channels", "listrooms", "rooms"};
	}
}