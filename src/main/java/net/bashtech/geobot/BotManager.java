package net.bashtech.geobot;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import net.bashtech.geobot.gui.BotGUI;
import net.bashtech.geobot.modules.BotModule;
import net.bashtech.geobot.modules.Logger;

import org.jibble.pircbot.IrcException;
import org.jibble.pircbot.NickAlreadyInUseException;

public class BotManager {
	
	static BotManager instance;
	
	String nick;
	String server;
	int port;
	String password;
	String network;
	boolean publicJoin;
	
	boolean monitorPings;
	int pingInterval;
	
	boolean useGUI;
	BotGUI gui;
	
	Map<String,Bot> botList;
	Map<String,Channel> channelList;
	
	Set<String> admins;
	
	private Timer pjTimer;
	
	private PropertiesFile config;
	
	private Set<BotModule> modules;
	
	

	public BotManager(){
		BotManager.setInstance(this);
		botList = new HashMap<String,Bot>();
		channelList = new HashMap<String,Channel>();
		admins = new HashSet<String>();
		modules = new HashSet<BotModule>();
		
		loadGlobalProfile();
		
		if(useGUI){
			gui = new BotGUI();
		}
		
		botList.put(server, new Bot(this, server, port));
		
		for (Map.Entry<String, Channel> entry : channelList.entrySet())
		{	
			System.out.println("DEBUG: Checking for bot on server " + entry.getValue().getServer());
			if(botList.containsKey(entry.getValue().getServer())){
				System.out.println("DEBUG: Joining channel " + entry.getValue().getChannel() + " NO CREATE.");
				botList.get(entry.getValue().getServer()).joinChannel(entry.getValue().getChannel());
				System.out.println("DEBUG: Joined channel " + entry.getValue().getChannel());

			}else{
				System.out.println("DEBUG: Joining channel " + entry.getValue().getChannel() + " CREATE");
				botList.put(entry.getValue().getServer(), new Bot(this, entry.getValue().getServer(), entry.getValue().getPort()));
				botList.get(entry.getValue().getServer()).joinChannel(entry.getValue().getChannel());
				System.out.println("DEBUG: Joined channel " + entry.getValue().getChannel());
			}
		}
			
		
		
		Timer reconnectTimer = new Timer();
		reconnectTimer.scheduleAtFixedRate(new ReconnectTimer(botList), 30 * 1000, 30 * 1000);
		System.out.println("Reconnect timer scheduled.");
		
		this.autoPartandRejoin();
		
		// Load modules
		this.registerModule(new Logger());
	}
	
	private synchronized void loadGlobalProfile(){
		config = new PropertiesFile("global.properties");
		try {
			config.load();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if(!config.keyExists("nick")) {
			config.setString("nick", "ackbot");
		}
		if(!config.keyExists("server")) {
			config.setString("server", "");
		}
		
		if(!config.keyExists("password")) {
			config.setString("password", "");
		}
		
		if(!config.keyExists("port")) {
			config.setInt("port", 6667);
		}
		
		if(!config.keyExists("channelList")) {
			config.setString("channelList", "");
		}
		
		if(!config.keyExists("adminList")) {
			config.setString("adminList", "");
		}
		
		if(!config.keyExists("network")) {
			config.setString("network", "jtv");
		}
		
		if(!config.keyExists("publicJoin")) {
			config.setBoolean("publicJoin", false);
		}
		
		if(!config.keyExists("monitorPings")) {
			config.setBoolean("monitorPings", false);
		}
		
		if(!config.keyExists("pingInterval")) {
			config.setInt("pingInterval", 350);
		}
		
		if(!config.keyExists("useGUI")) {
			config.setBoolean("useGUI", false);
		}
		
		nick = config.getString("nick");
		server = config.getString("server");
		network = config.getString("network");
		port = Integer.parseInt(config.getString("port"));
		
		password = config.getString("password");
		
		useGUI = config.getBoolean("useGUI");
		
		monitorPings = config.getBoolean("monitorPings");
		pingInterval = config.getInt("pingInterval");

		publicJoin = config.getBoolean("publicJoin");
		
		for(String s:config.getString("channelList").split(",")) {
			System.out.println("DEBUG: Adding channel " + s);
			if(s.length() > 1){
				channelList.put(s.toLowerCase(),new Channel(s));
			}
		}
		
		for(String s:config.getString("adminList").split(",")) {
			System.out.println("DEBUG: Adding admin " + s);
			if(s.length() > 1){
				admins.add(s.toLowerCase());
			}
		}
		
		if(server.length() < 1){
			System.exit(1);
		}
	}
	
	public Channel getChannel(String channel){
		if(channelList.containsKey(channel.toLowerCase())){
			System.out.println("DEBUG: Matched channel");

			return channelList.get(channel.toLowerCase());
		}else{
			return null;
		}
	}
	
	public boolean isAdmin(String nick){
		if(admins.contains(nick.toLowerCase()))
			return true;
		else
			return false;
	}
	
	public boolean addChannel(String name, String server2, int mode){
		if(channelList.containsKey(name.toLowerCase())){
			System.out.println("INFO: Already in channel " + name);
			return false;
		}
		Channel tempChan = new Channel(name,server2);
		
		channelList.put(name, tempChan);

		
		if(botList.containsKey(tempChan.getServer())){
			System.out.println("DEBUG: Joining channel " + tempChan.getChannel() + " NO CREATE.");
			botList.get(tempChan.getServer()).joinChannel(tempChan.getChannel());
			System.out.println("DEBUG: Joined channel " + tempChan.getChannel());

		}else{
			System.out.println("DEBUG: Joining channel " + tempChan.getChannel() + " CREATE");
			botList.put(tempChan.getServer(), new Bot(this,tempChan.getServer(), tempChan.getPort()));
			botList.get(tempChan.getServer()).joinChannel(tempChan.getChannel());
			System.out.println("DEBUG: Joined channel " + tempChan.getChannel());
		}
		
		
		writeChannelList();
		return true;
	}

	public void removeChannel(String name){
		if(!channelList.containsKey(name.toLowerCase())){
			System.out.println("INFO: Not in channel " + name);
			return;
		}
		
		Channel tempChan = channelList.get(name.toLowerCase());
		
		if(botList.containsKey(tempChan.getServer())){
			Bot tempBot = botList.get(tempChan.getServer());
			tempBot.partChannel(name);
		}
		
		channelList.remove(name.toLowerCase());
		
		
		writeChannelList();
	}
	
	public boolean rejoinChannel(String name){
		if(!channelList.containsKey(name.toLowerCase())){
			System.out.println("INFO: Not in channel " + name);
			return false;
		}
		
		Channel tempChan = channelList.get(name.toLowerCase());
		
		if(botList.containsKey(tempChan.getServer())){
			Bot tempBot = botList.get(tempChan.getServer());
			tempBot.partChannel(name);		
			try {
				Thread.currentThread().sleep(20000);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			tempBot.joinChannel(name);
		}
		
		return true;
	}
	
	public synchronized void rejoinChannels(){
		System.out.println("INFO: Rejoining channels");
		for (Map.Entry<String, Channel> entry : channelList.entrySet())
		{	
			if((entry.getValue().getGiveaway() != null && entry.getValue().getGiveaway().getStatus()) || (entry.getValue().getPoll() != null && entry.getValue().getPoll().getStatus()))
				continue;
			
			System.out.println("INFO: Parting channel " + entry.getValue().getChannel());
			botList.get(entry.getValue().getServer()).partChannel(entry.getValue().getChannel());
			System.out.println("INFO: Joining channel " + entry.getValue().getChannel());
			botList.get(entry.getValue().getServer()).joinChannel(entry.getValue().getChannel());
		}

	}
	
	
	public synchronized void reconnectAllBotsSoft(){
		for (Map.Entry<String, Bot> entry : botList.entrySet())
		{
			Bot temp = entry.getValue();
			System.out.println("INFO: Disconnecting " + temp.getServer());
			temp.disconnect();
			System.out.println("INFO: " + temp.getServer() + " disconnected.");
		}
	}
	
	@SuppressWarnings("static-access")
	public synchronized void reconnectAllBotsHard(){
		for (Map.Entry<String, Bot> entry : botList.entrySet())
		{
			Bot temp = entry.getValue();
			System.out.println("INFO: Disconnecting " + temp.getServer());
			temp.disconnect();
			System.out.println("INFO: " + temp.getServer() + " disconnected.");
		}
		System.out.println("DEBUG: Waiting....");
		try {
			Thread.currentThread().sleep(20000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		System.out.println("DEBUG: Done waiting. Kappa");
		for (Map.Entry<String, Bot> entry : botList.entrySet())
		{
			Bot temp = entry.getValue();
			
			if(temp.isConnected())
				continue;
			
			System.out.println("INFO: Reconnecting " + temp.getServer());
			try {
				temp.reconnect();
			} catch (NickAlreadyInUseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IrcException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("INFO: " + temp.getServer() + " reconnected.");
		}
		
		rejoinChannels();
	}
	
	private synchronized void writeChannelList(){
		String channelString = "";
		for (Map.Entry<String, Channel> entry : channelList.entrySet())
		{	
			channelString += entry.getKey() + ","; 
		}
		
		config.setString("channelList", channelString);
	}
	
	
	private void autoPartandRejoin(){
		
		pjTimer = new Timer();
		
		int delay = 1800000;
		
		pjTimer.scheduleAtFixedRate(new TimerTask()
	       {
	        public void run() {
	        	BotManager.this.rejoinChannels();
	        }
	      },delay,delay);

	}
	
	public static void setInstance(BotManager bm){
		if(instance == null){
			instance = bm;
		}
	}
	
	public static BotManager getInstance(){
		return instance;
	}
	
	public void registerModule(BotModule module){
		modules.add(module);
	}
	
	public Set<BotModule> getModules(){
		return modules;
	}
	
	public BotGUI getGUI(){
		return gui;
	}
	
	public void sendGlobal(String message, String sender){

		System.out.println("INFO: Sending global message: " + message);
		for (Map.Entry<String, Bot> entry : botList.entrySet())
		{	
			Bot tempbot = (Bot)entry.getValue();

			for(String channel: tempbot.getChannels()){
				String globalMsg = "> Global: " + message + " (from " + sender + " to " + channel + ")";
				tempbot.sendMessage(channel, globalMsg);
			}
		}
	}
	
	
}