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
package io.github.katrix_.chitchat.lib;

public class LibPerm {

	public static final String PREFIX = "chitchat";
	public static final String PLAYER = ".player";

	public static final String COMMAND = PREFIX + ".command";
	public static final String CHAT = PREFIX + ".chat";
	public static final String CHANNEL = PREFIX + ".channel";
	public static final String ADMIN = PREFIX + ".admin";

	public static final String CHITCHAT = COMMAND + ".chitchat";
	public static final String HELP = COMMAND + ".help";

	public static final String SHOUT = CHAT + ".shout";
	public static final String PM = CHAT + ".pm";
	public static final String REPLY = CHAT + ".reply";
	public static final String ME = CHAT + ".me";
	public static final String ANNOUNCE = CHAT_MOD + ".announce";
	public static final String ANNOUNCE_CONSOLE = ANNOUNCE + ".console";
	public static final String CHAT_COLOR = CHAT + ".color";

	public static final String RELOAD = ADMIN + ".reload";
	public static final String CHANNEL_SAVE = ADMIN + ".save";
	public static final String CHANNEL_LOAD = ADMIN + ".load";

	public static final String CHANNEL_JOIN = CHANNEL + ".join";
	public static final String CHANNEL_LIST = CHANNEL + ".list";
	public static final String CHANNEL_MOD = CHANNEL + ".mod";
	public static final String CHANNEL_INFO = CHANNEL + ".info";
	public static final String CHANNEL_MODIFY = CHANNEL_MOD + ".modify";
	public static final String CHANNEL_NAME = CHANNEL_MODIFY + ".name";
	public static final String CHANNEL_DESCRIPTION = CHANNEL_MODIFY + ".description";
	public static final String CHANNEL_PREFIX = CHANNEL_MODIFY + ".prefix";
	public static final String CHANNEL_CREATE = CHANNEL_MOD + ".create";
	public static final String CHANNEL_REMOVE = CHANNEL_MOD + ".remove";
}
