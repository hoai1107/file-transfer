package org.example.client;

import picocli.CommandLine;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.SocketChannel;


@CommandLine.Command(
        name = "client",
        description = "Create client to interact with server"
)
public class FileTransferClient implements Runnable {

    @CommandLine.Option(names = {"--server-address"}, defaultValue = "localhost", description = "The address of server")
    private String serverAddress;

    @CommandLine.Option(names = {"--action"}, description = "Available action: download/upload/list")
    private String action;

    private final int DOWNLOAD_PORT = 8001;
    private final int UPLOAD_PORT = 8002;
    private final int LIST_PORT = 8003;

    private final int BUFFER_SIZE = 1024 * 1024 * 2;

    public void run() {
        try {
            int serverPort = 8000;

            switch (action) {
                case "list" -> serverPort = LIST_PORT;
                case "download" -> serverPort = DOWNLOAD_PORT;
                case "upload" -> serverPort = UPLOAD_PORT;
            }

            SocketChannel clientChannel = SocketChannel.open();
            clientChannel.connect(new InetSocketAddress(serverAddress, serverPort));
            Socket socket = clientChannel.socket();
            socket.setReceiveBufferSize(BUFFER_SIZE);

            switch (action) {
                case "list" -> FileTransferClientCommand.getAllFilesName(clientChannel);
                case "download" -> FileTransferClientCommand.getFileFromServer(clientChannel);
                case "upload" -> FileTransferClientCommand.uploadFileToServer(clientChannel);
            }

            clientChannel.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}

