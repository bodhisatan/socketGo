package com.go.client;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.go.com.go.util.ChessBoard;
import com.go.AI.Robot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

import static com.go.client.Client_A.*;

/**
 * Client
 */

class ClientThread_A implements Runnable {
    private Socket s;
    BufferedReader br = null;

    public ClientThread_A(Socket s) throws IOException {
        this.s = s;
        br = new BufferedReader(new InputStreamReader(s.getInputStream()));
    }

    @Override
    public void run() {
        String content = null;
        try {
            while ((content = br.readLine()) != null) {
                System.out.println("[log] 本次接收到的消息：" + content);
                // 开局
                if (content.equals("Start")) {
                    PrintStream ps = new PrintStream(s.getOutputStream());
                    chessBoard.makeMove(7, 7, colorA);
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("name", "A");
                    jsonObject.put("x", 7);
                    jsonObject.put("y", 7);
                    jsonObject.put("color", colorA);
                    jsonObject.put("isEnd", false);
                    System.out.println("[log] 本次发送的消息：" + jsonObject);
                    ps.println(jsonObject);
                } else {
                    JSONObject jsonFromServer = JSON.parseObject(content);
                    int color = jsonFromServer.getIntValue("color");
                    if (color == colorB) {

                        int x = jsonFromServer.getIntValue("x");
                        int y = jsonFromServer.getIntValue("y");
                        String name = jsonFromServer.getString("name");
                        boolean isEnd = jsonFromServer.getBooleanValue("isEnd");

                        chessBoard.makeMove(x, y, color);

                        int rob[] = Client_A.robot.getNext(colorA);
                        chessBoard.makeMove(rob[0], rob[1], colorA);
                        int rel = chessBoard.isEnd(rob[0], rob[1], colorA);
                        if (rel != 0) isEnd = true;
                        JSONObject jsonToSend = new JSONObject();
                        jsonToSend.put("x", rob[0]);
                        jsonToSend.put("y", rob[1]);
                        jsonToSend.put("name", "A");
                        jsonToSend.put("color", colorA);
                        jsonToSend.put("isEnd", isEnd);
                        System.out.println("[log] 本次发送的消息：" + jsonToSend);
                        PrintStream ps = new PrintStream(s.getOutputStream());
                        ps.println(jsonToSend);

                    }

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

// 先手AI
public class Client_A {
    private static final int SERVER_PORT = 60000;

    public static ChessBoard chessBoard = new ChessBoard();
    public static int colorA = ChessBoard.BLACK;
    public static int colorB = ChessBoard.WHITE;
    public static Robot robot = new Robot(ChessBoard.BLACK, chessBoard);

    public static void main(String[] args) throws IOException {
        Socket s = new Socket("172.16.0.120", SERVER_PORT);
        new Thread(new ClientThread_A(s)).start();
    }
}