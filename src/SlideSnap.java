import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import javax.imageio.ImageIO;
import javax.swing.Timer;

public class SlideSnap extends JFrame {

    private final String[] imageNames = {
            "C:\\Users\\AA\\Downloads\\tom.jpg",
            "C:\\Users\\AA\\Downloads\\cartoon.jfif",
            "C:\\Users\\AA\\Downloads\\cute bunny.jpg",
            "C:\\Users\\AA\\Downloads\\doraemon2.jpg"
    };

    private int gridSize = 3; // Default grid size for "Easy"
    private ArrayList<JButton> tiles = new ArrayList<>();
    private ArrayList<ImageIcon> tileIcons = new ArrayList<>();
    private int emptyIndex; // The index where the empty tile is
    private JLabel timerLabel;
    private Timer gameTimer;
    private int seconds = 0;
    private String currentImage;
    private JComboBox<String> levelSelector;
    private BufferedImage fullImage; // To display mini image

    public SlideSnap() {
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

        // Add mini image at the top for help
        JPanel topPanel = new JPanel();
        ImageIcon miniImageIcon = new ImageIcon(fullImage.getScaledInstance(100, 100, Image.SCALE_SMOOTH));
        JLabel miniImageLabel = new JLabel(miniImageIcon);
        topPanel.add(miniImageLabel);
        add(topPanel, BorderLayout.NORTH);

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
        JPanel bottomPanel = new JPanel(new GridLayout(2, 1));

        // Information panel (timer and level)
        JPanel infoPanel = new JPanel(new FlowLayout());
        timerLabel = new JLabel("Time: 0s");
        infoPanel.add(new JLabel("Difficulty: "));
        infoPanel.add(levelSelector);
        infoPanel.add(timerLabel);
        bottomPanel.add(infoPanel);

        // Control panel (restart, play again)
        JPanel controlPanel = new JPanel(new FlowLayout());
        JButton restartButton = new JButton("Restart");
        restartButton.addActionListener(e -> restartGame());
        JButton playAgainButton = new JButton("Play Again (New Image)");
        playAgainButton.addActionListener(e -> playAgain());
        controlPanel.add(restartButton);
        controlPanel.add(playAgainButton);
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
            fullImage = ImageIO.read(new java.io.File(currentImage)); // Loads full image
            int pieceWidth = fullImage.getWidth() / gridSize;
            int pieceHeight = fullImage.getHeight() / gridSize;

            for (int row = 0; row < gridSize; row++) {
                for (int col = 0; col < gridSize; col++) {
                    if (row == gridSize - 1 && col == gridSize - 1) {
                        tiles.add(createEmptyTile());
                    } else {
                        BufferedImage subImage = fullImage.getSubimage(col * pieceWidth, row * pieceHeight, pieceWidth, pieceHeight);

                        // Resize the sub-image to fit the tile size
                        Image resizedImage = subImage.getScaledInstance(100, 100, Image.SCALE_SMOOTH); // 100x100 = tile size (adjust as needed)
                        ImageIcon icon = new ImageIcon(resizedImage);

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

        shuffleTiles(); // Shuffle the tiles to make the puzzle harder
    }

    private JButton createEmptyTile() {
        JButton emptyTile = new JButton();
        emptyTile.setBackground(Color.LIGHT_GRAY);
        return emptyTile;
    }

    private boolean isAdjacent(int tileIndex, int emptyIndex) {
        int row1 = tileIndex / gridSize;
        int col1 = tileIndex % gridSize;
        int row2 = emptyIndex / gridSize;
        int col2 = emptyIndex % gridSize;
        return Math.abs(row1 - row2) + Math.abs(col1 - col2) == 1;
    }

    private void updateGrid() {
        getContentPane().removeAll();
        JPanel gamePanel = new JPanel(new GridLayout(gridSize, gridSize));
        for (JButton tile : tiles) {
            gamePanel.add(tile);
        }
        add(gamePanel, BorderLayout.CENTER);
        setupUI(); // Set up other UI components
    }

    private boolean isPuzzleSolved() {
        for (int i = 0; i < tiles.size() - 1; i++) {
            JButton tile = tiles.get(i);
            if (!tile.getIcon().equals(tileIcons.get(i))) {
                return false;
            }
        }
        return true;
    }


    private void startTimer() {
        gameTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                seconds++;
                timerLabel.setText("Time: " + seconds + "s");
                if (seconds >= 60) { // Stop after 60 seconds
                    gameTimer.stop();
                    JOptionPane.showMessageDialog(SlideSnap.this, "Game Over - Time's Up!");
                    restartGame();
                }
            }
        });
        gameTimer.start();
    }

    private void shuffleTiles() {
        Random rand = new Random();
        int shuffleCount = gridSize * gridSize * 10; // More shuffling for more difficulty
        for (int i = 0; i < shuffleCount; i++) {
            int randomTileIndex = rand.nextInt(tiles.size());
            if (isAdjacent(randomTileIndex, emptyIndex)) {
                Collections.swap(tiles, randomTileIndex, emptyIndex);
                emptyIndex = randomTileIndex;
            }
        }
    }

    private void restartGame() {
        gameTimer.stop();
        seconds = 0;
        timerLabel.setText("Time: 0s");
        loadAndSplitImage();
        setupUI();
        gameTimer.start();
    }

    private void playAgain() {
        gameTimer.stop();
        seconds = 0;
        timerLabel.setText("Time: 0s");
        selectRandomImage(); // Choose a new image
        loadAndSplitImage();
        setupUI();
        gameTimer.start();
    }

    // Tile click event handler
    private class TileClickListener implements ActionListener {
        private final int tileIndex;

        public TileClickListener(int tileIndex) {
            this.tileIndex = tileIndex;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (isAdjacent(tileIndex, emptyIndex)) {
                // Swap clicked tile with the empty space
                Collections.swap(tiles, tileIndex, emptyIndex);
                emptyIndex = tileIndex; // Update the empty space index

                updateGrid(); // Update the UI

                // Check if the puzzle is solved
                if (isPuzzleSolved()) {
                    int option = JOptionPane.showOptionDialog(
                            SlideSnap.this,
                            "Congratulations, You solved the puzzle!",
                            "Puzzle Solved",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.INFORMATION_MESSAGE,
                            null,
                            new String[]{"Play Again", "Next Level"},
                            "Play Again"
                    );

                    if (option == JOptionPane.YES_OPTION) {
                        playAgain();
                    } else if (option == JOptionPane.NO_OPTION) {
                        gridSize++; // Move to the next level (increase grid size)
                        if (gridSize > 5) gridSize = 3; // Reset to "Easy" level after "Hard"
                        playAgain();
                    }
                }
            }
        }
    }
}


