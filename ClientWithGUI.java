import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;
import java.util.Map;

public class ClientWithGUI {
    private ObjectOutputStream outStream;
    private ObjectInputStream inStream;

    private JFrame frame;
    private JTextField keywordField;
    private JTextField optionsField;
    private JButton submitButton;
    private JButton dictionaryButton;
    private JComboBox<String> keywordList;
    private JButton showTextButton;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                new ClientWithGUI().createAndShowGUI();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void createAndShowGUI() throws IOException {
        frame = new JFrame("Keyword Client");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        keywordField = new JTextField(20);
        optionsField = new JTextField(20);
        submitButton = new JButton("Submit");
        dictionaryButton = new JButton("Get Dictionary");
        keywordList = new JComboBox<>();
        showTextButton = new JButton("Show Text");

        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String keyword = keywordField.getText();
                String options = optionsField.getText();

                if (!keyword.isEmpty() && !options.isEmpty()) {
                    try {
                        String response = sendRequestToServer("fileRequest", keyword, options);
                        showResponseDialog(response);
                        loadKeywordList();
                    } catch (IOException | ClassNotFoundException ex) {
                        ex.printStackTrace();
                    }
                } else {
                    JOptionPane.showMessageDialog(frame, "Please enter both keyword and options.");
                }
            }
        });

        dictionaryButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    Map<String, String> dictionary = getDictionaryFromServer();
                    showDictionaryDialog(dictionary);
                } catch (IOException | ClassNotFoundException ex) {
                    ex.printStackTrace();
                }
            }
        });

        showTextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String selectedKeyword = (String) keywordList.getSelectedItem();
                    if (selectedKeyword != null) {
                        String text = getTextFromServer(selectedKeyword);
                        showTextDialog(selectedKeyword, text);
                    } else {
                        JOptionPane.showMessageDialog(frame, "Please select a keyword.");
                    }
                } catch (IOException | ClassNotFoundException ex) {
                    ex.printStackTrace();
                }
            }
        });

        JPanel panel = new JPanel();
        panel.add(new JLabel("Enter Keyword: "));
        panel.add(keywordField);
        panel.add(new JLabel("Enter Options: "));
        panel.add(optionsField);
        panel.add(submitButton);
        panel.add(dictionaryButton);

        JPanel textPanel = new JPanel();
        textPanel.add(new JLabel("Select Keyword: "));
        textPanel.add(keywordList);
        textPanel.add(showTextButton);

        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(BorderLayout.NORTH, panel);
        frame.getContentPane().add(BorderLayout.CENTER, textPanel);

        frame.setSize(500, 250);
        frame.setVisible(true);

        loadKeywordList();
    }

    private void loadKeywordList() {
        try {
            Map<String, String> dictionary = getDictionaryFromServer();

            keywordList.removeAllItems();
            for (String keyword : dictionary.keySet()) {
                keywordList.addItem(keyword);
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private String sendRequestToServer(String requestType, String keyword, String options) throws IOException, ClassNotFoundException {
        Socket socket = new Socket("127.0.0.1", 12345);

        outStream = new ObjectOutputStream(socket.getOutputStream());
        inStream = new ObjectInputStream(socket.getInputStream());

        outStream.writeObject(requestType);
        outStream.writeObject(keyword);
        outStream.writeObject(options);

        String response = (String) inStream.readObject();

        outStream.close();
        inStream.close();
        socket.close();

        return response;
    }

    private Map<String, String> getDictionaryFromServer() throws IOException, ClassNotFoundException {
        Socket socket = new Socket("127.0.0.1", 12345);

        outStream = new ObjectOutputStream(socket.getOutputStream());
        inStream = new ObjectInputStream(socket.getInputStream());

        outStream.writeObject("dictionaryRequest");

        @SuppressWarnings("unchecked")
        Map<String, String> dictionary = (Map<String, String>) inStream.readObject();

        outStream.close();
        inStream.close();
        socket.close();

        return dictionary;
    }

    private String getTextFromServer(String keyword) throws IOException, ClassNotFoundException {
        Socket socket = new Socket("127.0.0.1", 12345);

        outStream = new ObjectOutputStream(socket.getOutputStream());
        inStream = new ObjectInputStream(socket.getInputStream());

        outStream.writeObject("textRequest");
        outStream.writeObject(keyword);

        String text = (String) inStream.readObject();

        outStream.close();
        inStream.close();
        socket.close();

        return text;
    }

    private void showResponseDialog(String response) {
        JOptionPane.showMessageDialog(frame, "Server response: " + response);
    }

    private void showDictionaryDialog(Map<String, String> dictionary) {
        StringBuilder dictionaryText = new StringBuilder("Dictionary:\n");

        for (String keyword : dictionary.keySet()) {
            dictionaryText.append(keyword).append("\n");
        }

        JOptionPane.showMessageDialog(frame, dictionaryText.toString(), "Keyword List", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showTextDialog(String keyword, String text) {
        JTextArea textArea = new JTextArea(20, 40);
        textArea.setText(text);
        textArea.setEditable(false);

        JScrollPane scrollPane = new JScrollPane(textArea);

        JOptionPane.showMessageDialog(frame, scrollPane, "Text for " + keyword, JOptionPane.PLAIN_MESSAGE);
    }
}
