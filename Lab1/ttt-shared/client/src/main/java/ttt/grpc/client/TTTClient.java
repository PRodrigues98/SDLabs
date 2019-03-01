package ttt.grpc.client;

/* these are generated by the hello-world-server contract */
import ttt.grpc.TTT;
import ttt.grpc.TTTServiceGrpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class TTTClient {

	public static void main(String[] args) throws Exception {
		System.out.println(TTTClient.class.getSimpleName());

		// receive and print arguments
		System.out.printf("Received %d arguments%n", args.length);
		for (int i = 0; i < args.length; i++) {
			System.out.printf("arg[%d] = %s%n", i, args[i]);
		}

		// check arguments
		if (args.length < 2) {
			System.err.println("Argument(s) missing!");
			System.err.printf("Usage: java %s host port%n", TTTClient.class.getName());
			return;
		}

		final String host = args[0];
		final int port = Integer.parseInt(args[1]);
		final String target = host + ":" + port;

		// Channel is the abstraction to connect to a service endpoint
		// Let us use plaintext communication because we do not have certificates
		final ManagedChannel channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();

		// It is up to the client to determine whether to block the call
		// Here we create a blocking stub, but an async stub,
		// or an async stub with Future are always possible.
		TTTServiceGrpc.TTTServiceBlockingStub stub = TTTServiceGrpc.newBlockingStub(channel);

		playGame(stub);

		// A Channel should be shutdown before stopping the process.
		channel.shutdownNow();
	}


	public static void playGame(TTTServiceGrpc.TTTServiceBlockingStub stub) {
		int play;
		boolean playAccepted;
		Game game = new Game();
		int player = 1;
		int winner;

		do {
			player = ++player % 2;

			do {

				TTT.CurrentBoardRequest requestBoard = TTT.CurrentBoardRequest.newBuilder().build();
				TTT.CurrentBoardResponse responseBoard = stub.currentBoard(requestBoard);

				System.out.println(responseBoard.getCurrentBoard());

				play = game.readPlay(player);

				if(play != 0){

					TTT.PlayRequest.Builder requestPlay = TTT.PlayRequest.newBuilder();

					requestPlay.setRow(--play / 3);
					requestPlay.setColumn(play % 3);
					requestPlay.setPlayer(player);
					

					TTT.PlayResponse responsePlay = stub.play(requestPlay.build());

					playAccepted = responsePlay.getValidPlay();

					if (!playAccepted){
						System.out.println("Invalid play! Try again.");
					}

				} 
				else{
					System.out.println("You tried to clean the board!");

					playAccepted = false;
				}

			} while (!playAccepted);

			TTT.CheckWinnerRequest requestWinner = TTT.CheckWinnerRequest.newBuilder().build();
			TTT.CheckWinnerResponse responseWinner = stub.checkWinner(requestWinner);

			winner = responseWinner.getCurrentResult();

		} while (winner == -1);

		game.congratulate(winner);
	}

}
