package com.idowran.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Scanner;

public class NioClient {
	
	public void start(String nickname) throws IOException {
		// 连接服务器端
		SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress("127.0.0.1", 8000));
		
		// 接收服务器端响应，新开一个线程专门处理服务器响应信息
		Selector selector = Selector.open();
		socketChannel.configureBlocking(false);	// 配置为非阻塞方式
		socketChannel.register(selector, SelectionKey.OP_READ);
		new Thread(new NioClientHandler(selector)).start();
				
		// 向服务器端发送数据
		@SuppressWarnings("resource")
		Scanner scanner = new Scanner(System.in);
		while (scanner.hasNextLine()) {
			String request = scanner.nextLine();
			if (request != null && request.length() > 0) {
				// 写到通道中
				socketChannel.write(Charset.forName("UTF-8").encode(nickname + ": " + request));
			}
		}
	}
	
	public static void main(String[] args) throws IOException {
		NioClient nioClient = new NioClient();
		nioClient.start("C");
	}
}
