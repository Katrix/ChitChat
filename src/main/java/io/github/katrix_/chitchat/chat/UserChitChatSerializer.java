/*
 * This file is part of PermissionBlock, licensed under the MIT License (MIT).
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

import java.util.UUID;

import com.google.common.reflect.TypeToken;

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;

public class UserChitChatSerializer implements TypeSerializer<UserChitChat> {

	@Override
	public UserChitChat deserialize(TypeToken<?> type, ConfigurationNode value) throws ObjectMappingException {
		UUID uuid = value.getNode("uuid").getValue(TypeToken.of(UUID.class));
		String channelName = value.getNode("channel").getString();
		ChannelChitChat channel;

		if(ChitChatChannels.existName(channelName)) {
			channel = ChitChatChannels.get(channelName);
		}
		else {
			channel = ChitChatChannels.getGlobal();
		}

		return new UserChitChat(uuid, channel);
	}

	@Override
	public void serialize(TypeToken<?> type, UserChitChat obj, ConfigurationNode value) throws ObjectMappingException {
		value.getNode("uuid").setValue(obj.getUUID());
		value.getNode("channel").setValue(obj.getChannel().getName());
	}
}
