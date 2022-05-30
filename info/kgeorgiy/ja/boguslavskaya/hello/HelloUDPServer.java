package info.kgeorgiy.ja.boguslavskaya.hello;

import info.kgeorgiy.java.advanced.hello.HelloServer;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class HelloUDPServer implements HelloServer {

    private DatagramSocket socket;
    private ExecutorService threadExecutor, threadPool;
    DatagramPacket response;


    public static void main(String[] args) {
        if (args == null || args.length != 2 || args[0] == null || args[1] == null){
            System.out.println("Wrong arguments.");
            return;
        }
        int port = Integer.parseInt(args[0]);
        int threads = Integer.parseInt(args[1]);
        HelloServer server = new HelloUDPServer();
        server.start(port, threads);
    }

    @Override
    public void start(int port, int threads) {
        try {
            socket = new DatagramSocket(port);
            threadExecutor = Executors.newSingleThreadExecutor();
            threadPool = Executors.newFixedThreadPool(threads);
        } catch (SocketException e){
            System.err.println(e.getMessage());
            return;
        }
        threadExecutor.submit(() -> {
            while (!socket.isClosed()){
                try {
                    DatagramPacket responsePacket = new DatagramPacket(new byte[socket.getReceiveBufferSize()], socket.getReceiveBufferSize());
                    socket.receive(responsePacket);
                    threadPool.submit(() -> task(responsePacket, socket));
                } catch (SocketException e) {
                    // e.printStackTrace();
                } catch (IOException e){
                    if (!socket.isClosed()){
                        System.err.println(e.getMessage());
                    }
                }
            }
        });
    }

    private void task(DatagramPacket responsePacket, DatagramSocket socket){
            String stringResponse = new String(responsePacket.getData(), responsePacket.getOffset(), responsePacket.getLength(), StandardCharsets.UTF_8);
//            DatagramPacket response = new DatagramPacket(new byte[0], 0, responsePacket.getSocketAddress());
            responsePacket.setData(("Hello, " + stringResponse).getBytes(StandardCharsets.UTF_8));
            try {
                socket.send(responsePacket);
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
    }

    @Override
    public void close() {
        socket.close();
        threadExecutor.shutdownNow();
        threadPool.shutdownNow();
        try{
            threadPool.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e){
            System.err.println(e.getMessage());
        }
    }

}
