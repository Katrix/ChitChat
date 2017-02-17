package net.katsstuff.chitchat.lib

object LibPerm {

  final val ChitChat = "chitchat"

  final val Chat      = s"$ChitChat.chat"
  final val ChatColor = s"$Chat.color"

  final val UserCmd         = s"$ChitChat.userCmd"
  final val JoinChannelCmd  = s"$UserCmd.joinChannel"
  final val ListChannelsCmd = s"$UserCmd.listChannels"
  final val InChannelCmd    = s"$UserCmd.inChannel"

  final val MeCmd    = s"$UserCmd.meCmd"
  final val PMCmd    = s"$UserCmd.pmCmd"
  final val ShoutCmd = s"$UserCmd.shoutCmd"

  final val AdminCmd          = s"$ChitChat.adminCmd"
  final val AdminInterfaceCmd = s"$AdminCmd.interface"
  final val CreateChannelCmd  = s"$AdminCmd.createChannel"
  final val ChangeChannelCmd  = s"$AdminCmd.editChannel"
  final val DeleteChannelCmd  = s"$AdminCmd.deleteChannel"
  final val AnnounceCmd       = s"$AdminCmd.announceCmd"

}
