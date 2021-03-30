package Tests;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

public class Peer implements Runnable{
    public int port = 22222;
    public String ip = "localhost";
    public Scanner scaner = new Scanner(System.in);
    public String message = "";
    public String received = "";

    public int height = 576;
    public int width = 1024;

    public JFrame frame;
    public ServerSocket serverSocket;
    public DataOutputStream dos;
    public DataInputStream dis;

    public boolean accepted = false;

    public Integer key = 0;
    public int[] score = {0, 0};
    public boolean end = false;

    private final int paddleH = 5;
    private final int paddleW = 150;
    private final int paddleX_start = (width - paddleW) / 2;
    private int paddleX = paddleX_start;
    private int paddleX2 = paddleX_start;
    private final int paddleSpeed = 20;


    private final int ballR = 20;
    private final int ballX_start = 200 + (width - ballR) / 2;
    private final int ballY_start = (height - ballR) / 2;
    private int ballX = ballX_start;
    private int ballY = ballY_start;
    private int ballXSpeed_start = 2;
    private int ballYSpeed_start = -2;
    private int ballXSpeed = 0;
    private int ballYSpeed = 0;
    private int bounced = 0;

    private boolean readyYou = false;
    private boolean readyOpponent = false;


    public Peer(){
//        System.out.println("Podaj adres ipv4 z puli adresow prywatnych: ");
//        ip = scaner.nextLine();
//        System.out.println("Podaj numer portu z przedzialu 1024-65353");
//        port = scaner.nextInt();
        while(port < 1024 || port > 65353){
            System.out.println("Niepoprawny port, prosze wybrac z przedziału 1024-65353");
            port = scaner.nextInt();
        }

        frame = new JFrame();
        Game game = new Game();
        game.setPreferredSize(new Dimension(width, height));

        frame.setTitle("No czesc");
        frame.add(game);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setVisible(true);

        if (!connect()){
            initializeServer();
        }

        Thread thread = new Thread(this);
        thread.start();

    }

    public void run(){
        while (true){
            if (!accepted){
                listenForServerRequest();
            } else {
                tick();
            }
        }
    }

    public boolean connect(){
        try {
            Socket socket = new Socket(ip, port);
            dos = new DataOutputStream(socket.getOutputStream());
            dis = new DataInputStream(socket.getInputStream());
            accepted = true;
        } catch (IOException e){
            System.out.println("Cant find ip:port, strarting server");
            return false;
        }
        System.out.println("Connected to the server");
        return true;
    }

    public void initializeServer(){
        try {
            serverSocket = new ServerSocket(port, 8, InetAddress.getByName(ip));
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void listenForServerRequest(){
        Socket socket = null;
        try {
            socket = serverSocket.accept();
            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());
            accepted = true;
        } catch (Exception e){
            e.printStackTrace();
        }

        ballXSpeed_start *= -1;
        ballYSpeed_start *= -1;
        System.out.println("listenForServerRequest");
    }

    public void tick(){

        try{
            if (!message.equals("")) {
                dos.writeUTF(message);
                dos.flush();
                message = "";
            }

            if (dis.available() > 0) {
                received = dis.readUTF();
                System.out.println(received);
                if (received.equals(" ")){
                    readyOpponent = true;
                }
                if (received.equals("a")){
                    if (paddleX2 + paddleSpeed <= width - paddleW - 5) {
                        paddleX2 += paddleSpeed;
                    } else {
                        paddleX2 = width - paddleW - 5;
                    }
                }else if (received.equals("d")){
                    if (paddleX2 >= paddleSpeed + 5) {
                        paddleX2 -= paddleSpeed;
                    } else {
                        paddleX2 = 5;
                    }
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    @SuppressWarnings("unused")
    public static void main(String[] args) {
        Peer peer = new Peer();
    }

    public class Game extends JPanel implements KeyListener {
        public Timer timer = new Timer();
        public int delay = 10;

        Game(){
            setFocusable(true);
            requestFocus();
            this.addKeyListener(this);

            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
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

                key = e.getKeyCode();

                if (key == KeyEvent.VK_D) {
                    message = String.valueOf(e.getKeyChar());
//                    System.out.println("Paddle: " + paddleX + " " + paddleX2);
//                    System.out.println("Ball: " + ballX + " " + ballY);
                    if (paddleX + paddleSpeed <= getWidth() - paddleW - 5) {
                        paddleX += paddleSpeed;
                    } else {
                        paddleX = getWidth() - paddleW - 5;
                    }
                } else if (key == KeyEvent.VK_A) {
//                    System.out.println("Paddle: " + paddleX + " " + paddleX2);
//                    System.out.println("Ball: " + ballX + " " + ballY);
                    message = String.valueOf(e.getKeyChar());
                    if (paddleX >= paddleSpeed + 5) {
                        paddleX -= paddleSpeed;
                    } else {
                        paddleX = 5;
                    }
                } else if (key == KeyEvent.VK_SPACE){
                    message = String.valueOf(e.getKeyChar());
                    readyYou = true;
                }

                System.out.println(message);

        }


        public void moveBall(){
            if (ballX + ballXSpeed > width - ballR || ballX + ballXSpeed < ballR){
                ballXSpeed *= -1;
            }

            if ((ballY < 0 && (Math.abs(ballX + ballR/2 - (paddleX2 + paddleW/2))) < paddleW/2 + ballR)
                || (ballY > height - ballR  && (Math.abs(ballX + ballR/2 - (paddleX + paddleW/2))) < paddleW/2 + ballR)){
                if (bounced > 5 ){
                    ballYSpeed *= -1;
//                    System.out.println("Ball bounced: " + ballX + " " + ballY);
                    bounced = 0;
                }
            }   else  if (ballY > height - ballR){
                ballX = ballX_start;
                ballY = ballY_start;
                ballYSpeed = 0;
                ballXSpeed = 0;
                paddleX = paddleX_start;
                paddleX2 = paddleX_start;
                score[1]++;

            }   else if (ballY < 0){
                ballX = ballX_start;
                ballY = ballY_start;
                ballYSpeed = 0;
                ballXSpeed = 0;
                paddleX = paddleX_start;
                paddleX2 = paddleX_start;
                score[0]++;
            }

            bounced++;
            ballX += ballXSpeed;
            ballY += ballYSpeed;

            if (readyYou && readyOpponent){
                readyYou = false;
                readyOpponent = false;
                if (ballYSpeed == 0){
                    ballYSpeed = ballYSpeed_start;
                    ballXSpeed = ballXSpeed_start;
                }
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {

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

}
