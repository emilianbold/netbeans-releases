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

import org.openide.windows.TopComponent;

import javax.swing.*;

/**
 * XmlMultiviewElement.java
 *
 * Created on October 5, 2004, 1:35 PM
 * @author  mkuchtiak
 */
public class XmlMultiViewElement extends AbstractMultiViewElement implements java.io.Serializable {
    static final long serialVersionUID = -326467724916080580L;
    
    private TopComponent xmlTopComp;
    private transient javax.swing.JComponent toolbar;

    /** Creates a new instance of XmlMultiviewElement */
    public XmlMultiViewElement() {
    }
    
    /** Creates a new instance of XmlMultiviewElement */
    public XmlMultiViewElement(TopComponent xmlTopComponent, XmlMultiViewDataObject dObj) {
        super(dObj);
        this.xmlTopComp = xmlTopComponent;
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

    public org.openide.util.Lookup getLookup() {
        if (xmlTopComp != null) {
            return xmlTopComp.getLookup();
        } else {
            return null;
        }
    }

    public javax.swing.JComponent getToolbarRepresentation() {
        if (toolbar == null) {
            XmlMultiViewEditorSupport support = dObj.getEditorSupport();
            final JEditorPane[] panes = support.getOpenedPanes();
            if (panes!= null && panes[0] != null) {
                javax.swing.text.Document doc = panes[0].getDocument();
                if (doc instanceof org.openide.text.NbDocument.CustomToolbar) {
                    toolbar = ((org.openide.text.NbDocument.CustomToolbar) doc).createToolbar(panes[0]);
                }
            }
            if (toolbar == null) {
                // attempt to create own toolbar??
                toolbar = new javax.swing.JPanel();
            }
        }
        return toolbar;
    }

    public javax.swing.JComponent getVisualRepresentation() {
        if (xmlTopComp == null) {
            xmlTopComp = dObj.getEditorSupport().createSuperCloneableComponent();
        }
        return xmlTopComp;
    }
}
