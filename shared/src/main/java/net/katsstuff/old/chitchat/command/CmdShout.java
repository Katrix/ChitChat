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
package net.katsstuff.old.chitchat.command;

import java.util.Optional;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.event.message.MessageEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextFormat;
import org.spongepowered.api.text.transform.SimpleTextTemplateApplier;

import net.katsstuff.old.chitchat.ChitChat;
import net.katsstuff.old.chitchat.chat.events.MessageChannelEventTmp;
import net.katsstuff.old.chitchat.lib.LibPerm;
import net.katsstuff.old.chitchat.chat.ChannelChitChat;
import net.katsstuff.old.chitchat.io.ConfigSettings;
import net.katsstuff.old.chitchat.lib.LibCommandKey;
import net.katsstuff.old.chitchat.helper.TextHelper;
import net.katsstuff.old.chitchat.lib.LibCause;

public class CmdShout extends CommandBase {

	public static final CmdShout INSTANCE = new CmdShout();

	private CmdShout() {
		super(null);
	}

	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		String message = args.<String>getOne(LibCommandKey.MESSAGE).orElse("");
		Optional<ChannelChitChat> optChannel = args.getOne(LibCommandKey.CHANNEL_NAME);
		if(channelExists(src, optChannel)) {
			@SuppressWarnings("OptionalGetWithoutIsPresent")
			ChannelChitChat channel = optChannel.get();
			if(permissionChannel(channel.getQueryName(), src, LibPerm.SHOUT)) {

				Cause cause = Cause.builder().owner(src).named(LibCause.COMMAND, getCommand()).named(LibCause.PLUGIN, ChitChat.getPluginContainer()).build();
				Text rawMessage = Text.of(message);

				SimpleTextTemplateApplier headerApplier = new SimpleTextTemplateApplier(getCfg().getShoutTemplate());
				headerApplier.setParameter(ConfigSettings.TEMPLATE_HEADER, Text.of(src.getName()));

				MessageEvent.MessageFormatter formatter = new MessageEvent.MessageFormatter(rawMessage);
				formatter.getHeader().add(headerApplier);

				Text headerText = formatter.getHeader().format();
				formatter.setBody(Text.of(TextHelper.getFormatAtEnd(headerText).orElse(TextFormat.NONE), rawMessage));


				MessageChannelEvent event = new MessageChannelEventTmp(cause, channel, formatter, false);

				boolean cancelled = Sponge.getEventManager().post(event);

				if(!cancelled) {
					event.getChannel().ifPresent(c -> c.send(src, event.getMessage()));
					src.sendMessage(Text.of(TextColors.GREEN, "Sent message ", event.getFormatter().getBody().format(), " to channel " + channel.getName()));
					return CommandResult.builder().successCount(channel.getMembers().size()).build();
				}
				else {
					src.sendMessage(Text.of(TextColors.RED, "Failed to send message. Maybe some other plugin blocked it"));
					return CommandResult.empty();
				}
			}
		}
		return CommandResult.empty();
	}

	@Override
	public CommandSpec getCommand() {
		return CommandSpec.builder()
				.description(Text.of("Send a message to a channel"))
				.permission(LibPerm.SHOUT)
				.arguments(new CommandElementChannel(LibCommandKey.CHANNEL_NAME), GenericArguments.remainingJoinedStrings(Text.of(LibCommandKey.MESSAGE)))
				.executor(this)
				.build();
	}

	@Override
	public String[] getAliases() {
		return new String[] {"shout", "sendtochannel"};
	}
}
