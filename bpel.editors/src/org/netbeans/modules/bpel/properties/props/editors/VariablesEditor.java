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
 * License. When distributing the software, include this License Header
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
package org.netbeans.modules.bpel.properties.props.editors;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;

import java.beans.PropertyEditorSupport;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditor;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.netbeans.modules.soa.ui.form.ReusablePropertyCustomizer;

import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.Process;
import org.netbeans.modules.bpel.model.api.Validate;
import org.netbeans.modules.bpel.model.api.Variable;
import org.netbeans.modules.bpel.model.api.VariableContainer;
import org.netbeans.modules.bpel.model.api.VariableDeclaration;
import org.netbeans.modules.bpel.model.api.support.VariableUtil;
import org.netbeans.modules.bpel.model.api.references.BpelReference;
import org.netbeans.modules.bpel.model.api.references.ReferenceCollection;
import org.netbeans.modules.bpel.properties.props.PropertyVetoError;
import org.netbeans.modules.bpel.nodes.ValidateNode;
import static org.netbeans.modules.xml.misc.UI.*;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2009.04.10
 */
public class VariablesEditor extends PropertyEditorSupport implements ExPropertyEditor {
    
    public String getAsText() {
        Object value = super.getValue();

        if (value == null) {
            return null;
        }
        return value.toString();
    }
    
    public void setAsText(String text) throws IllegalArgumentException {
        setValue(text);
    }
    
    public boolean supportsCustomEditor() {
        return true;
    }
    
    public void attachEnv(PropertyEnv propertyEnv) {
        myPropertyEnv = propertyEnv;
    }

    public Component getCustomEditor() {
        return new VariablesListCustomizer(myPropertyEnv, this);
    }

    // ------------------------------------------------------------------------------------------------------------------------
    private static class VariablesListCustomizer extends JPanel implements PropertyChangeListener, ReusablePropertyCustomizer {
    
        VariablesListCustomizer(PropertyEnv propertyEnv, PropertyEditor propertyEditor) {
            super();
            init(propertyEnv, propertyEditor);
        }
        
        public void init(PropertyEnv propertyEnv, PropertyEditor propertyEditor) {
            myPropertyEditor = propertyEditor;
            propertyEnv.setState(PropertyEnv.STATE_NEEDS_VALIDATION);
            propertyEnv.addPropertyChangeListener(this);
            myValidate = getValidate(propertyEnv);

            JPanel panel = createPanel();
            // panel.setBorder(new javax.swing.border.LineBorder(java.awt.Color.red));
            // setBorder(new javax.swing.border.LineBorder(java.awt.Color.green));
            setLayout(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();

            c.weightx = 1.0;
            c.weighty = 1.0;
            c.anchor = GridBagConstraints.NORTHWEST;
            c.fill = GridBagConstraints.BOTH;

            JScrollPane scroll = new JScrollPane(panel);
            Dimension dimension = new Dimension(250, 200);
            scroll.setMinimumSize(dimension);
            scroll.setPreferredSize(dimension);
            add(scroll, c);
        }

        private JPanel createPanel() {
            JPanel panel = new JPanel(new GridBagLayout());
            // panel.setBorder(new javax.swing.border.LineBorder(java.awt.Color.blue));
            GridBagConstraints c = new GridBagConstraints();

            c.weightx = 1.0;
            c.weighty = 1.0;
            c.insets = new Insets(LARGE_SIZE, MEDIUM_SIZE, 0, 0);
            c.anchor = GridBagConstraints.NORTHWEST;
            panel.add(createInnerPanel(), c);

            return panel;
        }

        private JPanel createInnerPanel() {
            List<Object> objects = VariableUtil.getAllScopeVariables(myValidate);
            myAllVariables = new ArrayList<Variable>();

            for (Object object : objects) {
                if (object instanceof Variable) {
                    myAllVariables.add((Variable) object);
                }
            }
            List<Variable> variables = getVariables();

            int size = myAllVariables.size();
            myCheckBoxes = new JCheckBox[size];
            JLabel[] labels = new JLabel[size];
            Variable variable;

            JPanel panel = new JPanel(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();
            c.weighty = 0.0;

            for (int i=0; i < size; i++) {
                variable = myAllVariables.get(i);

                c.gridy++;
                c.weightx = 0.0;
                c.fill = GridBagConstraints.NONE;
                c.anchor = GridBagConstraints.NORTHWEST;
                myCheckBoxes[i] = new JCheckBox();
                myCheckBoxes[i].setSelected(variables.contains(variable));
                panel.add(myCheckBoxes[i], c);

                c.weightx = 1.0;
                c.anchor = GridBagConstraints.WEST;
                c.fill = GridBagConstraints.HORIZONTAL;
                labels[i] = createLabel(VariableUtil.getGoodVariableName(variable));
                panel.add(labels[i], c);
            }
            return panel;
        }

        public void propertyChange(PropertyChangeEvent event) {
            StringBuilder builder = new StringBuilder();
            BpelReference<Variable> reference;

            for (int i=0; i < myCheckBoxes.length; i++) {
                if ( !myCheckBoxes[i].isSelected()) {
                    continue;
                }
                reference = ((ReferenceCollection) myAllVariables.get(i)).createReference(myAllVariables.get(i), Variable.class);
                builder.append(reference.getRefString());
                builder.append(" "); // NOI18N
            }
            String value;

            if (builder.length() > 0) {
                value = builder.substring(0, builder.length() - 1);
            }
            else {
                value = builder.toString();
            }
            myPropertyEditor.setAsText(value);
        }

        private Validate getValidate(PropertyEnv propertyEnv) {
            Object[] beans = propertyEnv.getBeans();

            if (beans == null) {
                return null;
            }
            if (beans.length == 0) {
                return null;
            }
            Object bean = beans[0];

            if ( !(bean instanceof ValidateNode)) {
                return null;
            }
            return (Validate) ((ValidateNode) bean).getReference();
        }

        private List<Variable> getVariables() {
            List<Variable> list = new ArrayList<Variable>();
            List<BpelReference<VariableDeclaration>> refs = myValidate.getVariables();

            if (refs == null) {
                return list;
            }
            for (BpelReference<VariableDeclaration> ref : refs) {
                VariableDeclaration declaration = ref.get();

                if (declaration instanceof Variable) {
                    list.add((Variable) declaration);
                }
            }
            return list;
        }

        private Validate myValidate;
        private JCheckBox[] myCheckBoxes;
        private List<Variable> myAllVariables;
        private PropertyEditor myPropertyEditor;
    }

    private PropertyEnv myPropertyEnv;
}
