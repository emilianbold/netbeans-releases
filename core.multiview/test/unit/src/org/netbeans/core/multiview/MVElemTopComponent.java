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

package org.netbeans.core.multiview;

import org.netbeans.core.spi.multiview.MultiViewElementCallback;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewFactory;
import org.openide.awt.UndoRedo;
import org.openide.util.lookup.Lookups;
import org.openide.windows.TopComponent;

/**
 *
 * @author  mkleint
 */
public class MVElemTopComponent extends TopComponent implements MultiViewElement {
    private StringBuffer log;
    public MultiViewElementCallback observer;
    private transient UndoRedo undoredo;
    
    MVElemTopComponent() {
        resetLog();
    }
    
    
    public String getLog() {
        return log.toString();
    }
    
    public void resetLog() {
        log = new StringBuffer();
    }
    
    public void componentActivated() {
        super.componentActivated();
        log.append("componentActivated-");
        
    }
    
    public void componentClosed() {
        super.componentClosed();
        log.append("componentClosed-");
    }
    
    public void componentDeactivated() {
        super.componentDeactivated();
        log.append("componentDeactivated-");
    }
    
    public void componentHidden() {
        super.componentHidden();
        log.append("componentHidden-");
    }
    
    public void componentOpened() {
        super.componentOpened();
        log.append("componentOpened-");
    }
    
    public void componentShowing() {
        super.componentShowing();
        log.append("componentShowing-");
    }
    
    
    public JComponent getToolbarRepresentation() {
        return new JToolBar();
    }
    
    public javax.swing.JComponent getVisualRepresentation() {
        return this;
    }
    
    public String preferredID() {
        return super.preferredID();
    }
    
//    public void removeActionRequestObserver() {
//        observer = null;
//    }
    
    
    public void setMultiViewCallback (MultiViewElementCallback callback) {
        this.observer = callback;
    }
    
    public void doRequestActive() {
        observer.requestActive();
    }

    public void doRequestVisible() {
        observer.requestVisible();
    }
    
    public void setUndoRedo(UndoRedo redo) {
        undoredo = redo;
    }
    
//    public UndoRedo getUndoRedo() {
//        return undoredo;
//    }
    
    public CloseOperationState canCloseElement() {
        return CloseOperationState.STATE_OK;
    }
    
}

