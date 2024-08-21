import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

public class FlappyBird extends JPanel implements ActionListener, KeyListener {
    int boardWidth = 360;
    int boardHeight = 640;

    // Images
    Image backgroundImg;
    Image birdImg;
    Image topPipeImg;
    Image bottomPipeImg;

    // Bird class
    int birdX = boardWidth / 8;
    int birdY = boardWidth / 2;
    int birdWidth = 34;
    int birdHeight = 24;

    class Bird {
        int x = birdX;
        int y = birdY;
        int width = birdWidth;
        int height = birdHeight;
        Image img;

        Bird(Image img) {
            this.img = img;
        }
    }

    // Pipe class
    int pipeX = boardWidth;
    int pipeY = 0;
    int pipeWidth = 64;  // Scaled by 1/6
    int pipeHeight = 512;

    class Pipe {
        int x = pipeX;
        int y = pipeY;
        int width = pipeWidth;
        int height = pipeHeight;
        Image img;
        boolean passed = false;

        Pipe(Image img) {
            this.img = img;
        }
    }

    // Game logic
    Bird bird;
    int velocityX = -4; // Move pipes to the left speed (simulates bird moving right)
    int velocityY = 0; // Move bird up/down speed.
    int gravity = 1;

    ArrayList<Pipe> pipes;
    Random random = new Random();

    Timer gameLoop;
    Timer placePipeTimer;
    boolean gameOver = false;
    double score = 0;

    // Buttons
    JButton startButton;
    JButton pauseButton;
    JButton resumeButton;
    JButton newGameButton;

    FlappyBird() {
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setFocusable(true);
        addKeyListener(this);

        // Load images
        backgroundImg = new ImageIcon(getClass().getResource("./flappybirdbg.png")).getImage();
        birdImg = new ImageIcon(getClass().getResource("./flappybird.png")).getImage();
        topPipeImg = new ImageIcon(getClass().getResource("./toppipe.png")).getImage();
        bottomPipeImg = new ImageIcon(getClass().getResource("./bottompipe.png")).getImage();

        // Bird
        bird = new Bird(birdImg);
        pipes = new ArrayList<Pipe>();

        // Place pipes timer
        placePipeTimer = new Timer(1500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                placePipes();
            }
        });

        // Game timer
        gameLoop = new Timer(1000 / 60, this); // how long it takes to start timer, milliseconds gone between frames

        // Buttons setup
        startButton = new JButton("Start Game");
        pauseButton = new JButton("Pause");
        resumeButton = new JButton("Resume");
        newGameButton = new JButton("New Game");

        startButton.setBounds(boardWidth / 2 - 60, boardHeight / 2 - 20, 120, 40);
        pauseButton.setBounds(10, 10, 100, 40);
        resumeButton.setBounds(boardWidth / 2 - 60, boardHeight / 2 - 20, 120, 40);
        newGameButton.setBounds(boardWidth / 2 - 60, boardHeight / 2 + 40, 120, 40);

        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startGame();
            }
        });

        pauseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                pauseGame();
            }
        });

        resumeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resumeGame();
            }
        });

        newGameButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startGame();
            }
        });

        setLayout(null);
        add(startButton);
        add(pauseButton);
        add(resumeButton);
        add(newGameButton);

        pauseButton.setVisible(false);
        resumeButton.setVisible(false);
        newGameButton.setVisible(false);
    }

    void startGame() {
        startButton.setVisible(false);
        pauseButton.setVisible(true);
        resumeButton.setVisible(false);
        newGameButton.setVisible(false);

        bird.y = birdY;
        velocityY = 0;
        pipes.clear();
        gameOver = false;
        score = 0;
        placePipeTimer.start();
        gameLoop.start();
    }

    void pauseGame() {
        gameLoop.stop();
        placePipeTimer.stop();
        pauseButton.setVisible(false);
        resumeButton.setVisible(true);
        newGameButton.setVisible(true);
    }

    void resumeGame() {
        gameLoop.start();
        placePipeTimer.start();
        pauseButton.setVisible(true);
        resumeButton.setVisible(false);
        newGameButton.setVisible(false);
    }

    void placePipes() {
        int randomPipeY = (int) (pipeY - pipeHeight / 4 - Math.random() * (pipeHeight / 2));
        int openingSpace = boardHeight / 4;

        Pipe topPipe = new Pipe(topPipeImg);
        topPipe.y = randomPipeY;
        pipes.add(topPipe);

        Pipe bottomPipe = new Pipe(bottomPipeImg);
        bottomPipe.y = topPipe.y + pipeHeight + openingSpace;
        pipes.add(bottomPipe);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {
        // Background
        g.drawImage(backgroundImg, 0, 0, this.boardWidth, this.boardHeight, null);
    
        // Bird
        g.drawImage(birdImg, bird.x, bird.y, bird.width, bird.height, null);
    
        // Pipes
        for (int i = 0; i < pipes.size(); i++) {
            Pipe pipe = pipes.get(i);
            g.drawImage(pipe.img, pipe.x, pipe.y, pipe.width, pipe.height, null);
        }
    
        // Score
        g.setColor(Color.white);
        g.setFont(new Font("Arial", Font.PLAIN, 32));
    
        if (gameOver) {
            // Top center the game over score
            String gameOverText = "Game Over: " + String.valueOf((int) score);
            FontMetrics metrics = g.getFontMetrics(g.getFont());
            int x = (boardWidth - metrics.stringWidth(gameOverText)) / 2;
            int y = metrics.getHeight(); // Adjusted to place it at the top, considering font height
            g.drawString(gameOverText, x, y);
        } else {
            // Score displayed on the top right during the game
            String scoreText = String.valueOf((int) score);
            int scoreX = boardWidth - 100; // Adjust position of score on the right side
            int scoreY = 35;
            g.drawString(scoreText, scoreX, scoreY);
        }
    }
    

    public void move() {
        // Bird
        velocityY += gravity;
        bird.y += velocityY;
        bird.y = Math.max(bird.y, 0); // Apply gravity to current bird.y, limit the bird.y to top of the canvas

        // Pipes
        for (int i = 0; i < pipes.size(); i++) {
            Pipe pipe = pipes.get(i);
            pipe.x += velocityX;

            if (!pipe.passed && bird.x > pipe.x + pipe.width) {
                score += 0.5; // 0.5 because there are 2 pipes! so 0.5 * 2 = 1, 1 for each set of pipes
                pipe.passed = true;
            }

            if (collision(bird, pipe)) {
                gameOver = true;
            }
        }

        if (bird.y > boardHeight) {
            gameOver = true;
        }
    }

    boolean collision(Bird a, Pipe b) {
        return a.x < b.x + b.width &&   // a's top left corner doesn't reach b's top right corner
               a.x + a.width > b.x &&   // a's top right corner passes b's top left corner
               a.y < b.y + b.height &&  // a's top left corner doesn't reach b's bottom left corner
               a.y + a.height > b.y;    // a's bottom left corner passes b's top left corner
    }

    @Override
    public void actionPerformed(ActionEvent e) { // Called every x milliseconds by gameLoop timer
        move();
        repaint();
        if (gameOver) {
            placePipeTimer.stop();
            gameLoop.stop();
            startButton.setVisible(true);
            pauseButton.setVisible(false);
            resumeButton.setVisible(false);
            newGameButton.setVisible(true);
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE && !gameOver) {
            velocityY = -9;
        }
    }

    // Not needed
    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {}

    public static void main(String[] args) {
        JFrame frame = new JFrame("Flappy Bird");
        FlappyBird game = new FlappyBird();
        frame.add(game);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
