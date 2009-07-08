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
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.text.Document;
import org.netbeans.api.editor.DialogBinding;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.modules.vmd.api.io.DataObjectContext;
import org.netbeans.modules.vmd.api.io.ProjectUtils;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.api.model.TypeID;
import org.netbeans.modules.vmd.api.properties.DesignPropertyEditor;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.points.CallPointCD;
import org.netbeans.modules.vmd.midp.components.points.IfPointCD;
import org.netbeans.modules.vmd.midp.components.points.MethodPointCD;
import org.netbeans.modules.vmd.midp.components.points.SwitchPointCD;
import org.netbeans.modules.vmd.midp.components.sources.SwitchCaseEventSourceCD;
import org.netbeans.modules.vmd.midp.propertyeditors.api.usercode.CodeUtils;
import org.netbeans.modules.vmd.midp.propertyeditors.api.usercode.PropertyEditorUserCode;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;

/**
 *
 * @author Anton Chechel
 */
public final class PropertyEditorJavaString extends DesignPropertyEditor {

    private static final String JAVA_CODE = NbBundle.getMessage(PropertyEditorJavaString.class, "LBL_JAVA_CODE_STR"); // NOI18N
    private static final String METHOD_NAME = NbBundle.getMessage(PropertyEditorJavaString.class, "LBL_METHOD_NAME_STR"); // NOI18N
    private static final String CONDITION_EXPRESSION = NbBundle.getMessage(PropertyEditorJavaString.class, "LBL_CONDITION_EXPRESSION_STR"); // NOI18N
    private static final String SWITCH_OPERAND = NbBundle.getMessage(PropertyEditorJavaString.class, "LBL_SWITCH_OPERAND_STR"); // NOI18N
    private static final String CASE_OPERAND = NbBundle.getMessage(PropertyEditorJavaString.class, "LBL_CASE_OPERAND_STR"); // NOI18N
    private static final String JAVA_EXPRESSION = NbBundle.getMessage(PropertyEditorJavaString.class, "LBL_JAVA_EXPRESSION_STR"); // NOI18N
    
    private static final String AMP   = "&";
    private static final String COLON = ":";
    
    protected WeakReference<DesignComponent> component;
    private TypeID typeID;
    private CustomEditor customEditor;
    private PropertyValue resetToDefaultValue;

    private PropertyEditorJavaString(TypeID typeID) {
        this.typeID = typeID;
    }
    
    private PropertyEditorJavaString(TypeID typeID, PropertyValue resetToDefaultValue) {
        this(typeID);
        this.resetToDefaultValue = resetToDefaultValue;
    }

    public static final PropertyEditorJavaString createInstance(TypeID typeID) {
        return new PropertyEditorJavaString(typeID);
    }
    
    public static final PropertyEditorJavaString createInstance(TypeID typeID, PropertyValue resetTodefaultValue) {
        return new PropertyEditorJavaString(typeID, resetTodefaultValue);
    }

    @Override
    public void cleanUp(DesignComponent component) {
        super.cleanUp(component);
        typeID = null;
        if (customEditor != null) {
            customEditor.cleanUp();
            customEditor = null;
        }
        this.component = null;
        resetToDefaultValue = null;
    }

    @Override
    public Component getCustomEditor() {
        if (customEditor == null) {
            customEditor = new CustomEditor();
        }
        PropertyValue value = (PropertyValue) super.getValue();
        if (value != null) {
            customEditor.setText(MidpTypes.getJavaCode(value));
        }
        customEditor.init();
        return customEditor;
    }

    @Override
    public String getAsText() {
        PropertyValue value = (PropertyValue) super.getValue();
        if (value == null) {
            return PropertyEditorUserCode.NULL_TEXT;
        }
        return MidpTypes.getJavaCode(value);
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

    @Override
    public Object getDefaultValue() {
        return resetToDefaultValue;
    }

    private void saveValue(String text) {
        if (text != null) {
            super.setValue(MidpTypes.createJavaCodeValue(text));
        }
    }

    @Override
    public void customEditorOKButtonPressed() {
        String text = customEditor.getText();
        saveValue(text);
    }

    @Override
    public boolean supportsDefaultValue() {
        return true;
    }

    @Override
    public String getCustomEditorTitle() {
        String title = getLabelName();
        title = title.replace( AMP , "");
        title = title.replace( COLON , "");
        return title;
    }

    @Override
    public void init(DesignComponent component) {
        if (component != null) {
            this.component = new WeakReference<DesignComponent>(component);
        }
    }

    private String getLabelName() {
        if (typeID.equals(CallPointCD.TYPEID)) {
            return JAVA_CODE;
        } else if (typeID.equals(MethodPointCD.TYPEID)) {
            return METHOD_NAME;
        } else if (typeID.equals(IfPointCD.TYPEID)) {
            return CONDITION_EXPRESSION;
        } else if (typeID.equals(SwitchPointCD.TYPEID)) {
            return SWITCH_OPERAND;
        } else if (typeID.equals(SwitchCaseEventSourceCD.TYPEID)) {
            return CASE_OPERAND;
        }
        return JAVA_EXPRESSION;
    }

    private final class CustomEditor extends JPanel {

        private JEditorPane textPane;

        public CustomEditor() {
            initComponents();
        }

        void cleanUp() {
            textPane = null;
            this.removeAll();
        }

        private void initComponents() {
            setLayout(new GridBagLayout());
            GridBagConstraints constraints = new GridBagConstraints();
            JLabel label = new JLabel();

            Mnemonics.setLocalizedText(label, getLabelName());

            constraints.insets = new Insets(12, 12, 3, 12);
            constraints.anchor = GridBagConstraints.NORTHWEST;
            constraints.gridx = 0;
            constraints.gridy = 0;
            constraints.weightx = 0.0;
            constraints.weighty = 0.0;
            constraints.fill = GridBagConstraints.BOTH;
            add(label, constraints);

            textPane = new JEditorPane();

            label.setLabelFor( textPane );
            
            textPane.getAccessibleContext().setAccessibleName( label.getText());
            textPane.getAccessibleContext().setAccessibleDescription( label.getText());
            
            SwingUtilities.invokeLater(new Runnable() {

                //otherwise we get: java.lang.AssertionError: BaseKit.install() incorrectly called from non-AWT thread.
                public void run() {
                    textPane.setContentType("text/x-java"); // NOI18N
                }
            });
            textPane.setPreferredSize(new Dimension(400, 100));
            JScrollPane jsp = new JScrollPane();
            jsp.setViewportView(textPane);
            constraints.insets = new Insets(0, 12, 12, 12);
            constraints.anchor = GridBagConstraints.NORTHWEST;
            constraints.gridx = 0;
            constraints.gridy = 1;
            constraints.weightx = 1.0;
            constraints.weighty = 1.0;
            constraints.fill = GridBagConstraints.BOTH;
            add(jsp, constraints);
        }

        public void setText(String text) {
            textPane.setText(text);
        }

        public String getText() {
            return textPane.getText();
        }

        public void init() {
            if (component == null || component.get() == null) {
                return;
            }
            DesignComponent _component = component.get();

            javax.swing.text.Document swingDoc = textPane.getDocument();
            if (swingDoc.getProperty(JavaSource.class) == null) {
                DataObjectContext context = ProjectUtils.getDataObjectContextForDocument(_component.getDocument());
                swingDoc.putProperty(Document.StreamDescriptionProperty, context.getDataObject());
                int offset = CodeUtils.getMethodOffset(context);
                String text = textPane.getText();
                DialogBinding.bindComponentToFile(context.getDataObject().getPrimaryFile(), offset, 0, textPane);
                textPane.setText(text);
            }
        }
    }
}