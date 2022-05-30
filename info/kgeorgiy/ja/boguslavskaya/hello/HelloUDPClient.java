package info.kgeorgiy.ja.boguslavskaya.hello;

import info.kgeorgiy.java.advanced.hello.HelloClient;

import java.io.IOException;
import java.net.*;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class HelloUDPClient implements HelloClient {

    public static void main(String[] args) {
        if (args == null || args.length != 5 || args[0] == null || args[1] == null
            || args[2] == null || args[3] == null || args[4] == null){
            System.out.println("Wrong arguments.");
            return;
        }
        String host = args[0];
        int port = Integer.parseInt(args[1]);
        String prefix = args[2];
        int threads = Integer.parseInt(args[3]);
        int requests = Integer.parseInt(args[4]);
        HelloClient client = new HelloUDPClient();
        client.run(host, port, prefix, threads, requests);
    }

    @Override
    public void run(String host, int port, String prefix, int threads, int requests) {
        InetSocketAddress address = new InetSocketAddress(host, port);
        ExecutorService threadPool = Executors.newFixedThreadPool(threads);
        for (int i = 0; i < threads; i++){
            final int finalI = i;
            threadPool.submit(new Thread(() -> task(requests, prefix, finalI, address)));
        }
        threadPool.shutdownNow();
        try {
            threadPool.awaitTermination(threads * requests * 100, TimeUnit.MINUTES);
        } catch (InterruptedException e){
            System.err.println(e.getMessage());
        }
    }

    private void task(int requests, String prefix, int finalI, InetSocketAddress address){
        try(DatagramSocket socket = new DatagramSocket()){
            socket.setSoTimeout(100);
            DatagramPacket responsePacket = new DatagramPacket(new byte[socket.getReceiveBufferSize()], socket.getReceiveBufferSize());
            for (int k = 0; k < requests; k++){
                String stringRequest = String.format("%s%d_%d", prefix, finalI, k);
                byte[] bytes = stringRequest.getBytes(StandardCharsets.UTF_8);
                DatagramPacket requestPacket = new DatagramPacket(bytes, bytes.length, address);
                while (true){
                    try {
                        socket.send(requestPacket);
                        socket.receive(responsePacket);
                    } catch (IOException e){
                        System.err.println(e.getMessage());
                        continue;
                    }
                    String res = new String(responsePacket.getData(), responsePacket.getOffset(), responsePacket.getLength(), StandardCharsets.UTF_8);
                    if (res.contains(stringRequest)){
                        System.out.println(res);
                        break;
                    }
                }
            }
        } catch (SocketException e){
            System.err.println(e.getMessage());
        }
    }

}
