package com.go.server;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.go.com.go.util.ChessBoard;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Server
 */
class ServerThread implements Runnable {
    Socket s = null;
    BufferedReader br = null;

    public ServerThread(Socket s) throws IOException {
        this.s = s;
        br = new BufferedReader(new InputStreamReader(s.getInputStream()));
    }

    @Override
    public void run() {
        try {
            if (Server.rounds == 0 && Server.sockets.size() == 2) {
                for (Socket socket : Server.sockets) {
                    PrintStream ps = new PrintStream(socket.getOutputStream());
                    ps.println("Start");
                }
                Server.rounds++;
            }

            String content = null;
            while ((content = readFromClient()) != null) {
                System.out.println("[log] 客户端发来内容：" + content);

                // 解析内容
                JSONObject jsonObject = JSON.parseObject(content);
                int x = jsonObject.getIntValue("x");
                int y = jsonObject.getIntValue("y");
                String name = jsonObject.getString("name");
                int color = jsonObject.getIntValue("color");
                boolean isEnd = jsonObject.getBooleanValue("isEnd");

                Server.chessBoard.makeMove(x, y, color);

                int roundNum = (Server.rounds + 1) / 2;
                System.out.println("ROUND " + roundNum + ": ");
                Server.rounds++;

                // 打印棋盘
                for (int i = 1; i <= ChessBoard.N; i++) {
                    System.out.print("|");
                    for (int j = 1; j <= ChessBoard.N; j++) {
                        if (Server.chessBoard.getColor(i, j) == ChessBoard.EMPTY) {
                            System.out.print(" |");
                        } else if (Server.chessBoard.getColor(i, j) == ChessBoard.BLACK) {
                            System.out.print("O|");
                        } else if (Server.chessBoard.getColor(i, j) == ChessBoard.WHITE) {
                            System.out.print("X|");
                        }
                    }
                    System.out.println();
                }

                // 游戏结束
                if (isEnd) {
                    System.out.println("Game Over! " + name + " Wins");
                    System.exit(0);
                }

                // 将消息分发给玩家
                for (Socket socket : Server.sockets) {
                    PrintStream ps = new PrintStream(socket.getOutputStream());
                    ps.println(content);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String readFromClient() {
        try {
            return br.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            Server.sockets.remove(s);
        }
        return null;
    }
}

public class Server {
    private static final int SERVER_PORT = 60000;
    public static ArrayList<Socket> sockets = new ArrayList<Socket>();
    public static int rounds = 0;
    public static ChessBoard chessBoard = new ChessBoard();

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
        while (true) {
            Socket s = serverSocket.accept();
            System.out.println("[log] someone in ...");
            sockets.add(s);
            new Thread(new ServerThread(s)).start();
        }
    }
}