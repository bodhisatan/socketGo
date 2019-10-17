package com.go.client.clientC;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.go.AI.AIForC;
import com.go.util.Chess;
import com.go.util.ChessBoard;
import com.go.util.Record;
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
import static java.lang.Thread.sleep;

/**
 * Client
 */

class ClientThread_C implements Runnable {
    private Socket s;
    private BufferedReader br = null;
    private int myColor;
    private MessageTrans ms;
    private AIForC aiForC;
    private Chess[][] chessBoard;

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
            ps.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String content = null;
        try {
            while ((content = br.readLine()) != null) {
                System.out.println("[log] 本次接收到的消息：" + content);
                ms.sendMessage("[log] 本次接收到的消息：" + content + "\n");

                if (content.equals("Start")) {
                    chessBoard = new Chess[15][15];

                    // 假如自己是先手
                    String prePlayer = br.readLine();
                    if (clientName.equals(prePlayer)) {
                        myColor = ChessBoard.BLACK;
                        aiForC = new AIForC(myColor);
                        // 落第一步棋
                        PrintStream ps = new PrintStream(s.getOutputStream());
                        chessBoard[8 - 1][8 - 1] = new Chess(myColor);
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("name", clientName);
                        jsonObject.put("x", 8);
                        jsonObject.put("y", 8);
                        jsonObject.put("color", myColor);
                        jsonObject.put("isEnd", false);
                        System.out.println("[log] 本次发送的消息：" + jsonObject);
                        ms.sendMessage("[log] 本次发送的消息：" + jsonObject + "\n");
                        ps.println(jsonObject);
                        ps.flush();

                    } else {
                        myColor = ChessBoard.WHITE;
                        aiForC = new AIForC(myColor);
                    }

                } else {
                    JSONObject jsonFromServer = JSON.parseObject(content);
                    int color = jsonFromServer.getIntValue("color");
                    if (color != myColor) {

                        int x = jsonFromServer.getIntValue("x");
                        int y = jsonFromServer.getIntValue("y");
                        boolean isEnd = jsonFromServer.getBooleanValue("isEnd");

                        chessBoard[x - 1][y - 1] = new Chess(color);

                        if (isEnd) {
                            System.out.println("[log] 本次发送的消息：" + content);
                            ms.sendMessage("[log] 本次发送的消息：" + content + "\n");
                            PrintStream ps = new PrintStream(s.getOutputStream());
                            ps.println(content);
                            ps.flush();
                        } else {
                            Record record = aiForC.play(chessBoard, x - 1, y - 1);
                            chessBoard[record.getI()][record.getJ()] = new Chess(myColor);
                            int rel = aiForC.isEnd(chessBoard, record.getI(), record.getJ(), myColor);
                            if (rel != 0) isEnd = true;
                            JSONObject jsonToSend = new JSONObject();
                            jsonToSend.put("x", record.getI() + 1);
                            jsonToSend.put("y", record.getJ() + 1);
                            jsonToSend.put("name", clientName);
                            jsonToSend.put("color", myColor);
                            jsonToSend.put("isEnd", isEnd);

                            System.out.println("[log] 本次发送的消息：" + jsonToSend);
                            ms.sendMessage("[log] 本次发送的消息：" + jsonToSend + "\n");

                            sleep(500);

                            PrintStream ps = new PrintStream(s.getOutputStream());
                            ps.println(jsonToSend);
                            ps.flush();

                        }

                    }
                }

            }
        } catch (Exception e) {
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
        btnConnect.setDisable(true);
        clientName = name.getText();

        Socket s = new Socket(serverIP.getText(), SERVER_PORT);
        new Thread(new ClientThread_C(s, this)).start();
    }

    @Override
    public void sendMessage(String message) {
        logContent.appendText(message);
    }
}