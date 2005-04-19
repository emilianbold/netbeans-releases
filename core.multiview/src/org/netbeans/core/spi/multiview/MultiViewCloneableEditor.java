/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core.spi.multiview;

import javax.swing.JComponent;
import javax.swing.JToolBar;
import javax.swing.text.Document;
import org.openide.text.CloneableEditor;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.NbDocument;

/**
 * @author  mkleint
 */
abstract class MultiViewCloneableEditor extends CloneableEditor  implements MultiViewElement {
    
    private static final long serialVersionUID =-3126744316644172415L;
    
    private transient MultiViewElementCallback multiViewObserver;
    private transient JToolBar bar;
    
    /** Creates a new instance of MultiViewClonableEditor */
    public MultiViewCloneableEditor() {
        this(null);
    }
    
    public MultiViewCloneableEditor(CloneableEditorSupport support) {
        super(support);
    }
    
    public JComponent getToolbarRepresentation() {
        Document doc = getEditorPane().getDocument();
        if (doc instanceof NbDocument.CustomToolbar) {
            if (bar == null) {
                bar = ((NbDocument.CustomToolbar)doc).createToolbar(getEditorPane());
            }
            return bar;
        }
        return null;
    }
    
    public javax.swing.JComponent getVisualRepresentation() {
        return this;
    }
    
    public final void setMultiViewCallback(MultiViewElementCallback callback) {
        multiViewObserver = callback;
    }
    
    protected final MultiViewElementCallback getElementObserver() {
        return multiViewObserver;
    }
    
    public void componentActivated() {
        super.componentActivated();
    }
    
    public void componentClosed() {
        super.componentClosed();
    }
    
    public void componentDeactivated() {
        super.componentDeactivated();
    }
    
    public void componentHidden() {
        super.componentHidden();
    }
    
    public void componentOpened() {
        super.componentOpened();
    }
    
    public void componentShowing() {
        if (multiViewObserver != null) {
            updateName();
        }
        super.componentShowing();
    }
    
    public javax.swing.Action[] getActions() {
        return super.getActions();
    }
    
    public org.openide.util.Lookup getLookup() {
        return super.getLookup();
    }
    
    public String preferredID() {
        return super.preferredID();
    }
    
    
    public void requestVisible() {
        if (multiViewObserver != null) {
            multiViewObserver.requestVisible();
        } else {
            super.requestVisible();
        }
    }
    
    public void requestActive() {
        if (multiViewObserver != null) {
            multiViewObserver.requestActive();
        } else {
            super.requestActive();
        }
    }
    
    
    public void updateName() {
        super.updateName();
        if (multiViewObserver != null) {
            multiViewObserver.updateTitle(getDisplayName());
        }
    }
    
    public void open() {
        if (multiViewObserver != null) {
            multiViewObserver.requestVisible();
        } else {
            super.open();
        }
        
    }
    
    public CloseOperationState canCloseElement() {
        throw new IllegalStateException("Not implemented yet.");
//        return CloseOperationState.STATE_OK;
    }
    
}
