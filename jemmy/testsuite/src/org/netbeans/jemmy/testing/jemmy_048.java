/*
 * Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License Version
 * 1.0 (the "License"). You may not use this file except in compliance with
 * the License. A copy of the License is available at http://www.sun.com/
 *
 * The Original Code is the Jemmy library.
 * The Initial Developer of the Original Code is Alexandre Iline.
 * All Rights Reserved.
 *
 * Contributor(s): Jiri Skrivanek.
 *
 */
package org.netbeans.jemmy.testing;

import java.util.*;
import javax.swing.*;
import org.netbeans.jemmy.operators.*;

/** Test whether Component.toString() is called in dispatch thread. It
 * is used in jemmy log messages.
 */
public class jemmy_048 extends JemmyTest {
    public int runIt(Object obj) {
        try {
            JFrame frame = new JFrame();
            frame.setTitle("Test Frame");
            frame.pack();
            frame.show();
            final JFrameOperator frameOper = new JFrameOperator("Test Frame");
            new Thread(new Runnable() {
                public void run() {
                    JLabelOperator labelOper = new JLabelOperator(frameOper);
                }
            }).start();
            MyLabel myLabel = new MyLabel();
            myLabel.setText("AAAAAAAAAAAAAA");
            frame.getContentPane().add(myLabel);
            frame.pack();
	} catch(Exception e) {
	    getOutput().printStackTrace(e);
	    finalize();
	    return(1);
	}
        return(0);
    }
    
    static class MyLabel extends JLabel {
        
        String dummy;
        
        public MyLabel() {
            super();
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    try {
                        Thread.sleep(500);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    dummy = "even more dummy";
                }
            });
            //System.out.println("Constructor finished...");
        }
        
        public String toString() {
            return dummy.toString();
        }
    }
}
