import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import javax.imageio.ImageIO;
import javax.swing.Timer;

class PicturePuzzle extends JFrame {
    private final String[] imageNames = { "landscape.jpg", "cityscape.jpg", "wildlife.jpg", "abstract.jpg" };
    private int gridSize = 3; // Default grid size for "Easy"
    private ArrayList<JButton> tiles = new ArrayList<>();
    private ArrayList<ImageIcon> tileIcons = new ArrayList<>();
    private int emptyIndex;
    private int moves = 0;
    private JLabel moveCounter;
    private JLabel timerLabel;
    private Timer gameTimer;
    private int seconds = 0;
    private String currentImage;
    private JComboBox<String> levelSelector;
    private JButton shuffleButton;

    public PicturePuzzle() {
        setTitle("Picture Puzzle Game");
        setLayout(new BorderLayout());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Initialize game components
        initializeGame();

        pack();
        setVisible(true);
    }

    private void initializeGame() {
        selectRandomImage();
        setupLevelSelector();
        loadAndSplitImage();
        setupUI();
        startTimer();
    }

    private void setupUI() {
        getContentPane().removeAll();
        setLayout(new BorderLayout());

        // Game panel
        JPanel gamePanel = new JPanel(new GridLayout(gridSize, gridSize));
        for (int i = 0; i < tiles.size(); i++) {
            JButton tile = tiles.get(i);
            tile.addActionListener(new TileClickListener(i));
            tile.setBackground(Color.LIGHT_GRAY);
            gamePanel.add(tile);
        }
        add(gamePanel, BorderLayout.CENTER);

        // Bottom panel with controls and display info
        JPanel bottomPanel = new JPanel(new GridLayout(3, 1));

        // Information panel (moves, timer, level)
        JPanel infoPanel = new JPanel(new FlowLayout());
        moveCounter = new JLabel("Moves: 0");
        timerLabel = new JLabel("Time: 0s");
        infoPanel.add(new JLabel("Difficulty: "));
        infoPanel.add(levelSelector);
        infoPanel.add(moveCounter);
        infoPanel.add(timerLabel);
        bottomPanel.add(infoPanel);

        // Control panel (restart, play again, shuffle)
        JPanel controlPanel = new JPanel(new FlowLayout());
        JButton restartButton = new JButton("Restart");
        restartButton.addActionListener(e -> restartGame());
        JButton playAgainButton = new JButton("Play Again (New Image)");
        playAgainButton.addActionListener(e -> playAgain());
        shuffleButton = new JButton("Shuffle");
        shuffleButton.addActionListener(e -> shuffleImages());  // Shuffle button functionality
        controlPanel.add(restartButton);
        controlPanel.add(playAgainButton);
        controlPanel.add(shuffleButton);
        bottomPanel.add(controlPanel);

        add(bottomPanel, BorderLayout.SOUTH);
        revalidate();
        repaint();
    }

    private void selectRandomImage() {
        Random rand = new Random();
        currentImage = imageNames[rand.nextInt(imageNames.length)];
    }

    private void setupLevelSelector() {
        levelSelector = new JComboBox<>(new String[]{"Easy", "Medium", "Hard"});
        levelSelector.setSelectedIndex(0); // Default to Easy
        levelSelector.addActionListener(e -> {
            String level = (String) levelSelector.getSelectedItem();
            switch (level) {
                case "Easy":
                    gridSize = 3;
                    break;
                case "Medium":
                    gridSize = 4;
                    break;
                case "Hard":
                    gridSize = 5;
                    break;
            }
            playAgain(); // Restart game with the new difficulty level
        });
    }

    private void loadAndSplitImage() {
        tiles.clear();
        tileIcons.clear();

        try {
            BufferedImage image = ImageIO.read(new File("src/resources/" + currentImage)); // Loads random image
            int pieceWidth = image.getWidth() / gridSize;
            int pieceHeight = image.getHeight() / gridSize;

            // Ensure square tiles
            int tileSize = Math.min(pieceWidth, pieceHeight);
            for (int row = 0; row < gridSize; row++) {
                for (int col = 0; col < gridSize; col++) {
                    if (row == gridSize - 1 && col == gridSize - 1) {
                        tiles.add(createEmptyTile());
                    } else {
                        BufferedImage subImage = image.getSubimage(col * pieceWidth, row * pieceHeight, pieceWidth, pieceHeight);
                        ImageIcon icon = new ImageIcon(subImage.getScaledInstance(tileSize, tileSize, Image.SCALE_SMOOTH));  // Ensuring square shape
                        JButton tile = new JButton(icon);
                        tileIcons.add(icon);
                        tiles.add(tile);
                    }
                }
            }
            emptyIndex = tiles.size() - 1; // Set the last index as empty
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Image not found!", "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    private JButton createEmptyTile() {
        JButton emptyTile = new JButton();
        emptyTile.setBackground(Color.LIGHT_GRAY);
        return emptyTile;
    }

    private void moveTile(int tileIndex) {
        if (isAdjacent(tileIndex, emptyIndex)) {
            // Swap the clicked tile with the empty tile
            Collections.swap(tiles, tileIndex, emptyIndex);
            emptyIndex = tileIndex; // Update the empty index after the swap
            moves++; // Increment the move counter
            moveCounter.setText("Moves: " + moves); // Update the move label
            updateGrid(); // Refresh the grid to reflect the new tile arrangement

            // Check if the puzzle is solved after the move
            if (isPuzzleSolved()) {
                gameTimer.stop(); // Stop the timer
                JOptionPane.showMessageDialog(this, "Congratulations! You solved the puzzle in " + moves + " moves and " + seconds + " seconds.");
                int choice = JOptionPane.showConfirmDialog(this, "Play Again?", "Puzzle Solved", JOptionPane.YES_NO_OPTION);
                if (choice == JOptionPane.YES_OPTION) {
                    playAgain(); // Restart the game with a new puzzle
                } else {
                    System.exit(0); // Exit the game if the user chooses "No"
                }
            }
        }
    }

    private boolean isAdjacent(int tileIndex, int emptyIndex) {
        int row1 = tileIndex / gridSize;
        int col1 = tileIndex % gridSize;
        int row2 = emptyIndex / gridSize;
        int col2 = emptyIndex % gridSize;
        return Math.abs(row1 - row2) + Math.abs(col1 - col2) == 1;
    }

    private void updateGrid() {
        // Remove the old grid before adding the updated tiles
        getContentPane().removeAll();
        JPanel gamePanel = new JPanel(new GridLayout(gridSize, gridSize));

        // Add the updated tiles (including the empty tile) to the panel
        for (JButton tile : tiles) {
            gamePanel.add(tile);
        }

        // Add the game panel with the tiles to the main window
        add(gamePanel, BorderLayout.CENTER);

        // Re-add the bottom panel for info and controls
        setupUI();
        revalidate();  // Revalidate the layout to ensure it updates
        repaint();     // Repaint the window to reflect changes
    }

    private boolean isPuzzleSolved() {
        for (int i = 0; i < tiles.size() - 1; i++) {
            JButton tile = tiles.get(i);
            if (!((JButton) tile).getIcon().equals(tileIcons.get(i))) {
                return false;
            }
        }
        return true;
    }

    private void startTimer() {
        gameTimer = new Timer(1000, e -> {
            seconds++;
            timerLabel.setText("Time: " + seconds + "s");
        });
        gameTimer.start();
    }

    private void restartGame() {
        gameTimer.stop();
        moves = 0;
        seconds = 0;
        moveCounter.setText("Moves: 0");
        timerLabel.setText("Time: 0s");
        loadAndSplitImage();
        setupUI();
        gameTimer.start();
    }

    private void playAgain() {
        gameTimer.stop();
        moves = 0;
        seconds = 0;
        moveCounter.setText("Moves: 0");
        timerLabel.setText("Time: 0s");
        selectRandomImage(); // Choose a new image
        loadAndSplitImage();
        setupUI();
        gameTimer.start();
    }

    private void shuffleImages() {
        Collections.shuffle(tiles);
        updateGrid(); // Update the grid layout after shuffling the tiles
    }

    private class TileClickListener implements ActionListener {
        private final int tileIndex;

        public TileClickListener(int tileIndex) {
            this.tileIndex = tileIndex;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            moveTile(tileIndex);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(PicturePuzzle::new);
    }
}
