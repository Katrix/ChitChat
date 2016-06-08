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
package io.github.katrix_.chitchat.chat;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.spongepowered.api.effect.sound.SoundTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.event.message.MessageEvent.MessageFormatter;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.service.permission.option.OptionSubject;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TextElement;
import org.spongepowered.api.text.TranslatableText;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.channel.type.CombinedMessageChannel;
import org.spongepowered.api.text.format.TextFormat;
import org.spongepowered.api.text.format.TextStyle;
import org.spongepowered.api.text.serializer.FormattingCodeTextSerializer;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.text.transform.SimpleTextTemplateApplier;

import com.google.common.collect.ImmutableMap;

import io.github.katrix_.chitchat.ChitChat;
import io.github.katrix_.chitchat.helper.LogHelper;
import io.github.katrix_.chitchat.io.ConfigSettings;
import io.github.katrix_.chitchat.lib.LibPerm;

public class ChatListener {

	@Listener(order = Order.LATE)
	public void onChat(MessageChannelEvent.Chat event, @First Player player) {
		ConfigSettings cfg = ChitChat.getConfig();
		MessageFormatter formatter = event.getFormatter();
		FormattingCodeTextSerializer serializer = TextSerializers.FORMATTING_CODE;
		Text prefix = cfg.getDefaultPrefix();
		Text suffix = cfg.getDefaultSuffix();
		TextElement name = getNameChat(formatter.getHeader().getAll(), "header").orElse(Text.of(player.getName()));

		Subject subject = player.getContainingCollection().get(player.getIdentifier());
		if(subject instanceof OptionSubject) {
			OptionSubject optionSubject = (OptionSubject)subject;
			Optional<String> option;

			option = optionSubject.getOption("prefix");
			prefix = option.isPresent() ? serializer.deserialize(option.get()) : prefix;

			option = optionSubject.getOption("suffix");
			suffix = option.isPresent() ? serializer.deserialize(option.get()) : suffix;

			option = optionSubject.getOption("nameColor");
			Optional<TextFormat> textFormat = option.flatMap(s -> {
				List<Text> children = serializer.deserialize(s).getChildren();
				if(!children.isEmpty()) {
					return Optional.of(children.get(0).getFormat());
				}
				else return Optional.empty();
			});
			name = textFormat.isPresent() ? Text.of(textFormat.get(), name) : name;
		}

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

		ChannelChitChat playerChannel = ChitChatPlayers.getOrCreate(player).getChannel();
		event.setChannel(new CombinedMessageChannel(playerChannel, MessageChannel.TO_CONSOLE));

		if(cfg.getChatPling()) {
			String message = event.getFormatter().getBody().format().toPlain();
			Map<Player, UserChitChat> playerMap = ChitChatPlayers.getMap();
			for(Map.Entry<Player, UserChitChat> entry : playerMap.entrySet()) {
				Player player1 = entry.getKey();
				if(entry.getValue().getChannel().equals(playerChannel) && message.contains(player1.getName())) {
					player1.playSound(SoundTypes.ORB_PICKUP, player1.getLocation().getPosition(), 0.5D);
				}
			}
		}
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
		ChannelChitChat channel = ChitChat.getStorage().getChannelForUser(player);
		ChitChatPlayers.getOrCreate(player).setChannel(channel);
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
}
