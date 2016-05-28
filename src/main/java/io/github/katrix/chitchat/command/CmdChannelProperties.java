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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.service.pagination.PaginationService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageReceiver;
import org.spongepowered.api.text.format.TextColors;

import com.google.common.collect.ImmutableList;

import io.github.katrix.chitchat.chat.ChannelChitChat;
import io.github.katrix.chitchat.lib.LibPerm;
import io.github.katrix.chitchat.lib.LibCommandKey;

public class CmdChannelProperties extends CommandBase {

	public static final CmdChannelProperties INSTANCE = new CmdChannelProperties(CmdChannel.INSTANCE);

	private CmdChannelProperties(CommandBase parent) {
		super(parent);
	}

	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		Optional<ChannelChitChat> optChannel = args.<ChannelChitChat>getOne(LibCommandKey.CHANNEL_NAME);
		if(sourceIsPlayer(src) && channelExists(src, optChannel)) {
			ChannelChitChat channel = optChannel.get();
			if(permissionChannel(channel.getQueryName(), src, LibPerm.CHANNEL_INFO)) {
				PaginationList.Builder pages = Sponge.getGame().getServiceManager().provideUnchecked(PaginationService.class).builder();
				pages.title(Text.of(TextColors.RED, channel.getName()));
				List<Text> list = new ArrayList<>();

				list.add(Text.of("Channel name: ", channel.getName()));
				list.add(Text.of("Channel prefix: ", channel.getPrefix()));
				list.add(Text.of("Channel description: ", channel.getDescription()));
				list.add(Text.of("Channel members:"));

				StringBuilder players = new StringBuilder();
				ChannelChitChat parentChannel = getChannelUser((User)src);

				Collection<MessageReceiver> playerMap = parentChannel.getMembers();
				Iterator<Player> iterator = playerMap.stream()
						.filter(m -> m instanceof Player)
						.map(m -> (Player)m)
						.filter(p -> getChannelUser(p).equals(channel))
						.collect(Collectors.toList()).iterator();

				while(iterator.hasNext()) {
					Player player = iterator.next();
					players.append(player.getName());
					if(iterator.hasNext()) {
						players.append(", ");
					}
				}

				list.add(Text.of(players));

				pages.contents(list);
				pages.sendTo(src);
				return CommandResult.success();
			}
		}
		return CommandResult.empty();
	}

	@Override
	public CommandSpec getCommand() {
		CommandSpec.Builder builder = CommandSpec.builder()
				.permission(LibPerm.CHANNEL_INFO)
				.description(Text.of("Shows you information about a channel."))
				.extendedDescription(Text.of("If you specify a channel as a parameter \nyou can see the currently set parameters \nfor that channel"))
				.arguments(new CommandElementChannel(LibCommandKey.CHANNEL_NAME))
				.executor(this);
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
				CmdChannelModifyName.INSTANCE,
				CmdChannelModifyPrefix.INSTANCE,
				CmdChannelModifyDescription.INSTANCE);
		//@formatter:on
	}
}
