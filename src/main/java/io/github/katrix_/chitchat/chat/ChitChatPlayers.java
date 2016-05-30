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
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import com.google.common.collect.ImmutableMap;

public class ChitChatPlayers {

	private static final Map<Player, UserChitChat> PLAYER_MAP = new WeakHashMap<>();

	public static UserChitChat getOrCreate(Player player) {
		UserChitChat playerChitChat = PLAYER_MAP.get(player);
		if(playerChitChat == null) {
			playerChitChat = new UserChitChat(player);
			PLAYER_MAP.put(player, playerChitChat);
		}
		return playerChitChat;
	}

	public static UserChitChat getFromUuid(UUID uuid) {
		return new UserChitChat(uuid);
	}

	@SuppressWarnings("unused")
	public static void moveAllToGlobal(@Nullable Text message) {
		movePlayersToGlobal(message, entry -> true);
	}

	@SuppressWarnings("WeakerAccess")
	public static void moveChannelToGlobal(ChannelChitChat channel, @Nullable Text message) {
		movePlayersToGlobal(message, entry -> entry.getValue().getChannel().equals(channel));
	}

	@SuppressWarnings("WeakerAccess")
	public static void movePlayersToGlobal(@Nullable Text message, Predicate<Map.Entry<Player, UserChitChat>> test) {
		ChannelChitChat global = ChitChatChannels.getGlobal();
		Text text = message != null ? message : Text.of(TextColors.RED, "You are being moved to the global channel");
		PLAYER_MAP.entrySet().stream().filter(test).forEach(entry -> {
			entry.getKey().sendMessage(text);
			entry.getValue().setChannel(global);
		});
	}

	public static ImmutableMap<Player, UserChitChat> getMap() {
		return ImmutableMap.copyOf(PLAYER_MAP);
	}
}
