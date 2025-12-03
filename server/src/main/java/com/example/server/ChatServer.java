package com.example.server;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
public class ChatServer {
    private static final int PORT = 8888;
    // 线程安全的集合，存储所有客户端连接
    private static final Set<PrintWriter> clientWriters = ConcurrentHashMap.newKeySet();

    public static void main(String[] args) throws Exception { // 注意加 throws Exception
        // 【新增】强行把 System.out 的编码设为 UTF-8
        System.setOut(new java.io.PrintStream(System.out, true, "UTF-8"));
        System.out.println(">>> 聊天服务器启动，端口: " + PORT);
        System.out.println(">>> 请确保手机和电脑在同一 WiFi 下，并查看电脑 IP 地址。");

        // 使用线程池处理并发连接
        ExecutorService pool = Executors.newFixedThreadPool(50);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("新设备连接: " + clientSocket.getInetAddress());
                pool.execute(new ClientHandler(clientSocket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ClientHandler implements Runnable {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
                out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);

                clientWriters.add(out);

                String message;
                while ((message = in.readLine()) != null) {
                    System.out.println("转发消息: " + message);
                    // 广播给所有连接的客户端
                    broadcast(message);
                }
            } catch (IOException e) {
                System.out.println("客户端断开连接");
            } finally {
                if (out != null) clientWriters.remove(out);
                try { socket.close(); } catch (IOException e) {}
            }
        }

        private void broadcast(String message) {
            for (PrintWriter writer : clientWriters) {
                try {
                    writer.println(message);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}