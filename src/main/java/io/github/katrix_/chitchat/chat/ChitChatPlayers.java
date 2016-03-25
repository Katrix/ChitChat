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
import java.util.WeakHashMap;
import javax.annotation.Nullable;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import com.google.common.collect.ImmutableMap;

public class ChitChatPlayers {

	private static final Map<Player, UserChitChat> PLAYER_MAP = new WeakHashMap<>();

	public static UserChitChat getOrCreatePlayer(Player player) {
		UserChitChat playerChitChat = PLAYER_MAP.get(player);
		if(playerChitChat == null) {
			playerChitChat = new UserChitChat(player);
			PLAYER_MAP.put(player, playerChitChat);
		}
		return playerChitChat;
	}
	
	public static void removePlayer(Player player) {
		PLAYER_MAP.remove(player);
	}

	public static void moveAllPlayersToGlobal(@Nullable Text message) {
		ChannelChitChat global = ChitChatChannels.getGlobalChannel();
		Text text = message != null ? message : Text.of(TextColors.RED, "You are being sent to the global channel");
		PLAYER_MAP.keySet().stream().forEach(player -> {
			player.sendMessage(text);
			PLAYER_MAP.get(player).setChannel(global);
		});
	}

	public static void movePlayersInChannelToGlobal(ChannelChitChat channel, @Nullable Text message) {
		ChannelChitChat global = ChitChatChannels.getGlobalChannel();
		Text text = message != null ? message : Text.of(TextColors.RED, "You are being sent to the global channel");
		PLAYER_MAP.keySet().stream().filter(player -> PLAYER_MAP.get(player).getChannel().equals(channel)).forEach(player -> {
			player.sendMessage(text);
			PLAYER_MAP.get(player).setChannel(global);
		});
	}

	public static ImmutableMap<Player, UserChitChat> getPlayerMap() {
		return ImmutableMap.copyOf(PLAYER_MAP);
	}
}
