/*
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

import java.util.Optional;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandMapping;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.event.message.MessageEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TextTemplate;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.transform.SimpleTextTemplateApplier;

import io.github.katrix.chitchat.ChitChat;
import io.github.katrix.chitchat.lib.LibCause;
import io.github.katrix.chitchat.lib.LibCommandKey;
import io.github.katrix.chitchat.lib.LibPerm;

public class CmdForceChat extends CommandBase {

	public static final  CmdForceChat INSTANCE = new CmdForceChat();

	private CmdForceChat() {
		super(null);
	}

	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		Optional<Player> optPlayer = args.getOne(LibCommandKey.PLAYER);
		Optional<String> optMessage = args.getOne(LibCommandKey.MESSAGE);
		if(optPlayer.isPresent() && optMessage.isPresent()) {
			Player player = optPlayer.get();
			String message = optMessage.get();

			if(message.startsWith("/")) {
				String actualCommand = message.split(" ")[0];
				Optional<? extends CommandMapping> optCommandMapping = Sponge.getCommandManager().get(actualCommand.substring(1), player);

				if(optCommandMapping.isPresent()) {
					CommandMapping commandMapping = optCommandMapping.get();
					String argString = message.substring(actualCommand.length());

					commandMapping.getCallable().process(player, argString);
					return CommandResult.success();
				}
				else {
					src.sendMessage(Text.of(TextColors.RED, "No command by that name found"));
					return CommandResult.empty();
				}
			}
			else {
				Text rawMessage = Text.of(message);

				MessageEvent.MessageFormatter formatter = new MessageEvent.MessageFormatter(rawMessage);
				TextTemplate headerTemplate = TextTemplate.of('<', TextTemplate.arg("header"), "> ");
				SimpleTextTemplateApplier headerApplier = new SimpleTextTemplateApplier(headerTemplate);

				Text parameter = Text.builder()
						.append(Text.of(player.getName()))
						.onClick(TextActions.suggestCommand("/msg " + player.getName() + " "))
						.onHover(TextActions.showEntity(player.getUniqueId(), player.getName()))
						.onShiftClick(TextActions.insertText(player.getName())).build();
				headerApplier.setParameter("header", parameter);
				formatter.getHeader().add(headerApplier);

				Cause cause = Cause.builder().owner(player).named(LibCause.COMMAND, getCommand()).named(LibCause.PLUGIN, ChitChat.getPluginContainer()).build();
				MessageChannel channel = player.getMessageChannel();

				MessageChannelEvent.Chat event = SpongeEventFactory.createMessageChannelEventChat(cause, channel, Optional.of(channel), formatter, rawMessage, false);
				boolean cancelled = Sponge.getEventManager().post(event);

				if(!cancelled) {
					event.getChannel().ifPresent(c -> c.send(player, event.getMessage()));
					return CommandResult.success();
				}
				else {
					src.sendMessage(Text.of(TextColors.RED, "For some reason the message was not sent. Maybe some other plugin blocked it"));
					return CommandResult.empty();
				}
			}
		}
		else {
			src.sendMessage(Text.of(TextColors.RED, "No player by that name found"));
			return CommandResult.empty();
		}
	}

	@Override
	public CommandSpec getCommand() {
		return CommandSpec.builder().permission(LibPerm.FORCE_CHAT).description(Text.of("Make someone else say something or execute some command"))
				.arguments(GenericArguments.player(LibCommandKey.PLAYER), GenericArguments.remainingJoinedStrings(LibCommandKey.MESSAGE))
				.executor(this).build();
	}

	@Override
	public String[] getAliases() {
		return new String[] {"forcechat"};
	}
}
