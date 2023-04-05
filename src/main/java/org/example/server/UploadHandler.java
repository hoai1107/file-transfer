package org.example.server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

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

            ByteBuffer nameBuffer = ByteBuffer.allocate(filenameLen);
            clientChannel.read(nameBuffer);
            nameBuffer.flip();
            filename = new String(nameBuffer.array());
            nameBuffer.clear();

            Path path = Path.of(ServerVariable.getRootDirectory(), filename);

//            System.out.println(path.getFileName());

            int bytesRead;
            ByteBuffer byteBuffer = ByteBuffer.allocate(BUFFER_SIZE);

            while (true) {
                bytesRead = clientChannel.read(byteBuffer);
//                System.out.println(bytesRead);

                if (bytesRead == -1) {
                    break;
                }

                byteBuffer.flip();
                byte[] chunk = new byte[bytesRead];
                byteBuffer.get(chunk);
                Files.write(path, chunk, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                byteBuffer.compact();
            }

            System.out.println("Finish uploading file " + filename);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
