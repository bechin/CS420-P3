
import java.util.Scanner;

public class ConnectFour {

    private char[][] board;
    private long waitTime;

    public ConnectFour() {
        this.board = new char[8][8];
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board.length; j++) {
                board[i][j] = '-';
            }
        }
    }
    
    public long getWaitTime() {
        return waitTime;
    }
    
    public void setWaitTime(long newWaitTime) {
        waitTime = newWaitTime;
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

    private void userPlayRound() {
        Scanner kb = new Scanner(System.in);
        String move = "";
        do {
            System.out.print("Enter your move: ");
            move = kb.nextLine().toUpperCase();
            if (!isValidMove(move)) {
                System.out.println("Invalid move or space taken. Try again.");
            }
        } while (!isValidMove(move));
        int row = (int) move.charAt(0) - 65;
        int col = Integer.parseInt(move.charAt(1)+"");
        this.placeToken(row, col-1, 'O');
        if (this.checkWin()) {
            System.out.println("User wins!");
            System.out.println(this);
        }
        System.out.println(this);
        this.cpuPlayRound();
    }

    private void cpuPlayRound() {
        String move = "";
        do {
            System.out.print("Enter your move: ");
            try {
                Thread.sleep(this.getWaitTime());
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
            System.out.println("A1");
            move = "A1"; //hardcoded for now, this needs to change
            if (!isValidMove(move)) {
                System.out.println("Invalid move or space taken. Try again.");
            }
        } while (!isValidMove(move));
        int row = (int) move.charAt(0) - 65;
        int col = Integer.parseInt(move.charAt(1)+"");
        this.placeToken(row, (col-1), 'X');
        if (this.checkWin()) {
            System.out.println("User wins!");
            System.out.println(this);
        }
        System.out.println(this);
        this.userPlayRound();
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
    
    public boolean checkWin() {
    for (int r = 0; r < 8; r++) {
        for (int c = 0; c < 8; c++) {
            char token = board[r][c];
            if (token == '-')
                continue; // don't check empty slots

            if (c + 3 < 8 &&
                token == board[r][c+1] && //horizontal right
                token == board[r][c+2] &&
                token == board[r][c+3])
                return true;
            if (c - 3 > 0 &&
                token == board[r][c-1] && //horizontal left
                token == board[r][c-2] &&
                token == board[r][c-3])
                return true;
            if (r + 3 < 8 &&
                token == board[r+1][c] && //vertical up
                token == board[r+2][c] &&
                token == board[r+3][c])
                return true;
            if (r - 3 > 0 &&
                token == board[r-1][c] && //vertical down
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
        Scanner kb = new Scanner(System.in);
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
        if (firstMove.equals("USER")) {
            cf.userPlayRound();
        } else {
            cf.cpuPlayRound();
        }

    }

}
