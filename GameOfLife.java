/*
 * Bailey Thompson
 * Game Of Life (1.2.1)
 * 20 February 2017
 */

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import static java.lang.Integer.parseInt;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;

/**
 * Every cell is either alive or dead, and interacts with other cells based on what their status is. Any live cell with
 * fewer than two live neighbours dies, as if caused by under-population. Any live cell with two or three live
 * neighbours lives on to the next generation. Any live cell with more than three live neighbours dies, as if by
 * over-population. Any dead cell with exactly three live neighbours becomes a live cell, as if by reproduction. User
 * has the ability to pause and resume the game at any time. At any time, user can click on the game board, and the cell
 * will become the opposite of what it currently is. If it was dead, it will become alive, if it was alive, it will
 * become dead. Change in cell state will have an effect on game logic like any normal cell would. User has the ability
 * to clear the game board at any time, making all cells dead. User has the ability to randomize the amount of live
 * cells, portion of live cells that should appear on the board. User has the ability to change the amount of time
 * between turns, having an immediate effect on game logic. User also has the ability to change row size and column size
 * at any time. If a selection pane other than the main game is closed, the pane acts as if the user did not enter
 * anything, by entering the value that the user previously put. All user preferences are saved with file IO so that the
 * next time the user opens the game, previous settings are used. The main game frame cannot be resized by sketching the
 * screen, unless amount of rows or columns in being changed
 */
class GameOfLife {

    private static final Path FILE = Paths.get("GameOfLife.txt");
    private static final String PROGRAM_NAME = "Game Of Life";

    private JFrame frame;
    private JLabel label;
    private JButton play;

    private boolean pause, editPress, changingLabel;
    private boolean[][] cells, tempCells;
    private int maxVertical, maxHorizontal, screenWidth, screenHeight, roundTime, randomSpawning, timeCounter;
    private String lifeBoard, tempSize = "", saveFile;
    private String[] split;

    public static void main(String[] args) {
        GameOfLife gameOfLife = new GameOfLife();
        gameOfLife.gameOfLifeLogic();
    }

    private void gameOfLifeLogic() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        screenWidth = (int) screenSize.getWidth() - 100;
        screenHeight = (int) screenSize.getHeight() - 100;
        if (((screenWidth - 10) / 14 - 1) < 25 || ((screenHeight - 70) / 16 - 1) < 25) {
            String[] buttonGameMode = {"Ok"};
            JOptionPane.showOptionDialog(null, "Sorry, your screen size is too small for this program to run.",
                    PROGRAM_NAME, JOptionPane.PLAIN_MESSAGE, JOptionPane.PLAIN_MESSAGE, null, buttonGameMode,
                    buttonGameMode[0]);
            System.exit(0);
        }
        load();
        cells = new boolean[maxVertical + 1][maxHorizontal + 1];
        tempCells = new boolean[maxVertical + 1][maxHorizontal + 1];
        fillInLoad();
        fillInArray();
        prepareGUI();
        while (true) {
            if (!pause) {
                while (editPress) {
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(GameOfLife.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                fillingAndChangingArray();
                for (timeCounter = 0; timeCounter < roundTime; timeCounter++) {
                    if (!pause) {
                        try {
                            Thread.sleep(1);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(GameOfLife.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
                if (!pause && frame.isShowing()) {
                    innerGameLogic();
                }
                save();
            } else {
                play.addActionListener((ActionEvent e) -> {
                    // Here to re-initialize the listener in method prepareGUI.
                });
            }
        }
    }

    private void innerGameLogic() {
        for (int vertical = 0; vertical < maxVertical + 1; vertical++) {
            for (int horizontal = 0; horizontal < maxHorizontal + 1; horizontal++) {
                int temp = 0;
                if (vertical - 1 >= 0 && horizontal - 1 >= 0 && vertical + 1 <= maxVertical
                        && horizontal + 1 <= maxHorizontal) {
                    if (cells[vertical - 1][horizontal - 1]) {
                        temp++;
                    }
                    if (cells[vertical - 1][horizontal]) {
                        temp++;
                    }
                    if (cells[vertical - 1][horizontal + 1]) {
                        temp++;
                    }
                    if (cells[vertical][horizontal - 1]) {
                        temp++;
                    }
                    if (cells[vertical][horizontal + 1]) {
                        temp++;
                    }
                    if (cells[vertical + 1][horizontal - 1]) {
                        temp++;
                    }
                    if (cells[vertical + 1][horizontal]) {
                        temp++;
                    }
                    if (cells[vertical + 1][horizontal + 1]) {
                        temp++;
                    }
                } else if (vertical - 1 < 0 && vertical + 1 <= maxVertical && horizontal - 1 >= 0
                        && horizontal + 1 <= maxHorizontal) {
                    if (cells[vertical][horizontal - 1]) {
                        temp++;
                    }
                    if (cells[vertical][horizontal + 1]) {
                        temp++;
                    }
                    if (cells[vertical + 1][horizontal - 1]) {
                        temp++;
                    }
                    if (cells[vertical + 1][horizontal]) {
                        temp++;
                    }
                    if (cells[vertical + 1][horizontal + 1]) {
                        temp++;
                    }
                } else if (vertical + 1 > maxVertical && vertical - 1 >= 0 && horizontal - 1 >= 0
                        && horizontal + 1 <= maxHorizontal) {
                    if (cells[vertical - 1][horizontal - 1]) {
                        temp++;
                    }
                    if (cells[vertical - 1][horizontal]) {
                        temp++;
                    }
                    if (cells[vertical - 1][horizontal + 1]) {
                        temp++;
                    }
                    if (cells[vertical][horizontal - 1]) {
                        temp++;
                    }
                    if (cells[vertical][horizontal + 1]) {
                        temp++;
                    }
                } else if (horizontal - 1 < 0 && horizontal + 1 <= maxHorizontal && vertical - 1 >= 0
                        && vertical + 1 <= maxVertical) {
                    if (cells[vertical - 1][horizontal]) {
                        temp++;
                    }
                    if (cells[vertical - 1][horizontal + 1]) {
                        temp++;
                    }
                    if (cells[vertical][horizontal + 1]) {
                        temp++;
                    }
                    if (cells[vertical + 1][horizontal]) {
                        temp++;
                    }
                    if (cells[vertical + 1][horizontal + 1]) {
                        temp++;
                    }
                } else if (horizontal + 1 > maxHorizontal && horizontal - 1 >= 0 && vertical - 1 >= 0
                        && vertical + 1 <= maxVertical) {
                    if (cells[vertical - 1][horizontal - 1]) {
                        temp++;
                    }
                    if (cells[vertical - 1][horizontal]) {
                        temp++;
                    }
                    if (cells[vertical][horizontal - 1]) {
                        temp++;
                    }
                    if (cells[vertical + 1][horizontal - 1]) {
                        temp++;
                    }
                    if (cells[vertical + 1][horizontal]) {
                        temp++;
                    }
                } else if (vertical - 1 < 0 && horizontal - 1 < 0 && horizontal + 1 <= maxHorizontal
                        && vertical + 1 <= maxVertical) {
                    if (cells[vertical][horizontal + 1]) {
                        temp++;
                    }
                    if (cells[vertical + 1][horizontal]) {
                        temp++;
                    }
                    if (cells[vertical + 1][horizontal + 1]) {
                        temp++;
                    }
                } else if (vertical - 1 < 0 && horizontal + 1 > maxHorizontal && horizontal - 1 >= 0
                        && vertical + 1 <= maxVertical) {
                    if (cells[vertical][horizontal - 1]) {
                        temp++;
                    }
                    if (cells[vertical + 1][horizontal - 1]) {
                        temp++;
                    }
                    if (cells[vertical + 1][horizontal]) {
                        temp++;
                    }
                } else if (vertical + 1 > maxVertical && horizontal - 1 < 0 && horizontal + 1 <= maxHorizontal
                        && vertical - 1 >= 0) {
                    if (cells[vertical - 1][horizontal]) {
                        temp++;
                    }
                    if (cells[vertical - 1][horizontal + 1]) {
                        temp++;
                    }
                    if (cells[vertical][horizontal + 1]) {
                        temp++;
                    }
                } else if (vertical + 1 > maxVertical && horizontal + 1 > maxHorizontal && horizontal - 1 >= 0
                        && vertical - 1 >= 0) {
                    if (cells[vertical - 1][horizontal - 1]) {
                        temp++;
                    }
                    if (cells[vertical - 1][horizontal]) {
                        temp++;
                    }
                    if (cells[vertical][horizontal - 1]) {
                        temp++;
                    }
                }
                if (cells[vertical][horizontal] && (temp < 2 || temp > 3)) {
                    tempCells[vertical][horizontal] = false;
                } else if (!cells[vertical][horizontal] && temp == 3) {
                    tempCells[vertical][horizontal] = true;
                } else if (cells[vertical][horizontal] && (temp == 2 || temp == 3)) {
                    tempCells[vertical][horizontal] = true;
                }
            }
        }
        for (int vertical = 0; vertical < maxVertical + 1; vertical++) {
            System.arraycopy(tempCells[vertical], 0, cells[vertical], 0, maxHorizontal + 1);
        }
    }

    private void fillInArray() {
        changingLabel = true;
        lifeBoard = "<html><span style='font-size:1em'>";
        for (int vertical = 0; vertical <= maxVertical; vertical++) {
            for (int horizontal = 0; horizontal <= maxHorizontal; horizontal++) {
                if (!cells[vertical][horizontal]) {
                    lifeBoard += "□ ";
                } else if (cells[vertical][horizontal]) {
                    lifeBoard += "■ ";
                }
            }
            lifeBoard += "<br>";
        }
        changingLabel = false;
    }

    private void prepareGUI() {
        if (!pause) {
            frame = new JFrame(PROGRAM_NAME);
        } else {
            frame = new JFrame(PROGRAM_NAME + " (Paused)");
        }
        frame.setLayout(new BorderLayout());
        frame.setSize(14 * (maxHorizontal + 1) + 5, 16 * (maxVertical + 1) + 95);
        frame.setResizable(false);

        label = new JLabel(lifeBoard, JLabel.CENTER);
        JPanel middlePanel = new JPanel();
        JPanel bottomPanel = new JPanel();

        JButton btnPause = new JButton("Pause");
        play = new JButton("Play");
        JButton btnRandom = new JButton("Random");
        JButton btnClear = new JButton("Clear");
        JButton btnTime = new JButton("Time");
        JButton btnColumn = new JButton("Column");
        JButton btnRow = new JButton("Row");
        JButton btnExit = new JButton("Exit");

        middlePanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.setLayout(new FlowLayout(FlowLayout.CENTER));

        middlePanel.add(btnPause);
        middlePanel.add(play);
        middlePanel.add(btnRandom);
        middlePanel.add(btnClear);

        bottomPanel.add(btnTime);
        bottomPanel.add(btnColumn);
        bottomPanel.add(btnRow);
        bottomPanel.add(btnExit);

        frame.add(label, BorderLayout.NORTH);
        frame.add(middlePanel, BorderLayout.CENTER);
        frame.add(bottomPanel, BorderLayout.SOUTH);

        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setVisible(true);

        label.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                final int HORIZONTAL_CLICK_POSITION = (int) Math.floor((e.getX()) / 14);
                final int VERTICAL_CLICK_POSITION = (int) Math.floor((e.getY()) / 16);

                editPress = true;
                if (VERTICAL_CLICK_POSITION <= maxVertical && HORIZONTAL_CLICK_POSITION <= maxHorizontal) {
                    cells[VERTICAL_CLICK_POSITION][HORIZONTAL_CLICK_POSITION] ^= true;
                    tempCells[VERTICAL_CLICK_POSITION][HORIZONTAL_CLICK_POSITION] ^= true;
                }
                fillingAndChangingArray();
                editPress = false;
                save();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                // Not used.
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                // Not used.
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                // Not used.
            }

            @Override
            public void mouseExited(MouseEvent e) {
                // Not used.
            }
        });

        btnPause.addActionListener((ActionEvent e) -> {
            pause = true;
            frame.setTitle(PROGRAM_NAME + " (Paused)");
        });

        play.addActionListener((ActionEvent e) -> {
            pause = false;
            frame.setTitle(PROGRAM_NAME);
        });

        btnRandom.addActionListener((ActionEvent e) -> {
            boolean skip = false;
            do {
                tempSize = JOptionPane.showInputDialog(null, "How much of the time out of 100 should a cell spawn "
                        + "alive, as an integer value?", PROGRAM_NAME, JOptionPane.PLAIN_MESSAGE);
                if (tempSize == null) {
                    tempSize = String.valueOf(randomSpawning);
                    skip = true;
                }
            } while ("".equals(tempSize) || !"".equals(tempSize.replaceAll("[0123456789]", ""))
                    || tempSize.length() > 9 || parseInt(tempSize, 10) > 100);
            if (!skip) {
                randomSpawning = parseInt(tempSize, 10);
                randomSpawns();
            }
        });

        btnClear.addActionListener((ActionEvent e) -> {
            randomSpawning = 0;
            randomSpawns();
        });

        btnTime.addActionListener((ActionEvent e) -> {
            final boolean TEMP_PAUSE = pause;
            pause = true;
            frame.setTitle(PROGRAM_NAME + " (Paused)");
            do {
                tempSize = JOptionPane.showInputDialog(null, "Please insert the amount of milli-seconds\nper turn as an"
                        + " integer value.\n1000 milli-seconds = 1 second.", PROGRAM_NAME, JOptionPane.PLAIN_MESSAGE);
                if (tempSize == null) {
                    tempSize = String.valueOf(roundTime);
                }
            } while ("".equals(tempSize) || !"".equals(tempSize.replaceAll("[0123456789]", ""))
                    || tempSize.length() > 9 || parseInt(tempSize, 10) == 0);
            roundTime = parseInt(tempSize, 10);
            save();
            if (!TEMP_PAUSE) {
                frame.setTitle(PROGRAM_NAME);
            }
            pause = TEMP_PAUSE;
        });

        btnColumn.addActionListener((ActionEvent e) -> {
            frame.setVisible(false);
            do {
                tempSize = JOptionPane.showInputDialog(null, "Please insert the column size as an integer value.\nMust "
                                + "be between 25 and maximum size allowed on your monitor.", PROGRAM_NAME,
                        JOptionPane.PLAIN_MESSAGE);
                if (tempSize == null) {
                    tempSize = String.valueOf(maxVertical + 1);
                }
            } while ("".equals(tempSize) || !"".equals(tempSize.replaceAll("[0123456789]", ""))
                    || tempSize.length() > 9 || parseInt(tempSize, 10) < 25
                    || parseInt(tempSize, 10) > (screenHeight - 95) / 16 - 1);
            maxVertical = parseInt(tempSize, 10) - 1;
            resize();
        });

        btnRow.addActionListener((ActionEvent e) -> {
            frame.setVisible(false);
            do {
                tempSize = JOptionPane.showInputDialog(null, "Please insert the row size as an integer value.\nMust "
                                + "be between 25 and maximum size allowed on your monitor.", PROGRAM_NAME,
                        JOptionPane.PLAIN_MESSAGE);
                if (tempSize == null) {
                    tempSize = String.valueOf(maxHorizontal + 1);
                }
            } while ("".equals(tempSize) || !"".equals(tempSize.replaceAll("[0123456789]", ""))
                    || tempSize.length() > 9 || parseInt(tempSize, 10) < 25
                    || parseInt(tempSize, 10) > (screenWidth - 5) / 14 - 1);
            maxHorizontal = parseInt(tempSize, 10) - 1;
            resize();
        });

        btnExit.addActionListener((ActionEvent e) -> System.exit(0));
    }

    private void resize() {
        cells = new boolean[maxVertical + 1][maxHorizontal + 1];
        tempCells = new boolean[maxVertical + 1][maxHorizontal + 1];
        save();
        frame.setSize(14 * (maxHorizontal + 1) + 5, 16 * (maxVertical + 1) + 95);
        fillingAndChangingArray();
        frame.setVisible(true);
        frame.setLocationRelativeTo(null);
    }

    private void randomSpawns() {
        for (int vertical = 0; vertical < maxVertical + 1; vertical++) {
            for (int horizontal = 0; horizontal < maxHorizontal + 1; horizontal++) {
                if (randomSpawning != 0) {
                    final int RANDOM_SPAWNS = (int) (Math.random() * 100);
                    if (RANDOM_SPAWNS >= 0 && RANDOM_SPAWNS < randomSpawning) {
                        cells[vertical][horizontal] = true;
                        tempCells[vertical][horizontal] = true;
                    } else {
                        cells[vertical][horizontal] = false;
                        tempCells[vertical][horizontal] = false;
                    }
                } else {
                    cells[vertical][horizontal] = false;
                    tempCells[vertical][horizontal] = false;
                }
            }
        }
        fillingAndChangingArray();
        timeCounter = 0;
        save();
    }

    private void fillingAndChangingArray() {
        if (!changingLabel) {
            fillInArray();
            label.setText(lifeBoard);
        }
    }

    private void fillInLoad() {
        for (int vertical = 0; vertical < maxVertical + 1; vertical++) {
            for (int horizontal = 0; horizontal < maxHorizontal + 1; horizontal++) {
                if (parseInt(split[vertical * (maxHorizontal + 1) + horizontal + 4], 10) == 1) {
                    cells[vertical][horizontal] = true;
                }
            }
        }
    }

    private void load() {
        try {
            Files.createFile(FILE);
        } catch (FileAlreadyExistsException x) {
            try (InputStream in = Files.newInputStream(FILE);
                 BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    saveFile = line;
                }
            } catch (IOException e) {
                System.err.println("Error 1 in load method");
            }
        } catch (IOException x) {
            System.err.println("Error 2 in load method");
        }
        if (saveFile == null) {
            saveFile = "24 24 200 0";
            for (int counterFillFalse = 0; counterFillFalse < 25 * 25; counterFillFalse++) {
                saveFile += " 0";
            }
        }
        split = saveFile.split("\\s+");
        maxVertical = parseInt(split[0], 10);
        maxHorizontal = parseInt(split[1], 10);
        roundTime = parseInt(split[2], 10);
        if (parseInt(split[3], 10) == 1) {
            pause = true;
        }
    }

    private void save() {
        saveFile = maxVertical + " " + maxHorizontal + " " + roundTime;
        if (!pause) {
            saveFile += " 0";
        } else {
            saveFile += " 1";
        }
        for (int vertical = 0; vertical < maxVertical + 1; vertical++) {
            for (int horizontal = 0; horizontal < maxHorizontal + 1; horizontal++) {
                if (!cells[vertical][horizontal]) {
                    saveFile += " 0";
                } else {
                    saveFile += " 1";
                }
            }
        }
        byte data[] = saveFile.getBytes();
        try (OutputStream out = new BufferedOutputStream(
                Files.newOutputStream(FILE, WRITE, TRUNCATE_EXISTING))) {
            out.write(data, 0, data.length);
        } catch (IOException x) {
            System.err.println("Error in save method");
        }
    }
}
