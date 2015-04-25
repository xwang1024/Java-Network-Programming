package me.xwang1024.network.nio;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class BlockingServer {
	public static void main(String[] args) {
		try {
			ServerSocket server = new ServerSocket(8888);
			while (true) {
				Socket client = server.accept();
				System.out.println("Accept but sleep");
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e1) {
				}
				System.out.println("Write");
				try {
					PrintWriter out = new PrintWriter(client.getOutputStream());
					out.println("Welcome connect to my server, but I'm not ready, bye~");
					out.flush();
//					client.close();
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					if (client != null) {
						try {
//							client.close();
						} catch (Exception e) {
						}
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
