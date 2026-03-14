import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.*;
import java.awt.*;

public class Main extends JFrame {
    private final JTextArea inputTextArea = new JTextArea();
    private final JTextArea typingArea = new JTextArea();
    private final JTextPane targetPane = new JTextPane();

    private final JButton startButton = new JButton("Start");
    private final JButton resetButton = new JButton("Reset");

    private final JLabel mistakesLabel = new JLabel("Mistakes: 0");
    private final JLabel accuracyLabel = new JLabel("Accuracy: 100.0%");
    private final JLabel wpmLabel = new JLabel("WPM: 0");
    private final JLabel progressLabel = new JLabel("Progress: 0 / 0");

    private String targetText = "";
    private boolean started = false;
    private long startTime = 0;

    public Main() {
        setTitle("Typing Trainer");
        setSize(900, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        setLayout(new BorderLayout(10, 10));

        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setBorder(BorderFactory.createTitledBorder("1. Paste your practice text here"));
        inputTextArea.setLineWrap(true);
        inputTextArea.setWrapStyleWord(true);
        inputTextArea.setFont(new Font("Monospaced", Font.PLAIN, 16));
        inputTextArea.setText("Paste your own text here, then press Start.");
        topPanel.add(new JScrollPane(inputTextArea), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(startButton);
        buttonPanel.add(resetButton);
        topPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new GridLayout(2, 1, 10, 10));

        targetPane.setEditable(false);
        targetPane.setFont(new Font("Monospaced", Font.PLAIN, 18));
        JScrollPane targetScroll = new JScrollPane(targetPane);
        targetScroll.setBorder(BorderFactory.createTitledBorder("2. Follow this text"));

        typingArea.setLineWrap(true);
        typingArea.setWrapStyleWord(true);
        typingArea.setFont(new Font("Monospaced", Font.PLAIN, 18));
        typingArea.setEnabled(false);
        JScrollPane typingScroll = new JScrollPane(typingArea);
        typingScroll.setBorder(BorderFactory.createTitledBorder("3. Type here"));

        centerPanel.add(targetScroll);
        centerPanel.add(typingScroll);

        add(centerPanel, BorderLayout.CENTER);

        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        statsPanel.setBorder(BorderFactory.createTitledBorder("Stats"));
        statsPanel.add(mistakesLabel);
        statsPanel.add(accuracyLabel);
        statsPanel.add(wpmLabel);
        statsPanel.add(progressLabel);

        add(statsPanel, BorderLayout.SOUTH);

        startButton.addActionListener(e -> startPractice());
        resetButton.addActionListener(e -> resetPractice());

        typingArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateTypingStats();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateTypingStats();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateTypingStats();
            }
        });
    }

    private void startPractice() {
        targetText = inputTextArea.getText();
        if (targetText.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please paste some text first.");
            return;
        }

        typingArea.setText("");
        typingArea.setEnabled(true);
        typingArea.requestFocus();

        started = false;
        startTime = 0;

        renderTargetText("");
        updateLabels(0, 100.0, 0, 0, targetText.length());
    }

    private void resetPractice() {
        typingArea.setText("");
        typingArea.setEnabled(false);
        targetPane.setText("");
        targetText = "";
        started = false;
        startTime = 0;

        mistakesLabel.setText("Mistakes: 0");
        accuracyLabel.setText("Accuracy: 100.0%");
        wpmLabel.setText("WPM: 0");
        progressLabel.setText("Progress: 0 / 0");
    }

    private void updateTypingStats() {
        if (targetText.isEmpty()) {
            return;
        }

        String typed = typingArea.getText();

        if (!started && !typed.isEmpty()) {
            started = true;
            startTime = System.currentTimeMillis();
        }

        int mistakes = 0;
        int correctChars = 0;

        int compareLength = Math.min(typed.length(), targetText.length());

        for (int i = 0; i < compareLength; i++) {
            if (typed.charAt(i) == targetText.charAt(i)) {
                correctChars++;
            } else {
                mistakes++;
            }
        }

        if (typed.length() > targetText.length()) {
            mistakes += typed.length() - targetText.length();
        }

        double accuracy = typed.isEmpty() ? 100.0 : (correctChars * 100.0) / typed.length();

        int wpm = 0;
        if (started) {
            long elapsedMillis = System.currentTimeMillis() - startTime;
            double minutes = elapsedMillis / 60000.0;
            if (minutes > 0) {
                wpm = (int) ((correctChars / 5.0) / minutes);
            }
        }

        renderTargetText(typed);
        updateLabels(mistakes, accuracy, wpm, typed.length(), targetText.length());
    }

    private void updateLabels(int mistakes, double accuracy, int wpm, int progress, int total) {
        mistakesLabel.setText("Mistakes: " + mistakes);
        accuracyLabel.setText(String.format("Accuracy: %.1f%%", accuracy));
        wpmLabel.setText("WPM: " + wpm);
        progressLabel.setText("Progress: " + progress + " / " + total);
    }

    private void renderTargetText(String typed) {
        StyledDocument doc = targetPane.getStyledDocument();
        try {
            doc.remove(0, doc.getLength());

            Style defaultStyle = targetPane.addStyle("default", null);
            StyleConstants.setForeground(defaultStyle, Color.GRAY);

            Style correctStyle = targetPane.addStyle("correct", null);
            StyleConstants.setForeground(correctStyle, new Color(0, 150, 0));

            Style wrongStyle = targetPane.addStyle("wrong", null);
            StyleConstants.setForeground(wrongStyle, Color.RED);

            Style currentStyle = targetPane.addStyle("current", null);
            StyleConstants.setBackground(currentStyle, Color.YELLOW);
            StyleConstants.setForeground(currentStyle, Color.BLACK);

            for (int i = 0; i < targetText.length(); i++) {
                char c = targetText.charAt(i);

                if (i < typed.length()) {
                    if (typed.charAt(i) == c) {
                        doc.insertString(doc.getLength(), String.valueOf(c), correctStyle);
                    } else {
                        doc.insertString(doc.getLength(), String.valueOf(c), wrongStyle);
                    }
                } else if (i == typed.length()) {
                    doc.insertString(doc.getLength(), String.valueOf(c), currentStyle);
                } else {
                    doc.insertString(doc.getLength(), String.valueOf(c), defaultStyle);
                }
            }
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Main app = new Main();
            app.setVisible(true);
        });
    }
}