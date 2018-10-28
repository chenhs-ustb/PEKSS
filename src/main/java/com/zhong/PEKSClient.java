package com.zhong;

import javax.crypto.SecretKey;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.MessageDigest;
import java.util.Random;

class PEKSCFrame extends JFrame implements ActionListener {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    JTextArea jta;
    JScrollPane jsp;
    JPanel eastPanel;
    JTextArea jta1;
    JScrollPane jsp1;
    JButton connect;
    JButton plainText;
    JButton cipherText;
    JPanel buttonPanel;
    BigInteger modules, eulermodules, generator;// system parameters
    ObjectInputStream ois;
    ObjectOutputStream oos;
    SecretKey key;
    Socket clientS;
    InputStream is;
    InetSocketAddress inetsa;
    String keyword;// keyword for search
    MessageDigest mdc;// hash function ( SHA1)
    boolean tempB;
    JPanel centerPanel;
    JPanel displayPanel;
    JPanel updatePanel;
    JPanel searchPanel;
    JPanel resultPanel;
    JTabbedPane jtp;
    JFileChooser result;
    JFileChooser update;
    JButton search;
    JTextField keywordField;
    DesEncrypter encrypter;
    boolean connected = true;

    /**
     * constructor, initial the graphic user interface
     */
    PEKSCFrame() {
        setTitle("PEKSClient");
        try {
            UIManager.setLookAndFeel("com.jtattoo.plaf.acryl.AcrylLookAndFeel");
            SwingUtilities.updateComponentTreeUI(rootPane);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        jta = new JTextArea(10, 10);
        jsp = new JScrollPane(jta1);
        jta.setLineWrap(true);
        jta.setEditable(true);
        jta.append("success!");

        jta1 = new JTextArea(10, 10);
        jsp1 = new JScrollPane(jta1);
        jta1.setLineWrap(true);
        displayPanel = new JPanel();
        displayPanel.setLayout(new BorderLayout());
        displayPanel.add(jsp1, BorderLayout.CENTER);
        connect = new JButton("Connect");
        displayPanel.add(connect, BorderLayout.SOUTH);
        connect.addActionListener(this);

        updatePanel = new JPanel(new BorderLayout());
        update = new JFileChooser("testset");
        update.setApproveButtonText("Update");
        updatePanel.add(update, BorderLayout.CENTER);

        searchPanel = new JPanel();
        keywordField = new JTextField(20);
        searchPanel.add(keywordField);
        search = new JButton("Search");
        searchPanel.add(search);
        search.addActionListener(this);

        resultPanel = new JPanel(new BorderLayout());
        result = new JFileChooser("search result");
        result.setControlButtonsAreShown(false);
        resultPanel.add(result, BorderLayout.CENTER);
        buttonPanel = new JPanel();
        cipherText = new JButton("Open the File");
        plainText = new JButton("Decrypt and Open");
        buttonPanel.add(cipherText);
        cipherText.addActionListener(this);
        buttonPanel.add(plainText);
        plainText.addActionListener(this);
        resultPanel.add(buttonPanel, BorderLayout.SOUTH);

        jtp = new JTabbedPane();
        jtp.add("Initial Information", displayPanel);
        jtp.add("Update File", updatePanel);
        jtp.add("Search Encrypted File", searchPanel);
        jtp.add("Results", resultPanel);

        centerPanel = new JPanel();
        centerPanel.setLayout(new BorderLayout());
        centerPanel.add(jtp, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        inetsa = new InetSocketAddress("127.0.0.1", 10086);

        setLocation(300, 10);
        pack();
        setResizable(false);

        tempB = false;
        loadParameters();
        displayInitialInformation();
    }

    /**
     * read in the system parameters
     */
    public void loadParameters() {
        try {
            ois = new ObjectInputStream(new FileInputStream("initial.data"));
            key = (SecretKey) ois.readObject();
            modules = (BigInteger) ois.readObject();
            generator = (BigInteger) ois.readObject();
            eulermodules = (BigInteger) ois.readObject();
            ois.close();
            mdc = MessageDigest.getInstance("SHA");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == connect) {
            clientS = new Socket();
            try {
                clientS.connect(inetsa, 10086);
                oos = new ObjectOutputStream(clientS.getOutputStream());
                ois = new ObjectInputStream(clientS.getInputStream());
            } catch (Exception e1) {
                e1.printStackTrace();
            }
            connect.setEnabled(false);
        }
        if (e.getSource() == search) {
            // generate token for search
            if (keywordField.getText().length() == 0) {
                JOptionPane.showMessageDialog(null, "Sorry, you have not input a valid keyword! Please try again!");
            } else {
                keyword = keywordField.getText().trim();
                mdc.update(keyword.getBytes());
                // h(w)
                BigInteger temp1 = new BigInteger(mdc.digest()).abs().nextProbablePrime();
                mdc.reset();
                // generate -h(w)
                BigInteger temp2 = temp1.modInverse(eulermodules);
                // generate r
                BigInteger temp3 = new BigInteger(512, new Random());
                // -r * h(w)
                BigInteger temp4 = temp2.multiply(temp3);
                // generate g^r
                BigInteger temp5 = generator.modPow(temp3, modules);

                Trapdoor t = new Trapdoor(temp4, temp5);

                try {
                    oos.writeObject(t);
                    oos.flush();
                    tempB = ois.readBoolean();
                    if (tempB) {
                        JOptionPane.showMessageDialog(jtp, "Search successed!");
                        String fileName = (String) ois.readObject();
                        System.out.println(fileName);
                        File tempFile = new File(fileName);
                        FileInputStream fis = new FileInputStream(tempFile);
                        String path = System.getProperty("user.dir");
                        FileOutputStream fos = new FileOutputStream(path+File.separator+"search result"+File.separator + tempFile.getName());
                        int i = 0;
                        while ((i = fis.read()) != -1) {
                            fos.write(i);
                        }
                        fis.close();
                        fos.close();
                    } else {
                        JOptionPane.showMessageDialog(jtp, "No data matches your keywords!");
                    }

                } catch (Exception e1) {
                    e1.printStackTrace();
                }

            }
        }
        // open file button clicked
        if (e.getSource() == cipherText) {
            if (result.getSelectedFile() == null) {
                JOptionPane.showMessageDialog(jtp,
                        "No Files selected, Please try again!");
            } else {
                String[] command = {"notepad",
                        result.getSelectedFile().getAbsolutePath()};
                try {
                    Runtime.getRuntime().exec(command);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }

        if (e.getSource() == plainText) {
            if (result.getSelectedFile() == null) {
                JOptionPane.showMessageDialog(jtp, "No Files selected, Please try again!");
            } else {
                try {
                    encrypter = new DesEncrypter(key);
                    encrypter.decrypt(
                            new FileInputStream(result.getSelectedFile()),
                            new FileOutputStream("decrypt"
                                    + result.getSelectedFile().getName()));
                    String[] command = {"notepad",
                            "decrypt" + result.getSelectedFile().getName()};
                    Runtime.getRuntime().exec(command);
                    result.rescanCurrentDirectory();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    /**
     * display the encryption key and generator, modules, eulermodules;
     */
    private void displayInitialInformation() {
        byte[] keyBytes = key.getEncoded();
        jta1.append("DES Key:\t");
        for (int i = 0; i < keyBytes.length; i++) {
            jta1.append(byte2HexStr(keyBytes[i]));
        }
        jta1.append("\n\n Modules:\t" + modules.toString(16));
        jta1.append("\n\n EulerModules:\t" + eulermodules.toString(16));
        jta1.append("\n\n Generator:\t" + generator.toString(16));
    }

    private String byte2HexStr(byte binary) {
        StringBuffer sb = new StringBuffer();
        int hex;

        hex = (int) binary & 0x000000ff;
        if (0 != (hex & 0xfffffff0)) {
            sb.append(Integer.toHexString(hex));
        } else {
            sb.append("0" + Integer.toHexString(hex));
        }
        return sb.toString();
    }

}

public class PEKSClient {
    public static void main(String[] args) {
        String path = System.getProperty("user.dir");
        File dir = new File(path,"search result") ;
        if(!dir.exists()){
            dir.mkdirs();
        }
        new Thread() {
            public void run() {
                PEKSCFrame peksc = new PEKSCFrame();
                peksc.setVisible(true);
                peksc.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            }
        }.start();
    }
}