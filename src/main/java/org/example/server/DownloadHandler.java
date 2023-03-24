package org.example.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;

public class DownloadHandler implements Handler {
    @Override
    public void handle(SocketChannel clientChannel) {
        try {
            ByteBuffer byteBuffer = ByteBuffer.allocate(BUFFER_SIZE);
            clientChannel.read(byteBuffer);

            String filename = new String(byteBuffer.array()).trim();
            File file = new File(ServerVariable.getRootDirectory() + File.separator + filename);

            byteBuffer.clear();

            if (file.exists()) {

                System.out.println(file.length());

                byteBuffer.putLong(file.length());
                byteBuffer.flip();
                clientChannel.write(byteBuffer);
                byteBuffer.clear();

                try (FileInputStream fis = new FileInputStream(file); FileChannel fileChannel = fis.getChannel()) {
                    while (fileChannel.read(byteBuffer) > 0) {
                        byteBuffer.flip();
                        clientChannel.write(byteBuffer);
                        byteBuffer.clear();
                    }
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
