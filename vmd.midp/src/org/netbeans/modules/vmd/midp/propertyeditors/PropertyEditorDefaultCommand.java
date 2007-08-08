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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.vmd.midp.propertyeditors;

import java.awt.BorderLayout;
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
import org.netbeans.modules.vmd.midp.components.MidpDocumentSupport;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.MidpValueSupport;
import org.netbeans.modules.vmd.midp.components.categories.CommandsCategoryCD;
import org.netbeans.modules.vmd.midp.components.commands.CommandCD;
import org.netbeans.modules.vmd.midp.components.displayables.DisplayableCD;
import org.netbeans.modules.vmd.midp.components.items.ItemCD;
import org.netbeans.modules.vmd.midp.components.sources.CommandEventSourceCD;
import org.netbeans.modules.vmd.midp.components.sources.ItemCommandEventSourceCD;
import org.netbeans.modules.vmd.midp.propertyeditors.usercode.PropertyEditorElement;
import org.netbeans.modules.vmd.midp.propertyeditors.usercode.PropertyEditorUserCode;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;

/**
 *
 * @author Karol Harezlak
 * @author Anton Chechel
 */
public final class PropertyEditorDefaultCommand extends PropertyEditorUserCode implements PropertyEditorElement {

    private static final String NONE_ITEM = NbBundle.getMessage(PropertyEditorDefaultCommand.class, "LBL_SELECTCOMMAND_NONE"); // NOI18N
    private final List<String> tags = new ArrayList<String>();
    private final Map<String, DesignComponent> values = new TreeMap<String, DesignComponent>();

    private CustomEditor customEditor;
    private JRadioButton radioButton;

    private PropertyEditorDefaultCommand() {
        super(NbBundle.getMessage(PropertyEditorDefaultCommand.class, "LBL_DEF_COMMAND_UCLABEL")); // NOI18N
        initComponents();

        initElements(Collections.<PropertyEditorElement>singleton(this));
    }

    public static PropertyEditorDefaultCommand createInstance() {
        return new PropertyEditorDefaultCommand();
    }

    private void initComponents() {
        radioButton = new JRadioButton();
        Mnemonics.setLocalizedText(radioButton, NbBundle.getMessage(PropertyEditorDefaultCommand.class, "LBL_DEF_COMMAND_STR")); // NOI18N
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
                DesignComponent itemCommandSource = getItemCommandEvenSource(text);
                if (itemCommandSource == null) {
                    DesignComponent command = values.get(text);
                    if (component != null && component.get() != null) {
                        itemCommandSource = MidpDocumentSupport.attachCommandToItem(component.get(), command);
                    }
                }
                super.setValue(PropertyValue.createComponentReference(itemCommandSource));
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
        if (isCurrentValueAUserCodeType()) {
            return null;
        }

        tags.clear();
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

    private class CustomEditor extends JPanel implements ActionListener {

        private JComboBox combobox;

        public CustomEditor() {
            initComponents();
        }

        private void initComponents() {
            setLayout(new BorderLayout());
            combobox = new JComboBox();
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
