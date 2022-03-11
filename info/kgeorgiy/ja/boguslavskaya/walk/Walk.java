package info.kgeorgiy.ja.boguslavskaya.walk;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.io.*;
import java.util.Arrays;

public class Walk {

    private static final int BUFF_SIZE = 1024;
    private static final byte[] ZERO_HASH = new byte[20];
    private static MessageDigest sha1;

    public static void main(String[] args) {
        if (args == null || args.length != 2 || args[0] == null || args[1] == null) {
            System.out.println("Problems with arguments. Please retry. Arguments should go <inputFile> <outputFile>");
            return;
        }

        Path inputFilePath;
        Path outputFilePath;

        Arrays.fill(ZERO_HASH, Byte.parseByte("0"));

        try {
            inputFilePath = Paths.get(args[0]);
            outputFilePath = Paths.get(args[1]);
        } catch (InvalidPathException e) {
            System.err.println("Problems with input/output path: " + e.getMessage());
            return;
        }
        try {
            // :NOTE: вложенный try - fixed
            // :NOTE: getParent() into a variable - fixed
            Path outParent = outputFilePath.getParent();
            if (outParent != null && Files.notExists(outParent)) {
                try {
                    Files.createDirectories(outParent);
                } catch (IOException e) {
                    // :NOTE: err - fixed
                    System.err.println("Can't create path " + e.getMessage());
                    return;
                }
            }

            // :NOTE: try with resources
            BufferedReader filesReader = Files.newBufferedReader(inputFilePath, StandardCharsets.UTF_8);
            BufferedWriter outputWriter = Files.newBufferedWriter(outputFilePath, StandardCharsets.UTF_8);

            String curFile = filesReader.readLine();

            try {
                sha1 = MessageDigest.getInstance("SHA-1");
            } catch (NoSuchAlgorithmException e) {
                System.err.println("Problem with algorithm" + e.getMessage());
                return;
            }

            while (curFile != null) {
                try {
                    // :NOTE: copy paste - fixed
                    Path curFilePath = Paths.get(curFile);
                    writeHash(getHash(curFilePath), outputWriter, curFile);
                } catch (InvalidPathException e) {
                    // :NOTE: выделить метод - fixed
                    writeHash(ZERO_HASH, outputWriter, curFile);
                } finally {
                    curFile = filesReader.readLine();
                }
            }
            outputWriter.close();
            filesReader.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void writeHash(byte[] curHash, BufferedWriter outputWriter, String curFile) throws IOException {
        StringBuilder hash = new StringBuilder();
        for (byte b : curHash) {
            hash.append(String.format("%02x", b));
        }
        // :NOTE: \n - fixed
        outputWriter.write(hash.toString() + " " + curFile);
        outputWriter.newLine();
    }

    private static byte[] getHash(Path filePath) {
        try (InputStream inputStream = Files.newInputStream(filePath)) {
            // :NOTE: move into const - fixed
            sha1.reset();
            int numOfReadBytes;
            // :NOTE: вынести в константу - fixed
            byte[] bytes = new byte[BUFF_SIZE];
            numOfReadBytes = inputStream.read(bytes);
            while (numOfReadBytes != -1) {
                sha1.update(bytes, 0, numOfReadBytes);
                // :NOTE: создание в цикле - fixed
                numOfReadBytes = inputStream.read(bytes);
            }
            // :NOTE: try with resources, unclosed inputStream - fixed
            return sha1.digest();
        } catch (IOException e) {
            // :NOTE: вынести в константу/поле - fixed
            return ZERO_HASH;
        }
    }
}