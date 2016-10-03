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

import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.effect.sound.SoundTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.message.MessageEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TextElement;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextFormat;
import org.spongepowered.api.text.transform.SimpleTextTemplateApplier;

import com.google.common.collect.ImmutableMap;

import net.katsstuff.old.chitchat.ChitChat;
import net.katsstuff.old.chitchat.chat.events.MessageEventTmp;
import net.katsstuff.old.chitchat.lib.LibPerm;
import net.katsstuff.old.chitchat.io.ConfigSettings;
import net.katsstuff.old.chitchat.lib.LibCommandKey;
import net.katsstuff.old.chitchat.helper.TextHelper;
import net.katsstuff.old.chitchat.lib.LibCause;

public class CmdPM extends CommandBase {

	public static final CmdPM INSTANCE = new CmdPM();
	private final Map<CommandSource, CommandSource> conversations = new WeakHashMap<>();

	private CmdPM() {
		super(null);
	}

	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		Optional<Player> optReceiver = args.getOne(LibCommandKey.PLAYER);
		if(optReceiver.isPresent()) {
			Player receiver = optReceiver.get();
			String message = args.<String>getOne(LibCommandKey.MESSAGE).orElse("");
			conversations.put(src, receiver);
			conversations.put(receiver, src);

			Cause cause = Cause.builder().owner(src).named(LibCause.COMMAND, getCommand()).named(LibCause.PLUGIN, ChitChat.getPluginContainer()).build();
			Text srcName = Text.of(src.getName());
			Text textMessage = Text.of(message);

			SimpleTextTemplateApplier headerApplier = new SimpleTextTemplateApplier(getCfg().getPmReceiverTemplate());
			headerApplier.setParameter(ConfigSettings.TEMPLATE_HEADER, srcName);

			MessageEvent.MessageFormatter formatter = new MessageEvent.MessageFormatter();
			formatter.getHeader().add(headerApplier);

			Text headerText = formatter.getHeader().format();
			formatter.setBody(Text.of(TextHelper.getFormatAtEnd(headerText).orElse(TextFormat.NONE), textMessage));

			MessageEvent event = new MessageEventTmp(cause, formatter, false);
			boolean cancelled = Sponge.getEventManager().post(event);

			if(!cancelled) {
				Map<String, TextElement> templateMap = ImmutableMap.of(ConfigSettings.TEMPLATE_HEADER, srcName);
				receiver.sendMessage(event.getMessage());
				src.sendMessage(getCfg().getPmSenderTemplate().apply(templateMap).append(event.getFormatter().getBody().format()).build());

				if(getCfg().getChatPling()) {
					receiver.playSound(SoundTypes.ENTITY_EXPERIENCE_ORB_PICKUP, receiver.getLocation().getPosition(), 0.5D);
				}

				return CommandResult.success();
			}
			else {
				src.sendMessage(Text.of(TextColors.RED, "Your message did not reach " + receiver.getName()));
				return CommandResult.empty();
			}
		}
		src.sendMessage(Text.of(TextColors.RED, "Player not found"));
		return CommandResult.empty();
	}

	@Override
	public CommandSpec getCommand() {
		return CommandSpec.builder()
				.description(Text.of("Send a private message to someone else"))
				.permission(LibPerm.PM)
				.arguments(GenericArguments.player(LibCommandKey.PLAYER), GenericArguments.remainingJoinedStrings(LibCommandKey.MESSAGE))
				.executor(this)
				.build();
	}

	@Override
	public String[] getAliases() {
		return new String[] {"pm", "msg", "tell", "w"};
	}

	public Optional<CommandSource> getConversationPartner(CommandSource player) {
		return Optional.ofNullable(conversations.get(player));
	}
}
