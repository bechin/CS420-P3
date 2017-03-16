/* 
 * File:   ConnectFour.java
 * Author: Ben Chin and Mitch Saulnier
 * Course: CS420
 * Due Dt: 03/15/2017
 * 
 * Description: Four in a row game with 
 *              AI computer opponent
 *
 */

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;

public class ConnectFour{

    private char[][] board;
    private String lastMove;
    private int value;

    private long waitTime;
    private static final Scanner kb = new Scanner(System.in);
    boolean run;

    public ConnectFour() {
        this.board = new char[8][8];
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board.length; j++) {
                board[i][j] = '-';
            }
        }
    }

    //this constructor makes it easier to mock
    //a ConnectFour object with a specific value,
    //namely Integer.MAX_VALUE and Integer.MIN_VALUE
    public ConnectFour(int value){
        board = null;
        lastMove = null;
        this.value = value;
    }

    //copy constructor
    public ConnectFour(ConnectFour that){
        this.board = that.getBoard();
        this.lastMove = that.getLastMove();
        this.value = that.getValue();
    }

    //must deep clone
    public char[][] getBoard() {
        char[][] copy = new char[board.length][];
        for (int r = 0; r < copy.length; r++) {
            copy[r] = board[r].clone();
        }
        return copy;
    }

    public long getWaitTime() {
        return waitTime;
    }
    
    public void setWaitTime(long newWaitTime) {
        waitTime = newWaitTime;
    }

    public int getValue() {
        return value;
    }

    public String getLastMove() {
        return lastMove;
    }

    public Set<ConnectFour> getSuccessors(boolean isCpuMove){
        Set<ConnectFour> successors = new HashSet<>();
        for (int r = 0; r < board.length; r++) {
            for (int c = 0; c < board.length; c++) {
                if (board[r][c] == '-'){
                    ConnectFour successor = new ConnectFour(this);
                    successor.placeToken(r, c , (isCpuMove)? 'X' : 'O'); //make move
                    successors.add(successor);
                }
            }
        }
        return successors;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(" ");
        for (int i = 1; i <= board.length; i++) {
            sb.append(" " + i);
        }
        sb.append("\n");
        for (int i = 0; i < board.length; i++) {
            sb.append((char) (i + 'A'));
            for (int j = 0; j < board.length; j++) {
                sb.append(" " + board[i][j]);
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    //sum of adjacent Os - sum of adjacent Xs
    //probably too complicated
    public int evaluation(){
        int result = 0;
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                char token = board[r][c];
                if (token == '-')
                    continue; // don't check empty slots

                int horizontalCounter = 1;
                while (c + horizontalCounter < 8 &&
                        token == board[r][c+horizontalCounter]) { //horizontal right
                    result += (token == 'X')? -1 : 1 ;
                    horizontalCounter++;
                }

                int verticalCounter = 1;
                while (r + verticalCounter < 8 &&
                        token == board[r+verticalCounter][c]) { //vertical down
                    result += (token == 'X')? -1 : 1 ;
                    verticalCounter++;
                }
            }
        }
        return result;
    }

    private boolean userPlayRound() {
        String move;
        do {
            System.out.print("Enter your move: ");
            move = kb.nextLine().toUpperCase();
            if (!isValidMove(move)) {
                System.out.println("Invalid move or space taken. Try again.");
            }
        } while (!isValidMove(move));
        //get zero based index for array placement
        int row = (int) move.charAt(0) - 65;
        int col = Integer.parseInt(String.valueOf(move.charAt(1))) - 1;
        this.placeToken(row, col, 'O');
        System.out.println(this);
        if (this.hasWinner()) {
            System.out.println("User wins!");
            return true;
        }
        return false;
    }

    private boolean cpuPlayRound() {
        System.out.print("CPU move is: ");
        String move = cpuMakeMove();
        System.out.println(move);
        //get zero based index for array placement
        int row = (int) move.charAt(0) - 65;
        int col = Integer.parseInt(String.valueOf(move.charAt(1))) - 1;
        this.placeToken(row, col, 'X');
        System.out.println(this);
        if (this.hasWinner()) {
            System.out.println("CPU wins!");
            return true;
        }
        return false;
    }

    //implements alpha-beta pruning with iterative deepening
    private String cpuMakeMove() {
        ArrayList<String> bestMoves = new ArrayList<>();
        ConnectFour current = this;
        //used a dedicated thread to time the calculation of the iterative deepening process
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                run = true;
                for (int depth = 1; depth < Integer.MAX_VALUE; depth++) {
                    ConnectFour theChosenOne = current.min(depth, Integer.MIN_VALUE, Integer.MAX_VALUE);
                    bestMoves.add(theChosenOne.getLastMove());
                    if(run == false)
                        break;
                }
                synchronized(this) {
                    this.notify();
                }
            }
        }
        );
        t.start();
        try {
            synchronized (this) {
                this.wait(this.getWaitTime());
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        run = false;
        //return the most recent best move after the time's up
        return bestMoves.get(bestMoves.size() - 1);
    }

    //used for testing the timing functionality of the cpuMakeMove
    private String generateRandomMove() {
        Random r = new Random();
        char row = (char) (r.nextInt(8) + 'A');
        int col = r.nextInt(8) + 1;
        String result = (row + "") + (col + "");
        return result;
    }

    private ConnectFour min(int depth, int alpha, int beta){
        if ( depth == 0 ){
            this.value = this.evaluation();
            return this;
        }

        //original pseudocode from slides calls for reassignment of beta
        //but changing arguments during execution makes it harder to trace
        //so I'm copying beta into a new variable
        int currentBeta = beta;

        ConnectFour theChosenOne = new ConnectFour(Integer.MAX_VALUE);

        for(ConnectFour successor : this.getSuccessors(isCpuMove(depth))){

            ConnectFour max = successor.max(depth - 1, alpha, currentBeta);

            theChosenOne = (max.value <= theChosenOne.value)? max
                    : theChosenOne;

            if (theChosenOne.value <= alpha) {
                return theChosenOne;
            }

            currentBeta = Math.min(currentBeta, theChosenOne.value);

        }

        return theChosenOne;
    }

    private ConnectFour max(int depth, int alpha, int beta){
        if ( depth == 0 ){
            this.value = this.evaluation();
            return this;
        }

        //original pseudocode from slides calls for reassignment of alpha
        //but changing arguments during execution makes it harder to trace
        //so I'm copying alpha into a new variable
        int currentAlpha = alpha;

        ConnectFour theChosenOne = new ConnectFour(Integer.MIN_VALUE);

        for(ConnectFour successor : this.getSuccessors(isCpuMove(depth))){

            ConnectFour min = successor.min(depth - 1, currentAlpha, beta);

            theChosenOne = (min.getValue() >= theChosenOne.value)? min
                    : theChosenOne;

            if (theChosenOne.value >= beta) {
                return theChosenOne;
            }

            currentAlpha = Math.max(currentAlpha, theChosenOne.value);

        }

        return theChosenOne;
    }

    //isCpuMove is a function of the relative depth, i.e. its parity
    private boolean isCpuMove(int n){
        return (n & 1) == 1;
    }
    //checks that the move is in bounds and follows the convention of letter, number
    private boolean isValidMove(String move) {
        if (move.length() != 2 || !Character.isLetter(move.charAt(0))) {
            return false;
        }
        int firstChar = (int)move.charAt(0)-64;
        int secondChar;
        try {
            secondChar = Integer.parseInt(move.charAt(1) + "");
        } catch (NumberFormatException nfe) {
            return false;
        }
        return  (firstChar >= 1 && firstChar <= 8) &&      //first char is in A-H
                (secondChar >= 1 && secondChar <= 8) &&    //second char is 1-8
                (board[firstChar-1][secondChar-1] == '-'); //space is open on board 
    }
    
    //checks for a win condition
    public boolean hasWinner() {
    for (int r = 0; r < 8; r++) {
        for (int c = 0; c < 8; c++) {
            char token = board[r][c];
            if (token == '-')
                continue; // don't check empty slots

            if (c + 3 < 8 &&
                token == board[r][c+1] && //horizontal
                token == board[r][c+2] &&
                token == board[r][c+3])
                return true;
            if (r - 3 > 0 &&
                token == board[r-1][c] && //vertical
                token == board[r-2][c] &&
                token == board[r-3][c])
                return true;

            }
        }
        return false; // no winner found
    }

    private void placeToken(int r, int c, char token) {
        this.board[r][c] = token;
        lastMove = (char)(r + 'A') + String.valueOf(c + 1);
    }

    public static void main(String[] args) {
        System.out.println("How much time (in seconds) will you allow the computer to generate an answer?");
        long seconds;
        do {
            System.out.print("\tTime: ");
            seconds = kb.nextLong();
            kb.nextLine();
            if (seconds > 30) {
                System.out.println("The maximum time allowed is 30 seconds. Please enter another time.");
            }
        } while (seconds > 30); //30 seconds is the max time alloted for the cpu move
        
        System.out.println("And who should go first?");
        String firstMove = "";
        boolean validChoice = false;
        do {
            System.out.print("\tCPU or USER? ");
            firstMove = kb.nextLine().toUpperCase();
            validChoice = firstMove.equals("USER") || firstMove.equals("CPU");
            if (!validChoice) {
                System.out.println("Not a valid choice. Please enter CPU or USER.");

            }
        } while (!validChoice);

        ConnectFour cf = new ConnectFour();
        System.out.println("");
        //convert to milliseconds for the timing function of java
        seconds *= 1000;
        cf.setWaitTime(seconds);
        System.out.println("Initial board:");
        System.out.println(cf);

        while ( !cf.hasWinner() ) {
            if (firstMove.equals("USER")) {
                if ( cf.userPlayRound() )
                    break;
                if ( cf.cpuPlayRound() )
                    break;
            } else {
                if ( cf.cpuPlayRound() )
                    break;
                if ( cf.userPlayRound() )
                    break;
            }
        }

    }

}
