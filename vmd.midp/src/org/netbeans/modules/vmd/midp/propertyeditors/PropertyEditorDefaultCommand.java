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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.DesignDocument;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.api.model.TypeID;
import org.netbeans.modules.vmd.midp.components.MidpDocumentSupport;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.MidpValueSupport;
import org.netbeans.modules.vmd.midp.components.categories.CommandsCategoryCD;
import org.netbeans.modules.vmd.midp.components.commands.CommandCD;
import org.netbeans.modules.vmd.midp.components.displayables.DisplayableCD;
import org.netbeans.modules.vmd.midp.components.items.ItemCD;
import org.netbeans.modules.vmd.midp.components.sources.CommandEventSourceCD;
import org.netbeans.modules.vmd.midp.components.sources.ItemCommandEventSourceCD;
import org.netbeans.modules.vmd.midp.propertyeditors.api.usercode.PropertyEditorElement;
import org.netbeans.modules.vmd.midp.propertyeditors.api.usercode.PropertyEditorUserCode;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;

/**
 *
 * 
 * 
 */
public final class PropertyEditorDefaultCommand extends PropertyEditorUserCode implements PropertyEditorElement,CleanUp {

    private static final String NONE_ITEM = NbBundle.getMessage(PropertyEditorDefaultCommand.class, "LBL_SELECTCOMMAND_NONE"); // NOI18N
    private List<String> tags = new ArrayList<String>();
    private Map<String, DesignComponent> values = new TreeMap<String, DesignComponent>();
    private CustomEditor customEditor;
    private JRadioButton radioButton;
    private TypeID parentTypeID;

    private PropertyEditorDefaultCommand(TypeID parentTypeID) {
        super(NbBundle.getMessage(PropertyEditorDefaultCommand.class, "LBL_DEF_COMMAND_UCLABEL")); // NOI18N
        this.parentTypeID = parentTypeID;
    }

    public static PropertyEditorDefaultCommand createInstance() {
        return new PropertyEditorDefaultCommand(null);
    }

    /**
     * Crates instance of PropertyEditorDefaultCommand NOT editable for given parent component TypeID.
     * @param parentTypeID parent components typeID
     */
    public static PropertyEditorDefaultCommand createInstance(TypeID parentTypeID) {
        return new PropertyEditorDefaultCommand(parentTypeID);
    }

    public void clean(DesignComponent component) {
        super.cleanUp(component);
        tags = null;
        values = null;
        if (customEditor != null) {
            customEditor.cleanUp();
            customEditor = null;
        }
        radioButton = null;
        parentTypeID = null;
    }
    
    private void initComponents() {
        radioButton = new JRadioButton();
        Mnemonics.setLocalizedText(radioButton, NbBundle.getMessage(PropertyEditorDefaultCommand.class, "LBL_DEF_COMMAND_STR")); // NOI18N
        
        radioButton.getAccessibleContext().setAccessibleName( radioButton.getText());
        radioButton.getAccessibleContext().setAccessibleDescription( radioButton.getText());
        
        customEditor = new CustomEditor();
        radioButton.addActionListener(customEditor);
    }

    public JComponent getCustomEditorComponent() {
        return customEditor;
    }

    public JRadioButton getRadioButton() {
        return radioButton;
    }

    public boolean isInitiallySelected() {
        return true;
    }

    public boolean isVerticallyResizable() {
        return false;
    }

    @Override
    public void setAsText(String text) {
        saveValue(text);
    }

    @Override
    public String getAsText() {
        if (isCurrentValueAUserCodeType()) {
            return USER_CODE_TEXT;
        } else if (isCurrentValueANull()) {
            return NONE_ITEM;
        }
        PropertyValue value = (PropertyValue) super.getValue();
        return getDecodeValue(value);
    }

    @Override
    public Boolean canEditAsText() {
        return null;
    }

    public void setTextForPropertyValue(String text) {
        saveValue(text);
    }

    public String getTextForPropertyValue() {
        return null;
    }

    public void updateState(PropertyValue value) {
        customEditor.updateModel();
        if (isCurrentValueANull() || value == null) {
            customEditor.setValue(null);
        } else {
            customEditor.setValue(value);
        }
        radioButton.setSelected(!isCurrentValueAUserCodeType());
    }

    private void saveValue(String text) {
        if (text.length() > 0) {
            if (NONE_ITEM.equals(text)) {
                super.setValue(NULL_VALUE);
            } else {
                final DesignComponent command = values.get(text);
                if (command != null) { // user code
                    final DesignComponent[] itemCommandSource = new DesignComponent[1];
                    itemCommandSource[0] = getItemCommandEvenSource(text);
                    if (itemCommandSource[0] == null) {
                        if (component != null && component.get() != null) {
                            component.get().getDocument().getTransactionManager().writeAccess(new Runnable() {

                                public void run() {
                                    itemCommandSource[0] = MidpDocumentSupport.attachCommandToItem(component.get(), command);
                                }
                            });
                        }
                    }
                    super.setValue(PropertyValue.createComponentReference(itemCommandSource[0]));
                }
            }
        }
    }

    @Override
    public void customEditorOKButtonPressed() {
        super.customEditorOKButtonPressed();
        if (radioButton.isSelected()) {
            saveValue(customEditor.getText());
        }
    }

    @Override
    public String[] getTags() {
        tags.clear();
        if (isCurrentValueAUserCodeType()) {
            tags.add(PropertyEditorUserCode.USER_CODE_TEXT);
        } else {
            tags.add(NONE_ITEM);
            values.clear();
            values.put(NONE_ITEM, null);

            if (component != null && component.get() != null) {
                final DesignComponent itemComponent = component.get();
                itemComponent.getDocument().getTransactionManager().readAccess(new Runnable() {

                    public void run() {
                        DesignComponent parentComponent = itemComponent.getParentComponent();
                        if (parentComponent != null) {
                            List<PropertyValue> formCmdESValues = parentComponent.readProperty(DisplayableCD.PROP_COMMANDS).getArray();
                            List<DesignComponent> formCommands = new ArrayList<DesignComponent>(formCmdESValues.size());

                            for (PropertyValue esValue : formCmdESValues) {
                                DesignComponent command = esValue.getComponent().readProperty(CommandEventSourceCD.PROP_COMMAND).getComponent();
                                if (command != null) {
                                    PropertyValue ordinaryValue = command.readProperty(CommandCD.PROP_ORDINARY);
                                    if (MidpTypes.getBoolean(ordinaryValue)) {
                                        formCommands.add(command);
                                    }
                                }
                            }

                            Collection<DesignComponent> components = MidpDocumentSupport.getCategoryComponent(itemComponent.getDocument(), CommandsCategoryCD.TYPEID).getComponents();
                            Collection<DesignComponent> commands = new ArrayList<DesignComponent>(components.size());
                            for (DesignComponent command : components) {
                                PropertyValue ordinaryValue = command.readProperty(CommandCD.PROP_ORDINARY);
                                if (MidpTypes.getBoolean(ordinaryValue)) {
                                    commands.add(command);
                                }
                            }
                            commands.removeAll(formCommands);

                            for (DesignComponent command : commands) {
                                String displayName = getComponentDisplayName(command);
                                tags.add(displayName);
                                values.put(displayName, command);
                            }
                        }
                    }
                });
            }
        }
        return tags.toArray(new String[tags.size()]);
    }

    private String getComponentDisplayName(DesignComponent component) {
        return MidpValueSupport.getHumanReadableString(component);
    }

    private String getDecodeValue(final PropertyValue value) {
        final String[] decodeValue = new String[1];
        if (component != null && component.get() != null) {
            final DesignDocument document = component.get().getDocument();
            document.getTransactionManager().readAccess(new Runnable() {

                public void run() {
                    decodeValue[0] = getComponentDisplayName(value.getComponent());
                }
            });
        }
        return decodeValue[0];
    }

    private DesignComponent getItemCommandEvenSource(final String name) {
        final DesignComponent[] itemCommandEvenSource = new DesignComponent[1];
        if (component != null && component.get() != null) {
            final DesignComponent itemComponent = component.get();
            itemComponent.getDocument().getTransactionManager().readAccess(new Runnable() {

                public void run() {
                    DesignComponent command = values.get(name);
                    List<PropertyValue> itemESValues = itemComponent.readProperty(ItemCD.PROP_COMMANDS).getArray();
                    for (PropertyValue esValue : itemESValues) {
                        DesignComponent existingES = esValue.getComponent();
                        if (existingES.readProperty(ItemCommandEventSourceCD.PROP_COMMAND).getComponent().equals(command)) {
                            itemCommandEvenSource[0] = existingES;
                            break;
                        }
                    }
                }
            });
        }
        return itemCommandEvenSource[0];
    }

    @Override
    public boolean canWrite() {
        if (!isWriteableByParentType()) {
            return false;
        }

        return super.canWrite();
    }

    @Override
    public boolean supportsCustomEditor() {
        if (!isWriteableByParentType()) {
            return false;
        }

        return super.supportsCustomEditor();
    }

    private boolean isWriteableByParentType() {
        if (component == null || component.get() == null) {
            return false;
        }

        if (parentTypeID != null) {
            final DesignComponent _component = component.get();
            final DesignComponent[] parent = new DesignComponent[1];
            _component.getDocument().getTransactionManager().readAccess(new Runnable() {

                public void run() {
                    parent[0] = _component.getParentComponent();
                }
            });

            if (parent[0] != null && parentTypeID.equals(parent[0].getType())) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Component getCustomEditor() {
        if (customEditor == null) {
            initComponents();
            initElements(Collections.<PropertyEditorElement>singleton(this));
        }
        return super.getCustomEditor();
    }



    private class CustomEditor extends JPanel implements ActionListener {

        private JComboBox combobox;

        void cleanUp() {
            if (combobox != null) {
                combobox.removeActionListener(this);
                combobox = null;
            }
            this.removeAll();
        }

        public CustomEditor() {
            initComponents();
        }

        private void initComponents() {
            setLayout(new BorderLayout());
            combobox = new JComboBox();
            
            combobox.getAccessibleContext().setAccessibleName( 
                    NbBundle.getMessage(PropertyEditorDefaultCommand.class, 
                            "ACSN_DefaultCommandChooser"));
            combobox.getAccessibleContext().setAccessibleDescription( 
                    NbBundle.getMessage(PropertyEditorDefaultCommand.class, 
                            "ACSD_DefaultCommandChooser"));
            
            combobox.setModel(new DefaultComboBoxModel());
            combobox.addActionListener(this);
            add(combobox, BorderLayout.CENTER);
        }

        public void setValue(final PropertyValue value) {
            if (value == null) {
                combobox.setSelectedItem(NONE_ITEM);
                return;
            }

            final PropertyValue[] cmdValue = new PropertyValue[1];
            if (component != null && component.get() != null) {
                DesignDocument document = component.get().getDocument();
                document.getTransactionManager().readAccess(new Runnable() {

                    public void run() {
                        cmdValue[0] = value.getComponent().readProperty(ItemCommandEventSourceCD.PROP_COMMAND);
                    }
                });
            }
            if (cmdValue[0] == null) {
                return;
            }

            DesignComponent command = cmdValue[0].getComponent();
            for (String key : values.keySet()) {
                DesignComponent tmpCommand = values.get(key);
                if (tmpCommand != null && tmpCommand.equals(command)) {
                    combobox.setSelectedItem(key);
                    break;
                }
            }
        }

        public String getText() {
            return (String) combobox.getSelectedItem();
        }

        public void updateModel() {
            DefaultComboBoxModel model = (DefaultComboBoxModel) combobox.getModel();
            model.removeAllElements();
            for (String tag : tags) {
                model.addElement(tag);
            }
        }

        public void actionPerformed(ActionEvent evt) {
            radioButton.setSelected(true);
        }
    }
}
