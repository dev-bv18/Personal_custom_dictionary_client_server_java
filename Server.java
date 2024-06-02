import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;

public class Server {
    private static Map<String, String> keywordDictionary = new HashMap<>();

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(12345);
            System.out.println("Server is listening on port 12345...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Accepted connection from " + clientSocket.getInetAddress().getHostAddress());

                // Handle the client in a separate thread
                new Thread(() -> handleClient(clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleClient(Socket clientSocket) {
        try {
            ObjectInputStream inStream = new ObjectInputStream(clientSocket.getInputStream());
            ObjectOutputStream outStream = new ObjectOutputStream(clientSocket.getOutputStream());

            String requestType = (String) inStream.readObject();

            if ("fileRequest".equals(requestType)) {
                String keyword = (String) inStream.readObject();
                String options = (String) inStream.readObject();

                createFileAndAddToDictionary(keyword, options);

                outStream.writeObject("File created successfully.");
            } else if ("dictionaryRequest".equals(requestType)) {
                outStream.writeObject(keywordDictionary);
            } else if ("textRequest".equals(requestType)) {
                String keyword = (String) inStream.readObject();
                String text = getTextForKeyword(keyword);

                outStream.writeObject(text);
            }

            inStream.close();
            outStream.close();
            clientSocket.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static void createFileAndAddToDictionary(String keyword, String options) {
        try {
            File file = new File(keyword + ".txt");
            FileWriter writer = new FileWriter(file);
            writer.write(options);
            writer.close();

            keywordDictionary.put(keyword, options);
            System.out.println("File created: " + keyword + ".txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getTextForKeyword(String keyword) {
        return keywordDictionary.getOrDefault(keyword, "Keyword not found.");
    }
}
