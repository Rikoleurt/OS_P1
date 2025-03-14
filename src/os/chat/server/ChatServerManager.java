package os.chat.server;

import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Vector;
import java.rmi.registry.Registry;

/**
 * This class manages the available {@link ChatServer}s and available rooms.
 * <p>
 * At first you should not modify its functionalities but only export
 * them for being called by the {@link ChatClient}.
 * <p>
 * Later you will modify this to allow creating new rooms and
 * looking them up from the {@link ChatClient}.
 */
public class ChatServerManager implements ChatServerManagerInterface {

    /**
     * NOTE: technically this vector is redundant, since the room name can also
     * be retrieved from the chat server vector.
     */
	private Vector<String> chatRoomsList;

	private Vector<ChatServer> chatRooms;

    private static ChatServerManager instance = null;
	private Registry registry;

	/**
	 * Constructor of the <code>ChatServerManager</code>.
	 * <p>
	 * Must register its functionalities as stubs to be called from RMI by
	 * the {@link ChatClient}.
	 */
	public ChatServerManager () {

		chatRooms = new Vector<>();
		chatRoomsList = new Vector<>();

		// initial: we create a single chat room and the corresponding ChatServer

		/*
		 * TODO register the server manager object as a "ChatServerManager" on the RMI registry
		 * so it can be called by clients.
		 */
		try {
			ChatServerManagerInterface stub = (ChatServerManagerInterface) UnicastRemoteObject.exportObject(this,0);
			registry = LocateRegistry.getRegistry("127.0.0.1");
			registry.rebind("ChatServerManager", stub);
		} catch (RemoteException e) {
			System.out.println("Can not export the object");
			e.printStackTrace();
        }
		System.out.println("ChatServerManager was created");

		try {
			ChatServer chatRoom = new ChatServer("sports");
			chatRooms.add(chatRoom);
			chatRoomsList.add("sports");

			registry.rebind("sports", chatRoom);
		} catch (RemoteException e) {
			System.out.println("Error registering chat room 'sports'");
			e.printStackTrace();
		}
    }

    /**
     * Retrieves the chat server manager instance. This method creates a
     * singleton chat server manager instance if none was previously created.
     * @return a reference to the singleton chat server manager instance
     */
    public static ChatServerManager getInstance() {
	if (instance == null)
	    instance = new ChatServerManager();

	return instance;
    }

        /**
	 * Getter method for list of chat rooms.
	 * @return  a list of chat rooms
	 * @see Vector
	 */
	public Vector<String> getRoomsList() {
		return chatRoomsList;
	}

        /**
	 * Creates a chat room with a specified room name <code>roomName</code>.
	 * @param roomName the name of the chat room
	 * @return <code>true</code> if the chat room was successfully created,
	 * <code>false</code> otherwise.
	 */
		public boolean createRoom(String roomName) {
			try {
				chatRooms = new Vector<>();
				ChatServer chatRoom = new ChatServer(roomName);
				chatRooms.add(chatRoom);
				chatRoomsList.add(roomName);
				System.out.println("Room " + roomName + " created");
				return true;
			} catch (Exception e) {
				System.out.println("Can not create room '"+roomName+"'");
				e.printStackTrace();
			}
			return false;
		}


	public static void main(String[] args) {
		 try {
			 LocateRegistry.createRegistry(1099);
			 } catch (RemoteException e) {
			 System.out.println("Error: Can not create registry");
			 e.printStackTrace();
			 }
		 System.out.println("Registry was created");
		 getInstance();
		 }
}
