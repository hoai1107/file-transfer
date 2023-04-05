package org.example.client;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Scanner;

public class FileTransferClientCommand {
    private static final Scanner scanner = new Scanner(System.in);

    private static final int BUFFER_SIZE = 1024 * 1024;

    public static String inputChoice() {
        String choice;

        String menu = """
                list: Get list of available files in server
                download <filename>: Download file from server
                upload <path/to/file>: Upload file to server
                """;

        System.out.print(menu);
        System.out.print("Enter your choice: ");
        choice = scanner.nextLine();
        return choice;
    }

    public static void printHelp() {
        String menu = """
                list: Get list of available files in server
                download: Download file from server
                upload: Upload file to server
                """;

        System.out.print(menu);
    }

    public static void getFileFromServer(SocketChannel clientChannel) {
        try {
            ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
            ByteBuffer lenBuffer = ByteBuffer.allocate(Long.BYTES);

            // Send filename to server
            System.out.print("Enter filename: ");
            String filename = scanner.nextLine();

            buffer.put(filename.getBytes());
            buffer.flip();
            clientChannel.write(buffer);
            buffer.clear();

            // Get fileLength
            clientChannel.read(lenBuffer);
            lenBuffer.flip();
            long fileLength = lenBuffer.getLong();
            lenBuffer.clear();

            Path path = Path.of(filename);

            // Get data from channel and create file
            int bytesRead, bytesReceived = 0;


            while (true) {
                bytesRead = clientChannel.read(buffer);
                if (bytesRead == -1) {
                    break;
                }

                bytesReceived += bytesRead;

                System.out.printf("\r%.2fMB / %.2fMB", (bytesReceived * 1.0) / 1048576, (fileLength * 1.0) / 1048576);

                buffer.flip();
                byte[] chunk = new byte[bytesRead];
                buffer.get(chunk);
                Files.write(path, chunk, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                buffer.compact();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public static void getAllFilesName(SocketChannel clientSocket) {
        ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
        int bytesRead;

        try {
            while ((bytesRead = clientSocket.read(buffer)) > 0) {
                buffer.flip();
                byte[] bytes = new byte[bytesRead];
                buffer.get(bytes);
                System.out.print(new String(bytes));
                buffer.compact();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void uploadFileToServer(SocketChannel clientSocket) throws IOException {

        ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);

        System.out.print("Enter filename to upload: ");
        String filename = scanner.nextLine();

        Path path = Path.of(filename);

        if (!Files.exists(path)) {
            System.err.println("Files not exist.");
            return;
        }

        filename = path.getFileName().toString();

        // Send filename length
        buffer.putInt(filename.length());
        buffer.flip();
        clientSocket.write(buffer);
        buffer.clear();

        // Send filename
        buffer.put(filename.getBytes());
        buffer.flip();
        clientSocket.write(buffer);
        buffer.clear();

        // Send file content
        int progressBarWidth = 40;
        int bytesRead, bytesWritten = 0;
        long totalBytes = Files.size(path);

        try (FileChannel fileChannel = FileChannel.open(path)) {
            while ((bytesRead = fileChannel.read(buffer)) > 0) {
                bytesWritten += bytesRead;

                // Calculate the progress percentage and update the progress bar
                int progress = (int) (bytesWritten * 100 / totalBytes);
                int progressBarLength = (int) (progressBarWidth * progress / 100);
                String progressBar = "[" + "=".repeat(progressBarLength) + " ".repeat(progressBarWidth - progressBarLength) + "]";

                // Print the progress bar to the console
                System.out.print("\r" + progressBar + " " + progress + "%");
                System.out.flush();

                buffer.flip();
                clientSocket.write(buffer);
                buffer.compact();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

