package org.barwing;
import javax.swing.JPanel;
public class Main extends JPanel implements Runnable {
    public @Override void run() {
        new Main().setOpaque(true);
    }
}

