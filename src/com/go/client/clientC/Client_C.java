package com.go.client.clientC;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.go.util.ChessBoard;
import com.go.AI.Robot;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

import static com.go.client.clientC.Client_C.*;

/**
 * Client
 */

class ClientThread_C implements Runnable {
    private ChessBoard chessBoard = new ChessBoard();
    private Robot robot;
    private Socket s;
    private BufferedReader br = null;
    private int myColor;
    private MessageTrans ms;

    ClientThread_C(Socket s, MessageTrans messageTrans) throws IOException {
        this.s = s;
        br = new BufferedReader(new InputStreamReader(s.getInputStream()));
        ms = messageTrans;
    }

    public interface MessageTrans {
        // 发送内容给前端显示
        void sendMessage(String message);
    }

    @Override
    public void run() {
        try {
            PrintStream ps = new PrintStream(s.getOutputStream());
            ps.println(clientName);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String content = null;
        try {
            while ((content = br.readLine()) != null) {
                System.out.println("[log] 本次接收到的消息：" + content);
                ms.sendMessage("[log] 本次接收到的消息：" + content + "\n");

                if (content.equals("Start")) {
                    // 初始化棋盘
                    chessBoard = new ChessBoard();

                    // 假如自己是先手
                    String prePlayer = br.readLine();
                    if (clientName.equals(prePlayer)) {
                        robot = new Robot(ChessBoard.BLACK, chessBoard);
                        myColor = ChessBoard.BLACK;
                        // 落第一步棋
                        PrintStream ps = new PrintStream(s.getOutputStream());
                        chessBoard.makeMove(8, 8, myColor);
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("name", clientName);
                        jsonObject.put("x", 8);
                        jsonObject.put("y", 8);
                        jsonObject.put("color", myColor);
                        jsonObject.put("isEnd", false);
                        System.out.println("[log] 本次发送的消息：" + jsonObject);
                        ms.sendMessage("[log] 本次发送的消息：" + jsonObject + "\n");
                        ps.println(jsonObject);

                    } else {
                        robot = new Robot(ChessBoard.WHITE, chessBoard);
                        myColor = ChessBoard.WHITE;
                    }

                } else {
                    JSONObject jsonFromServer = JSON.parseObject(content);
                    int color = jsonFromServer.getIntValue("color");
                    if (color != myColor) {

                        int x = jsonFromServer.getIntValue("x");
                        int y = jsonFromServer.getIntValue("y");
                        boolean isEnd = jsonFromServer.getBooleanValue("isEnd");

                        chessBoard.makeMove(x, y, color);

                        if (isEnd) {
                            System.out.println("[log] 本次发送的消息：" + content);
                            ms.sendMessage("[log] 本次发送的消息：" + content + "\n");
                            PrintStream ps = new PrintStream(s.getOutputStream());
                            ps.println(content);
                        } else {
                            int rob[] = robot.getNext(myColor);
                            chessBoard.makeMove(rob[0], rob[1], myColor);
                            int rel = chessBoard.isEnd(rob[0], rob[1], myColor);
                            if (rel != 0) isEnd = true;
                            JSONObject jsonToSend = new JSONObject();
                            jsonToSend.put("x", rob[0]);
                            jsonToSend.put("y", rob[1]);
                            jsonToSend.put("name", clientName);
                            jsonToSend.put("color", myColor);
                            jsonToSend.put("isEnd", isEnd);

                            System.out.println("[log] 本次发送的消息：" + jsonToSend);
                            ms.sendMessage("[log] 本次发送的消息：" + jsonToSend + "\n");

                            PrintStream ps = new PrintStream(s.getOutputStream());
                            ps.println(jsonToSend);

                        }

                    }
                }

            }
        } catch (
                IOException e) {
            e.printStackTrace();
        }
    }
}

public class Client_C implements ClientThread_C.MessageTrans {
    private static final int SERVER_PORT = 60000;
    static String clientName;

    @FXML
    TextArea logContent;
    @FXML
    TextField serverIP;
    @FXML
    Button btnConnect;
    @FXML
    TextField name;

    @FXML
    protected void handleConnectClicked(ActionEvent event) throws IOException {
        clientName = name.getText();

        Socket s = new Socket(serverIP.getText(), SERVER_PORT);
        new Thread(new ClientThread_C(s, this)).start();
    }

    @Override
    public void sendMessage(String message) {
        logContent.appendText(message);
    }
}