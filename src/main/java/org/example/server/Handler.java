package org.example.server;

import java.io.File;
import java.nio.channels.SocketChannel;

public interface Handler {
    String ROOT_DIRECTORY = System.getProperty("user.dir") + File.separator + "files";
    int BUFFER_SIZE = 1024 * 1024;

    void handle(SocketChannel clientChannel);
}
