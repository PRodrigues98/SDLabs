package ttt;

import java.rmi.Remote;
import java.rmi.RemoteException;

/** Tic Tac Toe game remote interface. */
public interface TTTService extends Remote {
    
    /* TO-DO */
	String currentBoard() throws RemoteException;
	
	int checkWinner() throws RemoteException;
	
	boolean play(int row, int column, int player) throws RemoteException;

	void JogaCantoAleatorio() throws RemoteException;
}
