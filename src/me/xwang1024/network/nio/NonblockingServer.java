package me.xwang1024.network.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Iterator;
import java.util.Set;

import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream;

public class NonblockingServer {
	// 非阻塞，连接对应线程，这个例子中打开了两个通道：服务器和客户端
	public static void mainOld(String[] args) {
		try {
			ServerSocketChannel serverChannel = ServerSocketChannel.open(); // 打开服务器通道，但没有监听任何端口
			ServerSocket serverSocket = serverChannel.socket(); // 获得对等peer端对象
			serverSocket.bind(new InetSocketAddress(8888)); // 将对等peer端绑定到端口
			serverChannel.configureBlocking(false); // 设置是否非阻塞
			while (true) {
				System.out.println("Waiting for new connection...");
				SocketChannel clientChannel = serverChannel.accept(); // accept一个连接并拿到他的通道，这种方法仍然是连接和线程对应，我们的目标是事件和线程对应
				if (clientChannel != null) {
					System.out.println("Here comes a new connection");
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// 非阻塞，事件对应线程
	public static void main(String[] args) {
		ServerSocketChannel serverChannel;
		Selector selector;
		try { // 准备阶段
			serverChannel = ServerSocketChannel.open(); // 打开服务器通道，但没有监听任何端口
			ServerSocket serverSocket = serverChannel.socket(); // 获得对等peer端对象
			serverSocket.bind(new InetSocketAddress(8888)); // 将对等peer端绑定到端口
			serverChannel.configureBlocking(false); // 设置是否非阻塞
			selector = Selector.open(); // 新建一个事件选择器，一个选择器可以监听不同通道的不同事件！

			// SelectionKey：一个键对应一个对等端，并且可以绑定一个对象记录这个对等端的状态和行为，也可以保存向这个对等端的操作（例如：保存要写入的缓冲）
			SelectionKey serverKey = serverChannel.register(selector, SelectionKey.OP_ACCEPT); // 在服务器的通道上注册事件选择器，关注的事件是accept（serverChannel只能是这个）
			System.out.println("[Key]" + serverKey);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		while (true) {
			try { // 事件捕获和处理
				System.out.println("Waiting for key...");
				selector.select(); // 该方法是阻塞的，选择发生了关注事件的键
			} catch (IOException e) {
				e.printStackTrace();
				break;
			}
			Set<SelectionKey> readyKeys = selector.selectedKeys(); // 取得键集
			for (Iterator<SelectionKey> iterator = readyKeys.iterator(); iterator.hasNext();) { // 对于每一个键
				SelectionKey actionKey = iterator.next();
				iterator.remove(); // 删除相应的键，因为已经被获取
				try {
					if ((actionKey.interestOps() & SelectionKey.OP_ACCEPT) != 0) { // 发出accept事件的只有可能是服务器的通道
						System.out.println("[Key]" + actionKey + " OP_ACCEPT");
						ServerSocketChannel serverActionChannel = (ServerSocketChannel) actionKey.channel();
						System.out.println("[ServerChannel]" + serverActionChannel);
						SocketChannel clientChannel = serverActionChannel.accept();
						System.out.println("[ClientChannel]" + clientChannel + " accepted");
						clientChannel.configureBlocking(false);
						SelectionKey clientKey = clientChannel.register(selector, SelectionKey.OP_WRITE);
						clientKey.attach("Test Client");
					}
					if ((actionKey.interestOps() & SelectionKey.OP_WRITE) != 0) { // 发出write事件的只有可能是客户端的通道
						System.out.println("[Key]" + actionKey + " OP_WRITE");
						System.out.println("[Attachment]" + actionKey.attachment());
						SocketChannel clientChannel = (SocketChannel) actionKey.channel();
						ByteBuffer buffer = ByteBuffer.allocate(5);
						String data = "Welcome connect to my server!";
						byte[] byteData = data.getBytes();
						for (int i = 0; i < byteData.length / buffer.capacity() + 1; i++) {
							buffer.rewind();
							int len = byteData.length - i*buffer.capacity();
							len = len > buffer.capacity()?buffer.capacity():len;
							buffer.put(byteData,i*buffer.capacity(),len);
							buffer.flip();
							clientChannel.write(buffer);
						}
						actionKey.interestOps(SelectionKey.OP_READ);
						actionKey.selector().wakeup();
					}
					if ((actionKey.interestOps() & SelectionKey.OP_READ) != 0) { // 发出write事件的只有可能是客户端的通道
						System.out.println("[Key]" + actionKey + " OP_READ");
						System.out.println("[Attachment]" + actionKey.attachment());
						SocketChannel clientChannel = (SocketChannel) actionKey.channel();
						WritableByteChannel output = Channels.newChannel(System.out);
						ByteBuffer buffer = ByteBuffer.allocate(128);
						int readStatus = 0;
						while(true) {
							if((readStatus = clientChannel.read(buffer)) > 0) {
								System.out.print("[Read:"+readStatus+"]");
								buffer.flip(); // 缓冲区读写翻转，指针从头开始
								output.write(buffer); // 缓冲区的内容输出到指向console的通道中
								buffer.clear(); // 结束本次读取过程，清空缓冲区
							} else if (readStatus == -1) {
								// 客户端断开了连接
								System.out.print("[End Read:"+readStatus+"]");
								break;
							}
						}
						actionKey.interestOps(SelectionKey.OP_WRITE);
					}
				} catch (Exception e) {
					actionKey.cancel();
					try {
						actionKey.channel().close();
					} catch (IOException e1) {
					}
					e.printStackTrace();
				}
			}
		}
	}
}
