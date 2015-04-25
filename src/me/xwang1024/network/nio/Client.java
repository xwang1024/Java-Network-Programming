package me.xwang1024.network.nio;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.SocketChannel;
import java.nio.channels.WritableByteChannel;

public class Client {
	public static void main(String[] args) {
		try {
			SocketAddress localhost = new InetSocketAddress("localhost", 8888);
			SocketChannel client = SocketChannel.open(localhost); // 使用工厂新建一个套接字通道
			client.configureBlocking(true); // 设置为非阻塞
			ByteBuffer buffer = ByteBuffer.allocate(5); // 开辟一个大小为128字节的字节型缓冲区
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			WritableByteChannel output = Channels.newChannel(baos); // 如果想把收到的消息显示再console中，新建一个指向System.out的通道
			System.out.println("Ready to get response");
			int readStatus = 0;
			// 阻塞式循环
//			while ((readStatus = client.read(buffer)) != -1) { // 从socket中读取信息并填充到buffer中，返回-1表示数据结束
//				System.out.print("[Read:"+readStatus+"]");
//				buffer.flip(); // 缓冲区读写翻转，指针从头开始
//				output.write(buffer); // 缓冲区的内容输出到指向console的通道中
//				buffer.clear(); // 结束本次读取过程，清空缓冲区
//			}
			// 非阻塞式循环
			while(true) {
				if((readStatus = client.read(buffer)) > 0) {
					System.out.print("[Read:"+readStatus+"]");
					buffer.flip(); // 缓冲区读写翻转，指针从头开始
					output.write(buffer); // 缓冲区的内容输出到指向console的通道中
					buffer.clear(); // 结束本次读取过程，清空缓冲区
				} else if (readStatus == -1) {
					// 服务器断开了连接
					System.out.println("[End Read:"+readStatus+"]");
					break;
				}
			}
			System.out.println(new String(baos.toByteArray()));

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
