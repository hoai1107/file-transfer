package org.example.server;

import javax.swing.plaf.synth.SynthTextAreaUI;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class FileTransferServer {

    private static final int DOWNLOAD_PORT = 8001;
    private static final int UPLOAD_PORT = 8002;
    private static final int LIST_PORT = 8003;

    public static void main(String[] args) {

        if (args.length == 0) {
            System.err.println("Usage: java FileServer <path/to/root/directory>");
            return;
        }

        ServerVariable.setRootDirectory(args[0]);

        if (!Files.exists(Path.of(ServerVariable.getRootDirectory()))) {
            System.err.printf("Directory %s not exist.", ServerVariable.getRootDirectory());
            return;
        }

        Map<SelectableChannel, Handler> channelHandlerMap = new HashMap<>();

        try (ServerSocketChannel uploadChannel = ServerSocketChannel.open();
             ServerSocketChannel downloadChannel = ServerSocketChannel.open();
             ServerSocketChannel listChannel = ServerSocketChannel.open();
        ) {
            uploadChannel.bind(new InetSocketAddress(UPLOAD_PORT));
            uploadChannel.configureBlocking(false);

            downloadChannel.bind(new InetSocketAddress(DOWNLOAD_PORT));
            downloadChannel.configureBlocking(false);

            listChannel.bind(new InetSocketAddress(LIST_PORT));
            listChannel.configureBlocking(false);

            Selector selector = Selector.open();

            uploadChannel.register(selector, SelectionKey.OP_ACCEPT);
            downloadChannel.register(selector, SelectionKey.OP_ACCEPT);
            listChannel.register(selector, SelectionKey.OP_ACCEPT);

            System.out.printf("Download server starts on port %d\n", DOWNLOAD_PORT);
            System.out.printf("Upload server starts on port %d\n", UPLOAD_PORT);
            System.out.printf("List server starts on port %d\n", LIST_PORT);

            while (true) {
                selector.select();

                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> iter = selectedKeys.iterator();

                while (iter.hasNext()) {
                    SelectionKey key = iter.next();

                    if (!key.isValid()) {
                        continue;
                    }

                    if (key.isAcceptable()) {
                        Handler handler = null;
                        int opKey = 0;
                        ServerSocketChannel channel = (ServerSocketChannel) key.channel();
                        SocketChannel clientChannel = channel.accept();
                        Socket socket = clientChannel.socket();
                        clientChannel.configureBlocking(false);
                        socket.setKeepAlive(false);

                        if (channel == downloadChannel) {
                            opKey = SelectionKey.OP_READ;
                            handler = new DownloadHandler();
                        } else if (channel == listChannel) {
                            opKey = SelectionKey.OP_WRITE;
                            handler = new ListHandler();
                        } else if (channel == uploadChannel) {
                            opKey = SelectionKey.OP_READ;
                            handler = new UploadHandler();
                        }

                        clientChannel.register(selector, opKey);
                        channelHandlerMap.put(clientChannel, handler);
                    } else if (key.isReadable()) {
                        System.out.println("Handling reading ...");

                        try (SocketChannel channel = (SocketChannel) key.channel()) {
                            Handler handler = channelHandlerMap.get(channel);
                            handler.handle(channel);

                            channelHandlerMap.remove(channel);
                        }
                    } else if (key.isWritable()) {
                        System.out.println("Handling writing ...");

                        try (SocketChannel channel = (SocketChannel) key.channel()) {
                            Handler handler = channelHandlerMap.get(channel);
                            handler.handle(channel);

                            channelHandlerMap.remove(channel);
                        }
                    }

                    iter.remove();
                }
            }

        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

}
