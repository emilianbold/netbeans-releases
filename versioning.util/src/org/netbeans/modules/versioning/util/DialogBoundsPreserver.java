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
package org.netbeans.modules.versioning.util;

import java.awt.Rectangle;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.prefs.Preferences;
import org.openide.util.Utilities;

/**
 *
 * Stores under a key the registered windows size and position when it si closed and
 * sets them back when the window gets open again.
 * 
 * @author Tomas Stupka
 */
public class DialogBoundsPreserver implements WindowListener {

    private static final String DELIMITER = "#";        // NOI18N
    private Preferences preferences;
    private String key;

    public DialogBoundsPreserver(Preferences preferences, String key) {
        this.preferences = preferences;            
        this.key = key;
    }

    public void windowOpened(WindowEvent evt) {
        Rectangle r = getDialogBounds();        
        if(r != null && checkBounds(r) ) {         
            evt.getWindow().setBounds(r);            
        }                
    }
    public void windowClosing(WindowEvent evt) {
        // ignore 
    }
    public void windowClosed(WindowEvent evt) {
        Rectangle r = evt.getWindow().getBounds();
        if(checkBounds(r)) {
            setDialogBounds(r);   
        }   
    }
    public void windowIconified(WindowEvent arg0) {
        // ignore
    }
    public void windowDeiconified(WindowEvent arg0) {
        // ignore
    }
    public void windowActivated(WindowEvent arg0) {
        // ignore
    }
    public void windowDeactivated(WindowEvent arg0) {
        // ignore
    }    

    private boolean checkBounds(Rectangle r) {
        Rectangle screen = Utilities.getUsableScreenBounds();
        return r.getX() >= 0 && r.getX() < screen.getWidth() && 
               r.getY() >= 0 && r.getY() < screen.getHeight() &&
               r.getWidth() <= screen.getWidth() - r.getX() && 
               r.getHeight() <= screen.getHeight() - r.getY();                 
    }   

    private void setDialogBounds(Rectangle r) {        
        preferences.put(key, r.getX() + DELIMITER + r.getY() + DELIMITER + r.getWidth() + DELIMITER + r.getHeight());         // NOI18N   
    }        

    private Rectangle getDialogBounds() {
        String size = preferences.get(key, DELIMITER);        
        if(size != null) {                                    
            String[] dim = size.split(DELIMITER);             
            if(dim.length != 4 || 
               dim[0].trim().equals("") ||                                      // NOI18N 
               dim[1].trim().equals("") ||                                      // NOI18N
               dim[2].trim().equals("") ||                                      // NOI18N    
               dim[3].trim().equals("") )                                       // NOI18N
            {
                return null;
            }
            Rectangle r = new Rectangle();
            r.setRect(Double.parseDouble(dim[0]), 
                      Double.parseDouble(dim[1]), 
                      Double.parseDouble(dim[2]), 
                      Double.parseDouble(dim[3]));
            return r;
        }
        return null;                
    }        
    
}
