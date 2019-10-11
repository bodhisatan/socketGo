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

import static com.go.client.Client_B.*;

/**
 * Client
 */

class ClientThread_B implements Runnable {
    private Socket s;
    BufferedReader br = null;

    public ClientThread_B(Socket s) throws IOException {
        this.s = s;
        br = new BufferedReader(new InputStreamReader(s.getInputStream()));
    }

    @Override
    public void run() {
        String content = null;
        try {
            while ((content = br.readLine()) != null) {
                System.out.println("[log] 本次接收到的消息：" + content);
                if (!content.equals("Start")) {
                    JSONObject jsonFromServer = JSON.parseObject(content);
                    int color = jsonFromServer.getIntValue("color");
                    if (color == colorA) {

                        int x = jsonFromServer.getIntValue("x");
                        int y = jsonFromServer.getIntValue("y");
                        String name = jsonFromServer.getString("name");
                        boolean isEnd = jsonFromServer.getBooleanValue("isEnd");

                        chessBoard.makeMove(x, y, color);

                        int rob[] = Client_B.robot.getNext(colorB);
                        chessBoard.makeMove(rob[0], rob[1], colorB);
                        int rel = chessBoard.isEnd(rob[0], rob[1], colorB);
                        if (rel != 0) isEnd = true;
                        JSONObject jsonToSend = new JSONObject();
                        jsonToSend.put("x", rob[0]);
                        jsonToSend.put("y", rob[1]);
                        jsonToSend.put("name", "B");
                        jsonToSend.put("color", colorB);
                        jsonToSend.put("isEnd", isEnd);
                        System.out.println("[log] 本次发送的消息：" + jsonToSend);
                        PrintStream ps = new PrintStream(s.getOutputStream());
                        ps.println(jsonToSend);

                    }
                }

            }
        } catch (
                IOException e) {
            e.printStackTrace();
        }
    }
}

// 后手AI
public class Client_B {
    private static final int SERVER_PORT = 60000;

    public static ChessBoard chessBoard = new ChessBoard();
    public static int colorA = ChessBoard.BLACK;
    public static int colorB = ChessBoard.WHITE;
    public static Robot robot = new Robot(ChessBoard.WHITE, chessBoard);

    public static void main(String[] args) throws IOException {
        Socket s = new Socket("172.16.0.120", SERVER_PORT);
        new Thread(new ClientThread_B(s)).start();
    }
}