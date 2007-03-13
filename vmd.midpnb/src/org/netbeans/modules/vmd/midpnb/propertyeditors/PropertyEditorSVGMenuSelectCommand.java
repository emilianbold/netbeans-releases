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

package org.netbeans.modules.vmd.midpnb.propertyeditors;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
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
import org.netbeans.modules.vmd.midp.components.sources.CommandEventSourceCD;
import org.netbeans.modules.vmd.midp.propertyeditors.usercode.PropertyEditorElement;
import org.netbeans.modules.vmd.midp.propertyeditors.usercode.PropertyEditorUserCode;
import org.netbeans.modules.vmd.midpnb.components.commands.SVGMenuSelectCommandCD;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;

/**
 *
 * @author Anton Chechel
 */
public class PropertyEditorSVGMenuSelectCommand extends PropertyEditorUserCode implements PropertyEditorElement {
    
    private DesignDocument document;
    private DesignComponent list;
    private DesignComponent listSelectCommand;
    
    private final List<String> tags = new ArrayList<String>();
    private final Map<String, DesignComponent> values = new TreeMap<String, DesignComponent>();
    
    private CustomEditor customEditor;
    private JRadioButton radioButton;
    private TypeID typeID;
    
    private String noneItem;
    private String defaultItem;
    
    public static PropertyEditorSVGMenuSelectCommand createInstanceMenuSelect() {
        String mnemonic =  NbBundle.getMessage(PropertyEditorSVGMenuSelectCommand.class, "LBL_SEL_COMMAND_STR");
        String noneItem = NbBundle.getMessage(PropertyEditorSVGMenuSelectCommand.class, "LBL_SELECTCOMMAND_NONE");  // NOI18N
        String defaultItem  = NbBundle.getMessage(PropertyEditorSVGMenuSelectCommand.class, "LBL_SELECTCOMMAND_DEFAULT");  // NOI18N
        
        return new PropertyEditorSVGMenuSelectCommand(SVGMenuSelectCommandCD.TYPEID, mnemonic, noneItem, defaultItem);
    }
    
    public PropertyEditorSVGMenuSelectCommand(TypeID typeID, String mnemonic, String none_item, String defaultItem) {
        super();
        initComponents();
        this.typeID = typeID;
        Mnemonics.setLocalizedText(radioButton, mnemonic);
        this.noneItem = noneItem;
        this.defaultItem = defaultItem;
        Collection<PropertyEditorElement> elements = new ArrayList<PropertyEditorElement>(1);
        elements.add(this);
        initElements(elements);
    }
    
    private void initComponents() {
        radioButton = new JRadioButton();
        
        customEditor = new CustomEditor();
        radioButton.addActionListener(customEditor);
    }
    
    public JComponent getComponent() {
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
    
    public String getAsText() {
        if (isCurrentValueAUserCodeType()) {
            return USER_CODE_TEXT;
        } else if (isCurrentValueANull()) {
            return noneItem;
        }
        
        PropertyValue value = (PropertyValue) super.getValue();
        return getDecodeValue(value);
    }
    
    public String getText() {
        return null;
    }
    
    public void setText(String text) {
        saveValue(text);
    }
    
    public void setPropertyValue(PropertyValue value) {
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
            if (noneItem.equals(text)) {
                super.setValue(NULL_VALUE);
            } else {
                super.setValue(PropertyValue.createComponentReference(getCommandEvenSource(text)));
            }
        }
    }
    
    public void customEditorOKButtonPressed() {
        if (radioButton.isSelected()) {
            saveValue(customEditor.getText());
        }
    }
    
    public String[] getTags() {
        if (isCurrentValueAUserCodeType()) {
            return null;
        }
        
        tags.clear();
        tags.add(noneItem);
        values.clear();
        values.put(noneItem, null);
        
        document.getTransactionManager().writeAccess( new Runnable() {
            public void run() {
                Collection<DesignComponent> components = MidpDocumentSupport.getCategoryComponent(document, CommandsCategoryCD.TYPEID).getComponents();
                Collection<DesignComponent> commands = new ArrayList<DesignComponent>(components.size());
                for (DesignComponent command : components) {
                    PropertyValue ordinaryValue = command.readProperty(CommandCD.PROP_ORDINARY);
                    if (MidpTypes.getBoolean(ordinaryValue)) {
                        commands.add(command);
                    }
                }
                
                tags.add(defaultItem);
                values.put(defaultItem, getListSelectCommand());
                
                for (DesignComponent command : commands) {
                    String displayName = getComponentDisplayName(command);
                    tags.add(displayName);
                    values.put(displayName, command);
                }
            }
        });
        
        return tags.toArray(new String[tags.size()]);
    }
    
    private DesignComponent getListSelectCommand() {
        if (listSelectCommand == null) {
            listSelectCommand = MidpDocumentSupport.getSingletonCommand(document, typeID);
        }
        return listSelectCommand;
    }
    
    public void init(DesignComponent component) {
        super.init(component);
        list = component;
        document = component.getDocument();
    }
    
    private String getComponentDisplayName(DesignComponent component) {
        return MidpValueSupport.getHumanReadableString(component);
    }
    
    private String getDecodeValue(final PropertyValue value){
        final String[] decodeValue = new String[1];
        document.getTransactionManager().readAccess(new Runnable() {
            public void run() {
                DesignComponent valueComponent = value.getComponent();
                if (valueComponent != null) {
                    PropertyValue pv = valueComponent.readProperty(CommandEventSourceCD.PROP_COMMAND);
                    if (pv != null) {
                        DesignComponent refComponent = pv.getComponent();
                        if (refComponent != null && refComponent.equals(getListSelectCommand())) {
                            decodeValue[0] = defaultItem;
                        } else {
                            decodeValue[0] = getComponentDisplayName(valueComponent);
                        }
                    } else {
                        decodeValue[0] = noneItem;
                    }
                } else {
                    decodeValue[0] = noneItem;
                }
            }
        });
        
        return decodeValue[0];
    }
    
    private DesignComponent getCommandEvenSource(final String name) {
        final DesignComponent[] itemCommandEvenSource = new DesignComponent[1];
        document.getTransactionManager().writeAccess( new Runnable() {
            public void run() {
                DesignComponent command = values.get(name);
                List<PropertyValue> listESValues = list.readProperty(DisplayableCD.PROP_COMMANDS).getArray();
                for (PropertyValue esValue : listESValues) {
                    DesignComponent existingES = esValue.getComponent();
                    if (existingES.readProperty(CommandEventSourceCD.PROP_COMMAND).getComponent().equals(command)) {
                        itemCommandEvenSource[0] = existingES;
                        break;
                    }
                }
                
                if (itemCommandEvenSource[0] == null) { // create new ItemCommandEvenSource
                    itemCommandEvenSource[0] = MidpDocumentSupport.attachCommandToDisplayable(list, command);
                }
            }
        });
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
                combobox.setSelectedItem(noneItem);
                return;
            }
            
            final PropertyValue[] cmdValue = new PropertyValue[1];
            document.getTransactionManager().readAccess(new Runnable() {
                public void run() {
                    cmdValue[0] = value.getComponent().readProperty(CommandEventSourceCD.PROP_COMMAND);
                }
            });
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
