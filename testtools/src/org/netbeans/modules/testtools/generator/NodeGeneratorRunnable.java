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
import org.openide.ErrorManager;
import org.openide.util.NbBundle;

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
    
    /** Creates new instance of NodeGeneratorRunnable
     * @param directory String destination directory (root of packages)
     * @param nodesPackage String package for nodes
     * @param actionsPackage String package for actions
     * @param defaultInline boolean default inline selection
     * @param defaultNoBlock boolean default no block selection
     * @param panel NodeGeneratorPanel (caller)
     */    
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
        if ((aWTEvent instanceof KeyEvent)&&(aWTEvent.getID()==KeyEvent.KEY_RELEASED)&&(((KeyEvent)aWTEvent).getKeyCode()==KeyEvent.VK_F11)&&((((KeyEvent)aWTEvent).getModifiers()&KeyEvent.CTRL_MASK)!=0)) {
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
                help.setText(NbBundle.getMessage(NodeGeneratorRunnable.class, "MSG_Waiting"));  // NOI18N
                try {
                    JPopupMenuOperator popup=new JPopupMenuOperator();
                    help.setText(NbBundle.getMessage(NodeGeneratorRunnable.class, "MSG_PleaseWait"));  // NOI18N
                    int i=2;
                    String name = "NewNode"; // NOI18N
                    String index = ""; // NOI18N
                    while ((file=new File(directory+"/"+nodesPackage.replace('.', '/')+"/"+name+index+".java")).exists()) {  // NOI18N
                        index = String.valueOf(i++);
                    }
                    NodeGenerator gen = new NodeGenerator(actionsPackage, nodesPackage, name+index, popup, defaultInline, defaultNoBlock);
                    if (NodeEditorPanel.showDialog(gen)) {
                        gen.saveNewSources(directory);
                        help.setText(NbBundle.getMessage(NodeGeneratorRunnable.class, "MSG_Finished")+gen.getNodeName());  // NOI18N
                    } else {
                        help.setText(NbBundle.getMessage(NodeGeneratorRunnable.class, "MSG_Canceled"));  // NOI18N
                    }
                } catch (JemmyException je) {
                    help.setText(NbBundle.getMessage(NodeGeneratorRunnable.class, "ERR_NoPopup")); // NOI18N
                } catch (Exception e) {
                    help.setText(NbBundle.getMessage(NodeGeneratorRunnable.class, "MSG_Exception")+e.getMessage());  // NOI18N
                    ErrorManager.getDefault().notify(e);
                }
            }
        } catch (InterruptedException ie) {
        } finally {
            Toolkit.getDefaultToolkit().removeAWTEventListener(this);
        }
    }
    
}
