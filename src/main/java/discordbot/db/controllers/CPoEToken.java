package discordbot.db.controllers;

import discordbot.core.Logger;
import discordbot.db.WebDb;
import discordbot.db.model.OPoEToken;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * data communication with the controllers `poe_token`
 */
public class CPoEToken {
	public static OPoEToken findBy(String discordId) {
		OPoEToken token = new OPoEToken();
		int userId = CUser.getCachedId(discordId);
		token.userId = userId;
		try (ResultSet rs = WebDb.get().select(
				"SELECT *  " +
						"FROM poe_token " +
						"WHERE user_id = ? ", userId)) {
			if (rs.next()) {
				token = fillRecord(rs);
			}
			rs.getStatement().close();
		} catch (Exception e) {
			Logger.fatal(e);
		}
		return token;
	}

	private static OPoEToken fillRecord(ResultSet resultset) throws SQLException {
		OPoEToken token = new OPoEToken();
		token.userId = resultset.getInt("user_id");
		token.session_id = resultset.getString("session_id");
		return token;
	}

	public static void insertOrUpdate(OPoEToken token) {
		try {
			WebDb.get().insert(
					"INSERT INTO poe_token(user_id, session_id) " +
							"VALUES (?,?) ON DUPLICATE KEY UPDATE session_id = ?",
					token.userId, token.session_id, token.session_id);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
