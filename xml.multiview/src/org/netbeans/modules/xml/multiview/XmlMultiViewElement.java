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
import org.openide.util.NbBundle;

/**
 * XmlMultiviewElement.java
 *
 * Created on October 5, 2004, 1:35 PM
 * @author  mkuchtiak
 */
public class XmlMultiViewElement implements MultiViewElement, java.io.Serializable {
    static final long serialVersionUID = -326467724916080580L;
    
    private TopComponent xmlTopComp;
    private XmlMultiViewDataObject dObj;
    private transient javax.swing.JComponent toolbar;
    
    /** Creates a new instance of XmlMultiviewElement */
    public XmlMultiViewElement() {
    }
    
    /** Creates a new instance of XmlMultiviewElement */
    public XmlMultiViewElement(TopComponent xmlTopComp, XmlMultiViewDataObject dObj) {
        this();
        this.dObj=dObj;
        this.xmlTopComp=xmlTopComp;
    }
    
    public CloseOperationState canCloseElement() {
        // resolving problem with closing deserialized editor (issue 57483)
        if (!dObj.canClose()) {
            return MultiViewFactory.createUnsafeCloseState(NbBundle.getMessage(ToolBarMultiViewElement.class,
                    "LBL_DataObjectModified"), null, null);
        } else {
            return CloseOperationState.STATE_OK;
        }
    }

    public void componentOpened() {
        XmlMultiViewEditorSupport support = dObj.getEditorSupport();
        if (support!=null) support.addListener();
    }

    public void componentClosed() {
        XmlMultiViewEditorSupport support = dObj.getEditorSupport();
        if (support!=null) support.removeListener();
    }

    public void componentDeactivated() {
    }

    public void componentHidden() {
    }

    public void componentActivated() {
    }

    public void componentShowing() {
    }
    
    public javax.swing.Action[] getActions() {
        return xmlTopComp.getActions();
    }
    
    public org.openide.util.Lookup getLookup() {
        if (xmlTopComp!=null) 
            return xmlTopComp.getLookup();
        else 
            return null;
    }

    public javax.swing.JComponent getToolbarRepresentation() {
            if (toolbar == null) {
                XmlMultiViewEditorSupport support = dObj.getEditorSupport(); ;
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
        return dObj.getEditorSupport().getUndoRedo0();
    }

    public javax.swing.JComponent getVisualRepresentation() {
        return xmlTopComp;
    }

    public void setMultiViewCallback(MultiViewElementCallback callback) {
        if (dObj!=null) {
            XmlMultiViewEditorSupport support = dObj.getEditorSupport();
            if (support!=null) {
                if (support.getMVTC() == null) {
                    support.setMVTC(callback.getTopComponent());
                }
                support.updateDisplayName();
            }
        }
    }
}
