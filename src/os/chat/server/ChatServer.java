package os.chat.server;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Vector;

import os.chat.client.CommandsFromServer;

/**
 * Each instance of this class is a server for one room.
 * <p>
 * At first there is only one room server, and the names of the room available
 * is fixed.
 * <p>
 * Later you will have multiple room server, each managed by its own
 * <code>ChatServer</code>. A {@link ChatServerManager} will then be responsible
 * for creating and adding new rooms.
 */
public class ChatServer implements ChatServerInterface {
	
	private String roomName;
	private Vector<CommandsFromServer> registeredClients;
	private Registry registry;
	String IP = "172.20.10.3";
	
  /**
   * Constructs and initializes the chat room before registering it to the RMI
   * registry.
   * @param roomName the name of the chat room
   */
	public ChatServer(String roomName){
		this.roomName = roomName;
		registeredClients = new Vector<CommandsFromServer>();
		try {
			System.setProperty("java.security.policy", "server.policy");
			ChatServerInterface stub = (ChatServerInterface) UnicastRemoteObject.exportObject(this,0);
			registry = LocateRegistry.getRegistry(IP);
			registry.rebind("room " + roomName, stub);
			System.out.println("Room " + roomName + " rebounded");
		} catch (RemoteException e) {
			System.out.println("Can not export the object");
			e.printStackTrace();
		}
		System.out.println(roomName + " was created");
	}

	/**
	 * Publishes to all subscribed clients (i.e. all clients registered to a
	 * chat room) a message send from a client.
	 * @param message the message to propagate
	 * @param publisher the client from which the message originates
	 */
	public void publish(String message, String publisher) throws RemoteException {
		System.out.println("[" + roomName + "] " + publisher + ": " + message);

		for (CommandsFromServer client : registeredClients) {

			client.receiveMsg(roomName, "[" + publisher + "]: " + message);
		}
	}

	/**
	 * Registers a new client to the chat room.
	 * @param client the name of the client as registered with the RMI
	 * registry
	 */
	public void register(CommandsFromServer client) throws RemoteException {
		try {
			if (!registeredClients.contains(client)) {
				registeredClients.add(client);
				System.out.println("Client registered : " + client);
				System.out.println("Number of registered clients : " + registeredClients.size());
			}
		} catch (Exception e) {
			System.out.println("Error registering client: " + e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Unregisters a client from the chat room.
	 * @param client the name of the client as registered with the RMI
	 * registry
	 */
	public void unregister(CommandsFromServer client) throws RemoteException {
		try {
			if (registeredClients.contains(client)) {
				registeredClients.remove(client);
				System.out.println("Client unregistered: " + client);
			}
		} catch (Exception e) {
			System.out.println("Error unregistering client: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
}
