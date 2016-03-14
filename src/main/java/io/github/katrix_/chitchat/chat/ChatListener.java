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
import java.util.Optional;
import java.util.stream.Collectors;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.effect.sound.SoundTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.event.message.MessageEvent.MessageFormatter;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.service.ProviderRegistration;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.service.permission.option.OptionSubject;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Text.Builder;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.text.transform.SimpleTextTemplateApplier;

import com.google.common.collect.ImmutableMap;

import io.github.katrix_.chitchat.io.ConfigSettings;
import io.github.katrix_.chitchat.lib.LibPerm;

public class ChatListener {

	@Listener(order = Order.EARLY)
	public void onChat(MessageChannelEvent.Chat event, @First Player player) {
		MessageFormatter formatter = event.getFormatter();
		String nameString = player.getName();
		Text prefixName = ConfigSettings.getHeaderTemplate()
				.apply(ImmutableMap.of(ConfigSettings.TEMPLATE_HEADER,
						ConfigSettings.getDefaultHeader().apply(ImmutableMap.of(ConfigSettings.TEMPLATE_PLAYER, Text.of(nameString))).build()))
				.build();
		Text suffix = ConfigSettings.getDefaultSuffix();

		if(player.hasPermission(LibPerm.CHAT_COLOR)) {
			for(SimpleTextTemplateApplier applier : formatter.getBody()) {
				if(applier.getParameters().keySet().contains("body")) {
					Text message = Text.of(applier.getParameter("body"));
					message = TextSerializers.FORMATTING_CODE.deserialize(message.toPlain());
					applier.setParameter("body", message);
				}
			}
		}

		Optional<ProviderRegistration<PermissionService>> optPerm = Sponge.getServiceManager().getRegistration(PermissionService.class);
		if(optPerm.isPresent()) {
			Subject subject = optPerm.get().getProvider().getUserSubjects().get(player.getIdentifier());
			if(subject instanceof OptionSubject) {
				OptionSubject optionSubject = (OptionSubject)subject;
				prefixName = optionSubject.getOption("prefix").isPresent() ? ConfigSettings.getHeaderTemplate()
						.apply(ImmutableMap.of(ConfigSettings.TEMPLATE_HEADER,
								TextSerializers.FORMATTING_CODE.deserialize(optionSubject.getOption("prefix").get() + nameString)))
						.build() : prefixName;
				suffix = optionSubject.getOption("suffix").isPresent() ? TextSerializers.FORMATTING_CODE
						.deserialize(optionSubject.getOption("suffix").orElse("")) : suffix;
			}
		}

		if(!suffix.isEmpty()) {
			SimpleTextTemplateApplier applier = new SimpleTextTemplateApplier(ConfigSettings.getSuffixTemplate());
			applier.setParameter(ConfigSettings.TEMPLATE_SUFFIX, suffix);
			formatter.getFooter().add(applier);
		}

		Builder header = Text.builder();
		header.append(prefixName);
		header.onClick(TextActions.suggestCommand("/msg " + nameString + " "));
		header.onShiftClick(TextActions.insertText(nameString));
		header.onHover(TextActions.showEntity(player.getUniqueId(), nameString));
		formatter.setHeader(header);

		ChannelChitChat channel = ChitChatChannels.getChannelForPlayer(player);
		event.setChannel(channel);
		if(ConfigSettings.getChatPling()) {
			String message = event.getFormatter().getBody().format().toPlain();
			List<Player> channelPlayers = ChitChatChannels.getChannelPlayerMap().keySet().stream()
					.filter(channelPlayer -> ChitChatChannels.getChannelForPlayer(channelPlayer).equals(channel)).collect(Collectors.toList());
			channelPlayers.stream().filter(channelPlayer -> message.contains(channelPlayer.getName()))
					.forEach(channelPlayer -> channelPlayer.playSound(SoundTypes.ORB_PICKUP, player.getLocation().getPosition(), 0.5D));
		}
	}

	@Listener
	public void onJoin(ClientConnectionEvent.Join event) {
		Player player = event.getTargetEntity();
		ChannelChitChat global = ChitChatChannels.getGlobalChannel();
		global.addMember(player);
		ChitChatChannels.setChannelForPlayer(player, global);
	}
}
