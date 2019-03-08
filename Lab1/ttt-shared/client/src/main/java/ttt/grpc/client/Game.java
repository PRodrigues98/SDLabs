package ttt.grpc.client;

import java.util.Scanner;

public class Game {
	Scanner keyboardSc;

	public Game() {
		keyboardSc = new Scanner(System.in);
	}

	public int readPlay(int player) {
		int play;
		do {
			System.out.printf(
					"\nPlayer %d, please enter the number of the square "
							+ "where you want to place your %c (or 0 to refresh the board, 10 to undo 2 plays): \n",
					player, (player == 1) ? 'X' : 'O');
			play = keyboardSc.nextInt();
		} while (play > 10 || play < 0);
		return play;
	}

	public void congratulate(int currentResult) {
		if (currentResult == 2)
			System.out.printf("\nHow boring, it is a draw\n");
		else
			System.out.printf("\nCongratulations, player %d, YOU ARE THE WINNER!\n", currentResult);
	}

}
