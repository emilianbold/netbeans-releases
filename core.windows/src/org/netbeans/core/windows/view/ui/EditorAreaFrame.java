/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.core.windows.view.ui;


import java.awt.Component;
import javax.swing.JFrame;

import org.openide.util.NbBundle;


/**
 * Frame representing separate editor area.
 *
 * @author  Peter Zavadsky
 */
public class EditorAreaFrame extends JFrame {
    
    
    private Component desktop;
    
    /** Creates a new instance of EditorAreaFrame */
    public EditorAreaFrame() {
        super(NbBundle.getMessage(EditorAreaFrame.class, "LBL_EditorAreaFrameTitle"));
        
        setIconImage(MainWindow.createIDEImage());
        
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
