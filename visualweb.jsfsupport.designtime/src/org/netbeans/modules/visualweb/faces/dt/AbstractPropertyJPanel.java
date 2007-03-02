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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.visualweb.faces.dt;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.PropertyEditorSupport;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import com.sun.rave.designtime.DesignProperty;

/**
 * @author eric
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public abstract class AbstractPropertyJPanel extends JPanel implements ActionListener, KeyListener,
    DocumentListener, ListSelectionListener {

    protected boolean initializing;
    protected DesignProperty liveProperty;
    protected AbstractPropertyEditor propertyEditor;

    public AbstractPropertyJPanel(AbstractPropertyEditor propertyEditor, DesignProperty liveProperty) {

        super(new BorderLayout());
        initializing = true;
        setPropertyEditorAndDesignProperty(propertyEditor, liveProperty);
        initializeComponents();
    }

    public void actionPerformed(ActionEvent event) {

    }

    /**
     * Gives notification that an attribute or set of attributes changed.
     */
    public void changedUpdate(DocumentEvent event) {

        if (initializing) {
            return;
        }
        documentEvent(event);
    }

    public void documentEvent(DocumentEvent event) {
    }

    public void doLayout() {

        super.doLayout();
        initializing = false;
    }

    protected DesignProperty getDesignProperty() {

        return liveProperty;
    }

    protected PropertyEditorSupport getPropertyEditor() {

        return propertyEditor;
    }

    protected void grabCurrentValueFromPropertyEditor() {

    }

    protected abstract void initializeComponents();

    /**
     * Gives notification that there was an insert into the document.  The
     * range given by the DocumentEvent bounds the freshly inserted region.
     *
     * @param e the document event
     */
    public void insertUpdate(DocumentEvent event) {

        if (initializing) {
            return;
        }
        documentEvent(event);
    }

    public void keyPressed(KeyEvent event) {
    }

    public void keyReleased(KeyEvent event) {
    }

    public void keyTyped(KeyEvent event) {
    }

    /**
     * Gives notification that a portion of the document has been
     * removed.  The range is given in terms of what the view last
     * saw (that is, before updating sticky positions).
     *
     * @param e the document event
     */
    public void removeUpdate(DocumentEvent event) {

        if (initializing) {
            return;
        }
        documentEvent(event);
    }

    protected void setPropertyEditorAndDesignProperty(AbstractPropertyEditor propertyEditor,
        DesignProperty liveProperty) {

        this.propertyEditor = propertyEditor;
        this.liveProperty = liveProperty;
        grabCurrentValueFromPropertyEditor();
    }

    public void valueChanged(ListSelectionEvent event) {

    }
}
