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
package org.netbeans.modules.edm.editor.property.impl;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import java.util.Vector;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListModel;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.InplaceEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.explorer.propertysheet.PropertyModel;

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

        @Override
        public Component getCustomEditor() {
            return sPane;
        }

        @Override
        public boolean supportsCustomEditor() {
            return true;
        }
    }

    public static class PasswordTextEditor extends PropertyEditorSupport implements ExPropertyEditor, InplaceEditor.Factory {

        private InplaceEditor ed = null;

        public InplaceEditor getInplaceEditor() {
            if (ed == null) {
                ed = new InplacePassword();
            }
            return ed;
        }

        public void attachEnv(PropertyEnv env) {
            env.registerInplaceEditorFactory(this);
        }

        @Override
        public String getAsText() {
            return getEncPassword((String) this.getValue());
        }

        private String getEncPassword(String val) {
            StringBuilder buf = new StringBuilder(30);
            for (int i = 0; i < val.length(); i++) {
                buf.append('*');
            }
            return buf.toString();
        }
    }

    private static class InplacePassword implements InplaceEditor {

        private JPasswordField passField = new JPasswordField();
        private PropertyEditor editor = null;

        public void connect(PropertyEditor propertyEditor, PropertyEnv env) {
            editor = propertyEditor;
            reset();
        }

        public JComponent getComponent() {
            return passField;
        }

        public void clear() {
            //avoid memory leaks:
            editor = null;
            model = null;
        }

        public Object getValue() {
            return new String(passField.getPassword());
        }

        public void setValue(Object value) {
            passField.setText((String) value);
        }

        public boolean supportsTextEntry() {
            return true;
        }

        public void reset() {
            String value = (String) editor.getValue();
            if (value != null) {
                passField.setText(value);
            }
        }

        public KeyStroke[] getKeyStrokes() {
            return new KeyStroke[0];
        }

        public PropertyEditor getPropertyEditor() {
            return editor;
        }

        public PropertyModel getPropertyModel() {
            return model;
        }
        private PropertyModel model;

        public void setPropertyModel(PropertyModel propertyModel) {
            this.model = propertyModel;
        }

        public boolean isKnownComponent(Component component) {
            return component == passField || passField.isAncestorOf(component);
        }

        public void addActionListener(ActionListener actionListener) {
            //do nothing - not needed for this component
        }

        public void removeActionListener(ActionListener actionListener) {
            //do nothing - not needed for this component
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

        @Override
        public Component getCustomEditor() {
            return tf;
        }

        @Override
        public boolean supportsCustomEditor() {
            return false;
        }
    }

    /** Creates a new instance of DefaultPropertyEditor */
    public DefaultPropertyEditor() {
    }
}