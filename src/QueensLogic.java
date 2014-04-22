/**
 * This class implements the logic behind the BDD for the n-queens problem
 * You should implement all the missing methods
 *
 * @author Stavros Amanatidis
 *
 */

import net.sf.javabdd.*;

public class QueensLogic {
    private int x = 0;
    private int y = 0;
    private int[][] board;
    private BDD queensBDD;
    private BDDFactory fact; // do we need it? is it like a pool of all nodes?


    public QueensLogic() {
        //constructor
    }

    public void initializeGame(int size) {
        this.x = size;
        this.y = size;
        this.board = new int[x][y];

        buildBDD(size * size);
        updateBoard();
    }


    public int[][] getGameBoard() {
        return board;
    }

    public boolean insertQueen(int column, int row) {

        if (board[column][row] == -1 || board[column][row] == 1) {
            return false;
        }
        board[column][row] = 1;
        addRestriction(column, row);
        updateBoard();


        return true;
    }

    private void buildBDD(int size) {

        fact = JFactory.init(2000000, 200000);
        fact.setVarNum(size);
        BDD True = fact.one();
        BDD False = fact.zero();
        BDD rules = True;
        queensBDD=True;
        BDD temp_exp;


        //one queen in each row
        for (int j = 0; j < y; j++) {
            temp_exp = False;
            for (int i = 0; i < x; i++) {
               temp_exp=temp_exp.or(getVar(i,j));
            }
            queensBDD=queensBDD.andWith(temp_exp);
        }

        // add dynamic rules
        for (int j = 0; j < y; j++) {
            temp_exp = False;
            for (int i = 0; i < x; i++) {
                temp_exp = temp_exp.or(getRules(i, j));
            }
            rules = rules.and(temp_exp);
        }


        queensBDD=queensBDD.and(rules);

        // Checks whether or not expression is unsat
        //System.out.println("rules is unsat? : " + rules.isZero());

        // checks whether expression is tautology
        //System.out.println("rules is tautology? : " + rules.isOne());

    }

    private void addRestriction(int column, int row) {

        queensBDD = queensBDD.restrict(getVar(column, row));

    }

    private BDD getRules(int column, int row) {

        BDD temp_exp = fact.one();
        //vertical
        temp_exp = temp_exp.and(vertical(column, row));

        // horizontal
        temp_exp = temp_exp.and(horizontal(column, row));

        //diagonal bottom right - top left
        temp_exp = temp_exp.and(diagonalRL(column, row));

        //diagonal bottom left - top right
        temp_exp = temp_exp.and(diagonalLR(column, row));

        return temp_exp;
    }

    private BDD vertical(int column, int row) {
        BDD temp_exp = fact.one();
        for (int i = 0; i < y; i++) {
            if (i != row) {
                temp_exp = temp_exp.and(getNotVar(column, i));
            }
        }
        return temp_exp;
    }

    private BDD horizontal(int column, int row) {
        BDD temp_exp = fact.one();
        for (int j = 0; j < x; j++) {
            if (j != column) {
                temp_exp = temp_exp.and(getNotVar(j, row));
            }
        }
        return temp_exp;
    }

    private BDD diagonalRL(int column, int row) {
        BDD temp_exp = fact.one();
        for (int k = 0; k < x; k++) {
            if (k != column && row + k - column < x && row + k - column >= 0) {
                //System.out.println("k, row+k-column is: " + k + " " + (row + k - column));
                temp_exp = temp_exp.and(getNotVar(k, row + k - column));
            }
        }
        return temp_exp;
    }

    private BDD diagonalLR(int column, int row) {
        BDD temp_exp = fact.one();
        for (int k = 0; k < x; k++) {
            if (k != column & row + column - k < x && row + column - k >= 0) {
                temp_exp = temp_exp.and(getNotVar(k, row + column - k));
                //System.out.println("k, row + column - k is: " + k + " " + (row + column - k));
            }
        }
        return temp_exp;
    }


    private void updateBoard() {

        BDD temp_var;
        int column, row;


       // System.out.println("is bdd generally nonsatisfiable now? "+ queensBDD.isZero());

        for (int i = 0; i < x * y; i++) {

            //try to put a queen into a cell and see if it still satisfies the rules - if no place cross
            column = i / x;
            row = i - column * y;
            temp_var = getVar(column, row);

            if (queensBDD.restrict(temp_var).isZero()) board[column][row] = -1;

        }
    }

    private BDD getVar(int column, int row) {
        int u = x * column + row;
        return fact.ithVar(u);
    }

    private BDD getNotVar(int column, int row) {
        int u = x * column + row;
        return fact.nithVar(u);
    }
}
