package org.example.client;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

public class FileTransferClient {

    private static final String SERVER_HOST = "localhost";
    private static final int DOWNLOAD_PORT = 8001;
    private static final int UPLOAD_PORT = 8002;
    private static final int LIST_PORT = 8003;

    public static void main(String[] args) {
        try {
            String choice;
            int port = 8080;

            System.out.println("-------------------------------------------");
            choice = FileTransferClientCommand.inputChoice();

            switch (choice) {
                case "list" -> port = LIST_PORT;
                case "download" -> port = DOWNLOAD_PORT;
                case "upload" -> port = UPLOAD_PORT;
            }

            SocketChannel clientChannel = SocketChannel.open();
            clientChannel.connect(new InetSocketAddress(SERVER_HOST, port));

            switch (choice) {
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

