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
package net.katsstuff.old.chitchat.chat;

import java.util.List;
import java.util.Optional;

import org.spongepowered.api.effect.sound.SoundTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.event.message.MessageEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TextElement;
import org.spongepowered.api.text.TranslatableText;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.channel.type.CombinedMessageChannel;
import org.spongepowered.api.text.format.TextFormat;
import org.spongepowered.api.text.serializer.FormattingCodeTextSerializer;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.text.transform.SimpleTextTemplateApplier;

import com.google.common.collect.ImmutableMap;

import net.katsstuff.old.chitchat.ChitChat;
import net.katsstuff.old.chitchat.helper.TextHelper;
import net.katsstuff.old.chitchat.io.ConfigSettings;
import net.katsstuff.old.chitchat.lib.LibKeys;
import net.katsstuff.old.chitchat.lib.LibPerm;

public class ChatListener {

	@Listener(order = Order.LATE)
	public void onMessageChannel(MessageChannelEvent event) {
		Optional<Player> optPlayer = event.getCause().first(Player.class);
		if(event instanceof MessageChannelEvent.Chat && optPlayer.isPresent()) {
			Player player = optPlayer.get();
			ConfigSettings cfg = ChitChat.getConfig();
			MessageEvent.MessageFormatter formatter = event.getFormatter();
			FormattingCodeTextSerializer serializer = TextSerializers.FORMATTING_CODE;
			Text prefix = cfg.getDefaultPrefix();
			Text suffix = cfg.getDefaultSuffix();
			TextElement name = getNameChat(formatter.getHeader().getAll(), "header").orElse(Text.of(player.getName()));


			Subject subject = player.getContainingCollection().get(player.getIdentifier());
			Optional<String> option;

			option = subject.getOption("prefix");
			prefix = option.isPresent() ? serializer.deserialize(option.get()) : prefix;

			option = subject.getOption("suffix");
			suffix = option.isPresent() ? serializer.deserialize(option.get()) : suffix;

			option = subject.getOption("nameColor");
			Optional<TextFormat> textFormat = option.flatMap(s -> TextHelper.getFormatAtEnd(serializer.deserialize(s)));
			name = textFormat.isPresent() ? Text.of(textFormat.get(), name) : name;

			for(SimpleTextTemplateApplier applier : formatter.getHeader()) {
				if(applier.getParameters().containsKey("header")) {
					applier.setTemplate(cfg.getHeaderTemplate());
					applier.setParameter("header", name);
				}

				applier.setParameter(ConfigSettings.TEMPLATE_PREFIX, prefix);
			}

			if(player.hasPermission(LibPerm.CHAT_COLOR)) {
				for(SimpleTextTemplateApplier applier : formatter.getBody()) {
					if(applier.getParameters().containsKey("body")) {
						applier.setParameter("body", serializer.deserialize(serializer.serialize(Text.of(applier.getParameter("body"))))); //In case the message is already formatted
					}
				}
			}

			SimpleTextTemplateApplier suffixApplier = new SimpleTextTemplateApplier(cfg.getSuffixTemplate());
			formatter.getFooter().add(suffixApplier);

			for(SimpleTextTemplateApplier applier : formatter.getFooter().getAll()) {
				applier.setParameter(ConfigSettings.TEMPLATE_SUFFIX, suffix);
			}

			ChannelChitChat playerChannel = getPlayerChannel(player);
			event.setChannel(playerChannel);

			if(cfg.getChatPling()) {
				String message = event.getFormatter().getBody().format().toPlain();
				playerChannel.getMembers().stream()
						.filter(messageReceiver -> messageReceiver instanceof Player)
						.map(m -> (Player)m)
						.filter(p -> message.contains(p.getName()))
						.forEach(p -> p.playSound(SoundTypes.ENTITY_EXPERIENCE_ORB_PICKUP, p.getLocation().getPosition(), 0.5D));
			}
		}

		//Console knows all!!!
		Optional<MessageChannel> currentChannel = event.getChannel();
		currentChannel.ifPresent(c -> event.setChannel(new CombinedMessageChannel(c, MessageChannel.TO_CONSOLE)));
	}

	@SuppressWarnings("Convert2streamapi")
	@Listener
	public void onJoin(ClientConnectionEvent.Join event) {
		ConfigSettings cfg = ChitChat.getConfig();

		List<SimpleTextTemplateApplier> appliers = event.getFormatter().getBody().getAll();
		TextElement name = getNameConnect(appliers, "body").orElse(Text.of(event.getTargetEntity().getName()));

		for(SimpleTextTemplateApplier applier : appliers) {
			if(applier.getParameters().containsKey("body")) {
				applier.setTemplate(ChitChat.getConfig().getJoinTemplate());
				applier.setParameter("body", name);
			}
		}

		Player player = event.getTargetEntity();
		ChannelChitChat channel = getPlayerChannel(player);
		player.sendMessage(cfg.getChattingJoinTemplate(), ImmutableMap.of(ConfigSettings.TEMPLATE_CHANNEL, Text.of(channel.getName())));
	}

	@SuppressWarnings("Convert2streamapi")
	@Listener
	public void onQuit(ClientConnectionEvent.Disconnect event) {
		List<SimpleTextTemplateApplier> appliers = event.getFormatter().getBody().getAll();
		TextElement name = getNameConnect(appliers, "body").orElse(Text.of(event.getTargetEntity().getName()));

		for(SimpleTextTemplateApplier applier : appliers) {
			if(applier.getParameters().containsKey("body")) {
				applier.setTemplate(ChitChat.getConfig().getDisconnectTemplate());
				applier.setParameter("body", name);
			}
		}
	}

	private Optional<TextElement> getNameChat(List<SimpleTextTemplateApplier> applierList, String parameterName) {
		for(SimpleTextTemplateApplier applier : applierList) {
			if(applier.getParameters().containsKey(parameterName)) {
				TextElement textElement = applier.getParameter(parameterName);
				return Optional.of(textElement);
			}
		}

		return Optional.empty();
	}

	private Optional<Text> getNameConnect(List<SimpleTextTemplateApplier> applierList, String parameterName) {
		for(SimpleTextTemplateApplier applier : applierList) {
			if(applier.getParameters().containsKey(parameterName)) {
				TextElement textElement = applier.getParameter(parameterName);
				if(textElement instanceof TranslatableText) {
					TranslatableText translatable = (TranslatableText)textElement;
					return translatable.getArguments().stream()
							.filter(o -> o instanceof Text)
							.map(o -> (Text)o)
							.findFirst();
				}
			}
		}

		return Optional.empty();
	}

	private ChannelChitChat getPlayerChannel(Player player) {
		return ChannelChitChat.getRoot().getChannel(player
				.get(LibKeys.USER_CHANNEL)
				.orElse(ChannelChitChat.getRoot().getQueryName()))
				.orElse(ChannelChitChat.getRoot());
	}
}
