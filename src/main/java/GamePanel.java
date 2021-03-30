import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Timer;
import java.util.TimerTask;

public class GamePanel extends JPanel implements KeyListener {

    public Timer timer = new Timer();
    public int delay = 10;
    public boolean accepted = false;
    public boolean end = false;
    public int[] score = {0, 0};
    public boolean readyYou = false;
    public boolean readyOpponent = false;

    public int height = 576;
    public int width = 1024;

    private final int paddleH = 5;
    private final int paddleW = 150;
    private final int paddleX_start = (width - paddleW) / 2;
    private int paddleX = paddleX_start;
    private int paddleX2 = paddleX_start;
    private final int paddleSpeed = 20;


    private final int ballR = 20;
    private final int ballX_start = (width - ballR) / 2;
    private final int ballY_start = (height - ballR) / 2;
    private int ballX = ballX_start;
    private int ballY = ballY_start;
    private int ballXSpeed_start = 2;
    private int ballYSpeed_start = -2;
    private int ballXSpeed = 0;
    private int ballYSpeed = 0;
    private int bounced = 0;

    public Connection connection = new Connection(this);

    GamePanel(){
        super();
        this.setFocusable(true);
        this.requestFocus();
        this.addKeyListener(this);
        this.setPreferredSize(new Dimension(width, height));

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                System.out.println(accepted);
                if (accepted) moveBall();
                repaint();
                if (end){
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.exit(0);
                }
            }
        }, 0, delay);
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {

        int key = e.getKeyCode();

        if (key == KeyEvent.VK_D) {
            connection.setMessage(String.valueOf(e.getKeyChar()));
            if (paddleX + paddleSpeed <= getWidth() - paddleW - 5) {
                paddleX += paddleSpeed;
            } else {
                paddleX = getWidth() - paddleW - 5;
            }
        } else if (key == KeyEvent.VK_A) {
            connection.setMessage(String.valueOf(e.getKeyChar()));
            if (paddleX >= paddleSpeed + 5) {
                paddleX -= paddleSpeed;
            } else {
                paddleX = 5;
            }
        } else if (key == KeyEvent.VK_SPACE){
            connection.setMessage(String.valueOf(e.getKeyChar()));
            readyYou = true;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    public void setAccepted(boolean state){
        accepted = state;
    }

    public void setReadyYou(boolean state){
        readyYou = state;
    }

    public void setReadyOpponent(boolean state){
        readyOpponent = state;
    }

    public void moveBall(){
        if (ballX + ballXSpeed > width - ballR || ballX + ballXSpeed < ballR){
            ballXSpeed *= -1;
        }

        if ((ballY < 0 && (Math.abs(ballX + ballR/2 - (paddleX2 + paddleW/2))) < paddleW/2 + ballR)
                || (ballY > height - ballR  && (Math.abs(ballX + ballR/2 - (paddleX + paddleW/2))) < paddleW/2 + ballR)){
            if (bounced > 5 ){
                ballYSpeed *= -1;
                bounced = 0;
            }
        }   else  if (ballY > height - ballR){
            resetGame();
            score[1]++;

        }   else if (ballY < 0){
            resetGame();
            score[0]++;
        }

        bounced++;
        ballX += ballXSpeed;
        ballY += ballYSpeed;

        if (readyYou && readyOpponent){
            readyYou = false;
            readyOpponent = false;
            if (ballYSpeed == 0){
                setDefaultBallSpeed();
            }
        }
    }

    public void movePaddle(boolean you, String key){
        if (you){
            if (key.equals("d")){
                if (paddleX + paddleSpeed <= getWidth() - paddleW - 5) {
                    paddleX += paddleSpeed;
                } else {
                    paddleX = getWidth() - paddleW - 5;
                }
            } else if (key.equals("a")){
                if (paddleX >= paddleSpeed + 5) {
                    paddleX -= paddleSpeed;
                } else {
                    paddleX = 5;
                }
            }
        } else {
            if (key.equals("d")){
                if (paddleX2 >= paddleSpeed + 5) {
                    paddleX2 -= paddleSpeed;
                } else {
                    paddleX2 = 5;
                }
            } else if (key.equals("a")){
                if (paddleX2 + paddleSpeed <= width - paddleW - 5) {
                    paddleX2 += paddleSpeed;
                } else {
                    paddleX2 = width - paddleW - 5;
                }
            }
        }
    }

    public void setDefaultBallSpeed(){
        ballXSpeed = ballXSpeed_start;
        ballYSpeed = ballYSpeed_start;
    }

    public void resetGame(){
        ballX = ballX_start;
        ballY = ballY_start;
        ballYSpeed = 0;
        ballXSpeed = 0;
        paddleX = paddleX_start;
        paddleX2 = paddleX_start;
    }

    public void reverseBallSpeed(){
        ballXSpeed_start *= -1;
        ballYSpeed_start *= -1;
    }

    @Override
    public void paintComponent(Graphics g){
        super.paintComponent(g);
        g.clearRect(0,0,getWidth(),getHeight());

        g.setColor(new Color(87,89,93));
        g.fillRect(0,0,getWidth(),getHeight());
        g.setColor(new Color(255, 255, 0));
        g.fillOval(ballX, ballY, ballR, ballR);

        g.setColor(new Color(0,255,0));
        g.fillRect(paddleX, getHeight() - paddleH, paddleW, paddleH);
        g.fillRect(paddleX2, 0, paddleW, paddleH);

        g.setFont(new Font(Font.SANS_SERIF,  Font.BOLD, 20));
        g.drawString("You: " + String.valueOf(score[0]), 10, 20);
        g.drawString("Opponent: " + String.valueOf(score[1]), width - 130, 20);

        if (score[0] >= 10){
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 75));
            g.drawString("Victory!", width/2 - 130, height/2);
            end = true;
        }else if (score[1] >= 10){
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 75));
            g.setColor(Color.red);
            g.drawString("Defeated.", width/2 - 130, height/2);
            end = true;
        }
    }

}
