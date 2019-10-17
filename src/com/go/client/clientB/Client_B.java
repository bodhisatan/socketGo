package com.go.client.clientB;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.go.AI.AIForB;
import com.go.util.ChessBoard;
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

import static com.go.client.clientB.Client_B.*;
import static java.lang.Thread.sleep;

/**
 * Client
 */

class ClientThread_B implements Runnable {
    private Socket s;
    private BufferedReader br = null;
    private int myColor;
    private MessageTrans ms;
    private AIForB aiForB;
    ClientThread_B(Socket s, MessageTrans messageTrans) throws IOException {
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

                    // 假如自己是先手
                    String prePlayer = br.readLine();
                    if (clientName.equals(prePlayer)) {

                        myColor = ChessBoard.BLACK;
                        aiForB = new AIForB(myColor);

                        // 落第一步棋
                        PrintStream ps = new PrintStream(s.getOutputStream());
                        aiForB.place(8 - 1, 8 - 1, myColor);
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
                        aiForB = new AIForB(ChessBoard.WHITE);
                        myColor = ChessBoard.WHITE;
                    }

                } else {
                    JSONObject jsonFromServer = JSON.parseObject(content);
                    int color = jsonFromServer.getIntValue("color");
                    if (color != myColor) {

                        int x = jsonFromServer.getIntValue("x");
                        int y = jsonFromServer.getIntValue("y");
                        boolean isEnd = jsonFromServer.getBooleanValue("isEnd");

                        aiForB.place(x - 1, y - 1, color);
                        if (isEnd) {
                            System.out.println("[log] 本次发送的消息：" + content);
                            ms.sendMessage("[log] 本次发送的消息：" + content + "\n");
                            PrintStream ps = new PrintStream(s.getOutputStream());
                            ps.println(content);
                            ps.flush();
                        } else {

                            int rob[] = aiForB.forEach();
                            aiForB.place(rob[0], rob[1], myColor);
                            int rel = aiForB.isEnd(rob[0], rob[1], myColor);
                            if (rel != 0) isEnd = true;
                            JSONObject jsonToSend = new JSONObject();
                            jsonToSend.put("x", rob[0] + 1);
                            jsonToSend.put("y", rob[1] + 1);
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

public class Client_B implements ClientThread_B.MessageTrans {
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
        new Thread(new ClientThread_B(s, this)).start();
    }

    @Override
    public void sendMessage(String message) {
        logContent.appendText(message);
    }
}