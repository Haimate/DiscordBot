package novaz.handler;

import novaz.core.Logger;
import novaz.main.Config;
import novaz.main.NovaBot;
import novaz.util.Misc;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IPrivateChannel;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.*;

public class OutgoingContentHandler {
	private final NovaBot bot;

	public OutgoingContentHandler(NovaBot b) {
		bot = b;
	}

	/**
	 * @param channel channel to send to
	 * @param content the message
	 * @return IMessage or null
	 */
	public IMessage sendMessage(IChannel channel, String content) {
		RequestBuffer.RequestFuture<IMessage> request = bot.out.sendMessage(channel, new MessageBuilder(bot.instance).withChannel(channel).withContent(content));
		return request.get();
	}

	public void sendErrorToMe(Exception error, Object... extradetails) {
		String errorMessage = "I'm sorry to inform you that I've encountered a **" + error.getClass().getName() + "**" + Config.EOL;
		errorMessage += "Message: " + Config.EOL;
		errorMessage += error.getLocalizedMessage() + Config.EOL;
		String stack = "";
		int maxTrace = 6;
		StackTraceElement[] stackTrace1 = error.getStackTrace();
		for (int i = 0; i < stackTrace1.length; i++) {
			StackTraceElement stackTrace = stackTrace1[i];
			stack += stackTrace.toString() + Config.EOL;
			if (i > maxTrace) {
				break;
			}
		}
		errorMessage += "Accompanied stacktrace: " + Config.EOL + Misc.makeTable(stack) + Config.EOL;
		if (extradetails.length > 0) {
			errorMessage += "Extra information: " + Config.EOL;
			for (int i = 1; i < extradetails.length; i += 2) {
				if (extradetails[i] != null) {
					errorMessage += extradetails[i - 1] + " = " + extradetails[i] + Config.EOL;
				} else if (extradetails[i - 1] != null) {
					errorMessage += extradetails[i - 1];
				}
			}
		}
		sendPrivateMessage(bot.instance.getUserByID(Config.CREATOR_ID), errorMessage);
	}

	public void sendPrivateMessage(IUser target, String message) {
		RequestBuffer.request(() -> {
			try {
				IPrivateChannel pmChannel = bot.instance.getOrCreatePMChannel(target);
				return pmChannel.sendMessage(message);
			} catch (DiscordException e) {
				if (e.getErrorMessage().contains("502")) {
					throw new RateLimitException("Workaround because of 502", 1500, "editMessage", false);
				}
			} catch (MissingPermissionsException e) {
				Logger.fatal(e, "no permission");
				e.printStackTrace();
			}
			return null;
		});
	}

	public RequestBuffer.RequestFuture<IMessage> editMessage(IMessage msg, String newText) {
		return RequestBuffer.request(() -> {
			try {
				return msg.edit(newText);
			} catch (DiscordException e) {
				if (e.getErrorMessage().contains("502")) {
					throw new RateLimitException("Workaround because of 502", 1500, "editMessage", false);
				}
			} catch (MissingPermissionsException e) {
				Logger.fatal(e, "no permission");
				e.printStackTrace();
			}
			return null;
		});
	}

	public void deleteMessage(IMessage message) {
		RequestBuffer.request(() -> {
			try {
				message.delete();
			} catch (MissingPermissionsException | DiscordException e) {
				e.printStackTrace();
			}
			return null;
		});
	}

	public RequestBuffer.RequestFuture<IMessage> sendMessage(IChannel channel, MessageBuilder builder) {
		return RequestBuffer.request(() -> {
			try {
				return builder.send();
			} catch (DiscordException e) {
				if (e.getErrorMessage().contains("502")) {
					throw new RateLimitException("Workaround because of 502", 1000, "sendMessage", false);
				}
			} catch (MissingPermissionsException e) {
				Logger.fatal(e, "no permission");
				e.printStackTrace();
			}
			return null;
		});
	}
}
