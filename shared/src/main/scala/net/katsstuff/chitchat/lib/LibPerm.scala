package net.katsstuff.chitchat.lib

object LibPerm {

  final val ChitChat = "chitchat"

  final val Chat      = s"$ChitChat.chat"
  final val ChatColor = s"$Chat.color"

  final val UserCmd         = s"$ChitChat.user"
  final val JoinChannelCmd  = s"$UserCmd.joinchannel"
  final val ListChannelsCmd = s"$UserCmd.listchannels"
  final val InChannelCmd    = s"$UserCmd.inchannel"

  final val MeCmd    = s"$UserCmd.me"
  final val PMCmd    = s"$UserCmd.pm"
  final val ShoutCmd = s"$UserCmd.shout"

  final val ModCmd             = s"$ChitChat.mod"
  final val ModInterfaceCmd    = s"$ModCmd.interface"
  final val CreateChannelCmd   = s"$ModCmd.createchannel"
  final val ChangeChannelCmd   = s"$ModCmd.editchannel"
  final val DeleteChannelCmd   = s"$ModCmd.deletechannel"
  final val AnnounceCmd        = s"$ModCmd.announce"
  final val AnnounceConsoleCmd = s"$ModCmd.announceconsole"

}
