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

import java.net.URLEncoder;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

public class MessageReplaceParser {

	public static String parseMessage(String channel, String sender,
			String message, String[] args) {
		Channel ci = BotManager.getInstance().getChannel(channel);
		ReceiverBot rb = ReceiverBot.getInstance();

		if (message.contains("(_ONLINE_CHECK_)")) {
			if (!JSONUtil.krakenIsLive(channel.substring(1))) {
				message = "";
			} else {
				message = message.replace("(_ONLINE_CHECK_)", "");
			}
		}
		if(rb!=null&&message.contains("(_SUBMODE_ON_)")){
			rb.sendCommand(channel, ".subscribers");
			message = message.replace("(_SUBMODE_ON_)", "");
		}else if (rb!=null&&message.contains("(_SUBMODE_OFF_)")){
				rb.sendCommand(channel, ".subscribersoff");
				message = message.replace("(_SUBMODE_OFF_)", "");
		}
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
				} else if (JSONUtil.krakenGame(channel.substring(1))
						.equalsIgnoreCase("(Not set)")) {
					message = message.replace("(_STEAM_STORE_)", "");
				} else {
					message = message.replace("(_STEAM_STORE_)", JSONUtil
							.shortenUrlTinyURL("https://www.google.com/#q="
									+ URLEncoder.encode("buy " + game)));
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
					.shortenUrlTinyURL("https://twitter.com/intent/tweet?text="
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
			if (!amount.equals("")) {
				message = message.replace("(_EXTRALIFE_AMOUNT_)", "$" + amount);
			} else
				message = message.replace("(_EXTRALIFE_AMOUNT_)",
						"(Error getting amount)");

			System.out.println(message);
		}
		if (message.contains("(_LAST_SONG_)")) {
			String songName = JSONUtil.lastSongLastFM(ci.getLastfm());
			message = message.replace("(_LAST_SONG_)", songName);
		}
		if (message.contains("(_UPDATE_SITE_)")) {
			message = message.replace("(_UPDATE_SITE_)", "");
			ci.updateSite();
		}
		if(message.contains("(_GAME_IS_NOT_")&&message.contains("_)")){
			int start = message.indexOf("(_GAME_IS_NOT_")+10;
			int end = message.indexOf("_)",start+1);
			String wantedGame = message.substring(start,end);
			System.out.println(wantedGame);
			System.out.println(JSONUtil.krakenGame(channel.substring(1)).replace(" ", "-"));
			
			if(JSONUtil.krakenGame(channel.substring(1)).replace(" ","-").equalsIgnoreCase(wantedGame)){
				message = "";
				
			}else{
				message = message.replace("(_GAME_IS_NOT_"+wantedGame+"_)", "");
			}
		}
		if(message.contains("(_GAME_IS_")&&message.contains("_)")){
			int start = message.indexOf("(_GAME_IS_")+10;
			int end = message.indexOf("_)",start+1);
			String wantedGame = message.substring(start,end);
			System.out.println(wantedGame);
			System.out.println(JSONUtil.krakenGame(channel.substring(1)).replace(" ", "-"));
			
			if(JSONUtil.krakenGame(channel.substring(1)).replace(" ","-").equalsIgnoreCase(wantedGame)){
				message = message.replace("(_GAME_IS_"+wantedGame+"_)", "");
			}else{
				message = "";
			}
		}
		

		if (message.contains("(_DATE_") && message.contains("_)")) {
			message = handleDatetime(message, "(_DATE_", "_)", "MMM d, yyyy");
		}

		if (message.contains("(_TIME_") && message.contains("_)")) {
			message = handleDatetime(message, "(_TIME_", "_)", "h:mm a");
		}

		if (message.contains("(_TIME24_") && message.contains("_)")) {
			message = handleDatetime(message, "(_TIME24_", "_)", "k:mm");
		}

		if (message.contains("(_DATETIME_") && message.contains("_)")) {
			message = handleDatetime(message, "(_DATETIME_", "_)",
					"MMM d, yyyy h:mm a");
		}

		if (message.contains("(_DATETIME24_") && message.contains("_)")) {
			message = handleDatetime(message, "(_DATETIME24_", "_)",
					"MMM d, yyyy k:mm");
		}

		if (message.contains("(_UNTIL_") && message.contains("_)")) {
			PeriodFormatter formatDHM = new PeriodFormatterBuilder()
					.printZeroNever().appendDays()
					.appendSuffix(" day", " days").appendSeparator(", ")
					.printZeroNever().appendHours().appendSuffix(" hr", " hrs")
					.appendSeparator(", ").printZeroRarelyLast()
					.appendMinutes().appendSuffix(" min", " mins")
					.toFormatter();
			message = handleUntil(message, "(_UNTIL_", "_)", formatDHM);
		}

		if (message.contains("(_UNTILSHORT_") && message.contains("_)")) {
			PeriodFormatter formatShortDHM = new PeriodFormatterBuilder()
					.printZeroNever().appendDays().appendSuffix("d")
					.printZeroNever().appendHours().appendSuffix("h")
					.printZeroRarelyLast().appendMinutes().appendSuffix("m")
					.toFormatter();
			message = handleUntil(message, "(_UNTILSHORT_", "_)",
					formatShortDHM);
		}

		if (message.contains("(_UNTILLONG_") && message.contains("_)")) {
			PeriodFormatter formatLongDHM = new PeriodFormatterBuilder()
					.printZeroNever().appendDays()
					.appendSuffix(" day", " days")
					.appendSeparator(", ", " and ").printZeroNever()
					.appendHours().appendSuffix(" hour", " hours")
					.appendSeparator(", ", " and ").printZeroRarelyLast()
					.appendMinutes().appendSuffix(" minute", " minutes")
					.toFormatter();
			message = handleUntil(message, "(_UNTILLONG_", "_)", formatLongDHM);
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
		if(message.contains("(_LIST_")&&message.contains("_RANDOM_)")){
			int listStart = message.indexOf("(_LIST_") + 7;
			int listEnd = message.indexOf("_", listStart);
			String listName = message.substring(listStart,listEnd).toLowerCase();
			if(ci.checkList(listName)){
				int size = ci.getListSize(listName);
				System.out.println("size " + size);
				int randReturn = (int) Math
						.round((Math.random() * (size - 1)) + 1);
				System.out.println("randReturn " + randReturn);
				String replacer = message.substring(listStart-7,message.indexOf("_RANDOM_)",listStart)+9);
				message=message.replace(replacer,ci.getListItem(listName,
						randReturn - 1));
			}
		}
		if (message.contains("(_VARS_")) {

			int begName = message.indexOf("(_VARS_") + 7;
			int endName = message.indexOf("_", begName);
			String varName = message.substring(begName, endName);
			System.out.println("varName = " + varName);
			int endMethod = message.indexOf("_", endName + 1);
			String method = message.substring(endName + 1, endMethod);
			System.out.println("method = " + method);

			if (method.equals("INCREMENT")) {
				int endInc = message.indexOf("_)", endMethod);
				System.out.println("index endInc =" + endInc);
				String inc = message.substring(endMethod + 1, endInc);
				System.out.println("inc = " + inc);
				int incValue = Integer.valueOf(inc);

				String response = JSONUtil.incVar(channel.substring(1),
						varName, incValue);
				if (response != null) {
					message = message.replace("(_VARS_" + varName
							+ "_INCREMENT_" + inc + "_)", response);
				} else {
					message = message.replace("(_VARS_" + varName
							+ "_INCREMENT_" + inc + "_)", "(error)");
				}
			}

			else if (method.equals("DECREMENT")) {
				int endDec = message.indexOf("_)", endMethod);
				String dec = message.substring(endMethod + 1, endDec);
				System.out.println("dec = " + dec);
				int decValue = Integer.valueOf(dec);
				String response = JSONUtil.decVar(channel.substring(1),
						varName, decValue);
				if (response != null) {
					message = message.replace("(_VARS_" + varName
							+ "_DECREMENT_" + dec + "_)", response);
				} else {
					message = message.replace("(_VARS_" + varName
							+ "_DECREMENT_" + dec + "_)", "(error)");
				}
			}

			else if (method.equals("GET")) {
				int endChannel = message.indexOf("_)", endMethod);

				String otherChannel = message.substring(endMethod + 1,
						endChannel);
				System.out.println(otherChannel);
				String response = JSONUtil.getVar(otherChannel, varName);
				if (response != null) {
					message = message.replace("(_VARS_" + varName + "_GET_"
							+ otherChannel + "_)", response);
				} else {
					message = message.replace("(_VARS_" + varName + "_GET_"
							+ otherChannel + "_)", "(error)");
				}

			} else if (method.equals("SET")) {
				int endNewVal = message.indexOf("_)", endMethod);
				String newVal = message.substring(endMethod + 1, endNewVal);
				System.out.println("New Value = " + newVal);
				boolean response = JSONUtil.setVar(channel.substring(1),
						varName, newVal);
				if (response) {
					message = message.replace("(_VARS_" + varName + "_SET_"
							+ newVal + "_)", newVal);
				} else {
					message = message.replace("(_VARS_" + varName + "_SET_"
							+ newVal + "_)", "(error)");
				}
			}

		}
		if (message.contains("(_SILENT_)")) {

			message = "";

		}

		return message;
	}

	public static String handleDatetime(String message, String prefix,
			String suffix, String format) {

		int commandStart = message.indexOf(prefix);
		int commandEnd = message.indexOf(suffix);

		String replaced = message.substring(commandStart,
				commandEnd + suffix.length());

		DateTimeZone tz;
		if (commandStart + prefix.length() < commandEnd) {
			String tzid = message.substring(commandStart + prefix.length(),
					commandEnd);
			try {
				tz = DateTimeZone.forID(tzid);
			} catch (IllegalArgumentException e) {
				tz = DateTimeZone.UTC;
			}
		} else {
			tz = DateTimeZone.UTC;
		}

		DateTimeFormatter fmt = DateTimeFormat.forPattern(format);
		fmt = fmt.withZone(tz);
		String dateStr = fmt.print(new DateTime());
		message = message.replace(replaced, dateStr);

		return message;
	}

	public static String handleUntil(String message, String prefix,
			String suffix, PeriodFormatter formatter) {

		int commandStart = message.indexOf(prefix);
		int commandEnd = message.indexOf(suffix);

		if (commandStart + prefix.length() < commandEnd) {

			String replaced = message.substring(commandStart, commandEnd
					+ suffix.length());
			String dateStr = message.substring(commandStart + prefix.length(),
					commandEnd);

			DateTimeFormatter fmt = DateTimeFormat
					.forPattern("yyyy-MM-dd'T'HH:mm");
			String until;
			try {
				DateTime future = fmt.parseDateTime(dateStr);

				PeriodType pType = PeriodType.dayTime().withMillisRemoved()
						.withSecondsRemoved();

				Period period = new Period(new DateTime(), future, pType);

				until = period.toString(formatter);

			} catch (IllegalArgumentException e) {
				until = "Unknown date";
				System.out.println(dateStr);
				e.printStackTrace();
			}

			message = message.replace(replaced, until);
		}
		return message;
	}
}
