import java.io.*;
import java.net.Socket;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class FileClient {
    private static String SERVER_IP = "localhost"; // Change this if connecting to a remote server
    private static int SERVER_PORT = 4331;  // Change to your server's port number

    public static void main(String[] args) {
        // Create the frame
        JFrame frame = new JFrame("Upload Python File");
        frame.setSize(450, 180);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Create panel with padding
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));  // Padding: top, left, bottom, right
        frame.add(panel);
        placeComponents(panel);

        frame.setVisible(true);
    }

    private static void placeComponents(JPanel panel) {
        panel.setLayout(new GridBagLayout());  // Use GridBagLayout for better control over component positioning
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10); // Padding between components

        // Label for instruction
        JLabel userLabel = new JLabel("Select Python File (.py):");
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(userLabel, gbc);

        // Text field to show the selected file path
        JTextField filePathField = new JTextField(20);
        filePathField.setEditable(false);  // Make it non-editable, so user cannot manually type file path
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(filePathField, gbc);

        // Select button to open file chooser dialog
        JButton selectButton = new JButton("Select");
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(selectButton, gbc);

        // Upload button to trigger file upload
        JButton uploadButton = new JButton("Upload");
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(uploadButton, gbc);

        // Action listener for the "Select" button
        selectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                int returnVal = fileChooser.showOpenDialog(null);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    filePathField.setText(selectedFile.getAbsolutePath());
                }
            }
        });

        // Action listener for the "Upload" button
        uploadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String filePath = filePathField.getText();
                if (!filePath.isEmpty()) {
                    File file = new File(filePath);
                    if (file.exists() && filePath.endsWith(".py")) {
                        sendFileToServer(file);
                    } else {
                        JOptionPane.showMessageDialog(null, "Please select a valid Python file (.py)!");
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "Please select a file!");
                }
            }
        });
    }

    private static void sendFileToServer(File file) {
        try (Socket socket = new Socket(SERVER_IP, SERVER_PORT)) {
            System.out.println("Connected to server");

            // Send file name first
            OutputStream outputStream = socket.getOutputStream();
            DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
            dataOutputStream.writeUTF(file.getName());

            // Send file content
            FileInputStream fileInputStream = new FileInputStream(file);
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                dataOutputStream.write(buffer, 0, bytesRead);
            }
            fileInputStream.close();
            dataOutputStream.flush();

            // Wait for result from server
            InputStream inputStream = socket.getInputStream();
            DataInputStream dataInputStream = new DataInputStream(inputStream);

            // Read the result size first
            int resultSize = dataInputStream.readInt();

            // Read the actual result
            String result = dataInputStream.readUTF();
            System.out.println("Result from server: " + result);

            // Show result in a dialog box
            JOptionPane.showMessageDialog(null, "Server response:\n" + result);

            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
