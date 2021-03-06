package discordbot.service;

import discordbot.core.AbstractService;
import discordbot.guildsettings.defaults.SettingRoleTimeRanks;
import discordbot.handler.GuildSettings;
import discordbot.main.BotContainer;
import discordbot.main.DiscordBot;
import discordbot.role.RoleRankings;
import net.dv8tion.jda.core.entities.Guild;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * updates the ranking of members within a guild
 */
public class UserRankingSystemService extends AbstractService {

	public UserRankingSystemService(BotContainer b) {
		super(b);
	}

	@Override
	public String getIdentifier() {
		return "user_role_ranking";
	}

	@Override
	public long getDelayBetweenRuns() {
		return TimeUnit.MINUTES.toMillis(15);
	}

	@Override
	public boolean shouldIRun() {
		return true;
	}

	@Override
	public void beforeRun() {
	}

	@Override
	public void run() {
		for (DiscordBot discordBot : bot.getShards()) {
			List<Guild> guilds = discordBot.client.getGuilds();
			for (Guild guild : guilds) {
				GuildSettings settings = GuildSettings.get(guild);
				if (settings != null && "true".equals(settings.getOrDefault(SettingRoleTimeRanks.class)) && RoleRankings.canModifyRoles(guild, discordBot.client.getSelfUser())) {
					handleGuild(discordBot, guild);
				}
			}
		}
	}

	private void handleGuild(DiscordBot bot, Guild guild) {
		RoleRankings.fixForServer(guild);
		guild.getMembers().stream().filter(user -> !user.getUser().isBot()).forEach(user -> RoleRankings.assignUserRole(bot, guild, user.getUser()));
	}

	@Override
	public void afterRun() {
		System.gc();
	}
}