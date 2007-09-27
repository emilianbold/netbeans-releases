/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
package org.netbeans.modules.sql.framework.ui.editor.property.impl;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.beans.PropertyEditorSupport;
import java.util.Vector;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListModel;

import org.openide.explorer.propertysheet.editors.EnhancedPropertyEditor;

/**
 * @author Ritesh Adval
 * @version $Revision$
 */
public class DefaultPropertyEditor {

    /**
     * Concrete implementation of PropertyEditorSupport to provide a list editor widget
     * for use in a bean property sheet.
     */
    public static class ListEditor extends PropertyEditorSupport {
        JList list;
        JPanel panel;
        JScrollPane sPane;

        public ListEditor(ListModel model) {
            this();
            list = new JList(model);
            panel.add(list, BorderLayout.CENTER);
        }

        public ListEditor(Vector listData) {
            this();
            list = new JList(listData);
            panel.add(list, BorderLayout.CENTER);
        }

        private ListEditor() {
            panel = new JPanel();
            panel.setLayout(new BorderLayout());
            sPane = new JScrollPane(panel);
            sPane.setPreferredSize(new Dimension(300, 150));
        }

        public Component getCustomEditor() {
            return sPane;
        }

        public boolean supportsCustomEditor() {
            return true;
        }
    }

    public static class PasswordTextEditor extends PropertyEditorSupport implements EnhancedPropertyEditor {
        class PasswordActionListener implements ActionListener {

            /**
             * Invoked when an action occurs.
             */
            public void actionPerformed(ActionEvent e) {
                setValue(new String(passField.getPassword()));
                passField.getFocusCycleRootAncestor().requestFocus();
                passField.requestFocus();
            }
        }

        class PasswordFocusAdapter extends FocusAdapter {
            public void focusLost(FocusEvent e) {
                setValue(new String(passField.getPassword()));
            }
        }

        private JPasswordField passField;

        public PasswordTextEditor() {
            super();
            this.passField = new JPasswordField();
            this.passField.addFocusListener(new PasswordFocusAdapter());
            this.passField.addActionListener(new PasswordActionListener());
        }

        public String getAsText() {
            return getEncPassword((String) this.getValue());
        }

        /**
         * Returns inplace custom editor
         * 
         * @return - inplace custom editor
         */
        public Component getInPlaceCustomEditor() {
            String value = (String) this.getValue();
            if (value != null) {
                passField.setText(value);
            }
            return passField;
        }

        /**
         * If has inplace costom editor
         * 
         * @return - true/false
         */
        public boolean hasInPlaceCustomEditor() {
            return true;
        }

        public boolean supportsCustomEditor() {
            return false;
        }

        /**
         * Test for support of editing of tagged values. Must also accept custom strings,
         * otherwise you may may specify a standard property editor accepting only tagged
         * values.
         * 
         * @return <code>true</code> if supported
         */
        public boolean supportsEditingTaggedValues() {
            return false;
        }

        private String getEncPassword(String val) {
            StringBuilder buf = new StringBuilder(30);

            for (int i = 0; i < val.length(); i++) {
                buf.append('*');
            }

            return buf.toString();
        }
    }

    /**
     * Concrete implementation of PropertyEditorSupport to provide a single-line textfield
     * widget (with no custom editor) for use in a bean property sheet.
     */
    public static class SingleLineTextEditor extends PropertyEditorSupport {
        private JTextField tf;

        public SingleLineTextEditor() {
            super();
            tf = new JTextField();
        }

        public Component getCustomEditor() {
            return tf;
        }

        public boolean supportsCustomEditor() {
            return false;
        }
    }

    /** Creates a new instance of DefaultPropertyEditor */
    public DefaultPropertyEditor() {
    }
}

