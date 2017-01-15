/**
 * Bailey Thompson
 * Game Of Life (1.1.6)
 * 14 January 2017
 * Game Rules: Any live cell with fewer than two live neighbours dies, as if caused by under-population.
 * Game Rules: Any live cell with two or three live neighbours lives on to the next generation.
 * Game Rules: Any live cell with more than three live neighbours dies, as if by over-population.
 * Game Rules: Any dead cell with exactly three live neighbours becomes a live cell, as if by reproduction.
 * Features: User  has  the  ability  to  pause and resume the game at any time. At any time, user can click on the game
 * Features: board, and the cell will become the opposite of what it currently is. If it was dead, it will become alive,
 * Features: if  it  was  alive,  it  will  become dead. Change in cell state will have an effect on game logic like any
 * Features: normal  cell  would.  User has the ability to clear the game board at any time, making all cells dead. User
 * Features: has  the  ability  to  randomize  the amount of live cells, portion of live cells that should appear on the
 * Features: board.  User has the ability to change the amount of time between turns, having an immediate effect on game
 * Features: logic.  User also has the ability to change row size and column size at any time. If a selection pane other
 * Features: than  the  main  game is closed, the pane acts as if the user did not enter anything, by entering the value
 * Features: that  the  user  previously put. All user preferences are saved with file IO so that the next time the user
 * Features: opens  the game, previous settings are used. The main game frame cannot be resized by sketching the screen,
 * Features: unless amount of rows or columns in being changed
 */
package gameoflife;

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
import static java.lang.Integer.parseInt;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

public class GameOfLife {

    private static final Path FILE = Paths.get("GameOfLife.txt");
    private JFrame frame;
    private JPanel middlePanel, bottomPanel;
    private JLabel label;
    private JButton btnPause, btnPlay, btnRandom, btnClear, btnTime, btnColumn, btnRow, btnExit;
    private boolean pause, editPress, changingLabel;
    private boolean[][] cells, tempCells;
    private int maxVertical, maxHorizontal, screenWidth, screenHeight, roundTime, randomSpawning, timeCounter;
    private String lifeBoard, tempSize = "", saveFile;
    private String[] split;

    public static void main(String[] args) {
        GameOfLife GameOfLife = new GameOfLife();
        GameOfLife.GameOfLife();
    }

    private void GameOfLife() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        screenWidth = (int) screenSize.getWidth() - 100;
        screenHeight = (int) screenSize.getHeight() - 100;
        if (((screenWidth - 10) / 14 - 1) < 25 || ((screenHeight - 70) / 16 - 1) < 25) {
            String[] buttonGameMode = {"Ok"};
            JOptionPane.showOptionDialog(null, "Sorry, your screen size is too small for this program to run.",
                    "Game Of Life", JOptionPane.PLAIN_MESSAGE, JOptionPane.PLAIN_MESSAGE, null, buttonGameMode,
                    buttonGameMode[0]);
            System.exit(0);
        }
        load();
        cells = new boolean[maxVertical + 1][maxHorizontal + 1];
        tempCells = new boolean[maxVertical + 1][maxHorizontal + 1];
        fillInload();
        fillInArray();
        prepareGUI();
        while (1 != 0) {
            if (!pause) {
                //preventing automatic editing of label if it is being changed from a mouse click; 
                //until editPress is set to false again, the code is basically paused
                while (editPress) {
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(GameOfLife.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                //sending to method fillingAndChangingArray
                fillingAndChangingArray();
                //instead of using the sleep amount for the round time that the user enters, a 1 milisecond sleep thread
                //is set the amount of times that the user specifies with round time, the sleep is only executed if 
                //pause is false, this is so that if pause becomes true, it stops and does not continue sleeping
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
                //the button btnPlay listens only if pause is true, and sets pause to false if it is clicked
            } else {
                btnPlay.addActionListener((ActionEvent e) -> {
                    //just here to re-initialized the btnplay listener in the method prepareGUI
                });
            }
        }
    }

    private void innerGameLogic() {
        for (int vertical = 0; vertical < maxVertical + 1; vertical++) {
            for (int horizontal = 0; horizontal < maxHorizontal + 1; horizontal++) {
                int temp = 0;
                //checks all 8 neighbours if it is not a side or corner cell
                if (vertical - 1 >= 0 && horizontal - 1 >= 0 && vertical + 1 <= maxVertical
                        && horizontal + 1 <= maxHorizontal) {
                    if (cells[vertical - 1][horizontal - 1]) {
                        temp += 1;
                    }
                    if (cells[vertical - 1][horizontal]) {
                        temp += 1;
                    }
                    if (cells[vertical - 1][horizontal + 1]) {
                        temp += 1;
                    }
                    if (cells[vertical][horizontal - 1]) {
                        temp += 1;
                    }
                    if (cells[vertical][horizontal + 1]) {
                        temp += 1;
                    }
                    if (cells[vertical + 1][horizontal - 1]) {
                        temp += 1;
                    }
                    if (cells[vertical + 1][horizontal]) {
                        temp += 1;
                    }
                    if (cells[vertical + 1][horizontal + 1]) {
                        temp += 1;
                    }
                    //checks 5 neighbours if it is a side cell
                } else if (vertical - 1 < 0 && vertical + 1 <= maxVertical && horizontal - 1 >= 0
                        && horizontal + 1 <= maxHorizontal) {
                    if (cells[vertical][horizontal - 1]) {
                        temp += 1;
                    }
                    if (cells[vertical][horizontal + 1]) {
                        temp += 1;
                    }
                    if (cells[vertical + 1][horizontal - 1]) {
                        temp += 1;
                    }
                    if (cells[vertical + 1][horizontal]) {
                        temp += 1;
                    }
                    if (cells[vertical + 1][horizontal + 1]) {
                        temp += 1;
                    }
                    //checks 5 neighbours if it is a side cell
                } else if (vertical + 1 > maxVertical && vertical - 1 >= 0 && horizontal - 1 >= 0
                        && horizontal + 1 <= maxHorizontal) {
                    if (cells[vertical - 1][horizontal - 1]) {
                        temp += 1;
                    }
                    if (cells[vertical - 1][horizontal]) {
                        temp += 1;
                    }
                    if (cells[vertical - 1][horizontal + 1]) {
                        temp += 1;
                    }
                    if (cells[vertical][horizontal - 1]) {
                        temp += 1;
                    }
                    if (cells[vertical][horizontal + 1]) {
                        temp += 1;
                    }
                    //checks 5 neighbours if it is a side cell
                } else if (horizontal - 1 < 0 && horizontal + 1 <= maxHorizontal && vertical - 1 >= 0
                        && vertical + 1 <= maxVertical) {
                    if (cells[vertical - 1][horizontal]) {
                        temp += 1;
                    }
                    if (cells[vertical - 1][horizontal + 1]) {
                        temp += 1;
                    }
                    if (cells[vertical][horizontal + 1]) {
                        temp += 1;
                    }
                    if (cells[vertical + 1][horizontal]) {
                        temp += 1;
                    }
                    if (cells[vertical + 1][horizontal + 1]) {
                        temp += 1;
                    }
                    //checks 5 neighbours if it is a side cell
                } else if (horizontal + 1 > maxHorizontal && horizontal - 1 >= 0 && vertical - 1 >= 0
                        && vertical + 1 <= maxVertical) {
                    if (cells[vertical - 1][horizontal - 1]) {
                        temp += 1;
                    }
                    if (cells[vertical - 1][horizontal]) {
                        temp += 1;
                    }
                    if (cells[vertical][horizontal - 1]) {
                        temp += 1;
                    }
                    if (cells[vertical + 1][horizontal - 1]) {
                        temp += 1;
                    }
                    if (cells[vertical + 1][horizontal]) {
                        temp += 1;
                    }
                    //checks 3 neighbours if it is a corner cell
                } else if (vertical - 1 < 0 && horizontal - 1 < 0 && horizontal + 1 <= maxHorizontal
                        && vertical + 1 <= maxVertical) {
                    if (cells[vertical][horizontal + 1]) {
                        temp += 1;
                    }
                    if (cells[vertical + 1][horizontal]) {
                        temp += 1;
                    }
                    if (cells[vertical + 1][horizontal + 1]) {
                        temp += 1;
                    }
                    //checks 3 neighbours if it is a corner cell
                } else if (vertical - 1 < 0 && horizontal + 1 > maxHorizontal && horizontal - 1 >= 0
                        && vertical + 1 <= maxVertical) {
                    if (cells[vertical][horizontal - 1]) {
                        temp += 1;
                    }
                    if (cells[vertical + 1][horizontal - 1]) {
                        temp += 1;
                    }
                    if (cells[vertical + 1][horizontal]) {
                        temp += 1;
                    }
                    //checks 3 neighbours if it is a corner cell
                } else if (vertical + 1 > maxVertical && horizontal - 1 < 0 && horizontal + 1 <= maxHorizontal
                        && vertical - 1 >= 0) {
                    if (cells[vertical - 1][horizontal]) {
                        temp += 1;
                    }
                    if (cells[vertical - 1][horizontal + 1]) {
                        temp += 1;
                    }
                    if (cells[vertical][horizontal + 1]) {
                        temp += 1;
                    }
                    //checks 3 neighbours if it is a corner cell
                } else if (vertical + 1 > maxVertical && horizontal + 1 > maxHorizontal && horizontal - 1 >= 0
                        && vertical - 1 >= 0) {
                    if (cells[vertical - 1][horizontal - 1]) {
                        temp += 1;
                    }
                    if (cells[vertical - 1][horizontal]) {
                        temp += 1;
                    }
                    if (cells[vertical][horizontal - 1]) {
                        temp += 1;
                    }
                }
                //if the cells at the position is alive and has more than 3 neighbours or less than 2, 
                //it dies but is temporairly stored on tempCells
                if (cells[vertical][horizontal] && (temp < 2 || temp > 3)) {
                    tempCells[vertical][horizontal] = false;
                    //if the cells at the position is dead but has 3 neighbours, 
                    //it becomes alive but is temporairly stored on tempCells
                } else if (!cells[vertical][horizontal] && temp == 3) {
                    tempCells[vertical][horizontal] = true;
                    //if the cells at the position is alive and has 2 or 3 neighbours, 
                    //it stays alive, and is temporairly stored on tempCells
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
                //display wether the cell is alive or dead
                if (!cells[vertical][horizontal]) {
                    lifeBoard += "□ ";
                } else if (cells[vertical][horizontal]) {
                    lifeBoard += "■ ";
                }
            }
            //skip a line every time a full horizontal row is displayed
            lifeBoard += "<br>";
        }
        //setting changingLabel to false
        changingLabel = false;
    }

    private void prepareGUI() {
        if (!pause) {
            frame = new JFrame("Game Of Life");
        } else {
            frame = new JFrame("Game Of Life (Paused)");
        }
        frame.setLayout(new BorderLayout());
        frame.setSize(14 * (maxHorizontal + 1) + 5, 16 * (maxVertical + 1) + 95);
        frame.setResizable(false);

        label = new JLabel(lifeBoard, JLabel.CENTER);
        middlePanel = new JPanel();
        bottomPanel = new JPanel();

        btnPause = new JButton("Pause");
        btnPlay = new JButton("Play");
        btnRandom = new JButton("Random");
        btnClear = new JButton("Clear");
        btnTime = new JButton("Time");
        btnColumn = new JButton("Column");
        btnRow = new JButton("Row");
        btnExit = new JButton("Exit");

        middlePanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.setLayout(new FlowLayout(FlowLayout.CENTER));

        middlePanel.add(btnPause);
        middlePanel.add(btnPlay);
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
                //position of click is set to variable
                int horizontalClickPosition = (int) Math.floor((e.getX()) / 14);
                int verticalClickPosition = (int) Math.floor((e.getY()) / 16);

                //prevent automatic write over of label while it is being edited from here
                editPress = true;
                //prevent glitching out of label when clicking buttons
                if (verticalClickPosition <= maxVertical && horizontalClickPosition <= maxHorizontal) {
                    //flips the value of both 2d array, if true, becomes false; if false, becomes true
                    cells[verticalClickPosition][horizontalClickPosition] ^= true;
                    tempCells[verticalClickPosition][horizontalClickPosition] ^= true;
                }
                fillingAndChangingArray();
                editPress = false;
                save();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                //useless
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                //useless
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                //useless
            }

            @Override
            public void mouseExited(MouseEvent e) {
                //useless
            }
        });

        btnPause.addActionListener((ActionEvent e) -> {
            pause = true;
            frame.setTitle("Game Of Life (Paused)");
        });

        btnPlay.addActionListener((ActionEvent e) -> {
            pause = false;
            frame.setTitle("Game Of Life");
        });

        btnRandom.addActionListener((ActionEvent e) -> {
            boolean skip = false;
            do {
                tempSize = JOptionPane.showInputDialog(null, "How much of the time out of 100 should a cell spawn "
                        + "alive, as an integer value?", "Game Of Life", JOptionPane.PLAIN_MESSAGE);
                //if user pressed cancel or the x button, the previous value is used
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
            boolean tempPause = pause;
            pause = true;
            frame.setTitle("Game Of Life (Paused)");
            do {
                tempSize = JOptionPane.showInputDialog(null, "Please insert the amount of miliseconds\nper turn as an "
                        + "integer value.\n1000 miliseconds = 1 second.", "Game Of Life", JOptionPane.PLAIN_MESSAGE);
                if (tempSize == null) {
                    tempSize = String.valueOf(roundTime);
                }
            } while ("".equals(tempSize) || !"".equals(tempSize.replaceAll("[0123456789]", ""))
                    || tempSize.length() > 9 || parseInt(tempSize, 10) == 0);
            roundTime = parseInt(tempSize, 10);
            save();
            if (!tempPause) {
                frame.setTitle("Game Of Life");
            }
            //sets pause back to the original setting
            pause = tempPause;
        });

        btnColumn.addActionListener((ActionEvent e) -> {
            frame.setVisible(false);
            do {
                tempSize = JOptionPane.showInputDialog(null, "Please insert the column size as an integer value.\nMust "
                        + "be between 25 and maximum size allowed on your monitor.", "Game Of Life",
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
                        + "be between 25 and maximum size allowed on your monitor.", "Game Of Life",
                        JOptionPane.PLAIN_MESSAGE);
                //if user pressed cancel or the x button, the previous value is used
                if (tempSize == null) {
                    tempSize = String.valueOf(maxHorizontal + 1);
                }
            } while ("".equals(tempSize) || !"".equals(tempSize.replaceAll("[0123456789]", ""))
                    || tempSize.length() > 9 || parseInt(tempSize, 10) < 25
                    || parseInt(tempSize, 10) > (screenWidth - 5) / 14 - 1);
            maxHorizontal = parseInt(tempSize, 10) - 1;
            resize();
        });

        btnExit.addActionListener((ActionEvent e) -> {
            System.exit(0);
        });
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
        int randomrandomSpawns;
        for (int vertical = 0; vertical < maxVertical + 1; vertical++) {
            for (int horizontal = 0; horizontal < maxHorizontal + 1; horizontal++) {
                if (randomSpawning != 0) {
                    randomrandomSpawns = (int) (Math.random() * 100);
                    if (randomrandomSpawns >= 0 && randomrandomSpawns < randomSpawning) {
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

    private void fillInload() {
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
            //file is read from and saved to variable saveFile is file already exists
            try (InputStream in = Files.newInputStream(FILE);
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    saveFile = line;
                }
            } catch (IOException e) {
                System.err.println(e);
            }
        } catch (IOException x) {
            System.err.println(x);
        }
        //if the file does not contain anything since it was just created, default variables are used for saveile
        if (saveFile == null) {
            saveFile = "24 24 200 0";
            for (int counterFillFalse = 0; counterFillFalse < 25 * 25; counterFillFalse++) {
                saveFile += " 0";
            }
        }
        //a String array is created and each part of the array is saved to from saveFile seperated by spaces
        split = saveFile.split("\\s+");
        //variable maxVertical is the first number
        maxVertical = parseInt(split[0], 10);
        //variable maxHorizontal is the second number
        maxHorizontal = parseInt(split[1], 10);
        //variable roundTime is the third number
        roundTime = parseInt(split[2], 10);
        //variable pause is the fourth number
        if (parseInt(split[3], 10) == 1) {
            //setting pause to true
            pause = true;
        }
    }

    private void save() {
        //saveFile is created using the four main variables, seperated by spaces
        saveFile = maxVertical + " " + maxHorizontal + " " + roundTime;
        if (!pause) {
            saveFile += " 0";
        } else {
            saveFile += " 1";
        }
        //saving the 2d array to file
        for (int vertical = 0; vertical < maxVertical + 1; vertical++) {
            for (int horizontal = 0; horizontal < maxHorizontal + 1; horizontal++) {
                if (!cells[vertical][horizontal]) {
                    saveFile += " 0";
                } else {
                    saveFile += " 1";
                }
            }
        }
        //saveFile is converted to byte data
        byte data[] = saveFile.getBytes();
        //byte data is saved to file using file io
        try (OutputStream out = new BufferedOutputStream(
                Files.newOutputStream(FILE, WRITE, TRUNCATE_EXISTING))) {
            out.write(data, 0, data.length);
        } catch (IOException x) {
            System.err.println(x);
        }
    }
}
