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
package io.github.katrix.chitchat.helper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextFormat;
import org.spongepowered.api.text.format.TextStyle;
import org.spongepowered.api.text.format.TextStyles;

@SuppressWarnings({"WeakerAccess", "unused"})
public class TextHelper {

	public static Optional<TextFormat> getFormatAtEnd(Text text) {
		return getTextAtEnd(text, t -> !t.getFormat().equals(TextFormat.NONE)).map(Text::getFormat);
	}

	public static Optional<TextColor> getColorAtEnd(Text text) {
		return getTextAtEnd(text, t -> !t.getColor().equals(TextColors.NONE)).map(Text::getColor);
	}

	public static Optional<TextStyle> getStyleAtEnd(Text text) {
		return getTextAtEnd(text, t -> !t.getStyle().equals(TextStyles.NONE)).map(Text::getStyle);
	}

	public static Optional<Text> getTextAtEnd(Text text, Predicate<Text> predicate) {
		List<Text> endTexts = new ArrayList<>();

		Text end = text;
		endTexts.add(end);

		List<Text> children = end.getChildren();
		while(!children.isEmpty()) {
			end = children.get(children.size() - 1);
			children = end.getChildren();
			endTexts.add(end);
		}

		return endTexts.stream().filter(predicate).findFirst();
	}
}
