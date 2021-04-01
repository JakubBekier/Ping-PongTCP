import javax.swing.*;

public class GameFrame extends JFrame {
    GameFrame(){
        super();
        GamePanel gamePanel = new GamePanel();

        this.setTitle("Ping Pong");
        this.add(gamePanel);
        this.pack();
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setResizable(false);
        this.setVisible(true);


    }
}
