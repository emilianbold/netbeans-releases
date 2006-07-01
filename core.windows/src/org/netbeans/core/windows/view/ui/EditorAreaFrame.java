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


package org.netbeans.core.windows.view.ui;


import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import org.netbeans.core.windows.view.Controller;
import org.openide.util.NbBundle;

import javax.swing.*;
import java.awt.*;


/**
 * Frame representing separate editor area.
 *
 * @author  Peter Zavadsky
 */
public class EditorAreaFrame extends JFrame {


    private Component desktop;
    private Controller controller;
    private long frametimestamp = 0;

    /** Creates a new instance of EditorAreaFrame */
    public EditorAreaFrame() {
        super(NbBundle.getMessage(EditorAreaFrame.class, "LBL_EditorAreaFrameTitle"));
        
        setIconImage(MainWindow.createIDEImage());
        
    }
    
    public void setWindowActivationListener(Controller control) {
        controller = control;
        addWindowListener(new WindowAdapter() {
            public void windowActivated(WindowEvent evt) {
                if (frametimestamp != 0 && System.currentTimeMillis() > frametimestamp + 500) {
                    controller.userActivatedEditorWindow();
                }
            }
            public void windowOpened(WindowEvent event) {
                frametimestamp = System.currentTimeMillis();
            }
        });
    }
    
    public void toFront() {
        // ignore the window activation event, is not done by user.
        frametimestamp = System.currentTimeMillis();
        super.toFront();
    }
    
    public void setVisible(boolean visible) {
        frametimestamp = System.currentTimeMillis();
        super.setVisible(visible);
    }
    
    public void setDesktop(Component component) {
        if(desktop == component) {
            return;
        }
        
        if(desktop != null) {
            getContentPane().remove(desktop);
        }
        
        desktop = component;
        
        if(component != null) {
            getContentPane().add(component);
        }
    }

    private long timeStamp = 0; 
    
    public void setUserStamp(long stamp) {
        timeStamp = stamp;
    }
    
    public long getUserStamp() {
        return timeStamp;
    }
    
    private long mainWindowStamp = 0;
    
    public void setMainWindowStamp(long stamp) {
        mainWindowStamp = stamp;
    }
    
    public long getMainWindowStamp() {
        return mainWindowStamp;
    }
    
    
    
}
