import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

public class Player extends Thread {

	public PrintWriter out;
	public boolean shouldGo = true;
	public String name;
	public Socket socket;
	private Room room = null;
	private Timer remindTimer = new Timer();
	private Timer endTimer = new Timer();
	
	public Player(Socket socket) {
		this.socket = socket;
		
		final Player me = this;
		
		remindTimer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				if (out != null) {
					out.println("Here?");
					System.out.println("Out: Here?");
					endTimer.schedule(new TimerTask() {
						@Override
						public void run() {
							
							if (room == null) 
							{
								System.out.println("Player " + name + " unresponsive, unqueuing...");
								NetworkServer.players.remove(me);
								remindTimer.cancel();
								try {
									socket.close();
								} catch (IOException e) {
									e.printStackTrace();
								}
								System.out.println("Player unqueued.");
								shouldGo = false;
							} 
							else 
							{
								System.out.println("Player " + name + " unresponsive, removing...");
								room.leavePlayer(me);
								remindTimer.cancel();
								System.out.println("Player removed.");
								shouldGo = false;
							}
						}
					}, 20*1000);
				}
			}
		}, 30*1000, 30*1000);
	}
	
	public void setRoom(Room room) {
		this.room = room;
		if (this.out != null) {
			this.room.playerConnected();
		}
	}
	
	@Override
	public void run() {
		String inputLine;
    	BufferedReader in = null;
		
		try {
	    	in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			this.out = new PrintWriter(socket.getOutputStream(), true);
		} catch (IOException e1) {
			System.out.println("Couldn't make streams");
			e1.printStackTrace();
		}
		
		if (this.room != null) {
			this.room.playerConnected();
		}
		
		while (shouldGo) {
			try {				
				inputLine = in.readLine();
				if ( inputLine != null ){
					System.out.println("In: " + inputLine);
					
					if (inputLine.equals("Yup%%#!%@")) {
						this.endTimer.cancel();
						this.endTimer = new Timer();
					} else if (room != null) {
						this.room.gotInput(this, inputLine);
					} else {
						this.name = inputLine;
					}
				}
			} catch (IOException e) {
				System.out.println("Failed read");
				shouldGo = false;
				if (this.room == null) {
					NetworkServer.players.remove(this);
					try {
						this.socket.close();
					} catch (IOException e1) {
						System.out.println("Couldn't D/C this player.");
						e1.printStackTrace();
					}
				} else {
					this.room.leavePlayer(this);
				}
				e.printStackTrace();
			}
		}
		try {
			this.socket.close();
		} catch (IOException e1) {
			//ignore, this is almost always OK
		}
		System.out.println("Player " + name + " disconnected.");
	}
	
}
