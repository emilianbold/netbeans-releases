/*
 * MVElem.java
 *
 * Created on April 2, 2004, 3:02 PM
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

