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

import javax.annotation.Nullable;

import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.AbstractMutableMessageChannel;
import org.spongepowered.api.text.channel.MessageReceiver;
import org.spongepowered.api.text.chat.ChatType;
import org.spongepowered.api.text.serializer.TextSerializers;

import com.google.common.collect.ImmutableMap;

import io.github.katrix_.chitchat.io.ConfigSettings;

public class ChannelChitChat extends AbstractMutableMessageChannel {

	private String name;
	private Text description;
	private Text prefix;

	public ChannelChitChat(String name, String description, String prefix) {
		this(name, TextSerializers.FORMATTING_CODE.deserialize(description), TextSerializers.FORMATTING_CODE.deserialize(prefix));
	}

	public ChannelChitChat(String name, Text description, Text prefix) {
		super();
		this.name = name;
		this.description = description;
		this.prefix = prefix;
	}

	@Override
	public Optional<Text> transformMessage(@Nullable Object sender, MessageReceiver recipient, Text original, ChatType type) {
		return Optional.of(original.toBuilder()
				.insert(0, ConfigSettings.getChannelTemplate().apply(ImmutableMap.of(ConfigSettings.TEMPLATE_PREFIX, prefix)).build()).build());
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Text getDescription() {
		return description;
	}

	public void setDescription(Text description) {
		this.description = description;
	}

	public void setDescription(String description) {
		this.description = TextSerializers.FORMATTING_CODE.deserialize(description);
	}

	public Text getPrefix() {
		return prefix;
	}

	public void setPrefix(Text prefix) {
		this.prefix = prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = TextSerializers.FORMATTING_CODE.deserialize(prefix);
	}
}
