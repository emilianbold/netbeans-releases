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

import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.netbeans.core.spi.multiview.MultiViewFactory;
import org.openide.awt.UndoRedo;
import org.openide.windows.TopComponent;

/**
 * XmlMultiviewElement.java
 *
 * Created on October 5, 2004, 1:35 PM
 * @author  mkuchtiak
 */
public class XmlMultiViewElement implements MultiViewElement {

    //private static final long serialVersionUID = 123456L;
    private TopComponent comp;
    private XmlMultiViewEditorSupport support;
    MultiViewElementCallback observer;
    private UndoRedo undoRedo;
    private javax.swing.JComponent toolbar;

    /** Creates a new instance of XmlMultiviewElement */
    public XmlMultiViewElement(TopComponent comp, XmlMultiViewEditorSupport support) {
        this.comp=comp;
        this.support=support;
        undoRedo = support.getUndoRedo0();
    }

    public CloseOperationState canCloseElement() {
        if (this.support.getDataObject().isModified()) {
            return MultiViewFactory.createUnsafeCloseState("Data object modified", null, null);
        } else {
            return CloseOperationState.STATE_OK;
        }
    }

    public void componentActivated() {
    }

    public void componentClosed() {
    }

    public void componentDeactivated() {
    }

    public void componentHidden() {
    }

    public void componentOpened() {
    }

    public void componentShowing() {
        /*
        XmlMultiViewDataObject dObj = (XmlMultiViewDataObject)support.getDataObject();
        if (dObj.changedFromUI) {
            dObj.updateDocument();
        }
         */
    }

    public javax.swing.Action[] getActions() {
        return new javax.swing.Action[]{};
    }

    public org.openide.util.Lookup getLookup() {
        return comp.getLookup();
    }

    public javax.swing.JComponent getToolbarRepresentation() {
            if (toolbar == null) {
                javax.swing.JEditorPane pane = support.getOpenedPanes()[0];
                if (pane != null) {
                    javax.swing.text.Document doc = pane.getDocument();
                    if (doc instanceof org.openide.text.NbDocument.CustomToolbar) {
                        toolbar = ((org.openide.text.NbDocument.CustomToolbar)doc).createToolbar(pane);
                    }
                }
                if (toolbar == null) {
                    // attempt to create own toolbar??
                    toolbar = new javax.swing.JPanel();
                }
            }
            return toolbar;
    }

    public org.openide.awt.UndoRedo getUndoRedo() {
        return undoRedo;
    }

    public javax.swing.JComponent getVisualRepresentation() {
        return comp;
    }

    public void setMultiViewCallback(MultiViewElementCallback callback) {
        observer=callback;
    }

}
