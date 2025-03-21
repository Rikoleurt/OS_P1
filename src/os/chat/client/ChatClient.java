package os.chat.client;


import java.io.IOException;
import java.io.ObjectInputStream;
import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Vector;

import os.chat.server.ChatServer;
import os.chat.server.ChatServerInterface;
import os.chat.server.ChatServerManager;
import os.chat.server.ChatServerManagerInterface;
import java.io.Serializable;

/**
 * This class implements a chat client that can be run locally or remotely to
 * communicate with a {@link ChatServer} using RMI.
 */
public class ChatClient implements CommandsFromWindow, CommandsFromServer {

	/**
	 * The name of the user of this client
	 */
	private String userName;

  /**
   * The graphical user interface, accessed through its interface. In return,
   * the GUI will use the CommandsFromWindow interface to call methods to the
   * ChatClient implementation.
   */
	private CommandsToWindow window ; // Transient to meet serialization constraints

  /**
   * Constructor for the <code>ChatClient</code>. Must perform the connection to the
   * server. If the connection is not successful, it must exit with an error.
   *
   * @param window reference to the GUI operating the chat client
   * @param userName the name of the user for this client
   * @since Q1
   */

  ChatServerManagerInterface csm;
  Registry registry;
  private CommandsFromServer stub;
  String IP = "172.20.10.3";


	public ChatClient(CommandsToWindow window, String userName) {
		this.window = window;
		this.userName = userName;

		try {
			System.setProperty("java.security.policy", "client.policy");
			registry = LocateRegistry.getRegistry(IP);
			csm = (ChatServerManagerInterface) registry.lookup("ChatServerManager");
		} catch (RemoteException e) {
			System.out.println("Can not locate registry");
			e.printStackTrace();
        } catch (NotBoundException e){
			System.out.println("Can not look up for ChatServerManager");
			e.printStackTrace();
		}
    }

	/*
	 * Implementation of the functions from the CommandsFromWindow interface.
	 * See methods description in the interface definition.
	 */

	/**
	 * Sends a new <code>message</code> to the server to propagate to all clients
	 * registered to the chat room <code>roomName</code>.
	 * @param roomName the chat room name
	 * @param message the message to send to the chat room on the server
	 */
	public void sendText(String roomName, String message) {

		/*
		 * TODO implement the method to send the message to the server.
		 */

		try {
			ChatServerInterface chatServer = (ChatServerInterface) registry.lookup("room " + roomName);
			if (chatServer != null) {
				System.out.println(window);
				chatServer.publish(message, userName);
			} else {
				System.out.println("Error : Not connected to chat's server !");
			}
		} catch (RemoteException | NotBoundException e) {
			System.out.println("Error while sending the message to the chat server");
			e.printStackTrace();
		}
	}

	/**
	 * Retrieves the list of chat rooms from the server (as a {@link Vector}
	 * of {@link String}s)
	 * @return a list of available chat rooms or an empty Vector if there is
	 * none, or if the server is unavailable
	 * @see Vector
	 */
	public Vector<String> getChatRoomsList() {

		// DONE

		try {
			return csm.getRoomsList();
		} catch (RemoteException e) {
			System.out.println("Can not call ChatServerManager.getRoomsList()");
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Join the chat room. Does not leave previously joined chat rooms. To
	 * join a chat room we need to know only the chat room's name.
	 * @param name the name (unique identifier) of the chat room
	 * @return <code>true</code> if joining the chat room was successful,
	 * <code>false</code> otherwise
	 */
	public boolean joinChatRoom(String roomName) {
		try {
			ChatServerInterface chatServer = (ChatServerInterface) registry.lookup("room " + roomName);
			if (stub == null) {
				stub = (CommandsFromServer) UnicastRemoteObject.exportObject(this, 0);
			}
			chatServer.register(stub);
			return true;
		} catch (RemoteException | NotBoundException e) {
			System.out.println("Can not join room :  " + roomName);
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * Leaves the chat room with the specified name
	 * <code>roomName</code>. The operation has no effect if has not
	 * previously joined the chat room.
	 * @param roomName the name (unique identifier) of the chat room
	 * @return <code>true</code> if leaving the chat room was successful,
	 * <code>false</code> otherwise
	 */
	public boolean leaveChatRoom(String roomName) {
		try {
			ChatServerInterface chatServer = (ChatServerInterface) registry.lookup("room " + roomName);
			if (stub == null) {
				stub = (CommandsFromServer) UnicastRemoteObject.exportObject(this, 0);
			}
			chatServer.unregister(stub);
			return true;
		} catch (RemoteException | NotBoundException e) {
			System.out.println("Can not leave room : " + roomName + ", cause : " + e.getMessage());
            e.printStackTrace();
        }
        return false;
	}

    /**
     * Creates a new room named <code>roomName</code> on the server.
     * @param roomName the chat room name
     * @return <code>true</code> if chat room was successfully created,
     * <code>false</code> otherwise.
     */
	public boolean createNewRoom(String roomName) {
		/*
		 * TODO implement the method to ask the server to create a new room (second part of the assignment only).
		 */
		try {
			csm.createRoom(roomName);
			System.out.println("Room : " + roomName + " created");
			return true;
		} catch (RemoteException e) {
			System.out.println("Can not create new room" + roomName + " : " + e.getMessage());
            throw new RuntimeException(e);
		}
	}

	/*
	 * Implementation of the functions from the CommandsFromServer interface.
	 * See methods description in the interface definition.
	 */


	/**
	 * Publish a <code>message</code> in the chat room <code>roomName</code>
	 * of the GUI interface. This method acts as a proxy for the
	 * {@link CommandsToWindow#publish(String chatName, String message)}
	 * interface i.e., when the server calls this method, the {@link
	 * ChatClient} calls the 
	 * {@link CommandsToWindow#publish(String chatName, String message)} method 
	 * of it's window to display the message.
	 * @param roomName the name of the chat room
	 * @param message the message to display
	 */
	public void receiveMsg(String roomName, String message) throws NullPointerException {
		try {
			System.out.println("Message received: " + message);
			window.publish(roomName, message);
		} catch (NullPointerException e) {
			System.out.println("Error in receiveMsg(): " + e.getMessage());
			e.printStackTrace();
		}
	}

	// This class does not contain a main method. You should launch the whole program by launching ChatClientWindow's main method.
}
