package org.example.server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.Path;

public class UploadHandler implements Handler {
    @Override
    public void handle(SocketChannel clientChannel) {
        try {
            int filenameLen;
            String filename;

            ByteBuffer intBuffer = ByteBuffer.allocate(Integer.BYTES);
            clientChannel.read(intBuffer);
            intBuffer.flip();
            filenameLen = intBuffer.getInt();
            intBuffer.clear();

            System.out.println(filenameLen);

            ByteBuffer nameBuffer = ByteBuffer.allocate(filenameLen);
            clientChannel.read(nameBuffer);
            nameBuffer.flip();
            filename = new String(nameBuffer.array());
            nameBuffer.clear();

            System.out.println(filename);

            Path path = Path.of(ServerVariable.getRootDirectory(), filename);
            if (!Files.exists(path)) {
                Files.createFile(path);
            }

            System.out.println(path.toAbsolutePath());

            int bytesRead;
            ByteBuffer byteBuffer = ByteBuffer.allocate(BUFFER_SIZE);
            while ((bytesRead = clientChannel.read((byteBuffer))) > 0) {
                byte[] chunk = new byte[bytesRead];
                byteBuffer.flip();
                byteBuffer.get(chunk);
                Files.write(path, chunk);
                byteBuffer.clear();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
