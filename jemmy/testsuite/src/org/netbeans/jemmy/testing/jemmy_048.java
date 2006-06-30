/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is the Jemmy library.
 * The Initial Developer of the Original Software is Alexandre Iline.
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
