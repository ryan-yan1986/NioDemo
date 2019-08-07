package com.idowran.nio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;

/**
 * 客户端线程类，专门接收服务端响应信息
 *
 */
public class NioClientHandler implements Runnable{
	
	private Selector selector;
	
	public NioClientHandler(Selector selector) {
		this.selector = selector;
	}
	
	public void run() {
		try {
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
					
					// 根据就绪状态，调用对应方法处理业务逻辑
					// 如果是读事件
					if (selectionKey.isReadable()) {
						this.readHandler(selectionKey, selector);
					}
				}
			}
		}catch (IOException e) {
			e.printStackTrace();
		}
		
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
		// 循环读取服务器端的相应信息
		String response = "";
		while (socketChannel.read(byteBuffer) > 0) {
			// 切换buffer为读模式
			byteBuffer.flip();
			// 读取buffer中的内容
			response += Charset.forName("UTF-8").decode(byteBuffer);
		} 
		
		// 将channel再次注册到selector上，监听他的可读事件
		socketChannel.register(selector, SelectionKey.OP_READ);

		// 将服务器端响应信息，打印到本地
		if (response.length() > 0) {
			System.out.println(response);
		}
	}
}
