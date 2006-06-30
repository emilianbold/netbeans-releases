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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.util.NbBundle;

/** class observing CTRL-F11 key and launching NodeGenerator
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a>
 * @version 0.1
 */
public class NodeGeneratorRunnable implements Runnable, AWTEventListener {
    
    // data folder where to store generated sources
    DataFolder targetDataFolder;
    String nodesPackage;
    String actionsPackage;
    JLabel help;
    NodeGeneratorPanel panel;
    boolean start;
    boolean defaultInline;
    boolean defaultNoBlock;
    
    /** Creates new instance of NodeGeneratorRunnable
     * @param targetDataFolder target data folder (root of packages)
     * @param nodesPackage String package for nodes
     * @param actionsPackage String package for actions
     * @param defaultInline boolean default inline selection
     * @param defaultNoBlock boolean default no block selection
     * @param panel NodeGeneratorPanel (caller)
     */    
    public NodeGeneratorRunnable(DataFolder targetDataFolder, String nodesPackage, String actionsPackage, boolean defaultInline, boolean defaultNoBlock, NodeGeneratorPanel panel) {
        this.targetDataFolder = targetDataFolder;
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
                    String directory = FileUtil.toFile(targetDataFolder.getPrimaryFile()).getAbsolutePath();
                    while ((file=new File(directory+"/"+nodesPackage.replace('.', '/')+"/"+name+index+".java")).exists()) {  // NOI18N
                        index = String.valueOf(i++);
                    }
                    NodeGenerator gen = new NodeGenerator(actionsPackage, nodesPackage, name+index, popup, defaultInline, defaultNoBlock);
                    if (NodeEditorPanel.showDialog(gen)) {
                        // save sources, refresh target folder and open node in source editor
                        gen.saveNewSources(targetDataFolder);
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
