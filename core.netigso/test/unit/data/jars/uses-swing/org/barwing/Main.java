package org.barwing;
import javax.swing.JFrame;
public class Main extends JFrame implements Runnable {
    public void run() {
        new Main().setVisible(true);
    }
}

