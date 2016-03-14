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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.pagination.PaginationList.Builder;
import org.spongepowered.api.service.pagination.PaginationService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import com.google.common.collect.ImmutableList;

import io.github.katrix_.chitchat.chat.ChannelChitChat;
import io.github.katrix_.chitchat.chat.ChitChatChannels;
import io.github.katrix_.chitchat.lib.LibCommandKey;
import io.github.katrix_.chitchat.lib.LibPerm;

public class CmdChannelProperties extends CommandBase {

	public static final CmdChannelProperties INSTACE = new CmdChannelProperties(CmdChannel.INSTACE);

	private CmdChannelProperties(CommandBase parent) {
		super(parent);
	}

	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		ChannelChitChat channel = args.<ChannelChitChat>getOne(LibCommandKey.CHANNEL_NAME).get();
		if(!permissionChannel(channel.getName(), src, LibPerm.CHANNEL_INFO)) return CommandResult.empty();

		Builder pages = Sponge.getGame().getServiceManager().provide(PaginationService.class).get().builder();
		pages.title(Text.of(TextColors.RED, channel.getName()));
		List<Text> list = new ArrayList<>();

		list.add(Text.of("Channel name: ", channel.getName()));
		list.add(Text.of("Channel prefix: ", channel.getPrefix()));
		list.add(Text.of("Channel description: ", channel.getDescription()));
		list.add(Text.of("Channel members:"));

		Map<Player, ChannelChitChat> playerChannelMap = ChitChatChannels.getChannelPlayerMap();

		playerChannelMap.keySet().stream().filter(player -> playerChannelMap.get(player).equals(channel))
				.forEach(player -> list.add(Text.of(player.getName())));

		pages.contents(list);
		pages.sendTo(src);
		return CommandResult.success();
	}

	@Override
	public CommandSpec getCommand() {
		CommandSpec.Builder builder = CommandSpec.builder().permission(LibPerm.CHANNEL_INFO)
				.description(Text.of("Shows you information about a channel."))
				.extendedDescription(Text.of("If you specify a channel as a paremater \nyou can see the currently set paramters \nfor that channel"))
				.arguments(new CommandElementChannel(LibCommandKey.CHANNEL_NAME)).executor(this);
		registerSubcommands(builder);
		return builder.build();
	}

	@Override
	public String[] getAliases() {
		return new String[] {"properties", "modify", "change"};
	}

	@Override
	public ImmutableList<CommandBase> getChildren() {
		//@formatter:off
		return ImmutableList.of(
				CmdChannelModifyName.INSTACE,
				CmdChannelModifyPrefix.INSTACE,
				CmdChannelModifyDescription.INSTACE);
		//@formatter:on
	}
}
