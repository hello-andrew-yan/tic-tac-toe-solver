
/*********************************************************
 *  Agent.java
 *  Nine-Board Tic-Tac-Toe Agent
 *  COMP3411/9814 Artificial Intelligence
 *  CSE, UNSW
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;

public class Robot {

    static int[][] boards = new int[10][10];
    static int previous_board = 0;

    static final int EMPTY_CELL = 0;
    static final int AGENT_MARK = 1;
    static final int PLAYER_MARK = 2;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                                      UNSW UTILITY FUNCTION | IGNORE                                                //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.out.println("Usage: java Agent -p (port)");
            return;
        }

        final String host = "localhost";
        final int portNumber = Integer.parseInt(args[1]);

        Socket socket = new Socket(host, portNumber);
        BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

        String line;

        while (true) {
            line = br.readLine();
            int move = parse(line);

            if (move == -1) {
                socket.close();
                return;
            } else if (move == 0) {
                // No action, could output a debug message if we wanted
            } else {
                out.println(move);
            }
        }
    }

    /**
     * From the server, init() tells us that a new game is about to begin.
     * <p>
     * start(x) or start(o) tell us whether we will be playing first (x)
     * or second (o); we might be able to ignore start if we internally
     * use 'X' for *our* moves and 'O' for *opponent* moves.
     *
     * @param line Command line string
     * @return Number state depending on the command.
     */
    public static int parse(String line) {
        if (line.contains("init")) {
            // No action, could output a debug message if we wanted
        } else if (line.contains("start")) {
            // No action, could output a debug message if we wanted
        } else if (line.contains("second_move")) {
            int argsStart = line.indexOf("(");
            int argsEnd = line.indexOf(")");

            String list = line.substring(argsStart + 1, argsEnd);
            String[] numbers = list.split(",");

            // Place the first move (randomly generated for opponent)
            place(Integer.parseInt(numbers[0]), Integer.parseInt(numbers[1]), PLAYER_MARK);

            // Choose and return the second move
            return play();
        } else if (line.contains("third_move")) {
            int argsStart = line.indexOf("(");
            int argsEnd = line.indexOf(")");

            String list = line.substring(argsStart + 1, argsEnd);
            String[] numbers = list.split(",");

            // Place the first move (randomly generated for us) and second move (chosen by
            // opponent)
            place(Integer.parseInt(numbers[0]), Integer.parseInt(numbers[1]), AGENT_MARK);
            place(Integer.parseInt(numbers[1]), Integer.parseInt(numbers[2]), PLAYER_MARK);

            // Choose and return the third move
            return play();
        } else if (line.contains("next_move")) {
            int argsStart = line.indexOf("(");
            int argsEnd = line.indexOf(")");

            String list = line.substring(argsStart + 1, argsEnd);

            // Place the previous move (chosen by opponent)
            place(previous_board, Integer.parseInt(list), PLAYER_MARK);

            // Choose and return the next move
            return play();
        } else if (line.contains("last_move")) {
            // No action, could output a debug message if we wanted
        } else if (line.contains("win")) {
            // No action, could output a debug message if we wanted
        } else if (line.contains("loss")) {
            // No action, could output a debug message if we wanted
        } else if (line.contains("end")) {
            return -1;
        }
        return 0;
    }

    public static void place(int board, int index, int mark) {
        previous_board = index;
        boards[board][index] = mark;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                                          I M P L E M E N T A T I O N                                               //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static int play() {
        int bestMove = findBestMove();
        place(previous_board, bestMove, AGENT_MARK);

        return bestMove;
    }

    //////////////////////////////////////////////////////////
    //                   FIND BEST MOVE                     //
    //////////////////////////////////////////////////////////
    /**
     * Finds the best moves out of all the cells available
     * in the current board the game has assigned to the agent.
     * <p>
     * This function runs in O(b^m) time. Where b = 9 in a game
     * of 9-Board Tic-Tac-Toe and m = MAXIMUM_DEPTH.
     *
     * @return The index of the best move to
     *         go in this particular scenario
     */
    public static int findBestMove() {
        int bestScore = Integer.MIN_VALUE;
        int bestMove = 0;

        // Important to create a copy as we wil be testing a variety of local moves
        int[][] copiedBoards = copyBoards(boards);

        // Iterations must be indexed at 1 - 9.
        for (int i = 1; i < 10; i++) {
            // Skips cells that already have values set
            if (copiedBoards[previous_board][i] != EMPTY_CELL) continue;

            // The first layer of the Minimax Algorithm is called here, next is players turn indexed at 0
            copiedBoards[previous_board][i] = AGENT_MARK;

            // DEBUG STATEMENT
            System.out.println("Depth = \u001B[32m0\u001B[0m");
            printBoard(copiedBoards[previous_board]);

            int score = negamax(copiedBoards, i, STARTING_DEPTH, Integer.MIN_VALUE, Integer.MAX_VALUE, -1);
            copiedBoards[previous_board][i] = EMPTY_CELL;

            if (score > bestScore) {
                bestScore =  score;
                bestMove = i;
            }
        }

        return bestMove;
    }

    //////////////////////////////////////////////////////////
    //                 NEGAMAX ALGORITHM                    //
    //////////////////////////////////////////////////////////

    static int STARTING_DEPTH = 0;
    static int MAXIMUM_DEPTH = 1;

    public static int negamax(int[][] boards, int previous_board, int depth, int alpha, int beta, int colour) {
        /*
         * Evaluation statements and functions here
         * ---------------------------------------------
         * if (depth = 0 or node is a terminal node) {
         * return the heuristic value of node
         * }
         * ---------------------------------------------
         * Evauluation function is multiplied by the colour.
         */

        int mark = colour == 1 ? AGENT_MARK : PLAYER_MARK;
        int[][] copiedBoards = copyBoards(boards);


        // Terminal States of the negamax algorithm
        if (isWinning(copiedBoards[previous_board], mark)) {
            // TEMPORARY RETURN
            return 100000 + (MAXIMUM_DEPTH / depth + 1);
        }
        if (depth == MAXIMUM_DEPTH) {
            return evaluateBoard(copiedBoards[previous_board], colour);
        }

        int bestScore = Integer.MIN_VALUE;

        // Iterations must be indexed at 1 - 9.
        for (int i = 1; i < 10; i++) {
            // Skips cells that already have values set
            if (copiedBoards[previous_board][i] != EMPTY_CELL) continue;

            copiedBoards[previous_board][i] = mark;

            // DEBUG STATEMENT
            System.out.printf("Depth = " + (colour == 1 ? "\u001B[32m" : "\u001B[31m") + "%s\u001B[0m\n", depth + 1);
            printBoard(copiedBoards[previous_board]);

            int score = -negamax(copiedBoards, i, depth + 1, -beta, -alpha, -colour);
            copiedBoards[previous_board][i] = EMPTY_CELL;

            bestScore = Math.max(score, bestScore);

            // Alpha-Beta Pruning
            // alpha = max(score, localAlpha)
            // if (alpha >= beta) break;
        }
        return bestScore;
    }

    //////////////////////////////////////////////////////////
    //                EVAULATION FUNCTION                   //
    //////////////////////////////////////////////////////////

    public static int evaluateBoard(int[] board, int mark) {
        return 1;
    }

    //////////////////////////////////////////////////////////
    //                  UTILITY FUNCTIONS                   //
    //////////////////////////////////////////////////////////

    public static int[][] copyBoards(int[][] original) {
        return Arrays.stream(original).map(int[]::clone).toArray(int[][]::new);
    }

    static final int[][] winningCombinations = {
            {1, 2, 3}, {4, 5, 6}, {7, 8, 9},  // Horizontal
            {1, 4, 7}, {2, 5, 8}, {3, 6, 9},  // Vertical
            {1, 5, 9}, {3, 5, 7}              // Diagonal
    };

    public static boolean isWinning(int[] board, int mark) {
        // Iterate over the winning combinations
        for (int[] combo : winningCombinations) {
            // Check if the values at the positions in the combo are the same and not empty.
            if (board[combo[0]] == mark && board[combo[1]] == mark && board[combo[2]] == mark) {
                return true;
            }
        }
        return false;
    }

    //////////////////////////////////////////////////////////
    //                  DEBUG FUNCTIONS                     //
    //////////////////////////////////////////////////////////

    // Prints a singlular board with pretty colours ^-^

    static String[] prints = {".", "\u001B[32mO\u001B[0m", "\u001B[31mX\u001B[0m"};

    public static void printBoard(int[] board) {
        for (int i = 1; i < 10; i++) {
            System.out.print(prints[board[i]] + " ");
            if (i % 3 == 0) System.out.println();
        }
    }

    public static void printBoards(int[][] boards) {
        for (int i = 1; i < 10; i++) {
            printBoard(boards[i]);
            System.out.print("------\n");
        }
    }
}