/** Project: Solo Lab 7 Assignment
 * Purpose Details: SpaceGame Mods
 * Course: IST 242 - Joe Oakes
 * Author: Jason Mathew
 * Date Developed: 05/01/2026
 * Last Date Changed: 05/03/2026
 * Rev: 1.0

 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.sound.sampled.*;

public class SpaceGame extends JFrame implements KeyListener {

    private static final int WIDTH = 500;
    private static final int HEIGHT = 500;
    private static final int PLAYER_WIDTH = 50;
    private static final int PLAYER_HEIGHT = 50;
    private static final int OBSTACLE_WIDTH = 20;
    private static final int OBSTACLE_HEIGHT = 20;
    private static final int PROJECTILE_WIDTH = 5;
    private static final int PROJECTILE_HEIGHT = 10;
    private static final int PLAYER_SPEED = 5;
    private static final int OBSTACLE_SPEED = 3;
    private static final int PROJECTILE_SPEED = 10;
    private static final int SHIELD_DURATION = 150;

    private int spriteWidth = 64;
    private int spriteHeight = 64;

    private int score = 0;
    private int health = 3;
    private int level = 1;
    private int countdown = 60;

    private boolean shieldActive = false;
    private int shieldTimer = 0;

    private JPanel gamePanel;
    private JLabel scoreLabel;
    private JLabel healthLabel;
    private JLabel levelLabel;
    private JLabel timerLabel;
    private JLabel shieldLabel;

    private Timer timer;
    private Timer countdownTimer;

    private boolean isGameOver;
    private int playerX, playerY;
    private int projectileX, projectileY;
    private boolean isProjectileVisible;
    private boolean isFiring;

    private List<Point> obstacles;
    private List<Point> stars;
    private List<Point> powerUps;

    private BufferedImage shipImage;
    private BufferedImage spriteSheet;

    private Clip fireClip;
    private Clip collisionClip;

    private Random rand = new Random();

    public SpaceGame() {
        setTitle("Space Game");
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        try {
            shipImage = ImageIO.read(new File("ship.png"));
            spriteSheet = ImageIO.read(new File("Obstacles.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Load audio clips
        fireClip = loadClip("fire.wav");
        collisionClip = loadClip("fire.wav"); // replace with collision.wav if you add one

        gamePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                draw(g);
            }
        };

        gamePanel.setLayout(null);

        scoreLabel = new JLabel("Score: 0");
        scoreLabel.setFont(new Font("Arial", Font.BOLD, 16));
        scoreLabel.setBounds(10, 10, 120, 20);
        scoreLabel.setForeground(Color.BLUE);
        gamePanel.add(scoreLabel);

        healthLabel = new JLabel("Health: ♥♥♥");
        healthLabel.setFont(new Font("Arial", Font.BOLD, 16));
        healthLabel.setBounds(10, 32, 150, 20);
        healthLabel.setForeground(Color.RED);
        gamePanel.add(healthLabel);

        levelLabel = new JLabel("Level: 1");
        levelLabel.setFont(new Font("Arial", Font.BOLD, 16));
        levelLabel.setBounds(10, 54, 120, 20);
        levelLabel.setForeground(Color.YELLOW);
        gamePanel.add(levelLabel);

        timerLabel = new JLabel("Time: 60");
        timerLabel.setFont(new Font("Arial", Font.BOLD, 16));
        timerLabel.setBounds(WIDTH - 110, 10, 100, 20);
        timerLabel.setForeground(Color.CYAN);
        gamePanel.add(timerLabel);

        shieldLabel = new JLabel("Shield: OFF");
        shieldLabel.setFont(new Font("Arial", Font.BOLD, 14));
        shieldLabel.setBounds(WIDTH - 120, 32, 110, 20);
        shieldLabel.setForeground(Color.LIGHT_GRAY);
        gamePanel.add(shieldLabel);

        add(gamePanel);
        gamePanel.setFocusable(true);
        gamePanel.addKeyListener(this);

        playerX = WIDTH / 2 - PLAYER_WIDTH / 2;
        playerY = HEIGHT - PLAYER_HEIGHT - 20;

        projectileX = playerX + PLAYER_WIDTH / 2 - PROJECTILE_WIDTH / 2;
        projectileY = playerY;

        isProjectileVisible = false;
        isGameOver = false;
        isFiring = false;

        obstacles = new ArrayList<>();
        powerUps = new ArrayList<>();

        stars = new ArrayList<>();
        for (int i = 0; i < 80; i++) {
            stars.add(new Point(rand.nextInt(WIDTH), rand.nextInt(HEIGHT)));
        }

        timer = new Timer(20, e -> {
            if (!isGameOver) {
                update();
                gamePanel.repaint();
            }
        });
        timer.start();

        countdownTimer = new Timer(1000, e -> {
            if (!isGameOver) {
                countdown--;
                timerLabel.setText("Time: " + countdown);
                if (countdown <= 0) {
                    isGameOver = true;
                    countdownTimer.stop();
                    timer.stop();
                    gamePanel.repaint();
                }
            }
        });
        countdownTimer.start();
    }

    private Clip loadClip(String filename) {
        try {
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(new File(filename));
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            return clip;
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void playSound(Clip clip) {
        if (clip != null) {
            clip.setFramePosition(0);
            clip.start();
        }
    }

    private void draw(Graphics g) {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, WIDTH, HEIGHT);

        for (Point star : stars) {
            g.setColor(generateRandomColor());
            g.fillOval(star.x, star.y, 2, 2);
        }

        if (shieldActive) {
            g.setColor(new Color(0, 180, 255, 150));
            g.fillOval(playerX - 8, playerY - 8, PLAYER_WIDTH + 16, PLAYER_HEIGHT + 16);
            g.setColor(Color.CYAN);
            g.drawOval(playerX - 8, playerY - 8, PLAYER_WIDTH + 16, PLAYER_HEIGHT + 16);
        }

        if (shipImage != null) {
            g.drawImage(shipImage, playerX, playerY, PLAYER_WIDTH, PLAYER_HEIGHT, null);
        } else {
            g.setColor(Color.WHITE);
            g.fillRect(playerX, playerY, PLAYER_WIDTH, PLAYER_HEIGHT);
        }

        if (isProjectileVisible) {
            g.setColor(Color.GREEN);
            g.fillRect(projectileX, projectileY, PROJECTILE_WIDTH, PROJECTILE_HEIGHT);
        }

        for (Point obstacle : obstacles) {
            if (spriteSheet != null) {
                int spriteIndex = rand.nextInt(4);
                int spriteX = spriteIndex * spriteWidth;
                g.drawImage(
                        spriteSheet.getSubimage(spriteX, 0, spriteWidth, spriteHeight),
                        obstacle.x, obstacle.y, OBSTACLE_WIDTH, OBSTACLE_HEIGHT, null
                );
            } else {
                g.setColor(Color.ORANGE);
                g.fillRect(obstacle.x, obstacle.y, OBSTACLE_WIDTH, OBSTACLE_HEIGHT);
            }
        }

        for (Point p : powerUps) {
            g.setColor(Color.GREEN);
            g.fillRect(p.x + 7, p.y, 6, 20);
            g.fillRect(p.x, p.y + 7, 20, 6);
        }

        if (isGameOver) {
            g.setColor(new Color(0, 0, 0, 160));
            g.fillRect(0, HEIGHT / 2 - 60, WIDTH, 120);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 28));
            g.drawString("Game Over!", WIDTH / 2 - 80, HEIGHT / 2 - 10);
            g.setFont(new Font("Arial", Font.BOLD, 18));
            g.drawString("Final Score: " + score, WIDTH / 2 - 65, HEIGHT / 2 + 25);
            g.drawString("Level Reached: " + level, WIDTH / 2 - 70, HEIGHT / 2 + 50);
        }
    }

    private void update() {
        int currentObstacleSpeed = OBSTACLE_SPEED + (level - 1);

        for (int i = 0; i < obstacles.size(); i++) {
            obstacles.get(i).y += currentObstacleSpeed;
            if (obstacles.get(i).y > HEIGHT) {
                obstacles.remove(i);
                i--;
            }
        }

        double spawnChance = 0.02 + (level - 1) * 0.005;
        if (Math.random() < spawnChance) {
            int obstacleX = rand.nextInt(WIDTH - OBSTACLE_WIDTH);
            obstacles.add(new Point(obstacleX, 0));
        }

        for (int i = 0; i < powerUps.size(); i++) {
            powerUps.get(i).y += 2;
            if (powerUps.get(i).y > HEIGHT) {
                powerUps.remove(i);
                i--;
            }
        }

        if (Math.random() < 0.003) {
            powerUps.add(new Point(rand.nextInt(WIDTH - 20), 0));
        }

        if (isProjectileVisible) {
            projectileY -= PROJECTILE_SPEED;
            if (projectileY < 0) {
                isProjectileVisible = false;
            }
        }

        if (shieldActive) {
            shieldTimer--;
            if (shieldTimer <= 0) {
                shieldActive = false;
                shieldLabel.setText("Shield: OFF");
                shieldLabel.setForeground(Color.LIGHT_GRAY);
            }
        }

        Rectangle playerRect = new Rectangle(playerX, playerY, PLAYER_WIDTH, PLAYER_HEIGHT);

        for (int i = 0; i < obstacles.size(); i++) {
            Rectangle obstacleRect = new Rectangle(
                    obstacles.get(i).x, obstacles.get(i).y, OBSTACLE_WIDTH, OBSTACLE_HEIGHT);
            if (playerRect.intersects(obstacleRect)) {
                obstacles.remove(i);
                i--;
                if (!shieldActive) {
                    playSound(collisionClip); // play collision sound
                    health--;
                    updateHealthLabel();
                    if (health <= 0) {
                        isGameOver = true;
                        timer.stop();
                        countdownTimer.stop();
                    }
                }
                break;
            }
        }

        for (int i = 0; i < powerUps.size(); i++) {
            Rectangle puRect = new Rectangle(powerUps.get(i).x, powerUps.get(i).y, 20, 20);
            if (playerRect.intersects(puRect)) {
                powerUps.remove(i);
                if (health < 3) health++;
                updateHealthLabel();
                break;
            }
        }

        Rectangle projectileRect = new Rectangle(projectileX, projectileY, PROJECTILE_WIDTH, PROJECTILE_HEIGHT);
        for (int i = 0; i < obstacles.size(); i++) {
            Rectangle obstacleRect = new Rectangle(
                    obstacles.get(i).x, obstacles.get(i).y, OBSTACLE_WIDTH, OBSTACLE_HEIGHT);
            if (projectileRect.intersects(obstacleRect)) {
                obstacles.remove(i);
                score += 10;
                isProjectileVisible = false;
                break;
            }
        }

        int newLevel = (score / 50) + 1;
        if (newLevel != level) {
            level = newLevel;
            levelLabel.setText("Level: " + level);
        }

        scoreLabel.setText("Score: " + score);
    }

    private void updateHealthLabel() {
        StringBuilder hearts = new StringBuilder("Health: ");
        for (int i = 0; i < health; i++) hearts.append("♥");
        for (int i = health; i < 3; i++) hearts.append("♡");
        healthLabel.setText(hearts.toString());
    }

    public static Color generateRandomColor() {
        Random rand = new Random();
        int r = rand.nextInt(256);
        int g = rand.nextInt(256);
        int b = rand.nextInt(256);
        return new Color(r, g, b);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();

        if (keyCode == KeyEvent.VK_LEFT && playerX > 0) {
            playerX -= PLAYER_SPEED;
        } else if (keyCode == KeyEvent.VK_RIGHT && playerX < WIDTH - PLAYER_WIDTH) {
            playerX += PLAYER_SPEED;
        } else if (keyCode == KeyEvent.VK_SPACE && !isFiring) {
            isFiring = true;
            projectileX = playerX + PLAYER_WIDTH / 2 - PROJECTILE_WIDTH / 2;
            projectileY = playerY;
            isProjectileVisible = true;
            playSound(fireClip); // play fire sound

            new Thread(() -> {
                try {
                    Thread.sleep(500);
                    isFiring = false;
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }).start();

        } else if (keyCode == KeyEvent.VK_CONTROL && !shieldActive) {
            shieldActive = true;
            shieldTimer = SHIELD_DURATION;
            shieldLabel.setText("Shield: ON");
            shieldLabel.setForeground(Color.CYAN);
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {}

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SpaceGame().setVisible(true));
    }
}
