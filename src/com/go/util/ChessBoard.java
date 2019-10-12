package com.go.util;

public class ChessBoard {
    public static final int N = 15;
    public static final int EMPTY = 0;
    public static final int BLACK = 1;
    public static final int WHITE = 2;

    private int[][] board = new int[N + 1][N + 1];

    public ChessBoard() {
    }

    public int getColor(int x, int y) {
        return board[x][y];
    }

    /*  判断该位置是否无子 */
    public boolean isEmpty(int x, int y) {
        return board[x][y] == EMPTY;
    }

    /* 落子 */
    public void makeMove(int x, int y, int color) {
        board[x][y] = color;
    }

    /* 撤子 */
    public void unMove(int x, int y) {
        board[x][y] = EMPTY;
    }

    /* 判断局面是否结束 0未结束 1WHITE赢 2BLACK赢 */
    public int isEnd(int x, int y, int color) {
        int dx[] = {1, 0, 1, 1};
        int dy[] = {0, 1, 1, -1};

        for (int i = 0; i < 4; i++) {
            int sum = 1;

            int tx = x + dx[i];
            int ty = y + dy[i];
            while (tx > 0 && tx <= N
                    && ty > 0 && ty <= N
                    && board[tx][ty] == color) {
                tx += dx[i];
                ty += dy[i];
                ++sum;
            }

            tx = x - dx[i];
            ty = y - dy[i];
            while (tx > 0 && tx <= N
                    && ty > 0 && ty <= N
                    && board[tx][ty] == color) {

                tx -= dx[i];
                ty -= dy[i];
                ++sum;
            }


            if (sum >= 5)
                return color;
        }
        return 0;
    }
}
