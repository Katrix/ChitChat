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

import java.util.Map;
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
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.text.transform.SimpleTextTemplateApplier;

import com.google.common.collect.ImmutableMap;

import io.github.katrix_.chitchat.io.ConfigSettings;
import io.github.katrix_.chitchat.io.SQLStorage;
import io.github.katrix_.chitchat.lib.LibPerm;

public class ChatListener {

	@Listener(order = Order.EARLY)
	public void onChat(MessageChannelEvent.Chat event, @First Player player) {
		MessageFormatter formatter = event.getFormatter();
		String nameString = player.getName();
		Text prefixName = ConfigSettings.getDefaultHeader().apply(ImmutableMap.of(ConfigSettings.TEMPLATE_PLAYER, Text.of(nameString))).build();
		Text suffix = ConfigSettings.getDefaultSuffix();

		if(player.hasPermission(LibPerm.CHAT_COLOR)) {
			for(SimpleTextTemplateApplier applier : formatter.getBody()) {
				if(applier.getParameters().containsKey("body")) {
					applier.setParameter("body", TextSerializers.FORMATTING_CODE.deserialize(Text.of(applier.getParameter("body")).toPlain()));
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
				applier.setTemplate(ConfigSettings.getHeaderTemplate());

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
			SimpleTextTemplateApplier applier = new SimpleTextTemplateApplier(ConfigSettings.getSuffixTemplate());
			applier.setParameter(ConfigSettings.TEMPLATE_SUFFIX, suffix);
			formatter.getFooter().add(applier);
		}

		ChannelChitChat channel = ChitChatPlayers.getOrCreatePlayer(player).getChannel();
		event.setChannel(channel);

		if(ConfigSettings.getChatPling()) {
			String message = event.getFormatter().getBody().format().toPlain();
			Map<Player, UserChitChat> playerMap = ChitChatPlayers.getPlayerMap();
			playerMap.keySet().stream().filter(player1 -> playerMap.get(player1).getChannel().equals(channel) && message.contains(player1.getName()))
					.forEach(player1 -> player1.playSound(SoundTypes.ORB_PICKUP, player1.getLocation().getPosition(), 0.5D));
		}
	}

	@Listener
	public void onJoin(ClientConnectionEvent.Join event) {
		Player player = event.getTargetEntity();
		ChannelChitChat channel = SQLStorage.getChannelForUser(player);
		ChitChatPlayers.getOrCreatePlayer(player).setChannel(channel);
		player.sendMessage(ConfigSettings.getChattingJoinTemplate(), ImmutableMap.of(ConfigSettings.TEMPLATE_CHANNEL, Text.of(channel.getName())));
	}
}
