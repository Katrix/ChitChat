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
package io.github.katrix_.chitchat.command;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import io.github.katrix_.chitchat.chat.CentralControl;
import io.github.katrix_.chitchat.chat.ChannelChitChat;
import io.github.katrix_.chitchat.lib.LibKeys;

import static org.spongepowered.api.util.SpongeApiTranslationHelper.t;

/**
 * PatternMatchingCommandElement with CommandSource available in getValue.
 */
public class CommandElementChannel extends CommandElement {

	private static final Text nullKeyArg = t("argument");

	public CommandElementChannel(@Nullable Text key) {
		super(key);
	}

	private Iterable<String> getChoices(CommandSource source) {
		if(source instanceof Player) {
			return getChannel((User)source).getChildren().stream().map(ChannelChitChat::getName).collect(Collectors.toList());
		}
		else {
			return ImmutableList.of();
		}
	}

	private Object getValue(String choice, CommandSource source) throws IllegalArgumentException {
		if(source instanceof Player) {
			return getChannel((User)source).getChild(choice).orElse(null);
		}
		else {
			return null;
		}
	}

	@Nullable
	@Override
	protected Object parseValue(CommandSource source, CommandArgs args) throws ArgumentParseException {
		final String unformattedPattern = args.next();
		Pattern pattern = getFormattedPattern(unformattedPattern);
		Iterable<String> filteredChoices = Iterables.filter(getChoices(source), element -> pattern.matcher(element).find());
		for(String el : filteredChoices) { // Match a single value
			if(el.equalsIgnoreCase(unformattedPattern)) {
				return getValue(el, source);
			}
		}
		Iterable<Object> ret = Iterables.transform(filteredChoices, s -> getValue(s, source));

		if(!ret.iterator().hasNext()) {
			throw args.createError(t("No values matching pattern '%s' present for %s!", unformattedPattern, getKey() == null ? nullKeyArg : getKey()));
		}
		return ret;
	}

	@Override
	public List<String> complete(CommandSource src, CommandArgs args, CommandContext context) {
		Iterable<String> choices = getChoices(src);
		final Optional<String> nextArg = args.nextIfPresent();
		if(nextArg.isPresent()) {
			choices = Iterables.filter(choices, input -> getFormattedPattern(nextArg.get()).matcher(input).find());
		}
		return ImmutableList.copyOf(choices);
	}

	private Pattern getFormattedPattern(String input) {
		if(!input.startsWith("^")) { // Anchor matches to the beginning -- this lets us use find()
			input = "^" + input;
		}
		return Pattern.compile(input, Pattern.CASE_INSENSITIVE);

	}

	private ChannelChitChat getChannel(User user) {
		return CentralControl.INSTANCE.getChannel((user)
				.get(LibKeys.USER_CHANNEL)
				.orElse(ChannelChitChat.getRoot().getQueryName()))
				.orElse(ChannelChitChat.getRoot());
	}
}
