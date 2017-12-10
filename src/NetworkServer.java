
import java.awt.Color;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class NetworkServer {
		
	public static final Color[] colors = new Color[]{Color.white, Color.cyan, Color.blue, Color.green, Color.red};
	public static final String[] strings = new String[]{"Andrew", "Crumpet", "Sack", "Suck", "Cuck", "Truck"};
	
	private static int playersPerGame = 3;
	public static ArrayList<Player> players = new ArrayList<>();
	public static ArrayList<Room> rooms = new ArrayList<>();
	private static Thread input;
	
	public static void main(String[] args) {
		playersPerGame = Integer.parseInt(args[0]);
		input = new Thread(new Runnable() {
			@Override
			public void run() {
				Scanner sc = new Scanner(System.in);
				String input = sc.nextLine();
				
				while (input != "") {
					input = sc.nextLine();
					System.out.println("Input: " + input);
					
					if (input.equals("rooms")) {
						System.out.println(rooms.size());
					} else if (input.equals("players")) {
						System.out.println(players.size());
					} else if (input.equals("threads")) {
						System.out.println(Thread.getAllStackTraces().keySet().size());
					}					
				}
				
				sc.close();
			}
		});
		input.start();
		
        ServerSocket serverSocket = null;
        Socket socket = null;
        int portNumber = 4444;
        System.out.println("Starting server...");

        try {
            serverSocket = new ServerSocket(portNumber);
        } catch (IOException e) {
        	System.out.println("Exception caught when trying to listen on port " + portNumber + " or listening for a connection");
	        System.out.println(e.getMessage());
            e.printStackTrace();
        }
        
        while (true) {
            try {
                socket = serverSocket.accept();
            } catch (IOException e) {
                System.out.println("I/O error: " + e);
            }
            
            Player newPlayer = new Player(socket);
            newPlayer.start();
	        players.add(newPlayer);
        	System.out.println("Created player, Added player to queue");
        	
        	if (players.size() >= playersPerGame) {
        		System.out.println("Creating room with " + Integer.toString(playersPerGame) + " players.");
        		
        		Room newRoom = new Room(players);
        		
        		for (Player player : players) {
        			player.setRoom(newRoom);
        		}
        		
        		rooms.add(newRoom);
        		players.clear();
        	}
        }
    }
    
}

