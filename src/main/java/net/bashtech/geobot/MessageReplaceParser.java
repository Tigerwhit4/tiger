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
import java.util.ArrayList;


public class MessageReplaceParser {
	private static ArrayList<String>quotesList = new ArrayList<String>();

    public static String parseMessage(String channel, String sender, String message, String[] args) {
        Channel ci = BotManager.getInstance().getChannel(channel);

        if (sender != null && message.contains("(_USER_)"))
            message = message.replace("(_USER_)", sender);
        if (message.contains("(_GAME_)"))
            message = message.replace("(_GAME_)", JSONUtil.krakenGame(channel.substring(1)));
        if (message.contains("(_STATUS_)"))
            message = message.replace("(_STATUS_)", JSONUtil.krakenStatus(channel.substring(1)));
        if (message.contains("(_JTV_STATUS_)"))
            message = message.replace("(_JTV_STATUS_)", JSONUtil.jtvStatus(channel.substring(1)));
        if (message.contains("(_VIEWERS_)"))
            message = message.replace("(_VIEWERS_)", "" + JSONUtil.krakenViewers(channel.substring(1)));
        if (message.contains("(_JTV_VIEWERS_)"))
            message = message.replace("(_JTV_VIEWERS_)", "" + JSONUtil.jtvViewers(channel.substring(1)));
//        if (message.contains("(_CHATTERS_)"))
//            message = message.replace("(_CHATTERS_)", "" + ReceiverBot.getInstance().getUsers(channel).length);
        if (message.contains("(_SONG_)"))
            message = message.replace("(_SONG_)", JSONUtil.lastFM(ci.getLastfm()));
        if (message.contains("(_SONG_)"))
            message = message.replace("(_SONG_)", JSONUtil.lastFM(ci.getLastfm()));
        if (message.contains("(_STEAM_PROFILE_)"))
            message = message.replace("(_STEAM_PROFILE_)", JSONUtil.steam(ci.getSteam(), "profile"));
        if (message.contains("(_STEAM_GAME_)"))
            message = message.replace("(_STEAM_GAME_)", JSONUtil.steam(ci.getSteam(), "game"));
        if (message.contains("(_STEAM_SERVER_)"))
            message = message.replace("(_STEAM_SERVER_)", JSONUtil.steam(ci.getSteam(), "server"));
        if (message.contains("(_STEAM_STORE_)"))
            message = message.replace("(_STEAM_STORE_)", JSONUtil.steam(ci.getSteam(), "store"));
        if (message.contains("(_BOT_HELP_)"))
            message = message.replace("(_BOT_HELP_)", BotManager.getInstance().bothelpMessage);
        if (message.contains("(_CHANNEL_URL_)"))
            message = message.replace("(_CHANNEL_URL_)", "twitch.tv/" + channel.substring(1));
        if (message.contains("(_TWEET_URL_)")) {
            String url = JSONUtil.shortenURL("https://twitter.com/intent/tweet?text=" + JSONUtil.urlEncode(MessageReplaceParser.parseMessage(channel, sender, ci.getClickToTweetFormat(), args)));
            message = message.replace("(_TWEET_URL_)", url);
        }
        if (message.contains("(_COMMERCIAL_)")){
        	ci.runCommercial();
        	message = "Running a commercial, thank you for supporting this channel";
        }
        
        if (message.contains("(_QUOTE_)")){
		try {
			read("quotesList" + channel+".txt");
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}			
			
				int randQuotes = (int) (Math.random()* quotesList.size());
				
				if (randQuotes >-1)
					message = message.replace("(_QUOTE_)", quotesList.get(randQuotes));
				else
					message = message.replace("(_QUOTE_)", "Error, whoops");
			
			
        }

        if (args != null) {
            int argCounter = 1;
            for (String argument : args) {
                if (message.contains("(_" + argCounter + "_)"))
                    message = message.replace("(_" + argCounter + "_)", argument);
                argCounter++;
            }
        }

        return message;
    }
    @SuppressWarnings("unchecked")
	public static void read(String fileName) throws Exception {
		FileInputStream fin= new FileInputStream (fileName);
		ObjectInputStream ois = new ObjectInputStream(fin);
		quotesList = (ArrayList<String>)ois.readObject();
		fin.close();
		}
}
