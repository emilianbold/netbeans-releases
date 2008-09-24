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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.vmd.midp.propertyeditors;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.lang.ref.WeakReference;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.DesignDocument;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.api.model.TypeID;
import org.netbeans.modules.vmd.api.properties.DesignPropertyEditor;
import org.netbeans.modules.vmd.midp.codegen.InstanceNameResolver;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.points.MethodPointCD;
import org.netbeans.modules.vmd.midp.propertyeditors.api.usercode.PropertyEditorUserCode;
import org.openide.util.NbBundle;
import org.openide.awt.Mnemonics;

/**
 *
 * @author Karol Harezlak
 * @author Anton Chechel
 */
public final class PropertyEditorInstanceName extends DesignPropertyEditor {

    private static final String METHOD_NAME_TEXT = NbBundle.getMessage(PropertyEditorInstanceName.class, "LBL_METHOD_NAME"); // NOI18N
    private static final String INSTANCE_NAME_TEXT = NbBundle.getMessage(PropertyEditorInstanceName.class, "LBL_INSTANCE_NAME"); // NOI18N

    private TypeID typeID;
    private CustomEditor customEditor;
    private WeakReference<DesignComponent> component;

    private PropertyEditorInstanceName(TypeID typeID) {
        this.typeID = typeID;
        customEditor = new CustomEditor();
    }

    public static final DesignPropertyEditor createInstance(TypeID typeID) {
        return new PropertyEditorInstanceName(typeID);
    }

    @Override
    public Component getCustomEditor() {
        PropertyValue value = (PropertyValue) super.getValue();
        if (value != null) {
            customEditor.setText(MidpTypes.getString(value));
        }
        return customEditor;
    }

    @Override
    public void cleanUp(DesignComponent component) {
        super.cleanUp(component);
        if (customEditor != null) {
            customEditor.cleanUp();
            customEditor = null;
        }
        typeID = null;
    }



    @Override
    public String getAsText() {
        PropertyValue value = (PropertyValue) super.getValue();
        return value == null ? PropertyEditorUserCode.NULL_TEXT : MidpTypes.getString(value);
    }

    @Override
    public void setAsText(String text) {
        PropertyValue value = (PropertyValue) super.getValue();
        if (value == null) {
            return;
        }
        Object pv = value.getPrimitiveValue();
        if (pv != null && pv.equals(text)) {
            return;
        }
        
        saveValue(text);
    }

    private void saveValue(final String text) {
        if (component != null && component.get() != null) {
            final DesignComponent _component = component.get();
            _component.getDocument().getTransactionManager().readAccess(new Runnable() {

                public void run() {
                    PropertyValue newInstanceName = InstanceNameResolver.createFromSuggested(_component, text);
                    PropertyEditorInstanceName.super.setValue(newInstanceName);
                }
            });
        }
    }

    @Override
    public void customEditorOKButtonPressed() {
        String text = customEditor.getText();
        if (text.length() > 0) {
            saveValue(text);
        }
    }

    @Override
    public void init(DesignComponent component) {
        this.component = new WeakReference<DesignComponent>(component);
    }

    @Override
    public boolean supportsDefaultValue() {
        return false;
    }

    @Override
    public boolean canWrite() {
        if (component == null || component.get() == null) {
            return false;
        }
        
        final boolean[] canWrite = new boolean[1];
        final DesignDocument document = component.get().getDocument();
        document.getTransactionManager().readAccess(new Runnable() {

            public void run() {
                canWrite[0] = document.getSelectedComponents().size() <= 1;
            }
        });

        return canWrite[0];
    }

    private String getLabelName() {
        return MethodPointCD.TYPEID.equals(typeID) ? METHOD_NAME_TEXT : INSTANCE_NAME_TEXT;
    }

    private final class CustomEditor extends JPanel {

        private JTextField textField;

        public CustomEditor() {
            initComponents();
        }

        void cleanUp() {
            textField = null;
            this.removeAll();
        }

        private void initComponents() {
            setLayout(new GridBagLayout());
            GridBagConstraints constraints = new GridBagConstraints();
            JLabel label = new JLabel();
            Mnemonics.setLocalizedText( label, getLabelName() );
            constraints.insets = new Insets(12, 12, 3, 12);
            constraints.anchor = GridBagConstraints.NORTHWEST;
            constraints.gridx = 0;
            constraints.gridy = 0;
            constraints.weightx = 0.0;
            constraints.weighty = 0.0;
            constraints.fill = GridBagConstraints.NONE;
            add(label, constraints);

            textField = new JTextField();
            label.setLabelFor( textField );
            constraints.insets = new Insets(0, 12, 12, 12);
            constraints.anchor = GridBagConstraints.NORTHWEST;
            constraints.gridx = 0;
            constraints.gridy = 1;
            constraints.weightx = 1.0;
            constraints.weighty = 0.0;
            constraints.fill = GridBagConstraints.HORIZONTAL;
            add(textField, constraints);
            
            textField.getAccessibleContext().setAccessibleName( label.getText() );
            textField.getAccessibleContext().setAccessibleDescription( 
                    label.getText() );

            JPanel spacer = new JPanel();
            constraints.insets = new Insets(0, 0, 0, 0);
            constraints.anchor = GridBagConstraints.NORTHWEST;
            constraints.gridx = 0;
            constraints.gridy = 2;
            constraints.weightx = 1.0;
            constraints.weighty = 1.0;
            constraints.fill = GridBagConstraints.BOTH;
            add(spacer, constraints);
            setPreferredSize(new Dimension(300, 64));
        }

        public void setText(String text) {
            textField.setText(text);
        }

        public String getText() {
            return textField.getText();
        }
    }
}
