import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;

public class ConnectFour {

    private char[][] board;
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

    public ConnectFour(char[][] board) {
        this.board = board;
    }
    
    public long getWaitTime() {
        return waitTime;
    }
    
    public void setWaitTime(long newWaitTime) {
        waitTime = newWaitTime;
    }

    public char[][] getBoard() {
        //must deep clone
        char[][] copy = new char[board.length][];
        for (int r = 0; r < copy.length; r++) {
            copy[r] = board[r].clone();
        }
        return copy;
    }

    public Set<ConnectFour> getSuccessors(){
        Set<ConnectFour> successors = new HashSet<>();
        for (int r = 0; r < board.length; r++) {
            for (int c = 0; c < board.length; c++) {
                if (board[r][c] == '-'){
                    ConnectFour successor = new ConnectFour(this.getBoard());
                    successor.placeToken(r, c , 'X');
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
            sb.append((char) (i + 65));
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
                    horizontalCounter++;// no winner found
                }

                int verticalCounter = 1;
                while (r + verticalCounter < 8 &&
                        token == board[r+verticalCounter][c]) { //vertical down
                    result += (token == 'X')? -1 : 1 ;
                    horizontalCounter++;
                }
            }
        }
        return result;
    }

    private boolean userPlayRound() {
        String move = "";
        do {
            System.out.print("Enter your move: ");
            move = kb.nextLine().toUpperCase();
            if (!isValidMove(move)) {
                System.out.println("Invalid move or space taken. Try again.");
            }
        } while (!isValidMove(move));
        int row = (int) move.charAt(0) - 65;
        int col = Integer.parseInt(move.charAt(1) + "");
        this.placeToken(row, col - 1, 'O');
        System.out.println(this);
        if (this.hasWinner()) {
            System.out.println("User wins!");
            return true;
        }
        return false;
    }

    private String cpuMakeMove() {
        //keep track of latest best move from iterative deepening
        ArrayList<String> bestMoves = new ArrayList<>();
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                run = true;
                while (run) {
                    //the alpha-beta pruning with minimax call goes here inside the add function
                    bestMoves.add(generateRandomMove());
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
        return bestMoves.get(bestMoves.size() - 1);
    }

    private boolean cpuPlayRound() {
        System.out.print("CPU move is: ");
        String move = cpuMakeMove();
        System.out.println(move);
        int row = (int) move.charAt(0) - 65;
        int col = Integer.parseInt(String.valueOf(move.charAt(1)));
        this.placeToken(row, (col - 1), 'X');
        System.out.println(this);
        if (this.hasWinner()) {
            System.out.println("CPU wins!");
            return true;
        }
        return false;
    }
    //used for testing the timing functionality of the cpuMakeMove
    private String generateRandomMove() {
        Random r = new Random();
        char row = (char) (r.nextInt(8) + 'A');
        int col = r.nextInt(8) + 1;
        String result = (row + "") + (col + "");
        return result;
    }

    //hardcoded for now, this needs to change
    private String aBPruning() {
        String move = "A1";
        /*
         * return max(Integer.MIN_VALUE, Integer.MAX_VALUE)
         *      if TERMINAL-TEST(state)
         *       then return UTILITY(state)
         *      v ←−∞
         *      for a  in ACTIONS(state) do
         *           v ←MAX(v,MIN-VALUE(alpha, beta)))
         *           if v >= beta
         *               then return v
         *           alpha = MAX(alpha, v)
         *      return v
         * */
        return move;
    }

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
        } while (seconds > 30);
        
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
        seconds *= 1000;
        cf.setWaitTime(seconds);
        System.out.println("Initial board:");
        System.out.println(cf);

        //unraveled cyclic method calls for fear of stack overflow
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
