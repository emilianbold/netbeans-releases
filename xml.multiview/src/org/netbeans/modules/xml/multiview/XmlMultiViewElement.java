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

import org.openide.text.CloneableEditor;
import org.openide.text.NbDocument;

import javax.swing.*;
import javax.swing.text.Document;

/**
 * XmlMultiviewElement.java
 *
 * Created on October 5, 2004, 1:35 PM
 * @author  mkuchtiak
 */
public class XmlMultiViewElement extends AbstractMultiViewElement implements java.io.Serializable {
    static final long serialVersionUID = -326467724916080580L;
    
    private transient CloneableEditor xmlEditor;
    private transient javax.swing.JComponent toolbar;

    /** Creates a new instance of XmlMultiviewElement */
    public XmlMultiViewElement() {
    }
    
    /** Creates a new instance of XmlMultiviewElement */
    public XmlMultiViewElement(XmlMultiViewDataObject dObj) {
        super(dObj);
    }

    public void componentOpened() {
        XmlMultiViewEditorSupport support = dObj.getEditorSupport();
        if (support!=null) support.addXmlDocListener();
    }

    public void componentClosed() {
        XmlMultiViewEditorSupport support = dObj.getEditorSupport();
        if (support!=null) support.removeXmlDocListener();
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
        final CloneableEditor xmlEditor = getXmlEditor();
        return xmlEditor != null ? xmlEditor.getLookup() : null;
    }

    public javax.swing.JComponent getToolbarRepresentation() {
        if (toolbar == null) {
            final JEditorPane editorPane = getXmlEditor().getEditorPane();
            if (editorPane!= null) {
                final Document doc = editorPane.getDocument();
                if (doc instanceof NbDocument.CustomToolbar) {
                    toolbar = ((NbDocument.CustomToolbar) doc).createToolbar(editorPane);
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
        return getXmlEditor();
    }

    private CloneableEditor getXmlEditor() {
        if (xmlEditor == null) {
            xmlEditor = dObj.getEditorSupport().createCloneableEditor();
        }
        return xmlEditor;
    }
}
