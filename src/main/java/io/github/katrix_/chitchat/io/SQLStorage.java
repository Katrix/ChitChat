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
package io.github.katrix_.chitchat.io;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nullable;
import javax.sql.DataSource;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.sql.SqlService;
import org.spongepowered.api.text.Text;

import io.github.katrix_.chitchat.ChitChat;
import io.github.katrix_.chitchat.chat.ChannelChitChat;
import io.github.katrix_.chitchat.chat.ChitChatChannels;
import io.github.katrix_.chitchat.chat.UserChitChat;
import io.github.katrix_.chitchat.helper.LogHelper;

public class SQLStorage implements IPersistentStorage {

	private DataSource source;

	public SQLStorage() throws SQLException {
		String path = "jdbc:h2:" + ChitChat.getPlugin().getConfigDir().toAbsolutePath().toString() + "/chitChat";
		SqlService sql = Sponge.getServiceManager().provideUnchecked(SqlService.class);
		source = sql.getDataSource(path);
		createTables();
	}

	@Override
	public boolean reloadChannels() {
		List<ChannelChitChat> channelList = null;

		try {
			channelList = getChannelList();
			ChitChatChannels.clearChannelMap();
			channelList.forEach(ChitChatChannels::addChannel);
		}
		catch(SQLException e) {
			e.printStackTrace();
		}

		return channelList != null;
	}

	@Override
	public boolean saveChannel(ChannelChitChat channel) {
		if(channel.equals(ChitChatChannels.getGlobalChannel())) return false;

		try {
			saveChannelDatabase(channel);
			return true;
		}
		catch(SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public boolean updateChannel(ChannelChitChat channel, @Nullable Text prefix, @Nullable Text description) {
		return updateChannel(channel.getName(), prefix, description);
	}

	@Override
	public boolean updateChannel(String channelName, @Nullable Text prefix, @Nullable Text description) {
		try {
			if(prefix != null) updateChannelPrefix(channelName, prefix.toPlain());
			if(description != null) updateChannelDescription(channelName, description.toPlain());
			return !(prefix == null || description == null);
		}
		catch(SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public boolean deleteChannel(ChannelChitChat channel) {
		return deleteChannel(channel.getName());
	}

	@Override
	public boolean deleteChannel(String channelName) {
		try {
			deleteChannelDatabase(channelName);
			return true;
		}
		catch(SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public ChannelChitChat getChannelForUser(User user) {
		return getChannelForUser(user.getUniqueId());
	}

	@Override
	public ChannelChitChat getChannelForUser(UUID uuid) {
		try {
			Optional<String> channelName = getChannelForUserDatabase(uuid);
			if(channelName.isPresent() && ChitChatChannels.doesChannelExist(channelName.get())) {
				return ChitChatChannels.getChannel(channelName.get());
			}
			else {
				return ChitChatChannels.getGlobalChannel();
			}
		}
		catch(SQLException e) {
			e.printStackTrace();
			return ChitChatChannels.getGlobalChannel();
		}
	}

	@Override
	public void updateUserChannel(UserChitChat user) {
		updateUserChannel(user.getUUID(), user.getChannel());
	}

	@Override
	public void updateUserChannel(User user, ChannelChitChat channel) {
		updateUserChannel(user.getUniqueId(), channel);
	}

	@Override
	public void updateUserChannel(UUID uuid, ChannelChitChat channel) {
		try {
			updateUserChannelDatabase(uuid, channel.getName());
		}
		catch(SQLException e) {
			e.printStackTrace();
		}
	}

	private Connection getConnection() throws SQLException {
		return source.getConnection();
	}

	private void createTables() throws SQLException {
		String channels = "CREATE TABLE IF NOT EXISTS channels(id int primary key auto_increment, name varchar, prefix varchar, description varchar)";
		String users = "CREATE TABLE IF NOT EXISTS users(uuid UUID primary key, channel varchar)";
		try(Connection con = getConnection();
				PreparedStatement statChannels = con.prepareStatement(channels);
				PreparedStatement statUsers = con.prepareStatement(users)) {
			statChannels.execute();
			statUsers.execute();
		}
	}

	private void saveChannelDatabase(ChannelChitChat channel) throws SQLException {
		String sql = "INSERT INTO channels (name, prefix, description) VALUES (?,?,?)";
		try(PreparedStatement stat = getConnection().prepareStatement(sql)) {
			stat.setString(1, channel.getName());
			stat.setString(2, channel.getPrefix().toPlain());
			stat.setString(3, channel.getDescription().toPlain());
			stat.executeUpdate();
		}
	}

	private List<ChannelChitChat> getChannelList() throws SQLException {
		List<ChannelChitChat> list = new ArrayList<>();
		String sql = "SELECT * FROM channels";
		try(PreparedStatement stat = getConnection().prepareStatement(sql); ResultSet result = stat.executeQuery()) {
			while(result.next()) {
				if(result.getString("name").equalsIgnoreCase("Global")) result.next();
				String name = result.getString("name");
				String prefix = result.getString("prefix");
				String description = result.getString("description");
				list.add(new ChannelChitChat(name, description, prefix));
			}
		}
		return list;
	}

	private void updateChannelPrefix(String channelName, String newPrefix) throws SQLException {
		String sql = "UPDATE channels SET prefix = ? WHERE name = ?";
		try(PreparedStatement stat = getConnection().prepareStatement(sql)) {
			stat.setString(1, newPrefix);
			stat.setString(2, channelName);
			stat.executeUpdate();
		}
	}

	private void updateChannelDescription(String channelName, String newDescription) throws SQLException {
		String sql = "UPDATE channels SET description = ? WHERE name = ?";
		try(PreparedStatement stat = getConnection().prepareStatement(sql)) {
			stat.setString(1, newDescription);
			stat.setString(2, channelName);
			stat.executeUpdate();
		}
	}

	private void deleteChannelDatabase(String channelName) throws SQLException {
		String sql = "DELETE FROM channels WHERE name = ?";
		try(PreparedStatement stat = getConnection().prepareStatement(sql)) {
			stat.setString(1, channelName);
			stat.executeUpdate();
		}
	}

	private Optional<String> getChannelForUserDatabase(UUID uuid) throws SQLException {
		String sql = "SELECT * FROM users WHERE uuid = ?";
		try(PreparedStatement stat = getConnection().prepareStatement(sql)) {
			stat.setObject(1, uuid);
			try(ResultSet result = stat.executeQuery()) {
				if(result.next()) {
					return Optional.ofNullable(result.getString("channel"));
				}
				else {
					LogHelper.debug("No channel for player found in database, using global");
					return Optional.empty();
				}
			}
		}
	}

	private void updateUserChannelDatabase(UUID uuid, String channelName) throws SQLException {
		String sqlUpdate = "UPDATE users SET channel = ? WHERE uuid = ?";
		try(PreparedStatement statUpdate = getConnection().prepareStatement(sqlUpdate)) {
			statUpdate.setString(1, channelName);
			statUpdate.setObject(2, uuid);
			int success = statUpdate.executeUpdate();
			if(success == 0) {
				String sqlInsert = "INSERT INTO users (uuid, channel) VALUES (?,?)";
				try(PreparedStatement statInsert = getConnection().prepareStatement(sqlInsert)) {
					statInsert.setObject(1, uuid);
					statInsert.setString(2, channelName);
					statInsert.executeUpdate();
				}
			}
		}
	}
}
