package discordbot.command.music;

import discordbot.command.CommandVisibility;
import discordbot.core.AbstractCommand;
import discordbot.db.controllers.CMusic;
import discordbot.db.controllers.CMusicVote;
import discordbot.db.controllers.CPlaylist;
import discordbot.db.controllers.CUser;
import discordbot.db.model.OMusic;
import discordbot.db.model.OMusicVote;
import discordbot.db.model.OPlaylist;
import discordbot.db.model.OUser;
import discordbot.guildsettings.music.SettingMusicClearAdminOnly;
import discordbot.guildsettings.music.SettingMusicRole;
import discordbot.guildsettings.music.SettingMusicShowListeners;
import discordbot.handler.GuildSettings;
import discordbot.handler.MusicPlayerHandler;
import discordbot.handler.Template;
import discordbot.main.Config;
import discordbot.main.DiscordBot;
import discordbot.permission.SimpleRank;
import discordbot.util.DisUtil;
import discordbot.util.Emojibet;
import discordbot.util.Misc;
import discordbot.util.MusicUtil;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.utils.PermissionUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * !current
 * retrieves information about the currently playing track
 */
public class NowPlayingCommand extends AbstractCommand {
	private static final Pattern votePattern = Pattern.compile("^(?>vote|rate)\\s?(\\d+)?$");


	public NowPlayingCommand() {
		super();
	}

	@Override
	public String getDescription() {
		return "retrieves information about the song currently playing";
	}

	@Override
	public String getCommand() {
		return "current";
	}

	@Override
	public String[] getUsage() {
		return new String[]{
				"current                 //info about the currently playing song",
				"current vote <1-10>     //Cast your vote to the song; 1=worst, 10=best",
				"current repeat          //repeats the currently playing song",
				"current update          //updates the now playing message every 10 seconds",
				"current updatetitle     //updates the topic of the music channel every 10 seconds",
				"current source          //Shows the source of the video",
				"current pm              //sends you a private message with the details",
				"",
				"current clear               //clears everything in the queue",
				"current clear admin         //check if clear is admin-only",
				"current clear admin toggle  //switch between admin-only and normal",
//				"current artist        //sets the artist of current song",
//				"current correct       //accept the systems suggestion of title/artist",
//				"current reversed      //accept the systems suggestion in reverse [title=artist,artist=title]",
//				"current title <title> //sets title of current song",
		};
	}

	@Override
	public String[] getAliases() {
		return new String[]{"playing", "np", "nowplaying"};
	}

	@Override
	public CommandVisibility getVisibility() {
		return CommandVisibility.PUBLIC;
	}

	@Override
	public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author) {
		Guild guild = ((TextChannel) channel).getGuild();
		SimpleRank userRank = bot.security.getSimpleRank(author, channel);
		if (!GuildSettings.get(guild).canUseMusicCommands(author, userRank)) {
			return Template.get(channel, "music_required_role_not_found", GuildSettings.getFor(channel, SettingMusicRole.class));
		}
		MusicPlayerHandler player = MusicPlayerHandler.getFor(guild, bot);
		OMusic song = CMusic.findById(player.getCurrentlyPlaying());
		if (song.id == 0 && (args.length == 0 || !args[0].equals("clear"))) {
			return Template.get("command_currentlyplaying_nosong");
		}

		if (args.length > 0) {
			String voteInput = args[0].toLowerCase();
			if (args.length > 1) {
				voteInput += " " + args[1];
			}
			Matcher m = votePattern.matcher(voteInput);
			if (m.find()) {
				OMusicVote voteRecord = CMusicVote.findBy(song.id, author.getId());
				if (m.group(1) != null) {
					int vote = Math.max(1, Math.min(10, Integer.parseInt(m.group(1))));
					CMusicVote.insertOrUpdate(song.id, author.getId(), vote);
					return "vote is registered (" + vote + ")";
				}
				if (voteRecord.vote > 0) {
					return Template.get("music_your_vote", song.youtubeTitle, voteRecord.vote);
				} else {
					return Template.get("music_not_voted", DisUtil.getCommandPrefix(channel) + "np vote ");
				}
			}
		}

		if (args.length >= 1) {
			switch (args[0].toLowerCase()) {
				case "repeat":
					boolean repeatMode = !player.isInRepeatMode();
					player.setRepeat(repeatMode);
					if (repeatMode) {
						return Template.get("music_repeat_mode");
					}
					return Template.get("music_repeat_mode_stopped");
				case "ban":
					if (userRank.isAtLeast(SimpleRank.CONTRIBUTOR) || CUser.findBy(author.getId()).hasPermission(OUser.PermissionNode.BAN_TRACKS)) {
						song.banned = 1;
						CMusic.update(song);
						player.forceSkip();
						return Template.get("command_current_banned_success");
					}
					return Template.get("no_permission");
				case "source":
					return Template.get(channel, "music_source_location", "<https://www.youtube.com/watch?v=" + song.youtubecode + ">");
				case "pm":
					bot.out.sendPrivateMessage(author,
							"The track I'm playing now is: " + song.youtubeTitle + Config.EOL +
									"You can find it here: https://www.youtube.com/watch?v=" + song.youtubecode
					);
					return Template.get(channel, "private_message_sent", guild.getMember(author).getEffectiveName());
				case "clear":
					boolean adminOnly = "true".equals(GuildSettings.getFor(channel, SettingMusicClearAdminOnly.class));
					if (userRank.isAtLeast(SimpleRank.GUILD_ADMIN) && args.length > 2 && args[1].equals("admin") && args[2].equalsIgnoreCase("toggle")) {
						GuildSettings.get(guild).set(SettingMusicClearAdminOnly.class, adminOnly ? "false" : "true");
						adminOnly = !adminOnly;
					} else if ((userRank.isAtLeast(SimpleRank.GUILD_ADMIN) || !adminOnly) && args.length == 1) {
						player.clearQueue();
						return Template.get("music_queue_cleared");
					}
					return Template.get("music_clear_mode", adminOnly ? "admin-only" : "normal");
			}
		}
		OPlaylist playlist = CPlaylist.findById(player.getActivePLaylistId());
		String ret = "";
		if (player.getRequiredVotes() > 1) {
			ret += player.getVoteCount() + "/" + player.getRequiredVotes() + Emojibet.NEXT_TRACK;
		}
		ret += "[`" + DisUtil.getCommandPrefix(channel) + "pl` " + playlist.title + "] " + "\uD83C\uDFB6 ";
		ret += song.youtubeTitle;
		final String autoUpdateText = ret;
		ret += Config.EOL + Config.EOL;
		MusicPlayerHandler musicHandler = MusicPlayerHandler.getFor(guild, bot);
		ret += MusicUtil.getMediaplayerProgressbar(musicHandler.getCurrentSongStartTime(), musicHandler.getCurrentSongLength(), musicHandler.getVolume(), musicHandler.isPaused()) + Config.EOL + Config.EOL;

		if (GuildSettings.get(guild).getOrDefault(SettingMusicShowListeners.class).equals("true")) {
			List<Member> userList = musicHandler.getUsersInVoiceChannel();
			if (userList.size() > 0) {
				ret += "\uD83C\uDFA7  Listeners" + Config.EOL;
				ArrayList<String> displayList = userList.stream().map(Member::getEffectiveName).collect(Collectors.toCollection(ArrayList::new));
				ret += Misc.makeTable(displayList);
			}
		}
		List<OMusic> queue = musicHandler.getQueue();
		if (queue.size() > 0) {
			ret += Config.EOL + "\uD83C\uDFB5 *Next up:* " + Config.EOL;
			for (int i = 0; i < Math.min(2, queue.size()); i++) {
				ret += "\uD83D\uDC49 " + queue.get(i).youtubeTitle + Config.EOL;
			}
			if (queue.size() > 2) {
				ret += Config.EOL + "... And **" + (queue.size() - 2) + "** more!";
			}

		}
		if (args.length == 1 && args[0].equals("update")) {
			channel.sendMessage(ret).queue(message -> {
				final Future<?>[] f = {null};
				bot.scheduleRepeat(
						() -> {
							if (player.getCurrentlyPlaying() != song.id) {
								f[0].cancel(false);
								return;
							}
							message.editMessage((player.isInRepeatMode() ? "\uD83D\uDD02 " : "") + autoUpdateText + Config.EOL +
									MusicUtil.getMediaplayerProgressbar(musicHandler.getCurrentSongStartTime(), musicHandler.getCurrentSongLength(), musicHandler.getVolume(), musicHandler.isPaused()) + Config.EOL + Config.EOL
							).queue(null, throwable -> f[0].cancel(false));
						}, 10_000L, 10_000L
				);
			});
			return "";
		} else if (args.length >= 1 && args[0].equals("updatetitle")) {
			if (!userRank.isAtLeast(SimpleRank.USER)) {
				return Template.get(channel, "command_no_permission");
			}
			if (player.isUpdateChannelTitle()) {
				player.setUpdateChannelTitle(false);
				return Template.get("music_channel_autotitle_stop");
			} else {
				TextChannel musicChannel = (TextChannel) channel;
				if (PermissionUtil.checkPermission(musicChannel, guild.getSelfMember(), Permission.MANAGE_CHANNEL)) {
					player.setUpdateChannelTitle(true);
					final Future<?>[] f = {null};
					bot.scheduleRepeat(() -> {
						if (!player.isUpdateChannelTitle() || !player.canTogglePause()) {
							player.setUpdateChannelTitle(false);
							musicChannel.getManager().setTopic("");
							f[0].cancel(false);
							return;
						}
						OMusic nowPlaying = CMusic.findById(player.getCurrentlyPlaying());
						musicChannel.getManager().setTopic(
								(player.isInRepeatMode() ? "\uD83D\uDD02 " : "") +
										MusicUtil.getMediaplayerProgressbar(musicHandler.getCurrentSongStartTime(), musicHandler.getCurrentSongLength(), musicHandler.getVolume(), musicHandler.isPaused()) +
										(nowPlaying.id > 0 ? "\uD83C\uDFB6 " + nowPlaying.youtubeTitle : "")
						).queue();
					}, 10_000L, 10_000L);
					return Template.get("music_channel_autotitle_start");
				}
				return Template.get("permission_missing_manage_channel");
			}
		}
		return (player.isInRepeatMode() ? "\uD83D\uDD01 " : "") + ret;
	}
}