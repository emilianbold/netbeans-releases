/*
 * Main.java
 * 
 * Created on 14.06.2007, 18:55:38
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.installer.infra.utils.style;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.installer.infra.utils.style.checkers.Checker;
import org.netbeans.installer.infra.utils.style.checkers.LineLengthChecker;
import org.netbeans.installer.infra.utils.style.checkers.UnescapedStringChecker;

/**
 *
 * @author ks152834
 */
public class BasicStyleCheckerEngine {
    public static void main(String[] args) throws IOException {
        BasicStyleCheckerEngine engine = new BasicStyleCheckerEngine();
        
        for (String arg: args) {
            engine.check(new File(arg));
        }
    }
    
    private List<Checker> checkers;
    
    public BasicStyleCheckerEngine() {
        checkers = new LinkedList<Checker>();
        
        checkers.add(new LineLengthChecker());
        checkers.add(new UnescapedStringChecker());
    }
    
    public void check(
            final File file) throws IOException {
        final BufferedReader reader = new BufferedReader(new FileReader(file));
        
        String line = null;
        for (int i = 1; (line = reader.readLine()) != null; i++) {
            String error = "";
            
            for (Checker checker: checkers) {
                if (checker.accept(file)) {
                    final String message = checker.check(line);
                    
                    if (message != null) {
                        error += "        " + message + "\n";
                    }
                }
            }
            
            if (!error.equals("")) {
                System.out.println("    Line " + i + ":");
                System.out.println(error);
            }
        }
        
        reader.close();
    }
}
