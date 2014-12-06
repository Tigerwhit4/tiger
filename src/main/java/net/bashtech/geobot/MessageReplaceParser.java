/*
 * Copyright 2012 Andrew Bashore
 * This file is part of GeoBot.
 *
 * GeoBot is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * GeoBot is distributed in the hope that it will be useful
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GeoBot.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.bashtech.geobot;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.net.URLEncoder;
import java.util.ArrayList;

public class MessageReplaceParser {

	public static String parseMessage(String channel, String sender,
			String message, String[] args) {
		Channel ci = BotManager.getInstance().getChannel(channel);

		if (sender != null && message.contains("(_USER_)"))
			message = message.replace("(_USER_)", sender);
		if (message.contains("(_GAME_)"))
			message = message.replace("(_GAME_)",
					JSONUtil.krakenGame(channel.substring(1)));
		if (message.contains("(_STATUS_)"))
			message = message.replace("(_STATUS_)",
					JSONUtil.krakenStatus(channel.substring(1)));
		// if (message.contains("(_JTV_STATUS_)"))
		// message = message.replace("(_JTV_STATUS_)",
		// JSONUtil.jtvStatus(channel.substring(1)));
		if (message.contains("(_VIEWERS_)"))
			message = message.replace("(_VIEWERS_)",
					"" + JSONUtil.krakenViewers(channel.substring(1)));
		// if (message.contains("(_JTV_VIEWERS_)"))
		// message = message.replace("(_JTV_VIEWERS_)",
		// "" + JSONUtil.jtvViewers(channel.substring(1)));
		// if (message.contains("(_CHATTERS_)"))
		// message = message.replace("(_CHATTERS_)", "" +
		// ReceiverBot.getInstance().getUsers(channel).length);
		if (message.contains("(_SONG_)"))
			message = message.replace("(_SONG_)",
					JSONUtil.lastFM(ci.getLastfm()));

		if (message.contains("(_STEAM_PROFILE_)"))
			message = message.replace("(_STEAM_PROFILE_)",
					JSONUtil.steam(ci.getSteam(), "profile"));
		if (message.contains("(_STEAM_GAME_)")) {
			String game = JSONUtil.steam(ci.getSteam(), "game");
			if (game.equalsIgnoreCase("(unavailable)")) {
				game = JSONUtil.krakenGame(channel.substring(1));

			}
			message = message.replace("(_STEAM_GAME_)", game);

		}
		if (message.contains("(_STEAM_SERVER_)"))
			message = message.replace("(_STEAM_SERVER_)",
					JSONUtil.steam(ci.getSteam(), "server"));
		if (message.contains("(_STEAM_STORE_)")) {
			
			String game = JSONUtil.steam(ci.getSteam(), "game");
			if (game.equalsIgnoreCase("(unavailable)")) {
				game = JSONUtil.krakenGame(channel.substring(1));

			}
			String storeLink = JSONUtil.steam(ci.getSteam(), "store");
			if (storeLink.equalsIgnoreCase("(unavailable)")) {
				if (JSONUtil.krakenGame(channel.substring(1)).equalsIgnoreCase(
						"minecraft")) {
					message = message.replace("(_STEAM_STORE_)",
							"minecraft.net");
				}else if(JSONUtil.krakenGame(channel.substring(1)).equalsIgnoreCase("(Not set)")){
					message = message.replace("(_STEAM_STORE_)","");
				}else{
					message = message.replace("(_STEAM_STORE_)", JSONUtil.googURL(
							"https://www.google.com/#q="+URLEncoder.encode("buy "+game)));
				}
			} else
				message = message.replace("(_STEAM_STORE_)", storeLink);
		}
		if (message.contains("(_BOT_HELP_)"))
			message = message.replace("(_BOT_HELP_)",
					BotManager.getInstance().bothelpMessage);
		if (message.contains("(_CHANNEL_URL_)"))
			message = message.replace("(_CHANNEL_URL_)",
					"twitch.tv/" + channel.substring(1));
		if (message.contains("(_TWEET_URL_)")) {
			String url = JSONUtil
					.googURL("https://twitter.com/intent/tweet?text="
							+ JSONUtil.urlEncode(MessageReplaceParser
									.parseMessage(channel, sender,
											ci.getClickToTweetFormat(), args)));
			message = message.replace("(_TWEET_URL_)", url);
		}
		if (message.contains("(_COMMERCIAL_)")) {
			if (JSONUtil.krakenIsLive(channel.substring(1)))
				ci.scheduleCommercial();

			message = message.replace("(_COMMERCIAL_)", "");

		}
		if (message.contains("(_ONLINE_CHECK_)")) {
			if (!JSONUtil.krakenIsLive(channel.substring(1))) {
				message = "";
			} else {
				message = message.replace("(_ONLINE_CHECK_)", "");
			}
		}
		if (message.contains("(_SONG_URL_)")) {
			message = message.replace("(_SONG_URL_)",
					JSONUtil.lastFMURL(ci.getLastfm()));
		}
		if (message.contains("(_QUOTE_)")) {
			int randQuotes = (int) (Math.random() * ci.getQuoteSize());
			String quote = ci.getQuote(randQuotes);
			message = message.replace("(_QUOTE_)", quote);

		}
		if (message.contains("(_NUMCHANNELS_)")) {
			message = message.replace("(_NUMCHANNELS_)",
					BotManager.getInstance().channelList.size() + "");
		}
		if (message.contains("(_XBOX_GAME_)")) {
			String gamerTag = ci.getGamerTag();
			String lastGame = JSONUtil.xboxLastGame(gamerTag);
			message = message.replace("(_XBOX_GAME_)", lastGame);
		}
		if (message.contains("(_XBOX_GAMERSCORE_)")) {
			String gamerTag = ci.getGamerTag();
			String gamerScore = JSONUtil.xboxGamerScore(gamerTag);
			message = message.replace("(_XBOX_GAMERSCORE_)", gamerScore);
		}
		if (message.contains("(_XBOX_PROGRESS_)")) {
			String gamerTag = ci.getGamerTag();
			String progress = JSONUtil.xboxLastGameProgress(gamerTag);
			message = message.replace("(_XBOX_PROGRESS_)", progress);
		}

		if (message.contains("(_EXTRALIFE_AMOUNT_)")) {
			String amount = JSONUtil.extraLifeAmount(channel);
			message = message.replace("(_EXTRALIFE_AMOUNT_)", "$"
					+ amount);
			System.out.println(message);
		}
		if (message.contains("(_LAST_SONG_)")) {
			String songName = JSONUtil.lastSongLastFM(ci.getLastfm());
			message = message.replace("(_LAST_SONG_)", songName);
		}

		if (args != null) {
			int argCounter = 1;
			for (String argument : args) {
				if (message.contains("(_" + argCounter + "_)"))
					message = message.replace("(_" + argCounter + "_)",
							argument);
				argCounter++;
			}
		}
		if (message.contains("(_") && message.contains("_COUNT_)")) {
			int commandStart = message.indexOf("(_");
			int commandEnd = message.indexOf("_COUNT_)");
			String commandName = message
					.substring(commandStart + 2, commandEnd).toLowerCase();
			String value = ci.getCommand(commandName);
			String replaced = message.substring(commandStart, commandEnd + 8);
			if (value != null) {

				int count = ci.getCurrentCount(commandName);
				if (count > -1) {
					message = message.replace(replaced, count + "");
				} else {
					message = message.replace(replaced,
							"No count for that command...");
				}

			} else {
				message = message.replace(replaced,
						"No count for that command...");
			}
		}

		return message;
	}

}
