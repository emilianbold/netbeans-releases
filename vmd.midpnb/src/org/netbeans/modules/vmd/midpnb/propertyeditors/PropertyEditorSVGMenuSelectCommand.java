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

package org.netbeans.modules.vmd.midpnb.propertyeditors;

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
import org.netbeans.modules.vmd.api.model.TypeID;
import org.netbeans.modules.vmd.midp.components.MidpDocumentSupport;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.MidpValueSupport;
import org.netbeans.modules.vmd.midp.components.categories.CommandsCategoryCD;
import org.netbeans.modules.vmd.midp.components.commands.CommandCD;
import org.netbeans.modules.vmd.midp.components.displayables.DisplayableCD;
import org.netbeans.modules.vmd.midp.components.sources.CommandEventSourceCD;
import org.netbeans.modules.vmd.midp.propertyeditors.api.usercode.PropertyEditorElement;
import org.netbeans.modules.vmd.midp.propertyeditors.api.usercode.PropertyEditorUserCode;
import org.netbeans.modules.vmd.midpnb.components.commands.SVGMenuSelectCommandCD;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;

/**
 *
 * @author Anton Chechel
 */
public class PropertyEditorSVGMenuSelectCommand extends PropertyEditorUserCode implements PropertyEditorElement {

    private List<String> tags = new ArrayList<String>();
    private Map<String, DesignComponent> values = new TreeMap<String, DesignComponent>();
    private CustomEditor customEditor;
    private JRadioButton radioButton;
    private TypeID typeID;
    private String noneItem;
    private String defaultItem;

    public static PropertyEditorSVGMenuSelectCommand createInstanceMenuSelect() {
        String mnemonic = NbBundle.getMessage(PropertyEditorSVGMenuSelectCommand.class, "LBL_SEL_COMMAND_STR"); // NOI18N
        String noneItem = NbBundle.getMessage(PropertyEditorSVGMenuSelectCommand.class, "LBL_SELECTCOMMAND_NONE"); // NOI18N
        String defaultItem = NbBundle.getMessage(PropertyEditorSVGMenuSelectCommand.class, "LBL_SELECTCOMMAND_DEFAULT"); // NOI18N
        String userCodeLabel = NbBundle.getMessage(PropertyEditorSVGMenuSelectCommand.class, "LBL_SELECTCOMMAND_UCLABEL"); // NOI18N
        return new PropertyEditorSVGMenuSelectCommand(SVGMenuSelectCommandCD.TYPEID, mnemonic, noneItem, defaultItem, userCodeLabel);
    }

    private PropertyEditorSVGMenuSelectCommand(TypeID typeID, String mnemonic, String noneItem, String defaultItem, String userCodeLabel) {
        super(userCodeLabel);
        initComponents();
        this.typeID = typeID;
        this.noneItem = noneItem;
        this.defaultItem = defaultItem;
        Mnemonics.setLocalizedText(radioButton, mnemonic);
        
        radioButton.getAccessibleContext().setAccessibleName( 
                radioButton.getText());
        radioButton.getAccessibleContext().setAccessibleDescription(
                radioButton.getText());

        initElements(Collections.<PropertyEditorElement>singleton(this));
    }

    @Override
    public void cleanUp(DesignComponent component) {
        super.cleanUp(component);
        if (customEditor != null) {
            customEditor.cleanUp();
            customEditor = null;
        }
        radioButton = null;
        typeID = null;
        if (values != null) {
            values.clear();
            values = null;
        }
        tags = null;
    }

    private void initComponents() {
        radioButton = new JRadioButton();
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
            return noneItem;
        }

        PropertyValue value = (PropertyValue) super.getValue();
        return getDecodeValue(value);
    }

    public String getTextForPropertyValue() {
        return null;
    }

    public void setTextForPropertyValue(String text) {
        saveValue(text);
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
            if (noneItem.equals(text)) {
                super.setValue(NULL_VALUE);
            } else {
                super.setValue(PropertyValue.createComponentReference(getCommandEvenSource(text)));
            }
        }
    }

    @Override
    public void customEditorOKButtonPressed() {
        if (radioButton.isSelected()) {
            saveValue(customEditor.getText());
        }
    }

    @Override
    public Boolean canEditAsText() {
        return null;
    }

    @Override
    public String[] getTags() {
        tags.clear();
        if (isCurrentValueAUserCodeType()) {
            tags.add(PropertyEditorUserCode.USER_CODE_TEXT);
        } else {
            tags.add(noneItem);
            values.clear();
            values.put(noneItem, null);

            if (component != null && component.get() != null) {
                final DesignDocument document = component.get().getDocument();
                document.getTransactionManager().readAccess(new Runnable() {

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
                        values.put(defaultItem, getListSelectCommand(document));

                        for (DesignComponent command : commands) {
                            String displayName = getComponentDisplayName(command);
                            tags.add(displayName);
                            values.put(displayName, command);
                        }
                    }
                });
            }
        }
        return tags.toArray(new String[tags.size()]);
    }

    private DesignComponent getListSelectCommand(DesignDocument document) {
        return MidpDocumentSupport.getSingletonCommand(document, typeID);
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
                    DesignComponent valueComponent = value.getComponent();
                    if (valueComponent != null) {
                        PropertyValue pv = valueComponent.readProperty(CommandEventSourceCD.PROP_COMMAND);
                        if (pv != null) {
                            DesignComponent refComponent = pv.getComponent();
                            if (refComponent != null && refComponent.equals(getListSelectCommand(document))) {
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
        }

        return decodeValue[0];
    }

    private DesignComponent getCommandEvenSource(final String name) {
        final DesignComponent[] itemCommandEvenSource = new DesignComponent[1];
        if (component != null && component.get() != null) {
            final DesignComponent listComponent = component.get();
            listComponent.getDocument().getTransactionManager().writeAccess(new Runnable() {

                public void run() {
                    DesignComponent command = values.get(name);
                    List<PropertyValue> listESValues = listComponent.readProperty(DisplayableCD.PROP_COMMANDS).getArray();
                    for (PropertyValue esValue : listESValues) {
                        DesignComponent existingES = esValue.getComponent();
                        if (existingES.readProperty(CommandEventSourceCD.PROP_COMMAND).getComponent().equals(command)) {
                            itemCommandEvenSource[0] = existingES;
                            break;
                        }
                    }

                    if (itemCommandEvenSource[0] == null) {
                        // create new ItemCommandEvenSource
                        itemCommandEvenSource[0] = MidpDocumentSupport.attachCommandToDisplayable(listComponent, command);
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

        void cleanUp() {
            combobox.removeActionListener(this);
            combobox = null;
            this.removeAll();
        }

        private void initComponents() {
            setLayout(new BorderLayout());
            combobox = new JComboBox();
            
            combobox.getAccessibleContext().setAccessibleName( 
                    radioButton.getAccessibleContext().getAccessibleName());
            combobox.getAccessibleContext().setAccessibleDescription(
                    radioButton.getAccessibleContext().getAccessibleDescription());
                    
            
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
            if (component != null && component.get() != null) {
                component.get().getDocument().getTransactionManager().readAccess(new Runnable() {

                    public void run() {
                        cmdValue[0] = value.getComponent().readProperty(CommandEventSourceCD.PROP_COMMAND);
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