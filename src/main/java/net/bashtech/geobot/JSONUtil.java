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

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.*;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JSONUtil {

	// public static void krakenStreams() throws Exception{
	// JSONParser parser = new JSONParser();
	// Object obj =
	// parser.parse(BotManager.getRemoteContent("https://api.twitch.tv/kraken/streams/mlglol"));
	//
	// JSONObject jsonObject = (JSONObject) obj;
	//
	// JSONObject stream = (JSONObject)(jsonObject.get("stream"));
	// Long viewers = (Long)stream.get("viewers");
	// System.out.println("Viewers: " + viewers);
	// }

	public static Long krakenViewers(String channel) {
		try {
			JSONParser parser = new JSONParser();
			Object obj = parser.parse(BotManager
					.getRemoteContent("https://api.twitch.tv/kraken/streams/"
							+ channel));

			JSONObject jsonObject = (JSONObject) obj;

			JSONObject stream = (JSONObject) (jsonObject.get("stream"));
			if (stream == null)
				return (long) 0;

			Long viewers = (Long) stream.get("viewers");
			return viewers;
		} catch (Exception ex) {
			System.out.println("Kraken Viewers isn't working");
			return (long) 0;
		}

	}

	// public static Long jtvViewers(String channel) {
	// try {
	// JSONParser parser = new JSONParser();
	// Object obj = parser
	// .parse(BotManager
	// .getRemoteContent("http://api.justin.tv/api/stream/summary.json?channel="
	// + channel));
	//
	// JSONObject jsonObject = (JSONObject) obj;
	//
	// Long viewers = (Long) jsonObject.get("viewers_count");
	// return viewers;
	// } catch (Exception ex) {
	// ex.printStackTrace();
	// return (long) 0;
	// }
	//
	// }

	public static String getGameChannel(String gameName) {
		gameName = gameName.replaceAll(" ", "+");
		try {
			JSONParser parser = new JSONParser();
			Object obj = parser
					.parse(BotManager
							.getRemoteContent("https://api.twitch.tv/kraken/search/streams?q="
									+ gameName));

			JSONObject jsonObject = (JSONObject) obj;
			Long total = (Long) jsonObject.get("_total");
			if (total > 0) {
				JSONArray streams = (JSONArray) jsonObject.get("streams");
				int numStreams = streams.size();
				int randomChannel = (int) (Math.random() * (numStreams - 1));
				JSONObject stream = (JSONObject) streams.get(randomChannel);
				JSONObject channel = (JSONObject) stream.get("channel");
				String url = (String) channel.get("display_name");

				return url;
			} else
				return "No other channels playing this game";

		} catch (Exception ex) {
			ex.printStackTrace();
			return "Error Querying API";
		}
	}

	public static String xboxGamerScore(String gamertag) {
		try {
			JSONParser parser = new JSONParser();
			Object obj = parser
					.parse(BotManager
							.getRemoteContent("http://www.xboxleaders.com/api/games.json?gamertag="
									+ gamertag));

			JSONObject jsonObject = (JSONObject) obj;

			JSONObject data = (JSONObject) jsonObject.get("data");
			JSONObject gamerScore = (JSONObject) data.get("gamerscore");
			String gamerScoreInt = (String) gamerScore.get("current")
					.toString();
			return gamerScoreInt;
		} catch (Exception ex) {
			ex.printStackTrace();
			return "(unavailable)";
		}
	}

	public static String xboxLastGameProgress(String gamertag) {
		try {
			JSONParser parser = new JSONParser();
			Object obj = parser
					.parse(BotManager
							.getRemoteContent("http://www.xboxleaders.com/api/games.json?gamertag="
									+ gamertag));

			JSONObject jsonObject = (JSONObject) obj;
			JSONObject data = (JSONObject) jsonObject.get("data");

			JSONArray games = (JSONArray) data.get("games");
			JSONObject lastGame = (JSONObject) games.get(0);
			String percent = (String) lastGame.get("progress").toString();
			return percent;
		} catch (Exception ex) {
			ex.printStackTrace();
			return "(unavailable)";
		}
	}

	public static String xboxLastGame(String gamertag) {
		try {
			JSONParser parser = new JSONParser();
			Object obj = parser
					.parse(BotManager
							.getRemoteContent("http://www.xboxleaders.com/api/games.json?gamertag="
									+ gamertag));

			JSONObject jsonObject = (JSONObject) obj;
			JSONObject data = (JSONObject) jsonObject.get("data");
			JSONArray games = (JSONArray) data.get("games");
			JSONObject lastGame = (JSONObject) games.get(0);
			String title = (String) lastGame.get("title");
			return title;
		} catch (Exception ex) {
			ex.printStackTrace();
			return "(unavailable)";
		}

	}

	public static String getWiki(String query, int tries) {
		try {
			query = query.replaceAll(" ", "_");
			JSONParser parser = new JSONParser();
			Object obj = parser
					.parse(BotManager
							.getRemoteContent("http://en.wikipedia.org/w/api.php?action=query&prop=revisions&rvprop=content&rvsection=0&titles="
									+ query + "&format=json"));

			JSONObject jsonObject = (JSONObject) obj;

			JSONObject jsonquery = (JSONObject) jsonObject.get("query");
			JSONObject pages = (JSONObject) jsonquery.get("pages");
			Set<String> keys = (Set<String>) pages.keySet();
			ArrayList<String> keyList = new ArrayList<String>();
			for (String s : keys)
				keyList.add(s);

			JSONObject id = (JSONObject) pages.get(keyList.get(0));
			JSONArray revisions = (JSONArray) id.get("revisions");
			JSONObject index0 = (JSONObject) revisions.get(0);

			String content = (String) index0.get("*");
			if (content.contains("#REDIRECT") && tries < 3) {
				String newSearch = content.substring(content.indexOf("[") + 2,
						content.indexOf("]"));

				return getWiki(newSearch, tries++);
			}

			// content = content.replaceAll("\\[", "");
			// content = content.replaceAll("\\]", "");
			// content = content.replaceAll("\\|", "");
			content = content.replaceAll("\\\\", "");
			content = content.replaceAll("\\{", "");
			content = content.replaceAll("\\}", "");
			int start = content.indexOf("'''");
			content = content.substring(start + 3);
			content = content.replaceAll("'''", "");
			int refIndex = content.indexOf("<ref");
			int refEndIndex = content.indexOf("</ref>");
			int commentIndex = content.indexOf("<!--");
			int commentEndIndex = content.indexOf("-->");
			String firstPart;
			String lastPart;

			while (refIndex > 0) {

				firstPart = content.substring(0, refIndex);
				lastPart = content.substring(refEndIndex + 6);
				content = firstPart + lastPart;
				refIndex = content.indexOf("<ref");
				refEndIndex = content.indexOf("</ref>", refEndIndex + 1);
			}
			while (commentIndex > 0) {

				firstPart = content.substring(0, commentIndex);
				lastPart = content.substring(commentEndIndex + 3);
				content = firstPart + lastPart;
				commentIndex = content.indexOf("<!--");
				commentEndIndex = content.indexOf("-->");
			}
			Pattern r = Pattern
					.compile("\\[\\[(?:[^\\|\\]]*\\|)?([^\\]]+)\\]\\]");
			Matcher m = r.matcher(content);
			while (m.find()) {
				String match = m.group(1);
				String match0 = m.group();
				// System.out.println(match0);
				// System.out.println(match);
				content = content.replace(match0, match);

			}
			return content;
		} catch (Exception ex) {
			ex.printStackTrace();
			return "Unable to find that article.";
		}
	}

	// public static String jtvStatus(String channel) {
	// try {
	// JSONParser parser = new JSONParser();
	// Object obj = parser.parse(BotManager
	// .getRemoteContent("http://api.justin.tv/api/channel/show/"
	// + channel + ".json"));
	//
	// JSONObject jsonObject = (JSONObject) obj;
	//
	// String status = (String) jsonObject.get("status");
	// return status;
	// } catch (Exception ex) {
	// ex.printStackTrace();
	// return "Unable to query API";
	// }
	//
	// }

	public static String krakenStatus(String channel) {
		try {
			JSONParser parser = new JSONParser();
			Object obj = parser.parse(BotManager.getRemoteContentTwitch(
					"https://api.twitch.tv/kraken/channels/" + channel, 2));

			JSONObject jsonObject = (JSONObject) obj;

			String status = (String) jsonObject.get("status");

			if (status == null)
				status = "(Not set)";

			return status;
		} catch (Exception ex) {
			ex.printStackTrace();
			return "(Error querying API)";
		}

	}

	public static String krakenGame(String channel) {
		try {
			JSONParser parser = new JSONParser();
			Object obj = parser.parse(BotManager.getRemoteContentTwitch(
					"https://api.twitch.tv/kraken/channels/" + channel, 2));

			JSONObject jsonObject = (JSONObject) obj;

			String game = (String) jsonObject.get("game");

			if (game == null)
				game = "(Not set)";

			return game;
		} catch (Exception ex) {
			ex.printStackTrace();
			return "(Error querying API)";
		}

	}

	public static String getXKCDTitle(int number) {
		try {
			JSONParser parser = new JSONParser();
			Object obj = parser.parse(BotManager
					.getRemoteContent("http://xkcd.com/" + number
							+ "/info.0.json"));

			JSONObject jsonObject = (JSONObject) obj;
			String safe_title = (String) jsonObject.get("safe_title");

			return safe_title;

		} catch (Exception ex) {
			System.out.println("Failed to get chatters");
			return null;
		}
	}

	public static String getXKCDImage(int number) {
		try {
			JSONParser parser = new JSONParser();
			Object obj = parser.parse(BotManager
					.getRemoteContent("http://xkcd.com/" + number
							+ "/info.0.json"));

			JSONObject jsonObject = (JSONObject) obj;
			String imagelink = (String) jsonObject.get("img");

			return imagelink;

		} catch (Exception ex) {
			System.out.println("Failed to get chatters");
			return null;
		}
	}

	public static String getXKCDAltText(int number) {
		try {
			JSONParser parser = new JSONParser();
			Object obj = parser.parse(BotManager
					.getRemoteContent("http://xkcd.com/" + number
							+ "/info.0.json"));

			JSONObject jsonObject = (JSONObject) obj;
			String alt = (String) jsonObject.get("alt");

			return alt;

		} catch (Exception ex) {
			System.out.println("Failed to get chatters");
			return null;
		}
	}

	public static String getRace(String channel) {
		String raceID = "";

		Set<String> entrantSet = new HashSet<String>();
		try {
			JSONParser parser = new JSONParser();
			Object obj = parser.parse(BotManager
					.getRemoteContent("http://api.speedrunslive.com:81/races"));

			JSONObject jsonObject = (JSONObject) obj;
			int count = Integer.parseInt((String) jsonObject.get("count"));
			JSONArray raceList = (JSONArray) jsonObject.get("races");
			// JSONObject[] raceArr= (JSONObject[]) raceList.toArray();
			// BotManager.getInstance().log(raceList.toJSONString());

			for (int i = 0; i < count; i++) {
				JSONObject races = (JSONObject) raceList.get(i);
				String id = (String) races.get("id");

				JSONObject entrants = (JSONObject) races.get("entrants");
				Set<String> entrantNames = entrants.keySet();
				String entrantsString = entrants.toJSONString();
				if (entrantsString.contains(channel)) {
					raceID = id;
					entrantSet.add(channel);
					for (String s : entrantNames) {

						JSONObject entrant = (JSONObject) entrants.get(s);
						String entranttwitch = (String) entrant.get("twitch");
						if (entrantSet.size() < 5) {
							entrantSet.add(entranttwitch);
						}

					}
				}

			}
			if (entrantSet.size() > 0) {
				String raceLink = "http://speedrun.tv/race:" + raceID;
				for (String s : entrantSet) {
					raceLink = raceLink + "/" + s;
				}
				return raceLink;
			} else {
				return null;
			}

		} catch (Exception ex) {
			System.out.println("Failed to get races.");
			return null;
		}
	}

	public static ArrayList<String> tmiChatters(String channel) {
		try {
			JSONParser parser = new JSONParser();
			Object obj = parser.parse(BotManager
					.getRemoteContent("https://tmi.twitch.tv/group/user/"
							+ channel + "/chatters"));

			JSONObject jsonObject = (JSONObject) obj;
			JSONObject chatters = (JSONObject) jsonObject.get("chatters");
			JSONArray viewers = (JSONArray) chatters.get("viewers");
			JSONArray moderators = (JSONArray) chatters.get("moderators");
			for (int i = 0; i < moderators.size(); i++) {
				viewers.add(moderators.get(i));
			}

			return viewers;

		} catch (Exception ex) {
			System.out.println("Failed to get chatters");
			return null;
		}
	}

	public static Long tmiChattersCount(String channel) {
		try {
			JSONParser parser = new JSONParser();
			Object obj = parser.parse(BotManager
					.getRemoteContent("https://tmi.twitch.tv/group/user/"
							+ channel + "/chatters"));

			JSONObject jsonObject = (JSONObject) obj;
			Long chatterCount = (Long) jsonObject.get("chatter_count");

			return chatterCount;

		} catch (Exception ex) {
			System.out.println("Failed to get chatters");
			return (long) 0;
		}
	}

	// public static String defineWord(String word) {
	// String returned = BotManager
	// .getRemoteContent("http://www.dictionaryapi.com/api/v1/references/collegiate/xml/"
	// + word + "?key=21f734f4-3d5f-4266-9c20-d6be5909c81a");
	// int start = returned.indexOf(":");
	// if (start > -1) {
	// int end = returned.indexOf("</", start + 1);
	// returned = "\"" + returned.substring(start + 1, end) + "\"";
	// return returned;
	// } else {
	// start = returned.indexOf("<suggestion>");
	// if (start > -1) {
	// int end = returned.indexOf("</", start);
	// returned =
	// "Couldn't find a definition for that word, maybe try this one: \""
	// + returned.substring(start + 12, end) + "\"";
	// return returned;
	// } else
	// return "Couldn't find any results, sorry";
	//
	// }
	//
	// }

	public static String defineUrban(String word) {

		try {
			JSONParser parser = new JSONParser();
			Object obj = parser
					.parse(BotManager
							.getRemoteContent("http://api.urbandictionary.com/v0/define?term="
									+ word));

			JSONObject jsonObject = (JSONObject) obj;
			JSONArray items = (JSONArray) jsonObject.get("list");
			JSONObject items0 = (JSONObject) items.get(0);
			String title = (String) items0.get("definition");

			return title;

		} catch (Exception ex) {

			return "Couldn't find any results, sorry";
		}

	}

	public static String youtubeTitle(String id) {
		String api_key = BotManager.getInstance().YoutubeAPIKey;
		try {
			JSONParser parser = new JSONParser();
			Object obj = parser
					.parse(BotManager
							.getRemoteContent("https://www.googleapis.com/youtube/v3/videos?id="
									+ id + "&key=" + api_key + "&part=snippet"));

			JSONObject jsonObject = (JSONObject) obj;
			JSONArray items = (JSONArray) jsonObject.get("items");
			JSONObject items0 = (JSONObject) items.get(0);
			JSONObject snippet = (JSONObject) items0.get("snippet");
			String title = (String) snippet.get("title");

			return title;

		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	public static String lastFM(String user) {
		String api_key = BotManager.getInstance().LastFMAPIKey;
		String lastSong = null;
		try {
			JSONParser parser = new JSONParser();
			Object obj = parser
					.parse(BotManager
							.getRemoteContent("http://ws.audioscrobbler.com/2.0/?method=user.getrecenttracks&user="
									+ user
									+ "&format=json&limit=1&api_key="
									+ api_key));

			JSONObject jsonObject = (JSONObject) obj;

			JSONObject recenttracks = (JSONObject) (jsonObject
					.get("recenttracks"));
			if (recenttracks.get("track") instanceof JSONArray) {
				JSONArray track = (JSONArray) recenttracks.get("track");

				JSONObject index0 = (JSONObject) track.get(0);
				String trackName = (String) index0.get("name");
				JSONObject artistO = (JSONObject) index0.get("artist");
				String artist = (String) artistO.get("#text");
				lastSong = trackName + " by " + artist;
				String url = (String) index0.get("url");

				return lastSong;

			} else {
				if (lastSong == null)
					return "(Nothing)";
				else
					return lastSong;
			}
		} catch (Exception ex) {

			return "(Error querying API)";
		}

	}

	public static String lastSongLastFM(String user) {
		String api_key = BotManager.getInstance().LastFMAPIKey;
		String lastSong = null;
		try {
			JSONParser parser = new JSONParser();
			Object obj = parser
					.parse(BotManager
							.getRemoteContent("http://ws.audioscrobbler.com/2.0/?method=user.getrecenttracks&user="
									+ user
									+ "&format=json&limit=2&api_key="
									+ api_key));

			JSONObject jsonObject = (JSONObject) obj;

			JSONObject recenttracks = (JSONObject) (jsonObject
					.get("recenttracks"));
			if (recenttracks.get("track") instanceof JSONArray) {
				JSONArray track = (JSONArray) recenttracks.get("track");

				JSONObject index0 = (JSONObject) track.get(1);
				String trackName = (String) index0.get("name");
				JSONObject artistO = (JSONObject) index0.get("artist");
				String artist = (String) artistO.get("#text");
				lastSong = trackName + " by " + artist;
				String url = (String) index0.get("url");

				return lastSong;

			} else {
				if (lastSong == null)
					return "(Nothing)";
				else
					return lastSong;
			}
		} catch (Exception ex) {

			return "(Error querying API)";
		}

	}

	public static String lastFMURL(String user) {
		String api_key = BotManager.getInstance().LastFMAPIKey;
		String lastSong = null;
		try {
			JSONParser parser = new JSONParser();
			Object obj = parser
					.parse(BotManager
							.getRemoteContent("http://ws.audioscrobbler.com/2.0/?method=user.getrecenttracks&user="
									+ user
									+ "&format=json&limit=1&api_key="
									+ api_key));

			JSONObject jsonObject = (JSONObject) obj;

			JSONObject recenttracks = (JSONObject) (jsonObject
					.get("recenttracks"));
			if (recenttracks.get("track") instanceof JSONArray) {
				JSONArray track = (JSONArray) recenttracks.get("track");

				JSONObject index0 = (JSONObject) track.get(0);
				String url = (String) index0.get("url");

				return url;

			} else {
				if (lastSong == null)
					return "(Nothing)";
				else
					return lastSong;
			}
		} catch (Exception ex) {

			return "(Error querying API)";
		}

	}

	public static String whatShouldIPlay(String userID) {
		String api_key = BotManager.getInstance().SteamAPIKey;

		try {
			JSONParser parser = new JSONParser();
			Object obj = parser
					.parse(BotManager
							.getRemoteContent("http://api.steampowered.com/IPlayerService/GetOwnedGames/v0001/?key="
									+ api_key
									+ "&steamid="
									+ userID
									+ "&format=json&include_appinfo=1"));

			JSONObject jsonObject = (JSONObject) obj;

			JSONObject response = (JSONObject) (jsonObject.get("response"));
			JSONArray games = (JSONArray) response.get("games");

			if (games.size() > 0) {
				int randomGame = (int) (Math.random() * games.size() - 1);
				JSONObject index0 = (JSONObject) games.get(randomGame);
				String randomGameName = (String) index0.get("name");
				return randomGameName;
			} else {
				return "User has no games";

			}

		} catch (Exception ex) {
			System.out.println("Failed to query Steam API");
			return "Error querying API";
		}
	}

	public static String krakenCreated_at(String channel) {
		try {
			JSONParser parser = new JSONParser();
			Object obj = parser.parse(BotManager.getRemoteContentTwitch(
					"https://api.twitch.tv/kraken/streams/" + channel, 2));

			JSONObject jsonObject = (JSONObject) obj;

			JSONObject stream = (JSONObject) (jsonObject.get("stream"));
			if (stream == null)
				return "(offline)";

			String viewers = (String) stream.get("created_at");
			return viewers;
		} catch (Exception ex) {
			ex.printStackTrace();
			return "(error)";
		}

	}

	public static String steam(String userID, String retValues) {
		String api_key = BotManager.getInstance().SteamAPIKey;

		try {
			JSONParser parser = new JSONParser();
			Object obj = parser
					.parse(BotManager
							.getRemoteContent("http://api.steampowered.com/ISteamUser/GetPlayerSummaries/v0002/?steamids="
									+ userID + "&key=" + api_key));

			JSONObject jsonObject = (JSONObject) obj;

			JSONObject response = (JSONObject) (jsonObject.get("response"));
			JSONArray players = (JSONArray) response.get("players");

			if (players.size() > 0) {
				JSONObject index0 = (JSONObject) players.get(0);
				String profileurl = (String) index0.get("profileurl");
				String gameextrainfo = (String) index0.get("gameextrainfo");
				String gameserverip = (String) index0.get("gameserverip");
				String gameid = (String) index0.get("gameid");

				if (retValues.equals("profile"))
					return JSONUtil.googURL(profileurl);
				else if (retValues.equals("game"))
					return (gameextrainfo != null ? gameextrainfo
							: "(unavailable)");
				else if (retValues.equals("server"))
					return (gameserverip != null ? gameserverip
							: "(unavailable)");
				else if (retValues.equals("store"))
					return (gameid != null ? "http://store.steampowered.com/app/"
							+ gameid
							: "(unavailable)");
				else
					return "Profile: "
							+ JSONUtil.googURL(profileurl)
							+ (gameextrainfo != null ? ", Game: "
									+ gameextrainfo : "")
							+ (gameserverip != null ? ", Server: "
									+ gameserverip : "");

			} else {
				return "Error querying API";
			}
		} catch (Exception ex) {
			System.out.println("Failed to query Steam API");
			return "Error querying API";
		}
	}

	public static String googURL(String url) {
		try {

			JSONParser parser = new JSONParser();
			Object obj = parser.parse(BotManager.postDataLinkShortener(url));

			JSONObject jsonObject = (JSONObject) obj;
			String response = (String) jsonObject.get("id");
			return response;

		} catch (Exception ex) {
			ex.printStackTrace();
			return url;
		}
	}

	// public static String shortenURL(String url) {
	// String login = BotManager.getInstance().bitlyLogin;
	// String api_key = BotManager.getInstance().bitlyAPIKey;
	//
	// try {
	// String encodedURL = "";
	// try {
	// encodedURL = URLEncoder.encode(url, "UTF-8");
	// } catch (UnsupportedEncodingException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	//
	// JSONParser parser = new JSONParser();
	// Object obj = parser.parse(BotManager
	// .getRemoteContent("http://api.bitly.com/v3/shorten?login="
	// + login + "&apiKey=" + api_key + "&longUrl="
	// + encodedURL + "&format=json"));
	//
	// JSONObject jsonObject = (JSONObject) obj;
	// String status_txt = (String) jsonObject.get("status_txt");
	//
	// if (status_txt.equalsIgnoreCase("OK")) {
	// JSONObject data = (JSONObject) jsonObject.get("data");
	// String shortenedUrl = (String) data.get("url");
	// return shortenedUrl;
	// } else {
	// return url;
	// }
	// } catch (Exception ex) {
	// ex.printStackTrace();
	// return url;
	// }
	// }

	public static String urlEncode(String data) {
		try {
			data = URLEncoder.encode(data, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		return data;
	}

	public static boolean krakenIsLive(String channel) {
		try {
			JSONParser parser = new JSONParser();
			Object obj = parser.parse(BotManager.getRemoteContentTwitch(
					"https://api.twitch.tv/kraken/streams/" + channel, 2));

			JSONObject jsonObject = (JSONObject) obj;

			JSONObject stream = (JSONObject) (jsonObject.get("stream"));

			if (stream != null)
				return true;
			else
				return false;
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}

	}

	public static String BOIItemName(String searchTerm) {
		String urlEncoded = urlEncode(searchTerm);
		String searchURL = "http://bindingofisaac.wikia.com/api/v1/Search/List/?query="
				+ urlEncoded + "&limit=1";

		try {
			JSONParser parser = new JSONParser();
			Object obj = parser.parse(BotManager.getRemoteContent(searchURL));

			JSONObject jsonObject = (JSONObject) obj;

			JSONArray items = (JSONArray) jsonObject.get("items");
			JSONObject item = (JSONObject) items.get(0);
			String itemName = (String) item.get("title");
			return (itemName);
		} catch (Exception ex) {
			// ex.printStackTrace();
			return "";
		}
	}

	public static String BOIItem(String searchTerm) {
		String urlEncoded = urlEncode(searchTerm);
		String searchURL = "http://bindingofisaac.wikia.com/api/v1/Search/List/?query="
				+ urlEncoded + "&limit=1";

		try {
			JSONParser parser = new JSONParser();
			Object obj = parser.parse(BotManager.getRemoteContent(searchURL));

			JSONObject jsonObject = (JSONObject) obj;

			JSONArray items = (JSONArray) jsonObject.get("items");
			JSONObject item = (JSONObject) items.get(0);

			Long itemID = (Long) item.get("id");
			Object obj1 = parser
					.parse(BotManager
							.getRemoteContent("http://bindingofisaac.wikia.com/api/v1/Articles/Details/?ids="
									+ itemID + "&abstract=140"));
			JSONObject itemResponse = (JSONObject) obj1;
			JSONObject items1 = (JSONObject) itemResponse.get("items");
			JSONObject item1 = (JSONObject) items1.get(itemID + "");
			String description = (String) item1.get("abstract");

			return (description);
		} catch (Exception ex) {
			// ex.printStackTrace();
			return "Error fetching item data";
		}

	}

	public static String extraLifeAmount(String channel) {
		String source = BotManager
				.getInstance()
				.getRemoteContent(
						"http://www.extra-life.org/index.cfm?fuseaction=donorDrive.participant&participantID="
								+ BotManager.getInstance().getChannel(channel)
										.getExtraLifeID());
		int indexStart = source.indexOf("actualAmount=") + 13;
		int indexEnd = source.indexOf("&", indexStart);
		String amount = source.substring(indexStart, indexEnd);
		return amount;
	}

	public static boolean krakenChannelExist(String channel) {
		if (BotManager.getInstance().twitchChannels == false)
			return true;

		try {
			JSONParser parser = new JSONParser();
			Object obj = parser.parse(BotManager.getRemoteContentTwitch(
					"https://api.twitch.tv/kraken/channels/" + channel, 2));

			JSONObject jsonObject = (JSONObject) obj;

			Long _id = (Long) jsonObject.get("_id");

			return (_id != null);
		} catch (Exception ex) {
			// ex.printStackTrace();
			return false;
		}

	}

	public static boolean krakenOutdatedChannel(String channel) {
		if (BotManager.getInstance().twitchChannels == false)
			return false;

		try {
			JSONParser parser = new JSONParser();
			Object obj = parser.parse(BotManager.getRemoteContentTwitch(
					"https://api.twitch.tv/kraken/channels/" + channel, 2));

			JSONObject jsonObject = (JSONObject) obj;

			Object statusO = jsonObject.get("status");
			Long status;
			if (statusO != null) {
				status = (Long) statusO;
				if (status == 422 || status == 404) {
					System.out.println("Channel " + channel
							+ " returned status: " + status
							+ ". Parting channel.");
					return true;
				}
			}

			String updatedAtString = (String) jsonObject.get("updated_at");
			// System.out.println("Time: " + updatedAtString);

			DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
			format.setTimeZone(java.util.TimeZone.getTimeZone("US/Pacific"));
			long differenceDay = 0;

			try {
				Date then = format.parse(updatedAtString);
				long differenceSec = (long) (System.currentTimeMillis() / 1000)
						- (then.getTime() / 1000);
				differenceDay = (long) (differenceSec / 86400);
			} catch (Exception exi) {
				exi.printStackTrace();
			}

			if (differenceDay > 30) {
				System.out.println("Channel " + channel + " not updated in "
						+ differenceDay + " days. Parting channel.");
				return true;
			}

		} catch (Exception ex) {
			return false;
		}

		return false;

	}

	public static Long updateTMIUserList(String channel, Set<String> staff,
			Set<String> admins, Set<String> mods) {
		try {
			JSONParser parser = new JSONParser();
			Object obj = parser.parse(BotManager
					.getRemoteContent("http://tmi.twitch.tv/group/user/"
							+ channel + "/chatters"));

			JSONObject jsonObject = (JSONObject) obj;

			Long chatter_count = (Long) jsonObject.get("chatter_count");

			JSONObject chatters = (JSONObject) jsonObject.get("chatters");

			JSONArray staffJO = (JSONArray) chatters.get("staff");
			for (Object user : staffJO) {
				staff.add((String) user);
			}

			JSONArray adminsJO = (JSONArray) chatters.get("admins");
			for (Object user : adminsJO) {
				admins.add((String) user);
			}

			JSONArray modsJO = (JSONArray) chatters.get("moderators");
			for (Object user : modsJO) {
				mods.add((String) user);
			}

			return chatter_count;
		} catch (Exception ex) {
			ex.printStackTrace();
			return new Long(-1);
		}

	}

	public static String highlightThat(String url) {

		String result = BotManager.getRemoteContent(url);

		return result;

	}

	// public static Double getSourceBitrate(String channel) {
	// try {
	// JSONParser parser = new JSONParser();
	// Object obj = parser
	// .parse(BotManager
	// .getRemoteContent("http://api.justin.tv/api/stream/list.json?channel="
	// + channel));
	//
	// JSONArray outerArray = (JSONArray) obj;
	//
	// if (outerArray.size() == 1) {
	// JSONObject channelObject = (JSONObject) outerArray.get(0);
	// Double bitrate = (Double) channelObject.get("video_bitrate");
	// return bitrate;
	// }
	//
	// return new Double(0);
	// } catch (Exception ex) {
	// ex.printStackTrace();
	// return new Double(0);
	// }
	//
	// }

	// public static String getSourceRes(String channel) {
	// try {
	// JSONParser parser = new JSONParser();
	// Object obj = parser
	// .parse(BotManager
	// .getRemoteContent("http://api.justin.tv/api/stream/list.json?channel="
	// + channel));
	//
	// JSONArray outerArray = (JSONArray) obj;
	//
	// if (outerArray.size() == 1) {
	// JSONObject channelObject = (JSONObject) outerArray.get(0);
	// Long width = (Long) channelObject.get("video_width");
	// Long height = (Long) channelObject.get("video_height");
	// return width + "x" + height;
	// }
	//
	// return "Unable to retrieve data";
	// } catch (Exception ex) {
	// ex.printStackTrace();
	// return "Unable to retrieve data";
	// }
	//
	// }

	public static String getChatProperties(String channel) {
		try {
			JSONParser parser = new JSONParser();
			Object obj = parser.parse(BotManager
					.getRemoteContent("http://api.twitch.tv/api/channels/"
							+ channel + "/chat_properties"));

			JSONObject jsonObject = (JSONObject) obj;

			Boolean hide_chat_links = (Boolean) jsonObject
					.get("hide_chat_links");
			Boolean devchat = (Boolean) jsonObject.get("devchat");
			Boolean eventchat = (Boolean) jsonObject.get("eventchat");
			Boolean require_verified_account = (Boolean) jsonObject
					.get("require_verified_account");

			String response = "Hide links: " + hide_chat_links
					+ ", Require verified account: " + require_verified_account;

			return response;
		} catch (Exception ex) {
			ex.printStackTrace();
			return "(Error querying API)";
		}

	}

	public static List<String> getEmotes() {
		List<String> emotes = new LinkedList<String>();
		try {
			JSONParser parser = new JSONParser();
			Object obj = parser
					.parse(BotManager
							.getRemoteContent("http://direct.twitchemotes.com/global.json"));

			JSONObject jsonObject = (JSONObject) obj;

			for (Object o : jsonObject.keySet()) {
				String name = (String) o;
				if (name.length() > 0)
					emotes.add(name);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			return emotes;
		}

	}

}
