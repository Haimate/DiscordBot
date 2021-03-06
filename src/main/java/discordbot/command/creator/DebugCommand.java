package discordbot.command.creator;

import discordbot.core.AbstractCommand;
import discordbot.handler.Template;
import discordbot.main.Config;
import discordbot.main.DiscordBot;
import discordbot.permission.SimpleRank;
import discordbot.util.Emojibet;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

/**
 */
public class DebugCommand extends AbstractCommand {
	public DebugCommand() {
		super();
	}

	@Override
	public String getDescription() {
		return "some debugging tools";
	}

	@Override
	public String getCommand() {
		return "debug";
	}

	@Override
	public boolean isListed() {
		return true;
	}

	@Override
	public String[] getUsage() {
		return new String[]{};
	}

	@Override
	public String[] getAliases() {
		return new String[]{};
	}

	@Override
	public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author) {
		if (!bot.security.getSimpleRank(author).isAtLeast(SimpleRank.BOT_ADMIN)) {
			return Template.get(channel, "command_no_permission");
		}
		if (args.length == 0) {
			return Emojibet.EYES;
		}
		boolean value = false;
		boolean updating = args.length > 1;
		if (updating) {
			value = args[1].equalsIgnoreCase("true") || args[1].equalsIgnoreCase("t") || args[1].equalsIgnoreCase("y");
		}
		switch (args[0].toLowerCase()) {
			case "yt":
			case "youtube":
				if (updating) {
					Config.YOUTUBEDL_DEBUG_PROCESS = value;
				}
				value = Config.YOUTUBEDL_DEBUG_PROCESS;
				break;
			default:
				return Emojibet.SHRUG;
		}
		if (updating) {
			return Emojibet.OKE_SIGN + " " + args[0] + " is set to " + value;
		}
		return Emojibet.UNLOCKED + " " + args[0] + " = `" + value + "`";
	}
}