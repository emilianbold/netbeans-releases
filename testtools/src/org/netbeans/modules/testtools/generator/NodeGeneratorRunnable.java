/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.testtools.generator;

/*
 * ComponentGeneratorRunnable.java
 *
 * Created on February 7, 2002, 4:41 PM
 */
import java.util.ArrayList;
import java.awt.event.AWTEventListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.*;
import javax.swing.JLabel;
import javax.swing.JInternalFrame;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.File;
import java.awt.event.ActionEvent;
import java.util.Properties;

import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.operators.*;
import org.netbeans.jemmy.operators.JPopupMenuOperator;

/** class observing CTRL-F11 key and launching NodeGenerator
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a>
 * @version 0.1
 */
public class NodeGeneratorRunnable implements Runnable, AWTEventListener {
    
    String directory;
    String nodesPackage;
    String actionsPackage;
    JLabel help;
    NodeGeneratorPanel panel;
    boolean start;
    boolean defaultInline;
    boolean defaultNoBlock;
    
    public NodeGeneratorRunnable(String directory, String nodesPackage, String actionsPackage, boolean defaultInline, boolean defaultNoBlock, NodeGeneratorPanel panel) {
        this.directory = directory;
        this.nodesPackage = nodesPackage;
        this.actionsPackage = actionsPackage;
        this.defaultInline = defaultInline;
        this.defaultNoBlock = defaultNoBlock;
        help = panel.getHelpLabel();
        this.panel = panel;
        start=false;
    }
    
    /** called when event is dispatched
     * @param aWTEvent aWTEvent
     */
    public void eventDispatched(java.awt.AWTEvent aWTEvent) {
        if ((aWTEvent instanceof KeyEvent)&&(aWTEvent.getID()==KeyEvent.KEY_RELEASED)&&(((KeyEvent)aWTEvent).getKeyCode()==KeyEvent.VK_F11)&&(((KeyEvent)aWTEvent).getModifiers()==KeyEvent.CTRL_MASK)) {
            start=true;
        }
    }
    
    
    /** method implementing Runnable interface
     */
    public void run() {
        try {
            Toolkit.getDefaultToolkit().addAWTEventListener(this, AWTEvent.KEY_EVENT_MASK);
            PrintStream out;
            File file;
            boolean write;
            while (!Thread.currentThread().interrupted()&&help.isVisible()) {
                while (!start) {
                    Thread.currentThread().sleep(100);
                }
                start=false;
                help.setText("Waiting for popup menu ..."); 
                try {
                    JPopupMenuOperator popup=new JPopupMenuOperator();
                    help.setText("Please wait, processing ..."); 
                    int i=2;
                    String name = "NewNode";
                    String index = "";
                    while ((file=new File(directory+"/"+nodesPackage.replace('.', '/')+"/"+name+index+".java")).exists()) { 
                        index = String.valueOf(i++);
                    }
                    NodeGenerator gen = new NodeGenerator(actionsPackage, nodesPackage, name+index, popup, defaultInline, defaultNoBlock);
                    if (NodeEditorPanel.showDialog(gen)) {
                        gen.saveNewSources(directory);
                        help.setText("Finished: "+gen.getNodeName()); 
                    } else {
                        help.setText("Operation canceled."); 
                    }
                } catch (JemmyException je) {
                    help.setText("No Popup menu found, try it again (Use CTRL-F11).");
                } catch (Exception e) {
                    help.setText("Exception: "+e.getMessage()); 
//                    e.printStackTrace();
                }
            }
        } catch (InterruptedException ie) {
        } finally {
            Toolkit.getDefaultToolkit().removeAWTEventListener(this);
        }
    }
    
}
