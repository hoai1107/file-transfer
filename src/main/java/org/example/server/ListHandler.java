package org.example.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class ListHandler implements Handler {

    @Override
    public void handle(SocketChannel clientChannel) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("ls", ServerVariable.getRootDirectory());
            Process p = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                line += '\n';
                ByteBuffer buffer = ByteBuffer.wrap(line.getBytes());
                clientChannel.write(buffer);
            }

            int exitCode = p.waitFor();
            // Print the exit code
            System.out.println("Process exited with code " + exitCode);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
