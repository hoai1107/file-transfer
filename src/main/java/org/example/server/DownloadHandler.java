package org.example.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.Path;

public class DownloadHandler implements Handler {
    @Override
    public void handle(SocketChannel clientChannel) {
        try {
            ByteBuffer byteBuffer = ByteBuffer.allocate(BUFFER_SIZE);
            clientChannel.read(byteBuffer);

            String filename = new String(byteBuffer.array()).trim();
            Path file = Path.of(ServerVariable.getRootDirectory(), filename);
//            File file = new File(ServerVariable.getRootDirectory() + File.separator + filename);

            byteBuffer.clear();

            if (Files.exists(file)) {
                byteBuffer.putLong(Files.size(file));
                byteBuffer.flip();
                clientChannel.write(byteBuffer);
                byteBuffer.clear();

                try (FileChannel fileChannel = FileChannel.open(file)) {
                    while (fileChannel.read(byteBuffer) > 0) {
                        byteBuffer.flip();
                        clientChannel.write(byteBuffer);
                        byteBuffer.compact();

                        Thread.sleep(500);
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            } else {
                byteBuffer.put("File not found on channel.".getBytes());
                byteBuffer.flip();
                clientChannel.write(byteBuffer);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
