package com.idowran.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;

public class NioServer {
	
	public void start() throws IOException {
		// 1 创建一个selector
		Selector selector = Selector.open();
		// 2 通过ServerSocketChannel创建通道
		ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
		// 3 为channel通道绑定监听端口
		serverSocketChannel.bind(new InetSocketAddress(8000));		
		// 4设置channel为非阻塞
		serverSocketChannel.configureBlocking(false);		
		// 5 将channel注册到selector上，监听连接事件
		serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
		System.out.println("服务器启动成功");
		
		// 6 循环等待新接入的连接
		for(;;) {
			// TODO 获取可用channel数量
			int readyChannels = selector.select();
			if(readyChannels == 0) continue;
			
			// 所有可用的Channel实例
			Set<SelectionKey> selectionKeys = selector.selectedKeys();
			Iterator<SelectionKey> iterator = selectionKeys.iterator();
			while (iterator.hasNext()){
				SelectionKey selectionKey = iterator.next();
				// 移除Set中当前的selectionKey
				iterator.remove();
				
				// 7 根据就绪状态，调用对应方法处理业务逻辑
				// 如果是接入事件
				if (selectionKey.isAcceptable()) {
					this.acceptHandler(serverSocketChannel, selector);
				}
				
				// 如果是读事件
				if (selectionKey.isReadable()) {
					this.readHandler(selectionKey, selector);
				}
			}
		}
	}
	
	/**
	 * 接入事件处理器
	 * @throws IOException 
	 */
	private void acceptHandler(ServerSocketChannel serverSocketChannel, Selector selector) throws IOException {
		// 创建一个socketChannel
		SocketChannel socketChannel = serverSocketChannel.accept();
		// 将socketChannel设置为非阻塞工作模式
		socketChannel.configureBlocking(false);
		// 将channel注册到selector上，监听可读事件
		socketChannel.register(selector, SelectionKey.OP_READ);
		// 回复客户端提示信息
		socketChannel.write(Charset.forName("UTF-8").encode("你与聊天室里其他人都不是朋友关系，请注意隐私安全"));
	}
	
	/**
	 * 可读事件处理器
	 * @throws IOException 
	 */
	private void readHandler(SelectionKey selectionKey, Selector selector) throws IOException {
		// 要从selectionKey中获取到已经就绪的channel
		SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
		// 创建buffer
		ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
		// 循环读取客户端请求信息
		String request = "";
		while (socketChannel.read(byteBuffer) > 0) {
			// 切换buffer为读模式
			byteBuffer.flip();
			// 读取buffer中的内容
			request += Charset.forName("UTF-8").decode(byteBuffer);
		} 
		
		// 将channel再次注册到selector上，监听他的可读事件
		socketChannel.register(selector, SelectionKey.OP_READ);

		// 将客户端发送的请求信息，广播给其他客户端
		if (request.length() > 0) {
			// 广播给其他客户端，待完成...
			this.broadCast(selector, socketChannel, request);
//			System.out.println(":: " + request);
		}
	}
	
	/**
	 * 广播给其他客户端
	 */
	private void broadCast(Selector selector, SocketChannel sourceChannel, String request) {
		// 获取到所有已接入的客户端channel
		Set<SelectionKey> selectionKeys = selector.keys();
		selectionKeys.forEach(selectionKey -> {
			Channel targetChannel = selectionKey.channel();
			// 剔除发消息的客户端
			if (targetChannel instanceof SocketChannel 
					&& targetChannel != sourceChannel) {
				// 将消息发送到targetChannel客户端
				try {
					((SocketChannel) targetChannel).write(Charset.forName("UTF-8").encode(request));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		// 循环想所有channel广播信息
	}
	
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		NioServer nioServer = new NioServer();
		nioServer.start();
	}
	
}
