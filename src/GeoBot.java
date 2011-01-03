import java.io.IOException;

import org.jibble.pircbot.*;

public class GeoBot extends PircBot {
	private boolean isGlobalChannel = false;
	private GlobalChannel globalChannel;
	private Channel channelInfo;
	
	public GeoBot(GlobalChannel g, Channel c){
		globalChannel = g;
		channelInfo = c;
		this.setName(g.getNick());
	}
	
	public GeoBot(GlobalChannel g, boolean gCheck){
		isGlobalChannel = gCheck;
		globalChannel = g;
		this.setName(g.getNick());
	}
	
	public void onMessage(String channel, String sender, String login, String hostname, String message){
			
			String[] msg = split(message.trim());
			User user = matchUser(sender, channel);
			
			boolean isOp = false;
			try{
				if(user.getPrefix().equalsIgnoreCase("@"))
					isOp = true;
				if(user.isOp())
					isOp = true;
			}catch(Exception e){
				System.out.println("Prefix exception.");
			}

			if(sender.equalsIgnoreCase("#" + channel))
				isOp = true;
			
			if(isOp)
				System.out.println("User is op");
			
			if(!isGlobalChannel){
				//Normal channel stuff
				// !time - All
				if (message.trim().equalsIgnoreCase("!time")) {
						String time = new java.util.Date().toString();
						sendMessage(channel, sender + ": The time is now " + time);
						//return;
				}
				
				// !clear - Ops
				if(message.trim().equalsIgnoreCase("!clear") && isOp){
					this.sendMessage(channel, "/clear");
					//return;
				}
				
				// !topic
				if(msg[0].equalsIgnoreCase("!topic")){
					if(msg.length < 2 || !isOp){
						this.sendMessage(channel, "> Topic: " + channelInfo.getTopic());
					}else if(msg.length > 1 && isOp){
						channelInfo.setTopic(message.substring(7));
						this.sendMessage(channel, "> Topic: " + channelInfo.getTopic());
					}
					//return;
				}
				
				// !command - Sets commands
 				if(msg[0].equalsIgnoreCase("!command")){
					if(msg.length < 3 && isOp){
						this.sendMessage(channel, "> !command add/delete name string");
					}else if(msg.length > 2 && isOp){
						if(msg[1].equalsIgnoreCase("add")){
							String key = "!" + msg[2];
							String value = "";
							
							for(int i = 3; i < msg.length; i++){
								value = value + msg[i] + " ";
							}
							
							channelInfo.setCommand(key, value);
							this.sendMessage(channel, "> " + channelInfo.getCommand(key));
						}else if(msg[1].equalsIgnoreCase("delete")){
							String key = "!" + msg[2];
							channelInfo.removeCommand(key);	
							this.sendMessage(channel, "> Command " + key + " removed.");

							}
					}
				}
 				
 				// !links - Turns on/off link filter
 				if(msg[0].equalsIgnoreCase("!links") && isOp){
 					if(msg.length == 2){
 						if(msg[1].equalsIgnoreCase("on")){
 							channelInfo.setFilterLinks(true);
 							this.sendMessage(channel, "> Link filter: " + channelInfo.getFilterLinks());
 						}else if(msg[1].equalsIgnoreCase("off")){
 							channelInfo.setFilterLinks(false);
 							this.sendMessage(channel, "> Link filter: " + channelInfo.getFilterLinks());
 						}
 					}
 				}
				
 				// !caps - Turns on/off caps filter and sets limit.
 				if(msg[0].equalsIgnoreCase("!caps") && isOp){
 					if(msg.length > 1){
 						if(msg[1].equalsIgnoreCase("on")){
 							channelInfo.setFilterCaps(true);
 							this.sendMessage(channel, "> Caps filter: " + channelInfo.getFilterCaps());
 						}else if(msg[1].equalsIgnoreCase("off")){
 							channelInfo.setFilterCaps(false);
 							this.sendMessage(channel, "> Caps filter: " + channelInfo.getFilterCaps());
 						}else if(msg[1].equalsIgnoreCase("limit")){
 							if(msg.length > 2){
 								channelInfo.setFilterCapsLimit(Integer.parseInt(msg[2]));
 	 							this.sendMessage(channel, "> Caps filter limit: " + channelInfo.getFilterCapsLimit());
 							}
 						}
 					}
 				}
 				
 				// !regulars - Add regulars
 				if(msg[0].equalsIgnoreCase("!regular")){
 					if(msg.length  > 2 && isOp){
 						if(msg[1].equalsIgnoreCase("add")){
 							if(channelInfo.isRegular(msg[2])){
 								sendMessage(channel,"> User already exists.");
 							}else{
 								channelInfo.addRegular(msg[2]);
 								sendMessage(channel,"> User added.");
 							}
 						}else if(msg[1].equalsIgnoreCase("delete")){
 							if(channelInfo.isRegular(msg[2])){
 								channelInfo.removeRegular(msg[2]);
 								sendMessage(channel,"> User removed.");
 							}else{
 								sendMessage(channel,"> User does not exist.");
 							}
 						}
 					}
 				}
 				
 				if(msg[0].equalsIgnoreCase("!permit")){
 					if(msg.length > 1 && isOp){
 						channelInfo.permitUser(msg[1]);
 						sendMessage(channel, "> " + msg[1] + " may now post 1 link.");
 					}
 				}
 				
				// Cap filter
				if(channelInfo.getFilterCaps() && countCapitals(message) > channelInfo.getFilterCapsLimit() && !(isOp || channelInfo.isRegular(sender))){
					this.kick(channel, sender);
					//this.unBan(channel,sender + "!" + sender + "@*.*");
				}
				
				// Link filter
				if(channelInfo.getFilterLinks() && this.containsLink(message) && !(channelInfo.linkPermissionCheck(sender) || isOp )){
					this.kick(channel, sender);
				}
				
				//Command catch all
				if(message.substring(0,1).equalsIgnoreCase("!") && !channelInfo.getCommand(message).equalsIgnoreCase("invalid")){
					sendMessage(channel, "> " + channelInfo.getCommand(message));
				}
	
			}else{
				//Global channel stuff
				if (msg[0].equalsIgnoreCase("!join") && msg.length > 1 && isOp) {
					try {
						globalChannel.addChannel(msg[1]);
						sendMessage(channel, "Channel "+ msg[1] +" joined.");
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
				}
				
				if (msg[0].equalsIgnoreCase("!leave") && msg.length > 1 && isOp) {
						globalChannel.removeChannel(split(message)[1]);
						sendMessage(channel, "Channel "+ msg[1] +" parted.");
				}
			}
			
			
			
	}
	
	public User matchUser(String nick, String channel){
		User[] userList = this.getUsers(channel);
		
		for(int i = 0; i < userList.length; i++){
			if(userList[i].equals(nick)){
				return userList[i];
			}
		}
		return null;
		
	}
	
//#################################################################################
	
	public int countCapitals(String s){
		int caps = 0;
		int max = 0;
		//boolean con = true;
		for (int i=0; i<s.length(); i++)
		{
			if (Character.isUpperCase(s.charAt(i))){
					caps++;
			}else{
				if(caps > 0 && caps > max)
						max = caps;
				caps = 0;
			}
		}
		if(caps > max)
			return caps;
		else
			return max;
	}
	
	private boolean containsLink(String m){
		if(m.contains(".com") || m.contains(".org") || m.contains(".net") || m.contains(".tv")){
			return true;
		}
		
		return false;
	}
	
	private String[] split(String s){
		return s.split(" ");
	}
	
	public Channel getChannel(){
		return channelInfo;
	}

}
