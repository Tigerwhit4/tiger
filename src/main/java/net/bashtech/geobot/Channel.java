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

import org.java_websocket.WebSocket;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Channel {
	public JSONObject config;

	private String channel;
	private String twitchname;

	boolean staticChannel;
	private HashMap<String, String> commands = new HashMap<String, String>();
	private HashMap<String, Integer> commandsRestrictions = new HashMap<String, Integer>();
	private ArrayList<String> quotes = new ArrayList<String>();
	HashMap<String, RepeatCommand> commandsRepeat = new HashMap<String, RepeatCommand>();
	HashMap<String, ScheduledCommand> commandsSchedule = new HashMap<String, ScheduledCommand>();
	List<Pattern> autoReplyTrigger = new ArrayList<Pattern>();
	List<String> autoReplyResponse = new ArrayList<String>();
	private boolean filterCaps;
	private int filterCapsPercent;
	private int filterCapsMinCharacters;
	private int filterCapsMinCapitals;
	private boolean filterLinks;
	private boolean filterOffensive;
	private boolean filterEmotes;
	private boolean filterSymbols;
	private int filterSymbolsPercent;
	private int filterSymbolsMin;
	private int filterEmotesMax;
	private boolean filterEmotesSingle;
	private int filterMaxLength;
	private String topic;
	private int topicTime;
	private Set<String> regulars = new HashSet<String>();
	private Set<String> subscribers = new HashSet<String>();
	private Set<String> moderators = new HashSet<String>();
	Set<String> tagModerators = new HashSet<String>();
	private Set<String> owners = new HashSet<String>();
	private Set<String> raidWhitelist = new HashSet<String>();
	private Set<String> permittedUsers = new HashSet<String>();
	private ArrayList<String> permittedDomains = new ArrayList<String>();
	public boolean useTopic = true;
	public boolean useFilters = true;
	private Poll currentPoll;
	private Giveaway currentGiveaway;
	private boolean enableThrow;
	private boolean signKicks;
	private boolean announceJoinParts;
	private String lastfm;
	private String steamID;
	private int mode; // 0: Admin/owner only; 1: Mod Only; 2: Everyone; -1
						// Special mode to admins to use for channel moderation
	private int bulletInt;
	Raffle raffle;
	public boolean logChat;
	public long messageCount;
	public int commercialLength;
	String clickToTweetFormat;
	private boolean filterColors;
	private boolean filterMe;
	private Set<String> offensiveWords = new HashSet<String>();
	private List<Pattern> offensiveWordsRegex = new LinkedList<Pattern>();
	Map<String, EnumMap<FilterType, Integer>> warningCount;
	Map<String, Long> warningTime;
	private int timeoutDuration;
	private boolean enableWarnings;
	Map<String, Long> commandCooldown;
	Set<WebSocket> wsSubscribers = new HashSet<WebSocket>();
	String prefix;
	String emoteSet;
	boolean subscriberRegulars;
	String lastSong = "";
	long songUpdated = System.currentTimeMillis();
	private boolean wpOn;
	private long sinceWp = System.currentTimeMillis();
	private int wpCount = 0;
	private String bullet = "#!";
	private String gamerTag;

	private Map<String, Object> defaults = new HashMap<String, Object>();

	private int cooldown = 0;

	private int maxViewers = 0;
	private boolean streamUp = false;
	private int streamMax = 0;
	private int streamNumber = 0;
	private int runningMaxViewers = 0;

	private int punishCount = 0;
	private int updateDelay = 120;

	private long sincePunish = System.currentTimeMillis();
	private String maxviewerDate = new java.util.Date().toString();

	public boolean subsRegsMinusLinks;

	public boolean active;
	private static Timer commercial;
	private int lastStrawpoll;
	PropertiesFile oldconfig;
	public Channel(String name) {
		channel = name;
		 oldconfig = new PropertiesFile(channel + ".properties");
		config = new JSONObject();
		loadProperties(name);
		warningCount = new HashMap<String, EnumMap<FilterType, Integer>>();
		warningTime = new HashMap<String, Long>();
		commandCooldown = new HashMap<String, Long>();

		twitchname = channel.substring(1);

	}

	public Channel(String name, int mode) {
		this(name);
		setMode(mode);
	}

	public String getChannel() {
		return channel;
	}

	public String getTwitchName() {
		return twitchname;
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix.charAt(0) + "";

		config.put("commandPrefix", this.prefix);
		saveConfig();
	}

	public void setLastStrawpoll(int newId) {
		lastStrawpoll = newId;
	}

	public int getLastStrawpoll() {
		return lastStrawpoll;
	}

	public boolean getWp() {
		return wpOn;
	}

	public void setStreamCount(int newCount) {
		streamNumber = newCount;
		config.put("streamCount", streamNumber);
		saveConfig();
	}

	public void setWp(boolean state) {
		wpOn = state;
		config.put("wpTimer", wpOn);
		saveConfig();
	}

	public long timeSinceSaid() {
		long now = System.currentTimeMillis();
		long differenceInSeconds = (now - sinceWp) / 1000L;
		sinceWp = now;
		config.put("sinceWp", sinceWp);
		saveConfig();
		return (differenceInSeconds);
	}

	public long timeSinceNoUpdate() {
		long now = System.currentTimeMillis();
		long differenceInSeconds = (now - sinceWp) / 1000L;

		return (differenceInSeconds);
	}

	public long timeSincePunished() {
		long now = System.currentTimeMillis();
		long differenceInSeconds = (now - sincePunish) / 1000L;

		return (differenceInSeconds);
	}

	public void setBullet(String newBullet) {
		bullet = newBullet;
		config.put("bullet", newBullet);
		saveConfig();
	}

	public String getChannelBullet() {
		return bullet;
	}

	public void increaseWpCount() {
		wpCount++;
		config.put("wpCount", wpCount);
		saveConfig();
	}

	public int getWpCount() {
		return wpCount;
	}

	public String getEmoteSet() {
		return emoteSet;
	}

	public void setEmoteSet(String emoteSet) {
		this.emoteSet = emoteSet;

		config.put("emoteSet", emoteSet);
		saveConfig();
	}

	public boolean getSubsRegsMinusLinks() {
		return subsRegsMinusLinks;
	}

	public void setSubsRegsMinusLinks(boolean on) {

		subscribers.clear();

		subsRegsMinusLinks = on;
		config.put("subsRegsMinusLinks", subsRegsMinusLinks);
		saveConfig();

	}

	public boolean getSubscriberRegulars() {
		return subscriberRegulars;
	}

	public void setSubscriberRegulars(boolean subscriberRegulars) {

		subscribers.clear();

		this.subscriberRegulars = subscriberRegulars;
		config.put("subscriberRegulars", subscriberRegulars);
		saveConfig();
	}

	// ##############################################################
	public int addQuote(String quote) {

		if (quotes.contains(quote)) {
			return -1;
		} else {
			quotes.add(quote);
			JSONArray quotesArray = new JSONArray();
			String quotesString = "";
			for (int i = 0; i < quotes.size(); i++) {
				quotesArray.add(quotes.get(i));
			}
			config.put("quotes", quotesArray);
			saveConfig();
			return quotes.indexOf(quote);
		}
	}

	public int getQuoteSize() {
		return quotes.size();
	}

	public String getQuote(int index) {
		if (index < quotes.size())
			return quotes.get(index);
		else
			return "No quote at requested index.";
	}

	public boolean deleteQuote(int index) {
		if (index > quotes.size() - 1)
			return false;
		else {
			quotes.remove(index);
			JSONArray quotesArray = new JSONArray();
			String quotesString = "";
			for (int i = 0; i < quotes.size(); i++) {
				quotesArray.add(quotes.get(i));
			}
			config.put("quotes", quotesArray);
			saveConfig();
			return true;
		}
	}

	public int getQuoteIndex(String quote) {
		if (quotes.contains(quote))
			return quotes.indexOf(quote);
		else
			return -1;
	}

	// ################################################################
	public String getCommand(String key) {
		key = key.toLowerCase();

		if (commands.containsKey(key)) {
			return commands.get(key);
		} else {
			return null;
		}
	}

	public void setCommand(String key, String command) {
		key = key.toLowerCase().replaceAll("[^a-zA-Z0-9]", "");
		System.out.println("Key: " + key);
		command = command.replaceAll(",,", "");

		if (key.length() < 1)
			return;

		if (commands.containsKey(key)) {
			commands.remove(key);
			commands.put(key, command);
		} else {
			commands.put(key, command);
		}

		saveCommands();

	}

	public void removeCommand(String key) {
		if (commands.containsKey(key)) {
			commands.remove(key);
			commandsRestrictions.remove(key);			

			saveCommands();

			
		}

	}
	public void saveCommands(){
		JSONArray commandsArr = new JSONArray();
		

		Iterator itr = commands.entrySet().iterator();

		while (itr.hasNext()) {
			Map.Entry pairs = (Map.Entry) itr.next();
			JSONObject commandObj = new JSONObject();
			commandObj.put("key", pairs.getKey());
			commandObj.put("value", pairs.getValue());
			if(commandsRestrictions.containsKey(pairs.getKey())){
				commandObj.put("restriction", commandsRestrictions.get(pairs.getKey()));
			}
			commandsArr.add(commandObj);
			
		}

		config.put("commands",commandsArr);
		saveConfig();
	}

	public boolean setCommandsRestriction(String command, int level) {
		command = command.toLowerCase();

		if (!commands.containsKey(command))
			return false;

		commandsRestrictions.put(command, level);

		saveCommands();

		return true;
	}

	public boolean checkCommandRestriction(String command, int level) {
		System.out.println("Checking command: " + command + " User level: "
				+ level);
		if (!commandsRestrictions.containsKey(command.toLowerCase()))
			return true;

		if (level >= commandsRestrictions.get(command.toLowerCase()))
			return true;

		return false;
	}

//	public void saveCommandRestrictions() {
//		String commandRestrictionsString = "";
//		JSONArray commandRestrictionsKey = new JSONArray();
//		JSONArray commandRestrictionsValue = new JSONArray();
//
//		Iterator itr = commandsRestrictions.entrySet().iterator();
//
//		while (itr.hasNext()) {
//			Map.Entry pairs = (Map.Entry) itr.next();
//			commandRestrictionsKey.add(pairs.getKey());
//			commandRestrictionsValue.add(pairs.getValue());
//
//		}
//
//		config.put("commandRestrictionsKey", commandRestrictionsKey);
//		config.put("commandRestrictionsValue", commandRestrictionsValue);
//		saveConfig();
//	}

	public void setRepeatCommand(String key, int delay, int diff) {
		key = key.toLowerCase();
		if (commandsRepeat.containsKey(key)) {
			commandsRepeat.get(key).timer.cancel();
			commandsRepeat.remove(key);
			RepeatCommand rc = new RepeatCommand(channel, key, delay, diff,
					true);
			commandsRepeat.put(key, rc);
		} else {
			RepeatCommand rc = new RepeatCommand(channel, key, delay, diff,
					true);
			commandsRepeat.put(key, rc);
		}

		saveRepeatCommands();
	}

	public void removeRepeatCommand(String key) {
		key = key.toLowerCase();
		if (commandsRepeat.containsKey(key)) {
			commandsRepeat.get(key).timer.cancel();
			commandsRepeat.remove(key);

			saveRepeatCommands();
		}
	}

	public void setRepeatCommandStatus(String key, boolean status) {
		if (commandsRepeat.containsKey(key)) {
			commandsRepeat.get(key).setStatus(status);
			saveRepeatCommands();
		}
	}

	private void saveRepeatCommands() {
		JSONArray repeatedCommands = new JSONArray();
		Iterator itr = commandsRepeat.entrySet().iterator();

		while (itr.hasNext()) {
			Map.Entry pairs = (Map.Entry) itr.next();
			JSONObject repeatObj = new JSONObject();
			repeatObj.put("name", pairs.getKey());
			repeatObj.put("delay", ((RepeatCommand)pairs.getValue()).delay);
			repeatObj.put("messageDifference", ((RepeatCommand)pairs.getValue()).messageDifference);
			repeatObj.put("active", ((RepeatCommand)pairs.getValue()).active);
			repeatedCommands.add(repeatObj);
			
		}

		config.put("repeatedCommands", repeatedCommands);
		saveConfig();
	}

	public void setScheduledCommand(String key, String pattern, int diff) {
		if (commandsSchedule.containsKey(key)) {
			commandsSchedule.get(key).s.stop();
			commandsSchedule.remove(key);
			ScheduledCommand rc = new ScheduledCommand(channel, key, pattern,
					diff, true);
			commandsSchedule.put(key, rc);
		} else {
			ScheduledCommand rc = new ScheduledCommand(channel, key, pattern,
					diff, true);
			commandsSchedule.put(key, rc);
		}

		saveScheduledCommands();

	}

	public void removeScheduledCommand(String key) {
		if (commandsSchedule.containsKey(key)) {
			commandsSchedule.get(key).s.stop();
			commandsSchedule.remove(key);

			saveScheduledCommands();
		}
	}

	public void setScheduledCommandStatus(String key, boolean status) {
		if (commandsSchedule.containsKey(key)) {
			commandsSchedule.get(key).setStatus(status);
			saveScheduledCommands();
		}
	}

	private void saveScheduledCommands() {
		
		JSONArray scheduledCommands = new JSONArray();

		Iterator itr = commandsSchedule.entrySet().iterator();

		while (itr.hasNext()) {
			Map.Entry pairs = (Map.Entry) itr.next();
			JSONObject scheduleObj = new JSONObject();
			scheduleObj.put("name", pairs.getKey());
			scheduleObj.put("pattern",((ScheduledCommand) pairs.getValue()).pattern);
			scheduleObj.put("messageDifference",((ScheduledCommand) pairs.getValue()).messageDifference);
			scheduleObj.put("active",((ScheduledCommand) pairs.getValue()).active);
			scheduledCommands.add(scheduleObj);
			
		

		}

		config.put("scheduledCommands",scheduledCommands);
		saveConfig();
	}

	public ArrayList<String> getCommandList() {

		ArrayList<String> sorted = new ArrayList<String>(commands.keySet());

		java.util.Collections.sort(sorted);
		return sorted;

	}

	public void addAutoReply(String trigger, String response) {
		trigger = trigger.replaceAll(",,", "");
		response.replaceAll(",,", "");

		if (!trigger.startsWith("REGEX:")) {
			String[] parts = trigger.replaceFirst("^\\*", "")
					.replaceFirst("\\*$", "").split("\\*");

			// Only apply leading & trailing any if an one was requested
			boolean trailingAny = trigger.endsWith("*");
			if (trigger.startsWith("*"))
				trigger = ".*";
			else
				trigger = "";

			for (int i = 0; i < parts.length; i++) {
				if (parts[i].length() < 1)
					continue;

				trigger += Pattern.quote(parts[i]);
				if (i != parts.length - 1)
					trigger += ".*";
			}

			if (trailingAny)
				trigger += ".*";

		} else {
			trigger = trigger.replaceAll("REGEX:", "");
		}

		System.out.println("Final: " + trigger);
		autoReplyTrigger
				.add(Pattern.compile(trigger, Pattern.CASE_INSENSITIVE));
		autoReplyResponse.add(response);

		saveAutoReply();
	}

	public boolean removeAutoReply(int pos) {
		pos = pos - 1;

		if (pos > autoReplyTrigger.size() - 1)
			return false;

		autoReplyTrigger.remove(pos);
		autoReplyResponse.remove(pos);

		saveAutoReply();

		return true;
	}

	private void saveAutoReply() {
		JSONArray triggerString = new JSONArray();
		JSONArray responseString = new JSONArray();
		JSONArray autoReplies = new JSONArray();

		for (int i = 0; i < autoReplyTrigger.size(); i++) {
			JSONObject autoreplyObj = new JSONObject();
			autoreplyObj.put("trigger",autoReplyTrigger.get(i).toString());
			autoreplyObj.put("response",autoReplyResponse.get(i).toString());
			autoReplies.add(autoreplyObj);
		}

		config.put("autoReplies",autoReplies);
		saveConfig();
	}

	// #####################################################

	public String getTopic() {
		return topic;
	}

	public void setTopic(String s) {
		topic = s;
		config.put("topic", topic);
		topicTime = (int) (System.currentTimeMillis() / 1000);
		config.put("topicTime", topicTime);
		saveConfig();
	}

	public void updateGame(String game) throws IOException {
		System.out.println(BotManager.putRemoteData(
				"https://api.twitch.tv/kraken/channels/"
						+ this.channel.substring(1),
				"{\"channel\": {\"game\": \"" + JSONObject.escape(game)
						+ "\"}}"));
	}

	public void updateStatus(String status) throws IOException {
		System.out.println(BotManager.putRemoteData(
				"https://api.twitch.tv/kraken/channels/"
						+ this.channel.substring(1),
				"{\"channel\": {\"status\": \"" + JSONObject.escape(status)
						+ "\"}}"));
	}

	public String getTopicTime() {
		int difference = (int) (System.currentTimeMillis() / 1000) - topicTime;
		String returnString = "";

		if (difference >= 86400) {
			int days = (int) (difference / 86400);
			returnString += days + "d ";
			difference -= days * 86400;
		}
		if (difference >= 3600) {
			int hours = (int) (difference / 3600);
			returnString += hours + "h ";
			difference -= hours * 3600;
		}

		int seconds = (int) (difference / 60);
		returnString += seconds + "m";
		difference -= seconds * 60;

		return returnString;
	}

	// #####################################################

	public int getFilterSymbolsMin() {
		return filterSymbolsMin;
	}

	public int getFilterSymbolsPercent() {
		return filterSymbolsPercent;
	}

	public void setFilterSymbolsMin(int symbols) {
		filterSymbolsMin = symbols;
		config.put("filterSymbolsMin", filterSymbolsMin);
		saveConfig();
	}

	public void setFilterSymbolsPercent(int symbols) {
		filterSymbolsPercent = symbols;
		config.put("filterSymbolsPercent", filterSymbolsPercent);
		saveConfig();
	}

	public boolean getFilterCaps() {
		return filterCaps;
	}

	public int getfilterCapsPercent() {
		return filterCapsPercent;
	}

	public int getfilterCapsMinCharacters() {
		return filterCapsMinCharacters;
	}

	public int getfilterCapsMinCapitals() {
		return filterCapsMinCapitals;
	}

	public void setFilterCaps(boolean caps) {
		filterCaps = caps;
		config.put("filterCaps", filterCaps);
		saveConfig();
	}

	public void setfilterCapsPercent(int caps) {
		filterCapsPercent = caps;
		config.put("filterCapsPercent", filterCapsPercent);
		saveConfig();
	}

	public void setfilterCapsMinCharacters(int caps) {
		filterCapsMinCharacters = caps;
		config.put("filterCapsMinCharacters", filterCapsMinCharacters);
		saveConfig();
	}

	public void setfilterCapsMinCapitals(int caps) {
		filterCapsMinCapitals = caps;
		config.put("filterCapsMinCapitals", filterCapsMinCapitals);
		saveConfig();
	}

	public void setFilterLinks(boolean links) {
		filterLinks = links;
		config.put("filterLinks", links);
		saveConfig();
	}

	public boolean getFilterLinks() {
		return filterLinks;
	}

	public void setFilterOffensive(boolean option) {
		filterOffensive = option;
		config.put("filterOffensive", option);
		saveConfig();
	}

	public boolean getFilterOffensive() {
		return filterOffensive;
	}

	public void setFilterEmotes(boolean option) {
		filterEmotes = option;
		config.put("filterEmotes", option);
		saveConfig();
	}

	public boolean getFilterEmotes() {
		return filterEmotes;
	}

	public void setFilterSymbols(boolean option) {
		filterSymbols = option;
		config.put("filterSymbols", option);
		saveConfig();
	}

	public boolean getFilterSymbols() {
		return filterSymbols;
	}

	public int getFilterMax() {
		return filterMaxLength;
	}

	public void setFilterMax(int option) {
		filterMaxLength = option;
		config.put("filterMaxLength", option);
		saveConfig();
	}

	public void setFilterEmotesMax(int option) {
		filterEmotesMax = option;
		config.put("filterEmotesMax", option);
		saveConfig();
	}

	public int getFilterEmotesMax() {
		return filterEmotesMax;
	}

	public boolean getFilterEmotesSingle() {
		return filterEmotesSingle;
	}

	public void setFilterEmotesSingle(boolean filterEmotesSingle) {
		this.filterEmotesSingle = filterEmotesSingle;

		config.put("filterEmotesSingle", filterEmotesSingle);
		saveConfig();
	}

	public void setAnnounceJoinParts(boolean bol) {
		announceJoinParts = bol;
		config.put("announceJoinParts", bol);
		saveConfig();
	}

	public boolean getAnnounceJoinParts() {
		return announceJoinParts;
	}

	public void setFilterColor(boolean option) {
		filterColors = option;
		config.put("filterColors", option);
		saveConfig();
	}

	public boolean getFilterColor() {
		return filterColors;
	}

	public void setFilterMe(boolean option) {
		filterMe = option;
		config.put("filterMe", option);
		saveConfig();
	}

	public boolean getFilterMe() {
		return filterMe;
	}

	public void setEnableWarnings(boolean option) {
		enableWarnings = option;
		config.put("enableWarnings", option);
		saveConfig();
	}

	public boolean getEnableWarnings() {
		return enableWarnings;
	}

	public void setTimeoutDuration(int option) {
		timeoutDuration = option;
		config.put("timeoutDuration", option);
		saveConfig();
	}

	public int getTimeoutDuration() {
		return timeoutDuration;
	}

	// ###################################################

	public boolean isRegular(String name) {
		synchronized (regulars) {
			for (String s : regulars) {
				if (s.equalsIgnoreCase(name)) {
					return true;
				}
			}
		}
		return false;
	}

	public void addRegular(String name) {
		synchronized (regulars) {
			regulars.add(name.toLowerCase());

		}

		JSONArray regularsArray = new JSONArray();

		synchronized (regulars) {
			for (String s : regulars) {
				regularsArray.add(s);
			}
		}

		config.put("regulars", regularsArray);
		saveConfig();
	}

	public void removeRegular(String name) {
		synchronized (regulars) {
			if (regulars.contains(name.toLowerCase()))
				regulars.remove(name.toLowerCase());
		}
		JSONArray regularsArray = new JSONArray();

		synchronized (regulars) {
			for (String s : regulars) {
				regularsArray.add(s);
			}
		}

		config.put("regulars", regularsArray);
		saveConfig();
	}

	public Set<String> getRegulars() {
		return regulars;
	}

	public void permitUser(String name) {
		synchronized (permittedUsers) {
			if (permittedUsers.contains(name.toLowerCase()))
				return;
		}

		synchronized (permittedUsers) {
			permittedUsers.add(name.toLowerCase());
		}
	}

	public boolean linkPermissionCheck(String name) {

		if (this.isRegular(name)) {
			return true;
		}

		synchronized (permittedUsers) {
			if (permittedUsers.contains(name.toLowerCase())) {
				permittedUsers.remove(name.toLowerCase());
				return true;
			}
		}

		return false;
	}

	// public boolean isSubscriber(String name) {
	// if (subscribers.contains(name.toLowerCase()))
	// return true;
	//
	// if (emoteSet.length() > 0)
	// if (BotManager.getInstance().checkEmoteSetMapping(name, emoteSet))
	// return true;
	// return false;
	// }
	public void addRaidWhitelist(String name) {
		raidWhitelist.add(name.toLowerCase());
		JSONArray raidWhitelistArray = new JSONArray();
		for (String s : raidWhitelist) {
			raidWhitelistArray.add(s);
		}
		config.put("raidWhitelist", raidWhitelistArray);
		saveConfig();
	}

	public void setGamertag(String gamerTag) {
		this.gamerTag = gamerTag.replaceAll(" ", "+");
		config.put("gamerTag", this.gamerTag);
		saveConfig();
	}

	public String getGamerTag() {
		return (gamerTag);
	}

	public void deleteRaidWhitelist(String name) {
		raidWhitelist.remove(name);
		JSONArray raidWhitelistArray = new JSONArray();
		for (String s : raidWhitelist) {
			raidWhitelistArray.add(s);
		}
		config.put("raidWhitelist", raidWhitelistArray);
		saveConfig();
	}

	public ArrayList<String> getRaidWhitelist() {
		ArrayList<String> list = new ArrayList<String>();
		for (String s : raidWhitelist) {
			list.add(s);
		}
		java.util.Collections.sort(list);
		return list;
	}

	// public void addSubscriber(String name) {
	// subscribers.add(name.toLowerCase());
	//
	// String subsString="";
	// for (String s : subscribers) {
	// subsString += s + "&&&";
	// }
	// config.setString("subscribers",subsString);
	// }

	// ###################################################

	public boolean isModerator(String name) {
		synchronized (tagModerators) {
			if (tagModerators.contains(name))
				return true;
		}
		synchronized (moderators) {
			if (moderators.contains(name.toLowerCase()))
				return true;
		}

		return false;
	}

	public void addModerator(String name) {
		synchronized (moderators) {
			moderators.add(name.toLowerCase());
		}

		JSONArray moderatorsArray = new JSONArray();

		synchronized (moderators) {
			for (String s : moderators) {
				moderatorsArray.add(s);
			}
		}

		config.put("moderators", moderatorsArray);
		saveConfig();
	}

	public void removeModerator(String name) {
		synchronized (moderators) {
			if (moderators.contains(name.toLowerCase()))
				moderators.remove(name.toLowerCase());
		}

		JSONArray moderatorsArray = new JSONArray();

		synchronized (moderators) {
			for (String s : moderators) {
				moderatorsArray.add(s);
			}
		}

		config.put("moderators", moderatorsArray);
		saveConfig();
	}

	public Set<String> getModerators() {
		return moderators;
	}

	// ###################################################

	public boolean isOwner(String name) {
		synchronized (owners) {
			if (owners.contains(name.toLowerCase()))
				return true;
		}

		return false;
	}

	public void addOwner(String name) {
		synchronized (owners) {
			owners.add(name.toLowerCase());
		}

		JSONArray ownersString = new JSONArray();

		synchronized (owners) {
			for (String s : owners) {
				ownersString.add(s);
			}
		}

		config.put("owners", ownersString);
		saveConfig();
	}

	public void removeOwner(String name) {
		synchronized (owners) {
			if (owners.contains(name.toLowerCase()))
				owners.remove(name.toLowerCase());
		}

		JSONArray ownersString = new JSONArray();

		synchronized (owners) {
			for (String s : owners) {
				ownersString.add(s);
			}
		}

		config.put("owners", ownersString);
		saveConfig();
	}

	public Set<String> getOwners() {
		return owners;
	}

	// ###################################################

	public void addPermittedDomain(String name) {
		synchronized (permittedDomains) {
			permittedDomains.add(name.toLowerCase());
		}

		JSONArray permittedDomainsString = new JSONArray();

		synchronized (permittedDomains) {
			for (String s : permittedDomains) {
				permittedDomainsString.add(s);
			}
		}

		config.put("permittedDomains", permittedDomainsString);
		saveConfig();
	}

	public void removePermittedDomain(String name) {
		synchronized (permittedDomains) {
			for (int i = 0; i < permittedDomains.size(); i++) {
				if (permittedDomains.get(i).equalsIgnoreCase(name)) {
					permittedDomains.remove(i);
				}
			}
		}

		JSONArray permittedDomainsString = new JSONArray();

		synchronized (permittedDomains) {
			for (String s : permittedDomains) {
				permittedDomainsString.add(s);
			}
		}

		config.put("permittedDomains", permittedDomainsString);
		saveConfig();

	}

	public boolean isDomainPermitted(String domain) {
		for (String d : permittedDomains) {
			if (d.equalsIgnoreCase(domain)) {
				return true;
			}
		}

		return false;
	}

	public ArrayList<String> getpermittedDomains() {
		return permittedDomains;
	}

	// #################################################

	public void addOffensive(String word) {
		synchronized (offensiveWords) {
			offensiveWords.add(word);
		}

		synchronized (offensiveWordsRegex) {
			if (word.startsWith("REGEX:")) {
				String line = word.substring(6);
				System.out.println("Adding: " + line);
				Pattern tempP = Pattern.compile(line);
				offensiveWordsRegex.add(tempP);
			} else {
				String line = ".*" + Pattern.quote(word) + ".*";
				System.out.println("Adding: " + line);
				Pattern tempP = Pattern.compile(line, Pattern.CASE_INSENSITIVE);
				offensiveWordsRegex.add(tempP);
			}

		}

		JSONArray offensiveWordsArray = new JSONArray();

		synchronized (offensiveWords) {
			for (String s : offensiveWords) {
				offensiveWordsArray.add(s);
			}
		}

		config.put("offensiveWords", offensiveWordsArray);
		saveConfig();
	}

	public void removeOffensive(String word) {
		synchronized (offensiveWords) {
			if (offensiveWords.contains(word))
				offensiveWords.remove(word);
		}

		JSONArray offensiveWordsArray = new JSONArray();

		synchronized (offensiveWords) {
			for (String s : offensiveWords) {
				offensiveWordsArray.add(s);
			}
		}

		config.put("offensiveWords", offensiveWordsArray);
		saveConfig();

		synchronized (offensiveWordsRegex) {
			offensiveWordsRegex.clear();

			for (String w : offensiveWords) {
				if (w.startsWith("REGEX:")) {
					String line = w.substring(6);
					System.out.println("ReAdding: " + line);
					Pattern tempP = Pattern.compile(line);
					offensiveWordsRegex.add(tempP);
				} else {
					String line = ".*" + Pattern.quote(w) + ".*";
					System.out.println("ReAdding: " + line);
					Pattern tempP = Pattern.compile(line);
					offensiveWordsRegex.add(tempP);
				}
			}
		}
	}

	public void clearBannedPhrases() {
		offensiveWords.clear();
		offensiveWordsRegex.clear();
		config.put("offensiveWords", new JSONArray());
		saveConfig();
	}

	public boolean isBannedPhrase(String phrase) {
		return offensiveWords.contains(phrase);
	}

	public boolean isOffensive(String word) {
		for (Pattern reg : offensiveWordsRegex) {
			Matcher match = reg.matcher(word.toLowerCase());
			if (match.find()) {
				System.out.println("Matched: " + reg.toString());
				return true;
			}
		}

		int severity = Integer.parseInt((String) config
				.get("banPhraseSeverity"));
		if (BotManager.getInstance().banPhraseLists.containsKey(severity)) {
			for (Pattern reg : BotManager.getInstance().banPhraseLists
					.get(severity)) {
				Matcher match = reg.matcher(word.toLowerCase());
				if (match.find()) {
					System.out.println("Matched: " + reg.toString());
					return true;
				}
			}
		}

		return false;
	}

	public Set<String> getOffensive() {
		return offensiveWords;
	}

	// ##################################################

	public void setTopicFeature(boolean setting) {
		this.useTopic = setting;
		config.put("useTopic", this.useTopic);
		saveConfig();

	}

	public void setFiltersFeature(boolean setting) {
		this.useFilters = setting;
		config.put("useFilters", this.useFilters);
		saveConfig();
	}

	public Poll getPoll() {
		return currentPoll;
	}

	public void setPoll(Poll _poll) {
		currentPoll = _poll;
	}

	public Giveaway getGiveaway() {
		return currentGiveaway;
	}

	public void setGiveaway(Giveaway _gw) {
		currentGiveaway = _gw;
	}

	public boolean checkThrow() {
		return enableThrow;
	}

	public void setThrow(boolean setting) {
		this.enableThrow = setting;
		config.put("enableThrow", this.enableThrow);
		saveConfig();
	}

	public boolean checkSignKicks() {
		return signKicks;
	}

	public void setSignKicks(boolean setting) {
		this.signKicks = setting;
		config.put("signKicks", this.signKicks);
		saveConfig();
	}

	public void setLogging(boolean option) {
		logChat = option;
		config.put("logChat", option);
		saveConfig();
	}

	public boolean getLogging() {
		return logChat;
	}

	public int getCommercialLength() {
		return commercialLength;
	}

	public void setCommercialLength(int commercialLength) {
		this.commercialLength = commercialLength;
		config.put("commercialLength", commercialLength);
		saveConfig();
	}

	// ##################################################

	public boolean checkPermittedDomain(String message) {
		// Allow base domain w/o a path
		if (message.matches(".*(twitch\\.tv|twitchtv\\.com|justin\\.tv)")) {
			System.out
					.println("INFO: Permitted domain match on jtv/ttv base domain.");
			return true;
		}

		for (String d : permittedDomains) {
			// d = d.replaceAll("\\.", "\\\\.");

			String test = ".*(\\.|^|//)" + Pattern.quote(d) + "(/|$).*";
			if (message.matches(test)) {
				// System.out.println("DEBUG: Matched permitted domain: " +
				// test);
				return true;
			}
		}
		return false;
	}

	// #################################################

	public String getLastfm() {
		return lastfm;
	}

	public void setLastfm(String string) {
		lastfm = string;
		config.put("lastfm", lastfm);
		saveConfig();
	}

	public boolean updateSong() {
		String newSong = JSONUtil.lastFM(getLastfm());
		if (newSong.equals(lastSong) || newSong.equals("(Nothing)")
				|| newSong.equalsIgnoreCase("(Error querying API)")) {
			return false;

		} else {
			long now = System.currentTimeMillis();
			if ((now * 1L) >= (songUpdated + updateDelay * 1000L)) {
				lastSong = newSong;
				songUpdated = now + 5000;
				return true;
			}
			return false;
		}

	}

	public void checkViewerStats(String name) {
		long viewers = JSONUtil.krakenViewers(name);
		if (viewers > maxViewers) {
			maxViewers = (int) viewers;
			config.put("maxViewers", maxViewers);
			maxviewerDate = new java.util.Date().toString();
			config.put("maxviewerDate", maxviewerDate);

		}
		saveConfig();

	}

	public int getViewerStats() {
		return maxViewers;
	}

	public String getViewerStatsTime() {
		return maxviewerDate;
	}

	public void resetMaxViewers(int newMax) {
		maxViewers = newMax;
		config.put("maxViewers", maxViewers);
		saveConfig();
	}

	public void increasePunCount() {
		punishCount++;
		sincePunish = System.currentTimeMillis();
		config.put("sincePunish", sincePunish);
		config.put("punishCount", punishCount);
		saveConfig();
	}

	public int getPunCount() {
		return punishCount;
	}

	// public void alive(String name) {
	// if (!streamUp) {
	//
	// }
	// streamUp = true;
	// config.setBoolean("streamAlive", true);
	// long curViewers = JSONUtil.krakenViewers(name);
	// if (curViewers > streamMax) {
	// streamMax = (int) curViewers;
	// config.setInt("maxViewersStream", (int) curViewers);
	// }
	// }

	public void dead(String name) {
		if (streamUp) {
			streamNumber++;
			config.put("streamCount", streamNumber);
			runningMaxViewers += streamMax;
			config.put("runningMaxViewers", runningMaxViewers);
		}
		streamUp = false;
		config.put("streamAlive", false);
		streamMax = 0;
		config.put("maxViewersStream", 0);
		saveConfig();
	}

	public double getAverage() {
		return (double) runningMaxViewers / (streamNumber);
	}

	// #################################################

	public String getSteam() {
		return steamID;
	}

	public void setSteam(String string) {
		steamID = string;
		config.put("steamID", steamID);
		saveConfig();
	}

	// #################################################

	public String getClickToTweetFormat() {
		return clickToTweetFormat;
	}

	public void setClickToTweetFormat(String string) {
		clickToTweetFormat = string;
		config.put("clickToTweetFormat", clickToTweetFormat);
		saveConfig();
	}

	public int getWarningCount(String name, FilterType type) {
		if (warningCount.containsKey(name.toLowerCase())
				&& warningCount.get(name.toLowerCase()).containsKey(type))
			return warningCount.get(name.toLowerCase()).get(type);
		else
			return 0;
	}

	public void incWarningCount(String name, FilterType type) {
		clearWarnings();
		synchronized (warningCount) {
			if (warningCount.containsKey(name.toLowerCase())) {
				if (warningCount.get(name.toLowerCase()).containsKey(type)) {
					warningCount.get(name.toLowerCase()).put(type,
							warningCount.get(name.toLowerCase()).get(type) + 1);
					warningTime.put(name.toLowerCase(), getTime());
				} else {
					warningCount.get(name.toLowerCase()).put(type, 1);
					warningTime.put(name.toLowerCase(), getTime());
				}
			} else {
				warningCount.put(name.toLowerCase(),
						new EnumMap<FilterType, Integer>(FilterType.class));
				warningCount.get(name.toLowerCase()).put(type, 1);
				warningTime.put(name.toLowerCase(), getTime());
			}
		}
	}

	public void clearWarnings() {
		List<String> toRemove = new ArrayList<String>();
		synchronized (warningTime) {
			synchronized (warningCount) {
				long time = getTime();
				for (Map.Entry<String, Long> entry : warningTime.entrySet()) {
					if ((time - entry.getValue()) > 3600) {
						toRemove.add((String) entry.getKey());
					}
				}
				for (String name : toRemove) {
					warningCount.remove(name);
					warningTime.remove(name);
				}
			}
		}
	}

	private void registerCommandUsage(String command) {
		synchronized (commandCooldown) {
			System.out.println("DEBUG: Adding command " + command
					+ " to cooldown list");
			commandCooldown.put(command.toLowerCase(), getTime());
		}
	}

	public boolean onCooldown(String command) {
		command = command.toLowerCase();
		if (commandCooldown.containsKey(command)) {
			long lastUse = commandCooldown.get(command);
			if ((getTime() - lastUse) > 30) {
				// Over
				System.out.println("DEBUG: Cooldown for " + command
						+ " is over");
				registerCommandUsage(command);
				return false;
			} else {
				// Not Over
				System.out.println("DEBUG: Cooldown for " + command
						+ " is NOT over");
				return true;
			}
		} else {
			registerCommandUsage(command);
			return false;
		}
	}

	public void setUpdateDelay(int newDelay) {
		updateDelay = newDelay;
		config.put("updateDelay", newDelay);
		saveConfig();
	}

	public void reload() {
		BotManager.getInstance().removeChannel(channel);
		BotManager.getInstance().addChannel(channel, mode);
	}

	private void setDefaults() {

		// defaults.put("channel", channel);
		defaults.put("subsRegsMinusLinks", false);
		defaults.put("filterCaps", false);
		defaults.put("filterOffensive", true);
		defaults.put("filterCapsPercent", 50);
		defaults.put("filterCapsMinCharacters", 0);
		defaults.put("filterCapsMinCapitals", 6);
		defaults.put("filterLinks", false);
		defaults.put("filterEmotes", false);
		defaults.put("filterSymbols", false);
		defaults.put("filterEmotesMax", 4);

		defaults.put("punishCount", 0);
		defaults.put("sincePunish", sincePunish);
		defaults.put("sinceWp", System.currentTimeMillis());
		defaults.put("maxviewerDate", "");

		defaults.put("topic", "");
		defaults.put("commands", new JSONArray());
		
		defaults.put("repeatedCommands", new JSONArray());
		defaults.put("scheduledCommands", new JSONArray());
		defaults.put("autoReplies", new JSONArray());
		defaults.put("regulars", new JSONArray());
		defaults.put("moderators", new JSONArray());
		defaults.put("owners", new JSONArray());
		defaults.put("useTopic", true);
		defaults.put("useFilters", false);
		defaults.put("enableThrow", true);
		defaults.put("permittedDomains", new JSONArray());
		defaults.put("signKicks", false);
		defaults.put("topicTime", 0);
		defaults.put("mode", 2);
		defaults.put("announceJoinParts", false);
		defaults.put("lastfm", "");
		defaults.put("steamID", "");
		defaults.put("logChat", false);
		defaults.put("filterMaxLength", 500);
		defaults.put("offensiveWords", new JSONArray());
		defaults.put("commercialLength", 30);
		defaults.put("filterColors", false);
		defaults.put("filterMe", false);
		defaults.put("staticChannel", false);
		defaults.put("enableWarnings", true);
		defaults.put("timeoutDuration", 600);
		defaults.put("clickToTweetFormat",
				"Checkout (_CHANNEL_URL_) playing (_GAME_) on @TwitchTV");
		defaults.put("filterSymbolsPercent", 50);
		defaults.put("filterSymbolsMin", 5);
		defaults.put("commandPrefix", "!");
		
		defaults.put("emoteSet", "");
		defaults.put("subscriberRegulars", false);
		defaults.put("filterEmotesSingle", false);
		defaults.put("subMessage", "(_1_) has subscribed!");
		defaults.put("subscriberAlert", false);
		defaults.put("banPhraseSeverity", 99);

		defaults.put("wpTimer", false);
		defaults.put("wpCount", 0);
		defaults.put("bullet", "coeBot");
		defaults.put("cooldown", 5);

		defaults.put("maxViewers", 0);
		defaults.put("runningMaxViewers", 0);
		defaults.put("streamCount", 0);
		defaults.put("streamAlive", false);
		defaults.put("maxViewersStream", 0);

		defaults.put("updateDelay", 120);
		defaults.put("quotes", new JSONArray());
		//defaults.put("subscribers", new JSONArray());
		defaults.put("raidWhitelist", new JSONArray());
		defaults.put("gamerTag", "");

		Iterator it = defaults.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pairs = (Map.Entry) it.next();
			String key = String.valueOf(pairs.getKey());
			String value = String.valueOf(pairs.getValue());
			if (!config.containsKey(key))
				config.put(key, value);
		}
		saveConfig();
	}

	public void updateConfigs() {
		config.put("subsRegsMinusLinks", subsRegsMinusLinks);
		config.put("filterCaps", filterCaps);
		config.put("filterOffensive", true);
		config.put("filterCapsPercent", filterCapsPercent);
		config.put("filterCapsMinCharacters", filterCapsMinCharacters);
		config.put("filterCapsMinCapitals", filterCapsMinCapitals);
		config.put("filterLinks", filterLinks);
		config.put("filterEmotes", filterEmotes);
		config.put("filterSymbols", filterSymbols);
		config.put("filterEmotesMax", filterEmotesMax);

		config.put("punishCount", punishCount);
		config.put("sincePunish", sincePunish);
		config.put("sinceWp", System.currentTimeMillis());
		config.put("maxviewerDate", maxviewerDate);

		config.put("topic", topic);
		this.saveCommands();
		this.saveAutoReply();
		this.saveRepeatCommands();
		this.saveScheduledCommands();
		

		this.addRegular("JSONCONVERT");
		this.removeRegular("JSONCONVERT");
		this.addModerator("JSONCONVERT");
		this.removeModerator("JSONCONVERT");
		this.addOwner("JSONCONVERT");
		this.removeOwner("JSONCONVERT");

		config.put("useTopic", useTopic);
		config.put("useFilters", useFilters);
		config.put("enableThrow", enableThrow);

		this.addPermittedDomain("JSONCONVERT");
		this.removePermittedDomain("JSONCONVERT");

		config.put("signKicks", signKicks);
		config.put("topicTime", topicTime);
		config.put("mode", mode);
		config.put("announceJoinParts", false);
		config.put("lastfm", lastfm);
		config.put("steamID", steamID);
		config.put("logChat", logChat);
		config.put("filterMaxLength", filterMaxLength);
		this.addOffensive("JSONCONVERT");
		this.removeOffensive("JSONCONVERT");

		config.put("commercialLength", commercialLength);
		config.put("filterColors", filterColors);
		config.put("filterMe", filterMe);
		config.put("staticChannel", staticChannel);
		config.put("enableWarnings", enableWarnings);
		config.put("timeoutDuration", timeoutDuration);
		config.put("clickToTweetFormat", clickToTweetFormat);
		config.put("filterSymbolsPercent", filterSymbolsPercent);
		config.put("filterSymbolsMin", filterSymbolsMin);
		config.put("commandPrefix", prefix);
		
		
		config.put("emoteSet", emoteSet);
		config.put("subscriberRegulars", subscriberRegulars);
		config.put("filterEmotesSingle", filterEmotesSingle);
		config.put("subMessage", oldconfig.getString("subMessage"));
		config.put("subscriberAlert", oldconfig.getString("subscriberAlert"));
		config.put("banPhraseSeverity", Integer.parseInt(oldconfig.getString("banPhraseSeverity")));

		config.put("wpTimer", Boolean.parseBoolean(oldconfig.getString("wpTimer")));
		config.put("wpCount", wpCount);
		config.put("bullet", bullet);
		config.put("cooldown", cooldown);

		config.put("maxViewers", maxViewers);
		config.put("runningMaxViewers", runningMaxViewers);
		config.put("streamCount", streamNumber);
		config.put("streamAlive", streamUp);
		config.put("maxViewersStream", streamMax);

		config.put("updateDelay", updateDelay);
		this.addQuote("JSONCONVERT");
		this.deleteQuote(this.getQuoteIndex("JSONCONVERT"));
		
		
		//config.put("subscribers", subscribers);
		this.addRaidWhitelist("JSONCONVERT");
		this.deleteRaidWhitelist("JSONCONVERT");
		
		config.put("gamerTag", gamerTag);
		saveConfig();

	}

	private void loadProperties(String name) {
		setDefaults();
		
		gamerTag = oldconfig.getString("gamerTag");
		// channel = config.getString("channel");
		subsRegsMinusLinks = Boolean.parseBoolean(oldconfig
				.getString("subsRegsMinusLinks"));
		updateDelay = Integer.parseInt(oldconfig.getString("updateDelay"));
		punishCount = Integer.parseInt(oldconfig.getString("punishCount"));
		streamUp = Boolean.parseBoolean(oldconfig.getString("streamAlive"));
		sinceWp = Long.parseLong(oldconfig.getString("sinceWp"));
		maxviewerDate = oldconfig.getString("maxviewerDate");
		runningMaxViewers = Integer.parseInt(oldconfig
				.getString("runningMaxViewers"));
		streamNumber = Integer.parseInt(oldconfig.getString("streamCount"));
		streamMax = Integer.parseInt(oldconfig.getString("maxViewersStream"));
		maxViewers = Integer.parseInt(oldconfig.getString("max viewers"));
		filterCaps = Boolean.parseBoolean(oldconfig.getString("filterCaps"));

		filterCapsPercent = Integer.parseInt(oldconfig
				.getString("filterCapsPercent"));
		filterCapsMinCharacters = Integer.parseInt(oldconfig
				.getString("filterCapsMinCharacters"));
		filterCapsMinCapitals = Integer.parseInt(oldconfig
				.getString("filterCapsMinCapitals"));
		filterLinks = Boolean.parseBoolean(oldconfig.getString("filterLinks"));
		filterOffensive = Boolean.parseBoolean(oldconfig
				.getString("filterOffensive"));
		filterEmotes = Boolean
				.parseBoolean(oldconfig.getString("filterEmotes"));

		wpOn = Boolean.parseBoolean(oldconfig.getString("wpTimer"));
		wpCount = Integer.parseInt(oldconfig.getString("wpCount"));
		bullet = oldconfig.getString("bullet");
		cooldown = Integer.parseInt(oldconfig.getString("cooldown"));
		sincePunish = Long.parseLong(oldconfig.getString("sincePunish"));

		filterSymbols = Boolean.parseBoolean(oldconfig
				.getString("filterSymbols"));
		filterSymbolsPercent = Integer.parseInt(oldconfig
				.getString("filterSymbolsPercent"));
		filterSymbolsMin = Integer.parseInt(oldconfig
				.getString("filterSymbolsMin"));
		filterEmotesMax = Integer.parseInt(oldconfig
				.getString("filterEmotesMax"));
		filterEmotesSingle = Boolean.parseBoolean(oldconfig
				.getString("filterEmotesSingle"));
		// announceJoinParts =
		// Boolean.parseBoolean(config.getString("announceJoinParts"));
		announceJoinParts = false;
		topic = oldconfig.getString("topic");
		topicTime = oldconfig.getInt("topicTime");
		useTopic = Boolean.parseBoolean(oldconfig.getString("useTopic"));
		useFilters = Boolean.parseBoolean(oldconfig.getString("useFilters"));
		enableThrow = Boolean.parseBoolean(oldconfig.getString("enableThrow"));
		signKicks = Boolean.parseBoolean(oldconfig.getString("signKicks"));
		lastfm = oldconfig.getString("lastfm");
		steamID = oldconfig.getString("steamID");
		logChat = Boolean.parseBoolean(oldconfig.getString("logChat"));
		mode = oldconfig.getInt("mode");
		filterMaxLength = oldconfig.getInt("filterMaxLength");
		commercialLength = oldconfig.getInt("commercialLength");
		filterColors = Boolean
				.parseBoolean(oldconfig.getString("filterColors"));
		filterMe = Boolean.parseBoolean(oldconfig.getString("filterMe"));
		staticChannel = Boolean.parseBoolean(oldconfig
				.getString("staticChannel"));
		clickToTweetFormat = oldconfig.getString("clickToTweetFormat");

		enableWarnings = Boolean.parseBoolean(oldconfig
				.getString("enableWarnings"));
		timeoutDuration = oldconfig.getInt("timeoutDuration");
		prefix = oldconfig.getString("commandPrefix").charAt(0) + "";
		emoteSet = oldconfig.getString("emoteSet");
		subscriberRegulars = oldconfig.getBoolean("subscriberRegulars");

		String[] quotesArray = oldconfig.getString("quotes").split("&&&");

		for (int i = 0; i < quotesArray.length; i++) {
			quotes.add(quotesArray[i]);
		}

		String[] raidWhitelistArray = oldconfig.getString("raidWhitelist")
				.split("&&&");

		for (int i = 0; i < raidWhitelistArray.length; i++) {
			raidWhitelist.add(raidWhitelistArray[i]);
		}

		String[] subsArray = oldconfig.getString("subscribers").split("&&&");

		for (int i = 0; i < subsArray.length; i++) {
			subscribers.add(subsArray[i]);
		}

		String[] commandsKey = oldconfig.getString("commandsKey").split(",");
		String[] commandsValue = oldconfig.getString("commandsValue").split(
				",,");

		for (int i = 0; i < commandsKey.length; i++) {
			if (commandsKey[i].length() > 1) {
				commands.put(commandsKey[i].replaceAll("[^a-zA-Z0-9]", "")
						.toLowerCase(), commandsValue[i]);
			}
		}

		String[] commandR = oldconfig.getString("commandRestrictions").split(
				",");
		for (int i = 0; i < commandR.length; i++) {
			if (commandR[i].length() > 1) {
				String[] parts = commandR[i].split("\\|");
				commandsRestrictions.put(parts[0], Integer.parseInt(parts[1]));
			}
		}

		String[] commandsRepeatKey = oldconfig.getString("commandsRepeatKey")
				.split(",");
		String[] commandsRepeatDelay = oldconfig.getString(
				"commandsRepeatDelay").split(",");
		String[] commandsRepeatDiff = oldconfig.getString("commandsRepeatDiff")
				.split(",");
		String[] commandsRepeatActive = oldconfig.getString(
				"commandsRepeatActive").split(",");

		for (int i = 0; i < commandsRepeatKey.length; i++) {
			if (commandsRepeatKey[i].length() > 1) {
				RepeatCommand rc = new RepeatCommand(channel,
						commandsRepeatKey[i].replaceAll("[^a-zA-Z0-9]", ""),
						Integer.parseInt(commandsRepeatDelay[i]),
						Integer.parseInt(commandsRepeatDiff[i]),
						Boolean.parseBoolean(commandsRepeatActive[i]));
				commandsRepeat
						.put(commandsRepeatKey[i]
								.replaceAll("[^a-zA-Z0-9]", ""), rc);
			}
		}

		String[] commandsScheduleKey = oldconfig.getString(
				"commandsScheduleKey").split(",,");
		String[] commandsSchedulePattern = oldconfig.getString(
				"commandsSchedulePattern").split(",,");
		String[] commandsScheduleDiff = oldconfig.getString(
				"commandsScheduleDiff").split(",,");
		String[] commandsScheduleActive = oldconfig.getString(
				"commandsScheduleActive").split(",,");

		for (int i = 0; i < commandsScheduleKey.length; i++) {
			if (commandsScheduleKey[i].length() > 1) {
				ScheduledCommand rc = new ScheduledCommand(channel,
						commandsScheduleKey[i].replaceAll("[^a-zA-Z0-9]", ""),
						commandsSchedulePattern[i],
						Integer.parseInt(commandsScheduleDiff[i]),
						Boolean.parseBoolean(commandsScheduleActive[i]));
				commandsSchedule.put(
						commandsScheduleKey[i].replaceAll("[^a-zA-Z0-9]", ""),
						rc);
			}
		}

		String[] autoReplyTriggersString = oldconfig.getString(
				"autoReplyTriggers").split(",,");
		String[] autoReplyResponseString = oldconfig.getString(
				"autoReplyResponse").split(",,");

		for (int i = 0; i < autoReplyTriggersString.length; i++) {
			if (autoReplyTriggersString[i].length() > 0) {
				autoReplyTrigger.add(Pattern.compile(
						autoReplyTriggersString[i], Pattern.CASE_INSENSITIVE));
				autoReplyResponse.add(autoReplyResponseString[i]);
			}
		}

		String[] regularsRaw = oldconfig.getString("regulars").split(",");
		synchronized (regulars) {
			for (int i = 0; i < regularsRaw.length; i++) {
				if (regularsRaw[i].length() > 1) {
					regulars.add(regularsRaw[i].toLowerCase());
				}
			}
		}

		String[] moderatorsRaw = oldconfig.getString("moderators").split(",");
		synchronized (moderators) {
			for (int i = 0; i < moderatorsRaw.length; i++) {
				if (moderatorsRaw[i].length() > 1) {
					moderators.add(moderatorsRaw[i].toLowerCase());
				}
			}
		}

		String[] ownersRaw = oldconfig.getString("owners").split(",");
		synchronized (owners) {
			for (int i = 0; i < ownersRaw.length; i++) {
				if (ownersRaw[i].length() > 1) {
					owners.add(ownersRaw[i].toLowerCase());
				}
			}
		}

		String[] domainsRaw = oldconfig.getString("permittedDomains")
				.split(",");
		synchronized (permittedDomains) {
			for (int i = 0; i < domainsRaw.length; i++) {
				if (domainsRaw[i].length() > 1) {
					// permittedDomains.add(domainsRaw[i].toLowerCase().replaceAll("\\.",
					// "\\\\."));
					permittedDomains.add(domainsRaw[i].toLowerCase());

				}
			}
		}
		System.out.println(oldconfig.getString("offensiveWords"));
		String[] offensiveWordsRaw = oldconfig.getString("offensiveWords")
				.split(",,");
		synchronized (offensiveWords) {
			synchronized (offensiveWordsRegex) {
				for (int i = 0; i < offensiveWordsRaw.length; i++) {
					if (offensiveWordsRaw[i].length() > 1) {
						String w = offensiveWordsRaw[i];
						offensiveWords.add(w);
						if (w.startsWith("REGEX:")) {
							String line = w.substring(6);
							System.out.println("Adding: " + line);
							Pattern tempP = Pattern.compile(line);
							offensiveWordsRegex.add(tempP);
						} else {
							String line = "(?i).*" + Pattern.quote(w) + ".*";
							System.out.println("Adding: " + line);
							Pattern tempP = Pattern.compile(line);
							offensiveWordsRegex.add(tempP);
						}

					}
				}
			}

		}

	}

	public void setMode(int mode) {
		this.mode = mode;
		config.put("mode", this.mode);

		if (mode == -1) {
			this.setFiltersFeature(true);
			this.setFilterEmotes(false);
			this.setFilterEmotesMax(5);
			this.setFilterSymbols(true);
			this.setFilterCaps(false);
			this.setFilterLinks(false);
			this.setFilterOffensive(true);
			this.setSignKicks(false);
			this.setTopicFeature(false);
			this.setThrow(false);
		}
		saveConfig();
	}

	public int getMode() {
		return mode;
	}

	private long getTime() {
		return (System.currentTimeMillis() / 1000L);
	}

	public void cancelCommercial() {
		commercial.cancel();
	}

	public void scheduleCommercial() {
		BotManager.getInstance().receiverBot
				.send(getChannel(),
						"A commercial will be run in 45 seconds. Thank you for supporting the channel!");
		commercial = new java.util.Timer();
		commercial.schedule(new java.util.TimerTask() {
			@Override
			public void run() {
				runCommercial();
			}
		}, 45000);
	}

	public void snoozeCommercial() {
		if (commercial != null) {
			commercial.cancel();
			commercial.schedule(new java.util.TimerTask() {
				@Override
				public void run() {
					scheduleCommercial();
				}
			}, 300000);
		}
	}

	public void testChannelSend() {
		BotManager.getInstance().receiverBot.send(getChannel(), "Success!");
	}

	public void runCommercial() {

		if (JSONUtil.krakenIsLive(getChannel().substring(1))) {
			String dataIn = "";
			dataIn = BotManager.postRemoteDataTwitch(
					"https://api.twitch.tv/kraken/channels/"
							+ getChannel().substring(1) + "/commercial",
					"length=" + commercialLength, 2);

		} else {
			System.out.println(getChannel().substring(1)
					+ " is not live. Skipping commercial.");
		}
	}

	public void setCooldown(int newCooldown) {
		cooldown = newCooldown;
		config.put("cooldown", newCooldown);
		saveConfig();
	}

	public long getCooldown() {

		return cooldown;
	}

	public void saveConfig() {
		try {

			FileWriter file = new FileWriter(channel + ".json");
			file.write(config.toJSONString());
			file.flush();
			file.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
