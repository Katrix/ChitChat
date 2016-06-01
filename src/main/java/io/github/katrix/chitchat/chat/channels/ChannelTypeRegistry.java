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
package io.github.katrix.chitchat.chat.channels;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import io.github.katrix.chitchat.io.StorageType;

public class ChannelTypeRegistry {

	private final Map<String, Class<? extends Channel>> identifierRegistry = new HashMap<>();
	private final Table<Class<? extends Channel>, StorageType, Object> serializerRegistry = HashBasedTable.create();
	public static final ChannelTypeRegistry INSTANCE = new ChannelTypeRegistry();

	private ChannelTypeRegistry() {}

	public <T extends Channel> void register(Class<T> channelClazz, String identifier, StorageType storage, Object serializer) {
		identifierRegistry.put(identifier, channelClazz);
		serializerRegistry.put(channelClazz, storage, serializer);
	}

	public Optional<Class<? extends Channel>> getClazz(String identifier) {
		return Optional.ofNullable(identifierRegistry.get(identifier));
	}

	public Optional<Object> getSerializer(String identifier, StorageType storage) {
		Class<? extends Channel> clazz = identifierRegistry.get(identifier);
		serializerRegistry.get(clazz, storage);

		return getClazz(identifier).flatMap(c -> Optional.ofNullable(serializerRegistry.get(c, storage)));
	}
}
