/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
