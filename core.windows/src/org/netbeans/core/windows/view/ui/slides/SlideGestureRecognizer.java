/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core.windows.view.ui.slides;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import org.openide.util.WeakListeners;
import org.openide.windows.TopComponent;

/*
 *
 * @author Dafe Simonek
 */
final class SlideGestureRecognizer implements ActionListener, MouseListener, MouseMotionListener {
    /** container of sliding buttons */
    private SlideBar slideBar;
    
    SlideGestureRecognizer(SlideBar slideBar) {
        this.slideBar = slideBar;
    }
    
    public void actionPerformed(ActionEvent e) {
        slideBar.userClickedSlidingButton((Component)e.getSource());
    }

    public void mouseDragged(MouseEvent e) {
    }
    
    public void mouseMoved(MouseEvent e) {
    }
    
    public void mouseClicked(MouseEvent e) {
    }
    
    public void mouseEntered(MouseEvent e) {
    }
    
    public void mouseExited(MouseEvent e) {
    }
    
    /** Reacts to popup triggers on sliding buttons */
    public void mousePressed(MouseEvent e) {
        handlePopupRequests(e);
    }
    
    /** Reacts to popup triggers on sliding buttons */
    public void mouseReleased(MouseEvent e) {
        handlePopupRequests(e);
    }
    
    private void handlePopupRequests (MouseEvent e) {
        // don't react on popup triggers on whole bar
        if (e.getSource().equals(slideBar)) {
            return;
        }
        
        if (e.isPopupTrigger()) {
            slideBar.userTriggeredPopup(e, (Component)e.getSource());
        }
    }
    
    
}