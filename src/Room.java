import java.util.ArrayList;
import java.util.Collections;
import java.util.Timer;
import java.util.TimerTask;

public class Room {

	private ArrayList<Player> players;
	private int activePlayers;
	private int currentPlayer = 0;
	private int connected = 0;
	private Timer timer = new Timer();
	private int sinceLastMessage = 0;
	private static final int maxTime = 60*3;
	
	public Room(ArrayList<Player> players) {
		this.players = (ArrayList<Player>)players.clone();
		this.activePlayers = players.size();
	}
	
	public void playerConnected() {
		this.connected++;
		if (connected >= this.players.size()) {
			this.start();
		}
	}
	
	public void start() {
		System.out.println("Started room");
		
		boolean[][] golds = TerrainGenerator.generateTerrain(25, 25, 4, 0.15, 0.5);
		
		for (int i = 0; i < 25; i++) {
			for (int j = 0; j < 25; j++) {
				if (golds[i][j]) {
					String outString = i+"|"+j+"|"+"GoldResource:";
					for (Player player : players) {
			    		player.out.println(outString);
			    	}
				}
			}
		}
		
		final Room me = this;
		
		timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				sinceLastMessage++;
				if (sinceLastMessage > maxTime) {
					System.out.println("Closing this room...");
					
					for (Player player : players) {
						player.out.println("Leave|");
						player.shouldGo = false;
					}
					
					timer.cancel();
					NetworkServer.rooms.remove(me);
					
					System.out.println("Room removed, " + NetworkServer.rooms.size() + " rooms running now.");
				}
			}
		}, 0, 1000);
		
		sendOrder();
	}
	
	public void gotInput(Player player, String inputLine) {
		sinceLastMessage = 0;
	
		if (inputLine.indexOf('|') == -1) {
			player.name = inputLine;
			sendOrder();
			return;
		}
	
		String[] parts = inputLine.split("\\|");
		
		if (parts[0].equals("Done")) {
			
			currentPlayer++;
			if (currentPlayer >= activePlayers) {
				currentPlayer = 0;
			}
			
			for (Player sendPlayer : players) {
				String out = "Turn|";
    			sendPlayer.out.println(out);
            	System.out.println("Out to " + sendPlayer.name + ": " + out);
			}
			
		} else if (parts[0].equals("Spec")) { 
		
			spectatePlayer(player);
			
		} else if (parts[0].equals("Leave")) { 
		
			leavePlayer(player);
		
		} else {
		
	    	for (Player otherPlayer : players) {
	    		if (otherPlayer.socket != player.socket) {
	    			String out = inputLine + "|" + players.indexOf(player);
	    			otherPlayer.out.println(out);
	            	System.out.println("Out to " + otherPlayer.name + ": " + out);
	    		}
	    	}
		
		}
	}
	
	private void sendOrder() {
		for (Player player : players) {
			if (player.name == null) {
				return;
			}
		}
		
		Collections.shuffle(players);
		
		for (Player playerWithData : players) {
			for (Player sendTo : players) {
				String out;
				if (sendTo == playerWithData) {
					out = "Player|"+playerWithData.name+"|Yours";
				} else {
					out = "Player|"+playerWithData.name;
				}
				
				sendTo.out.println(out);
				System.out.println("Out to " + sendTo.name + ": " + out);
			}
		}
		
		for (Player sendTo : players) {
			String out = "AllSent|";
			sendTo.out.println(out);
			System.out.println("Out to " + sendTo.name + ": " + out);
		}
		
		String outString = "Turn|";
		for (Player sendPlayer : players) {
			sendPlayer.out.println(outString);
			System.out.println("Out to " + sendPlayer.name + ": " + outString);
		}
		
	}
	
	private void removePlayer(Player player, int playerIndex) {
		if (playerIndex < activePlayers) {
			activePlayers--;
		}
		
		if (playerIndex == currentPlayer) {
			players.remove(playerIndex);
			if (currentPlayer >= activePlayers) {
				currentPlayer = 0;
			}
			
			for (Player sendPlayer : players) {
				sendPlayer.out.println("Leave|"+player.name);
				sendPlayer.out.println("Back|");
				sendPlayer.out.println("Turn|");
			}
		} else {
			players.remove(playerIndex);
			if (playerIndex < currentPlayer) {
				currentPlayer--;
				for (Player sendPlayer : players) {
					sendPlayer.out.println("Leave|"+player.name);
					sendPlayer.out.println("Back|");
				}
			} else {
				for (Player sendPlayer : players) {
					sendPlayer.out.println("Leave|"+player.name);
				}
			}
		}
		
		if (activePlayers == 0) {
			timer.cancel();
			NetworkServer.rooms.remove(this);
			System.out.println("Room removed, " + NetworkServer.rooms.size() + " rooms running now.");
		}
	}
	
	public void leavePlayer(Player player) {
		int playerIndex = players.indexOf(player);
		if (playerIndex == -1) {
			player.shouldGo = true;
			return;
		}
		
		removePlayer(player, playerIndex);
		player.shouldGo = true;
	}
	
	public void spectatePlayer(Player player) {
		removePlayer(player, players.indexOf(player));
		players.add(player);
	}
	
}


