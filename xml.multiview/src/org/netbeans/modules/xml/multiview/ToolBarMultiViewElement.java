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

package org.netbeans.modules.xml.multiview;

import org.netbeans.core.spi.multiview.*;
import org.openide.windows.TopComponent;
import org.openide.util.lookup.Lookups;
import org.openide.awt.UndoRedo;
import org.openide.nodes.*;
import org.openide.explorer.view.*;
import org.openide.util.lookup.ProxyLookup;

import org.netbeans.modules.xml.multiview.ui.ToolBarDesignEditor;

/**
 * XmlMultiviewElement.java
 *
 * Created on October 5, 2004, 1:35 PM
 * @author  mkuchtiak
 */
public abstract class ToolBarMultiViewElement implements MultiViewElement {
    MultiViewElementCallback observer;
    private javax.swing.JComponent toolbar;
    private ToolBarDesignEditor editor;
    private XmlMultiViewDataObject dObj;
    
    public ToolBarMultiViewElement(XmlMultiViewDataObject dObj, ToolBarDesignEditor editor) {
        this.dObj=dObj;
        this.editor=editor;
    }
    
    public ToolBarMultiViewElement(XmlMultiViewDataObject dObj) {
        this.dObj=dObj;
    }
    
    protected void setVisualEditor(ToolBarDesignEditor editor) {
        this.editor=editor;
    }
    
    public CloseOperationState canCloseElement() {
        return CloseOperationState.STATE_OK;
    }    
    
    public void componentActivated() {
       editor.componentActivated();
    }
    
    public void componentClosed() {
        editor.componentClosed();
    }
    
    public void componentDeactivated() {
        editor.componentDeactivated();
    }
    
    public void componentHidden() {
        editor.componentHidden();
    }
    
    public void componentOpened() {
        editor.componentOpened();
    }
    
    public void componentShowing() {
        editor.componentShowing();
    }
    
    public javax.swing.Action[] getActions() {
        return dObj.getEditorSupport().getXmlTopComponent().getActions();
    }
    
    public org.openide.util.Lookup getLookup() {
        return new ProxyLookup(new org.openide.util.Lookup[] {
            dObj.getNodeDelegate().getLookup()
        });
    }
    
    public javax.swing.JComponent getToolbarRepresentation() {
        return editor.getStructureView();
    }
    
    public org.openide.awt.UndoRedo getUndoRedo() {
        return null;
    }
    
    public void setMultiViewCallback(MultiViewElementCallback callback) {
        observer=callback;
        if (dObj!=null) {
            TopComponent tc = callback.getTopComponent();
            if (tc.getDisplayName()==null) {
                tc.setDisplayName(dObj.getDisplayName());
                tc.setToolTipText(dObj.getPrimaryFile().getPath());
            }
        }
    }

    public javax.swing.JComponent getVisualRepresentation() {
        return editor;
    }

}
