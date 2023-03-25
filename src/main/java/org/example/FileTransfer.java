package org.example;

import org.example.client.FileTransferClient;
import org.example.server.FileTransferServer;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(
        name = "FileTransfer",
        description = "A file transfer application."
)
public class FileTransfer implements Callable<Integer> {
    @Override
    public Integer call() throws Exception {
        return 0;
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new FileTransfer())
                .addSubcommand(FileTransferClient.class)
                .addSubcommand(FileTransferServer.class)
                .execute(args);
        System.exit(exitCode);
    }
}
