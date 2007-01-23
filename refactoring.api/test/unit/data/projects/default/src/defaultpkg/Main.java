/*
 * Main.java
 *
 * Created on January 18, 2007, 9:51 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package defaultpkg;

import java.util.List;

/**
 *
 * @author jp159440
 */
public class Main {
    
    /** Creates a new instance of Main */
    public Main() {
    }
    
    public void method(Main m) {
        List<Main> l;
    }
    
    public static Main getInstance() {
        return new Main();
    }
            
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Main m = new Main();
    }
    
}
