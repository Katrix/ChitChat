package io.github.katrix_.chitchat.lib;

import org.spongepowered.api.data.DataQuery;

public class LibDataQueries {

	public static final DataQuery CHANNEL_NAME = DataQuery.of("name");
	public static final DataQuery CHANNEL_PREFIX = DataQuery.of("prefix");
	public static final DataQuery CHANNEL_DESCRIPTION = DataQuery.of("description");
	public static final DataQuery CHANNEL_PARENT = DataQuery.of("parent");
	public static final DataQuery CHANNEL_CHILDREN = DataQuery.of("children");
}
