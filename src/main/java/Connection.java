import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class Connection implements Runnable{
    public GamePanel gamePanel;

    public String message = "";
    public String received;
    public boolean accepted = false;

    public int port = 22222;
    public String ip = "localhost";
    public Scanner scan = new Scanner(System.in);

    public ServerSocket serverSocket;
    public DataOutputStream dos;
    public DataInputStream dis;


    Connection(GamePanel gamePanel){
        this.gamePanel = gamePanel;

        System.out.println("Input the ip address of the second player: ");
        ip = scan.nextLine();
        System.out.println("Input the port (1024-65535) of the second player: ");
        port = scan.nextInt();

        if (!connect()){
            initializeServer();
        }

        Thread thread = new Thread(this);
        thread.start();
    }

    public boolean connect(){
        try {
            Socket socket = new Socket(ip, port);
            dos = new DataOutputStream(socket.getOutputStream());
            dis = new DataInputStream(socket.getInputStream());
            gamePanel.setAccepted(true);
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
            gamePanel.setAccepted(true);
            accepted = true;
        } catch (Exception e){
            e.printStackTrace();
        }

        gamePanel.reverseBallSpeed();
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
                if (received.equals(" ")){
                    gamePanel.setReadyOpponent(true);
                } else{
                    gamePanel.movePaddle(false ,received);
                }


            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(0);
        }
    }


    public void setMessage(String msg){
        message = msg;
    }

    @Override
    public void run() {
        while (true){
            if (!accepted){
                listenForServerRequest();
            } else {
                tick();
            }
        }
    }
}
