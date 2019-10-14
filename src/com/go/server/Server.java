package com.go.server;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.go.util.ChessBoard;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;

import static com.go.server.Server.*;


/**
 * Server
 */
class ServerThread implements Runnable {
    Socket s = null;
    BufferedReader br = null;
    MessageTrans messageTrans = null;

    public ServerThread(Socket s, MessageTrans ms) throws IOException {
        this.s = s;
        this.messageTrans = ms;
        br = new BufferedReader(new InputStreamReader(s.getInputStream()));
    }

    public interface MessageTrans {
        // 发送内容给前端显示
        void sendMessage(String message);

        // 画出棋子
        void drawChess(int color, int x, int y);
    }

    @Override
    public void run() {
        try {
            // 开局时发送 Start 信号 以及 先手玩家姓名
            if (Server.rounds == 0 && Server.sockets.size() == 2) {
                Server.rounds++;
                for (Socket socket : Server.sockets) {
                    PrintStream ps = new PrintStream(socket.getOutputStream());
                    ps.println("Start");
                    ps.println(prePlayer);
                }

            }

            String content = null; // 客户端发来的内容
            while ((content = readFromClient()) != null) {
                System.out.println("[log] 客户端发来内容：" + content);

                // 解析内容
                JSONObject jsonObject = JSON.parseObject(content);
                int x = jsonObject.getIntValue("x");
                int y = jsonObject.getIntValue("y");
                String name = jsonObject.getString("name");
                int color = jsonObject.getIntValue("color");
                boolean isEnd = jsonObject.getBooleanValue("isEnd");

                messageTrans.sendMessage("[log] " + name + " 落子于(" + x + ", " + y + ")\n");


                // 解析内容后落子
                Server.chessBoard.makeMove(x, y, color);

                // 画出棋子


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

                messageTrans.drawChess(color, x, y);

                // 游戏结束
                if (isEnd) {
                    System.out.println("Game Over! " + name + " Wins");
                    messageTrans.sendMessage("[log] Game Over! " + name + " 胜利\n");
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

public class Server implements ServerThread.MessageTrans {

    public static ChessBoard chessBoard = new ChessBoard();
    public static final int SERVER_PORT = 60000;
    public static ArrayList<Socket> sockets = new ArrayList<Socket>();
    public static int rounds = 0;

    @FXML
    Canvas canvas;
    @FXML
    TextArea taContent;
    @FXML
    Label serverIP;
    @FXML
    Button btnStart;
    @FXML
    TextField clientNum;
    @FXML
    Button btnListen;

    private Color colorChessboard = Color.valueOf("#FBE39B");
    private Color colorLine = Color.valueOf("#884B09");
    private Color colorMark = Color.valueOf("#FF7F27");
    private GraphicsContext gc;
    private double gapX, gapY;
    private double chessSize;
    private double broadPadding = 14;
    private String[] markX = new String[]{"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O"};
    private String[] markY = new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15"};

    HashMap<Socket, String> map = new HashMap<>();
    public static String prePlayer = "";

    public void initialize() {
        btnStart.setDisable(true);
        gc = canvas.getGraphicsContext2D();
        gapX = (canvas.getWidth() - broadPadding * 2) / 14;
        gapY = (canvas.getWidth() - broadPadding * 2) / 14;
        System.out.println();
        chessSize = gapX * 0.8;
        cleanChessBoard();
        try {
            serverIP.setText("本机IP：" + InetAddress.getLocalHost().getHostAddress());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

    }

    private void cleanChessBoard() {
        gc.setFill(colorChessboard);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        gc.setStroke(colorLine);
        for (int i = 0; i <= 20; i++) {
            gc.strokeLine(i * gapX + broadPadding, broadPadding, i * gapX + broadPadding, canvas.getHeight() - broadPadding);
            gc.strokeLine(broadPadding, i * gapY + broadPadding, canvas.getWidth() - broadPadding, i * gapY + broadPadding);
        }

        gc.setFill(colorMark);
        gc.setFont(Font.font(broadPadding / 2));
        for (int i = 0; i < 15; i++) {
            gc.fillText(markX[i], i * gapX + broadPadding - 5, broadPadding - 5);
            gc.fillText(markX[i], i * gapX + broadPadding - 5, canvas.getHeight() - 5);
            gc.fillText(markY[i], 5, gapY * i + broadPadding + 5);
            gc.fillText(markY[i], canvas.getWidth() - broadPadding + 5, gapY * i + broadPadding + 5);
        }
    }

    @FXML
    protected void handleStartListen(ActionEvent event) throws IOException {
        btnListen.setDisable(true);
        ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
        new Thread(() -> {
            while (true) {
                Socket s = null;
                try {
                    s = serverSocket.accept();
                    BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
                    String curName = br.readLine();
                    sockets.add(s);
                    map.put(s, curName);
                    taContent.appendText("[log] " + curName + "上线\n");
                    clientNum.setText("当前在线AI数目：" + sockets.size());
                    if (sockets.size() == 2) break;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            taContent.appendText("[log] 已建立连接\n");
            btnStart.setDisable(false);
        }).start();
    }

    @FXML
    protected void handleStartServer(ActionEvent event) throws IOException {
        taContent.appendText("[log] 开始博弈\n");
        int rand = Math.random() > 0.5 ? 1 : 0;
        Socket s = sockets.get(rand);
        prePlayer = map.get(s);
        taContent.appendText("[log] " + prePlayer + "先手执黑\n");
        btnStart.setDisable(true);
        for (Socket socket : sockets) {
            new Thread(new ServerThread(socket, this)).start();
        }
    }

    @Override
    public void sendMessage(String message) {
        taContent.appendText(message);
    }

    @Override
    public void drawChess(int color, int tx, int ty) {
        tx--;
        ty--;
        double x = tx * gapX + broadPadding;
        double y = ty * gapY + broadPadding;
        switch (color) {
            case ChessBoard.BLACK:
                gc.setFill(Color.BLACK);
                gc.fillOval(x - chessSize / 2, y - chessSize / 2, chessSize, chessSize);
                break;
            case ChessBoard.WHITE:
                gc.setFill(Color.WHITE);
                gc.fillOval(x - chessSize / 2, y - chessSize / 2, chessSize, chessSize);
                break;
        }
    }
}