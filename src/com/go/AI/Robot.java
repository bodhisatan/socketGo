package com.go.AI;

import com.go.util.ChessBoard;

import java.util.Random;

public class Robot {

    private static ChessBoard chess = null;
    private static int depth = 1;
    private static int robotColor;

    public Robot(int color, ChessBoard chessBoard) {
        robotColor = color;
        chess = chessBoard;
    }

    static public int reckon(int color) {

        int dx[] = {1, 0, 1, 1};
        int dy[] = {0, 1, 1, -1};
        int ans = 0;

        for (int x = 1; x <= chess.N; x++) {
            for (int y = 1; y <= chess.N; y++) {
                if (chess.getColor(x, y) != color)
                    continue;

                int num[][] = new int[2][100];

                for (int i = 0; i < 4; i++) {
                    int sum = 1;
                    int flag1 = 0, flag2 = 0;

                    int tx = x + dx[i];
                    int ty = y + dy[i];
                    while (tx > 0 && tx <= chess.N
                            && ty > 0 && ty <= chess.N
                            && chess.getColor(tx, ty) == color) {
                        tx += dx[i];
                        ty += dy[i];
                        ++sum;
                    }

                    if (tx > 0 && tx <= chess.N
                            && ty > 0 && ty <= chess.N
                            && chess.getColor(tx, ty) == chess.EMPTY)
                        flag1 = 1;

                    tx = x - dx[i];
                    ty = y - dy[i];
                    while (tx > 0 && tx <= chess.N
                            && ty > 0 && ty <= chess.N
                            && chess.getColor(tx, ty) == color) {
                        tx -= dx[i];
                        ty -= dy[i];
                        ++sum;
                    }

                    if (tx > 0 && tx <= chess.N
                            && ty > 0 && ty <= chess.N
                            && chess.getColor(tx, ty) == chess.EMPTY)
                        flag2 = 1;

                    if (flag1 + flag2 > 0)
                        ++num[flag1 + flag2 - 1][sum];
                }

                //成5
                if (num[0][5] + num[1][5] > 0)
                    ans = Math.max(ans, 100000);
                    //活4 | 双死四 | 死4活3
                else if (num[1][4] > 0
                        || num[0][4] > 1
                        || (num[0][4] > 0 && num[1][3] > 0))
                    ans = Math.max(ans, 10000);
                    //双活3
                else if (num[1][3] > 1)
                    ans = Math.max(ans, 5000);
                    //死3活3
                else if (num[1][3] > 0 && num[0][3] > 0)
                    ans = Math.max(ans, 1000);
                    //死4
                else if (num[0][4] > 0)
                    ans = Math.max(ans, 500);
                    //单活3
                else if (num[1][3] > 0)
                    ans = Math.max(ans, 200);
                    //双活2
                else if (num[1][2] > 1)
                    ans = Math.max(ans, 100);
                    //死3
                else if (num[0][3] > 0)
                    ans = Math.max(ans, 50);
                    //双活2
                else if (num[1][2] > 1)
                    ans = Math.max(ans, 10);
                    //单活2
                else if (num[1][2] > 0)
                    ans = Math.max(ans, 5);
                    //死2
                else if (num[0][2] > 0)
                    ans = Math.max(ans, 1);

            }
        }

        return ans;
    }

    /* alpha_beta剪枝搜索 */
    public int alpha_betaFind(int depth, int alpha, int beta, int color, int prex, int prey) {

        if (depth >= Robot.depth || 0 != chess.isEnd(prex, prey, color % 2 + 1)) {

            int ans = reckon(robotColor) - reckon(robotColor % 2 + 1);

            if (depth % 2 == 0)
                ans = -ans;

            return ans;
        }

        for (int x = 1; x <= chess.N; x++) {
            for (int y = 1; y <= chess.N; y++) {

                if (!chess.isEmpty(x, y))
                    continue;

                chess.makeMove(x, y, color);
                int val = -alpha_betaFind(depth + 1, -beta, -alpha, color % 2 + 1, x, y);

                chess.unMove(x, y);

                if (val >= beta)
                    return beta;

                if (val > alpha)
                    alpha = val;
            }
        }
        return alpha;
    }

    /* 返回AI走法 */
    public int[] getNext(int color) {
        int rel[] = new int[2];
        int ans = -100000000;

        Random random = new Random();

        for (int x = 1; x <= chess.N; x++) {
            for (int y = 1; y <= chess.N; y++) {

                if (!chess.isEmpty(x, y))
                    continue;

                chess.makeMove(x, y, color);

                int val = -alpha_betaFind(0, -100000000, 100000000, color % 2 + 1, x, y);

                int ra = random.nextInt(100);
                if (val > ans || val == ans && ra >= 50) {
                    ans = val;
                    rel[0] = x;
                    rel[1] = y;
                }
                chess.unMove(x, y);
            }
        }
        return rel;
    }
}
