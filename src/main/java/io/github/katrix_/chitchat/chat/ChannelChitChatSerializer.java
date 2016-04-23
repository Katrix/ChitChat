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

import org.spongepowered.api.text.Text;

import com.google.common.reflect.TypeToken;

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;

public class ChannelChitChatSerializer implements TypeSerializer<ChannelChitChat> {

	@Override
	public ChannelChitChat deserialize(TypeToken<?> type, ConfigurationNode value) throws ObjectMappingException {
		String name = value.getNode("name").getString();
		Text prefix = value.getNode("prefix").getValue(TypeToken.of(Text.class));
		Text description = value.getNode("description").getValue(TypeToken.of(Text.class));

		return new ChannelChitChat(name, prefix, description);
	}

	@Override
	public void serialize(TypeToken<?> type, ChannelChitChat obj, ConfigurationNode value) throws ObjectMappingException {
		value.getNode("name").setValue(obj.getName());
		value.getNode("prefix").setValue(obj.getPrefix());
		value.getNode("description").setValue(obj.getDescription());
	}
}
