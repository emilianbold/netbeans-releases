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
package org.netbeans.modules.vmd.midp.propertyeditors.api.resource;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.util.Collections;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.event.FocusListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.midp.actions.GoToSourceSupport;
import org.netbeans.modules.vmd.midp.propertyeditors.CleanUp;
import org.netbeans.modules.vmd.midp.propertyeditors.api.resource.element.PropertyEditorResourceElement;
import org.netbeans.modules.vmd.midp.propertyeditors.api.resource.element.PropertyEditorResourceElement.DesignComponentWrapper;
import org.netbeans.modules.vmd.midp.propertyeditors.api.resource.element.PropertyEditorResourceElementEvent;
import org.netbeans.modules.vmd.midp.propertyeditors.api.resource.element.PropertyEditorResourceElementListener;
import org.openide.awt.Mnemonics;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 *
 * 
 */

class ResourceEditorPanel extends JPanel implements PropertyEditorResourceElementListener, ListSelectionListener, ActionListener, UserCodeAwareness, CleanUp {

    private static final String ACTION_ADD_RESOURCE = "addResource"; // NOI18N
    private static final String ACTION_REMOVE_RESOURCE = "removeResource"; // NOI18N
    private static final String COMPONENT_CARD = "componentCard"; // NOI18N
    private static final String USER_CODE_CARD = "userCodeCard"; // NOI18N
    private static long componentIDCounter = -10000L;
    private PropertyEditorResourceElement element;
    private Map<String, DesignComponentWrapper> wrappersMap;
    private Set<String> changedComponents;
    private String noneComponentAsText;
    private JList componentsList;
    private JRadioButton radioButton;
    private Icon icon;
    private JPanel ucPanel;
    private CardLayout ucCardLayout;

    

    ResourceEditorPanel(PropertyEditorResourceElement element, String noneComponentAsText, JRadioButton radioButton) {
        if (noneComponentAsText == null || noneComponentAsText.length() == 0) {
            throw new IllegalArgumentException("Incorrect value of noneComponentAsText: " + noneComponentAsText); // NOI18N
        }

        if (element == null) {
            throw new IllegalArgumentException("PropertyEditorResourceElement shouls not be null"); // NOI18N
        }

        if (radioButton == null) {
            throw new IllegalArgumentException("Null radioButton value"); // NOI18N
        }
        this.element = element;
        this.noneComponentAsText = noneComponentAsText;
        this.radioButton = radioButton;
        changedComponents = new HashSet<String>();
        initComponents(element.getJComponent());

    }

    public void clean(DesignComponent component) {
        if (wrappersMap != null) {
            wrappersMap.clear();
        }
        element = null;
        wrappersMap = null;
        changedComponents = null;
        componentsList = null;
        radioButton = null;
        icon = null;
        ucCardLayout = null;
        ucPanel = null;
        this.removeAll();
    }

    private void initComponents(JComponent component) {
        setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();

        componentsList = new JList(new DefaultListModel());
        componentsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        componentsList.setCellRenderer(new ComponentsListRenderer());

        componentsList.getAccessibleContext().setAccessibleName(
                NbBundle.getMessage(ResourceEditorPanel.class, "ASCN_ResourcesList"));
        componentsList.getAccessibleContext().setAccessibleDescription(
                NbBundle.getMessage(ResourceEditorPanel.class, "ASCD_ResourcesList"));
        //componentsList.addListSelectionListener(this);
//        componentsList.setPreferredSize(new Dimension(120, 140));
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setViewportView(componentsList);
        constraints.insets = new Insets(0, 0, 0, 12);
        constraints.anchor = GridBagConstraints.NORTHWEST;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weightx = 0.0;
        constraints.weighty = 1.0;
        constraints.gridwidth = 2;
        constraints.fill = GridBagConstraints.BOTH;
        add(scrollPane, constraints);

        JButton addButton = new JButton();
        Mnemonics.setLocalizedText(addButton,
                NbBundle.getMessage(ResourceEditorPanel.class, "LBL_ADD_COMPONENT")); // NOI18N
        addButton.getAccessibleContext().setAccessibleName(
                NbBundle.getMessage(ResourceEditorPanel.class, "ACSN_ADD_COMPONENT")); // NOI18N
        addButton.getAccessibleContext().setAccessibleDescription(
                NbBundle.getMessage(ResourceEditorPanel.class, "ACSD_ADD_COMPONENT")); // NOI18N
        addButton.setActionCommand(ACTION_ADD_RESOURCE);
        addButton.addActionListener(this);
        constraints.insets = new Insets(6, 0, 0, 12);
        constraints.anchor = GridBagConstraints.NORTHWEST;
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.weightx = 0.0;
        constraints.weighty = 0.0;
        constraints.gridwidth = 1;
        constraints.fill = GridBagConstraints.NONE;
        add(addButton, constraints);

        JButton removeButton = new JButton();
        Mnemonics.setLocalizedText(removeButton,
                NbBundle.getMessage(ResourceEditorPanel.class, "LBL_REMOVE_COMPONENT")); // NOI18N
        removeButton.getAccessibleContext().setAccessibleName(
                NbBundle.getMessage(ResourceEditorPanel.class, "ACSN_REMOVE_COMPONENT")); // NOI18N
        removeButton.getAccessibleContext().setAccessibleDescription(
                NbBundle.getMessage(ResourceEditorPanel.class, "ACSD_REMOVE_COMPONENT")); // NOI18N
        removeButton.setActionCommand(ACTION_REMOVE_RESOURCE);
        removeButton.addActionListener(this);
        constraints.insets = new Insets(6, 0, 0, 12);
        constraints.anchor = GridBagConstraints.NORTHWEST;
        constraints.gridx = 1;
        constraints.gridy = 1;
        constraints.weightx = 0.0;
        constraints.weighty = 0.0;
        constraints.fill = GridBagConstraints.NONE;
        add(removeButton, constraints);

        constraints.insets = new Insets(0, 0, 0, 0);
        constraints.anchor = GridBagConstraints.NORTHWEST;
        constraints.gridx = 2;
        constraints.gridy = 0;
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        constraints.fill = GridBagConstraints.BOTH;
        add(createUCAwarePanel(component), constraints);

        icon = ImageUtilities.loadImageIcon(element.getIconPath(), false);
        componentsList.addFocusListener(new FocusListener() {

            public void focusGained(FocusEvent e) {
                radioButton.setSelected(true);
            }

            public void focusLost(FocusEvent e) {
            }
        });

        this.addAncestorListener(new AncestorListener() {

            public void ancestorAdded(AncestorEvent event) {
                componentsList.removeListSelectionListener(ResourceEditorPanel.this);
                componentsList.addListSelectionListener(ResourceEditorPanel.this);
            }

            public void ancestorRemoved(AncestorEvent event) {
                componentsList.removeListSelectionListener(ResourceEditorPanel.this);
            }

            public void ancestorMoved(AncestorEvent event) {
            }
        });
    }

    private JComponent createUCAwarePanel(JComponent component) {
        JPanel userCodeCardPanel = new UserCodeCardPanel(this);
        ucCardLayout = new CardLayout();
        ucPanel = new JPanel(ucCardLayout);
        ucPanel.add(component, COMPONENT_CARD);
        ucPanel.add(userCodeCardPanel, USER_CODE_CARD);
        return ucPanel;
    }

    private void setUserCode(boolean isUserCode) {
        ucCardLayout.show(ucPanel, isUserCode ? USER_CODE_CARD : COMPONENT_CARD);
    }

    private boolean isUserCodeInside(DesignComponentWrapper wrapper) {
        if (wrapper == null) {
            return false;
        }

        final DesignComponent component = wrapper.getComponent();
        if (component == null) {
            return false;
        }

        final List<String> propertyNames = element.getPropertyValueNames();
        final boolean[] isUC = new boolean[1];
        component.getDocument().getTransactionManager().readAccess(new Runnable() {

            public void run() {
                for (String propertyName : propertyNames) {
                    PropertyValue pv = component.readProperty(propertyName);
                    if (PropertyValue.Kind.USERCODE.equals(pv.getKind())) {
                        isUC[0] = true;
                        break;
                    }
                }
            }
        });
        return isUC[0];
    }

    void update(Map<String, DesignComponent> componentsMap, String selectedComponentName) {
        this.wrappersMap = new HashMap<String, DesignComponentWrapper>(componentsMap.size());
        for (String key : componentsMap.keySet()) {
            DesignComponent _component = componentsMap.get(key);
            wrappersMap.put(key, new DesignComponentWrapper(_component));
        }
        changedComponents.clear();
        sortAndSelect(selectedComponentName);
    }

    // issue# 112658
    private void sortAndSelect(String selectedComponentName) {
        DefaultListModel listModel = (DefaultListModel) componentsList.getModel();
        listModel.removeAllElements();
        listModel.addElement(noneComponentAsText);

        SortedSet<String> set = new TreeSet<String>();
        for (String componentName : wrappersMap.keySet()) {
            set.add(componentName);
        }
        for (String str : set) {
            listModel.addElement(str);
        }

        selectComponent(selectedComponentName);
    }

    private void selectComponent(String selectedComponentName) {
        // select value in the components list
        componentsList.setSelectedValue(selectedComponentName, false);

        // select according values in the element component
        if (noneComponentAsText.equals(selectedComponentName)) {
            element.setDesignComponentWrapper(null);
        } else {
            element.setDesignComponentWrapper(wrappersMap.get(selectedComponentName));
        }
    }

    public void valueChanged(ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) {
            return;
        }
        Object selectedName = getSelectedComponentName();
        if (selectedName == null || noneComponentAsText.equals(selectedName)) {
            element.setDesignComponentWrapper(null);
            setUserCode(false);
        } else {
            DesignComponentWrapper wrapper = wrappersMap.get(selectedName);
            element.setDesignComponentWrapper(wrapper);
            setUserCode(isUserCodeInside(wrapper));
        }
        element.listSelectionHappened();
    }

    public void elementChanged(PropertyEditorResourceElementEvent event) {
        long componentID = event.getComponentID();
        String propertyName = event.getPropertyName();
        PropertyValue propertyValue = event.getPropertyValue();

        DesignComponentWrapper wrapper = null;
        for (String key : wrappersMap.keySet()) {
            wrapper = wrappersMap.get(key);
            if (wrapper.getComponentID() == componentID) {
                wrapper.setChangeRecord(propertyName, propertyValue);
                changedComponents.add(key);

                // UI stuff
                // need to refresh list cell renderer
                componentsList.invalidate();
                componentsList.validate();
                componentsList.repaint();
                break;
            }
        }
    }

    public void actionPerformed(ActionEvent e) {
        Object selectedName = getSelectedComponentName();
        DefaultListModel model = (DefaultListModel) componentsList.getModel();
        if (ACTION_ADD_RESOURCE.equals(e.getActionCommand())) {
            String name = getNewComponentName();
            DesignComponentWrapper virtualComponent = new DesignComponentWrapper(componentIDCounter++, element.getTypeID());
            wrappersMap.put(name, virtualComponent);
            model.addElement(name);
            sortAndSelect(name);
            changedComponents.add(name);
        } else if (ACTION_REMOVE_RESOURCE.equals(e.getActionCommand())) {
            if (selectedName != null && !noneComponentAsText.equals(selectedName)) {
                wrappersMap.get(selectedName).deleteComponent();
                int index = model.indexOf(selectedName);
                model.removeElement(selectedName);
                componentsList.setSelectedIndex(index - 1);
            }
        }
    }

    private String getNewComponentName() {
        String base = element.getResourceNameSuggestion();
        Set<String> existingKeys = wrappersMap.keySet();
        Set<String> toBeDeleted = new HashSet<String>();
        for (String key : wrappersMap.keySet()) {
            if (wrappersMap.get(key).isDeleted()) {
                toBeDeleted.add(key);
            }
        }
        for (String key : toBeDeleted) {
            wrappersMap.remove(key);
        }

        String name;
        for (int i = 1;; i++) {
            name = base + i;
            if (!existingKeys.contains(name)) {
                break;
            }
        }
        return name;
    }

    private Object getSelectedComponentName() {
        return componentsList.getSelectedValue();
    }

    String getTextForPropertyValue() {
        String str = (String) componentsList.getSelectedValue();
        return str != null ? str : ""; // NOI18N
    }

    boolean wasAnyDesignComponentChanged() {
        for (String key : wrappersMap.keySet()) {
            if (wrappersMap.get(key).hasChanges()) {
                return true;
            }
        }
        return false;
    }

    Map<String, DesignComponentWrapper> getWrappersMap() {
        return Collections.unmodifiableMap(wrappersMap);
    }

    // do not update state of editor is shown
    boolean needsUpdate() {
//        return !isShowing();
        return true;
    }

    public void goToSource() {
        DesignComponentWrapper wrapper = wrappersMap.get(getSelectedComponentName());
        if (wrapper != null && wrapper.getComponent() != null) {
            GoToSourceSupport.goToSourceOfComponent(wrapper.getComponent());
        }
    }

    public void resetUserCode() {
        DesignComponentWrapper wrapper = wrappersMap.get(getSelectedComponentName());
        if (wrapper != null && wrapper.getComponent() != null) {
            final DesignComponent component = wrapper.getComponent();
            component.getDocument().getTransactionManager().writeAccess(new Runnable() {

                public void run() {
                    List<String> propertyNames = element.getPropertyValueNames();
                    for (String propertyName : propertyNames) {
                        PropertyValue pv = component.readProperty(propertyName);
                        if (PropertyValue.Kind.USERCODE.equals(pv.getKind())) {
                            component.writeProperty(propertyName, component.getComponentDescriptor().getPropertyDescriptor(propertyName).getDefaultValue());
                        }
                    }
                }
            });
            setUserCode(isUserCodeInside(wrapper));
        }
    }

    private class ComponentsListRenderer extends DefaultListCellRenderer {

        private Font changedFont;
        private Font defaultFont;

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel renderer = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (defaultFont == null) {
                defaultFont = renderer.getFont();
                changedFont = defaultFont.deriveFont(Font.BOLD);
            }

            if (!noneComponentAsText.equals(value) && icon != null) {
                renderer.setIcon(icon);
            }

            renderer.setFont(changedComponents.contains(value) ? changedFont : defaultFont);
            return renderer;
        }
    }
}
