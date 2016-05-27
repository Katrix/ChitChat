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

import java.util.Optional;

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
import org.spongepowered.api.text.serializer.FormattingCodeTextSerializer;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.text.transform.SimpleTextTemplateApplier;

import com.google.common.collect.ImmutableMap;

import io.github.katrix_.chitchat.ChitChat;
import io.github.katrix_.chitchat.io.ConfigSettings;
import io.github.katrix_.chitchat.lib.LibKeys;
import io.github.katrix_.chitchat.lib.LibPerm;

public class ChatListener {

	@Listener(order = Order.EARLY)
	public void onChat(MessageChannelEvent.Chat event, @First Player player) {
		ConfigSettings cfg = ChitChat.getConfig();
		MessageFormatter formatter = event.getFormatter();
		String nameString = player.getName();
		Text prefixName = cfg.getDefaultHeader().apply(ImmutableMap.of(ConfigSettings.TEMPLATE_PLAYER, Text.of(nameString))).build();
		Text suffix = cfg.getDefaultSuffix();

		if(player.hasPermission(LibPerm.CHAT_COLOR)) {
			FormattingCodeTextSerializer serializer = TextSerializers.FORMATTING_CODE;
			for(SimpleTextTemplateApplier applier : formatter.getBody()) {
				if(applier.getParameters().containsKey("body")) {
					applier.setParameter("body", serializer.deserialize(serializer.serialize(Text.of(applier.getParameter("body"))))); //In case the message is already formatted
				}
			}
		}

		Subject subject = player.getContainingCollection().get(player.getIdentifier());
		if(subject instanceof OptionSubject) {
			OptionSubject optionSubject = (OptionSubject)subject;
			Optional<String> option;
			option = optionSubject.getOption("prefix");
			prefixName = option.isPresent() ? TextSerializers.FORMATTING_CODE.deserialize(option.get() + nameString) : prefixName;
			option = optionSubject.getOption("suffix");
			suffix = option.isPresent() ? TextSerializers.FORMATTING_CODE.deserialize(option.get()) : suffix;
		}

		for(SimpleTextTemplateApplier applier : formatter.getHeader()) {
			if(applier.getParameters().containsKey("header")) {
				applier.setTemplate(cfg.getHeaderTemplate());

				Text oldHeader = Text.of(applier.getParameter("header"));
				Text.Builder newHeader = Text.builder();
				newHeader.append(prefixName);
				newHeader.onHover(oldHeader.getHoverAction().orElse(null));
				newHeader.onClick(oldHeader.getClickAction().orElse(null));
				newHeader.onShiftClick(oldHeader.getShiftClickAction().orElse(null));
				applier.setParameter("header", newHeader);
			}
		}

		if(!suffix.isEmpty()) {
			SimpleTextTemplateApplier applier = new SimpleTextTemplateApplier(cfg.getSuffixTemplate());
			applier.setParameter(ConfigSettings.TEMPLATE_SUFFIX, suffix);
			formatter.getFooter().add(applier);
		}

		ChannelChitChat playerChannel = CentralControl.INSTANCE.getChannel(player.get(LibKeys.USER_CHANNEL).orElse(ChannelChitChat.ChannelRoot.ROOT_QUERY)).orElse(ChannelChitChat.getRoot());
		event.setChannel(new CombinedMessageChannel(playerChannel, MessageChannel.TO_CONSOLE));

		if(cfg.getChatPling()) {
			String message = event.getFormatter().getBody().format().toPlain();

			playerChannel.getMembers().stream()
					.filter(messageReceiver -> messageReceiver instanceof Player)
					.map(m -> (Player)m)
					.filter(p -> message.contains(p.getName()))
					.forEach(p -> p.playSound(SoundTypes.ENTITY_EXPERIENCE_ORB_PICKUP, p.getLocation().getPosition(), 0.5D));
		}
	}

	@Listener
	public void onJoin(ClientConnectionEvent.Join event) {
		ConfigSettings cfg = ChitChat.getConfig();

		for(SimpleTextTemplateApplier applier : event.getFormatter().getBody().getAll()) {
			if(applier.getParameters().containsKey("body")) {
				TextElement textElement = applier.getParameter("body");
				TextElement playerName = Text.of(event.getTargetEntity().getName());
				if(textElement instanceof TranslatableText) {
					TranslatableText translatable = (TranslatableText)textElement;
					Object potentialName = translatable.getArguments().get(0);
					if(potentialName instanceof TextElement) {
						playerName = (TextElement)potentialName;
					}
				}
				applier.setTemplate(cfg.getJoinTemplate());
				applier.setParameter("body", playerName);
			}
		}

		Player player = event.getTargetEntity();
		ChannelChitChat channel = CentralControl.INSTANCE.getChannel(player.get(LibKeys.USER_CHANNEL).orElse(ChannelChitChat.ChannelRoot.ROOT_QUERY)).orElse(ChannelChitChat.getRoot());
		player.sendMessage(cfg.getChattingJoinTemplate(), ImmutableMap.of(ConfigSettings.TEMPLATE_CHANNEL, Text.of(channel.getName())));
	}

	@Listener
	public void onQuit(ClientConnectionEvent.Disconnect event) {
		for(SimpleTextTemplateApplier applier : event.getFormatter().getBody().getAll()) {
			if(applier.getParameters().containsKey("body")) {
				TextElement textElement = applier.getParameter("body");
				TextElement playerName = Text.of(event.getTargetEntity().getName());
				if(textElement instanceof TranslatableText) {
					TranslatableText translatable = (TranslatableText)textElement;
					Object potentialName = translatable.getArguments().get(0);
					if(potentialName instanceof TextElement) {
						playerName = (TextElement)potentialName;
					}
				}
				applier.setTemplate(ChitChat.getConfig().getDisconnectTemplate());
				applier.setParameter("body", playerName);
			}
		}
	}
}
