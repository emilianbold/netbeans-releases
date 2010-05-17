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
package com.sun.jsfcl.std.property;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.util.IdentityHashMap;
import java.util.Map;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import com.sun.jsfcl.std.reference.ReferenceDataItem;
import com.sun.rave.designtime.DesignProperty;

/**
 * @author eric
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ChooseManyReferenceDataPanel extends ChooseOneReferenceDataPanel {

    protected static final String DESELECT_ACTION = "deselect"; //NOI18N
    protected static final String DESELECT_ALL_ACTION = "deselect-all"; //NOI18N
    protected static final String DOWN_ACTION = "down"; //NOI18N
    protected static final String SELECT_ACTION = "select"; //NOI18N
    protected static final String UP_ACTION = "up"; //NOI18N

    protected JButton downJButton;
    protected JButton deselectJButton;
    protected JButton deselectAllJButton;
    protected JButton selectJButton;
    protected JList selectedJList;
    protected DefaultListModel selectedJListModel;
    protected JButton upJButton;

    /**
     * @param propertyEditor
     * @param liveProperty
     */
    public ChooseManyReferenceDataPanel(ChooseManyReferenceDataPropertyEditor propertyEditor,
        DesignProperty liveProperty) {

        super(propertyEditor, liveProperty);
    }

    public void actionPerformed(ActionEvent event) {

        super.actionPerformed(event);
        if (DESELECT_ACTION.equals(event.getActionCommand())) {
            handleDeselectAction(event);
            return;
        }
        if (DESELECT_ALL_ACTION.equals(event.getActionCommand())) {
            handleDeselectAllAction(event);
            return;
        }
        if (SELECT_ACTION.equals(event.getActionCommand())) {
            handleSelectAction(event);
            return;
        }
        if (DOWN_ACTION.equals(event.getActionCommand())) {
            handleDownAction(event);
            return;
        }
        if (UP_ACTION.equals(event.getActionCommand())) {
            handleUpAction(event);
            return;
        }
    }

    protected void adjustLeftColumnWidthIfNecessary(ReferenceDataItem item) {

        ReferenceDataTwoColumnListCellRenderer renderer = (ReferenceDataTwoColumnListCellRenderer)
            selectedJList.getCellRenderer();
        renderer.getListCellRendererComponent(choicesJList, item, -1, false, false);
        renderer.adjustLeftColumnWidthIfNecessary();
    }

    protected ChooseManyReferenceDataPropertyEditor getChooseManyReferenceDataPropertyEditor() {

        return (ChooseManyReferenceDataPropertyEditor)getPropertyEditor();
    }

    protected int getListSelectionStyle() {

        return ListSelectionModel.MULTIPLE_INTERVAL_SELECTION;
    }

    public Object getPropertyValue() {
        ReferenceDataItem[] selectedItems;

        selectedItems = getSelectedItems();
        if (selectedItems == null || selectedItems.length == 0) {
            return null;
        } else {
            String string = getChooseManyReferenceDataPropertyEditor().getStringForManyItems(
                selectedItems);
            return string;
        }
    }

    protected ReferenceDataItem[] getSelectedItems() {
        ReferenceDataItem[] result;

        result = new ReferenceDataItem[selectedJListModel.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = (ReferenceDataItem)selectedJListModel.get(i);
        }
        return result;
    }

    protected Map getSelectedItemsMap() {
        IdentityHashMap map;

        map = new IdentityHashMap();
        ReferenceDataItem items[] = getSelectedItems();
        Object dummy = new Object();
        for (int i = 0; i < items.length; i++) {
            ReferenceDataItem item = items[i];
            map.put(item, dummy);
        }
        return map;
    }

    protected String getTopLabel() {

        return getCompositeReferenceData().getChooseManyTitle();
    }

    protected int getValueLabelGridWidth() {

        return 6;
    }

    protected void grabCurrentValueFromPropertyEditor() {

    }

    protected void handleDeselectAction(ActionEvent event) {
        Object values[];

        values = selectedJList.getSelectedValues();
        for (int i = 0; i < values.length; i++) {
            selectedJListModel.removeElement(values[i]);
        }
        updateButtonsState();
    }

    protected void handleDeselectAllAction(ActionEvent event) {

        selectedJListModel.clear();
        updateButtonsState();
    }

    protected void handleDownAction(ActionEvent event) {
        int[] indices;
        int index, maxIndex;
        Object swap;

        indices = selectedJList.getSelectedIndices();
        maxIndex = selectedJListModel.size() - 1;
        for (int i = 0; i < indices.length; i++) {
            index = indices[i];
            if (index < maxIndex) {
                swap = selectedJListModel.elementAt(index + 1);
                selectedJListModel.set(index + 1, selectedJListModel.elementAt(index));
                selectedJListModel.set(index, swap);
                // so we can select the freshly moved up items
                indices[i]++;
            }
        }
        selectedJList.setSelectedIndices(indices);
    }

    protected void handleSelectAction(ActionEvent event) {

        Object values[] = choicesJList.getSelectedValues();
        int[] toSelect = new int[values.length];
        int lastToSelect = 0;
        for (int i = 0; i < values.length; i++) {
            ReferenceDataItem item = (ReferenceDataItem)values[i];
            if (getChooseManyReferenceDataPropertyEditor().getAllowDuplicates() ||
                !getSelectedItemsMap().containsKey(item)) {
                toSelect[lastToSelect] = selectedJListModel.size();
                lastToSelect++;
                selectedJListModel.addElement(item);
                adjustLeftColumnWidthIfNecessary(item);
            }
        }
        if (lastToSelect != values.length) {
            System.arraycopy(toSelect, 0, toSelect = new int[lastToSelect], 0, lastToSelect);
        }
        updateButtonsState();
        selectedJList.setSelectedIndices(toSelect);
        if (toSelect.length > 0) {
            selectedJList.ensureIndexIsVisible(toSelect[0]);
        }
    }

    protected void handleSelectedJListSelectionChanged(ListSelectionEvent event) {

        if (event.getValueIsAdjusting()) {
            return;
        }
        if (selectedJList != null) {
            updateButtonsState();
        }
    }

    protected void handleUpAction(ActionEvent event) {
        int[] indices;
        int index;
        Object swap;

        indices = selectedJList.getSelectedIndices();
        for (int i = 0; i < indices.length; i++) {
            index = indices[i];
            if (index <= 0) {
                break;
            } else {
                swap = selectedJListModel.elementAt(index - 1);
                selectedJListModel.set(index - 1, selectedJListModel.elementAt(index));
                selectedJListModel.set(index, swap);
                // so we can select the freshly moved up items
                indices[i]--;
            }
        }
        selectedJList.setSelectedIndices(indices);
    }

    protected boolean includeInChoicesJList(ReferenceDataItem item) {

        if (super.includeInChoicesJList(item)) {
            return item.getName().length() > 0;
        }
        return false;
    }

    protected void initializeComponents() {
        GridBagConstraints gridBagConstraints;
        JScrollPane selectedJListScrollPane;
        JPanel selectButtonsPanel, moveButtonsPanel;

        super.initializeComponents();

        // add select, deselect, deselectSelected buttons
        selectButtonsPanel = new JPanel();
        selectButtonsPanel.setLayout(new BoxLayout(selectButtonsPanel, BoxLayout.Y_AXIS));

        selectJButton = new JButton(">"); //NOI18N
        // some ridiculous size so it does not get in the way
        //selectJButton.setMaximumSize(new Dimension(200, 200));
        selectJButton.setActionCommand(SELECT_ACTION);
        selectJButton.addActionListener(this);
        selectJButton.setEnabled(false);
        selectButtonsPanel.add(selectJButton, null);
        selectButtonsPanel.add(Box.createRigidArea(new Dimension(0, 5)));

        deselectJButton = new JButton("<"); //NOI18N
        //deselectJButton.setMaximumSize(new Dimension(200, 200));
        deselectJButton.setActionCommand(DESELECT_ACTION);
        deselectJButton.addActionListener(this);
        deselectJButton.setEnabled(false);
        selectButtonsPanel.add(deselectJButton, null);
        selectButtonsPanel.add(Box.createRigidArea(new Dimension(0, 5)));

        deselectAllJButton = new JButton("<<"); //NOI18N
       // deselectAllJButton.setMaximumSize(new Dimension(200, 200));
        deselectAllJButton.setActionCommand(DESELECT_ALL_ACTION);
        deselectAllJButton.addActionListener(this);
        selectButtonsPanel.add(deselectAllJButton, null);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.anchor = GridBagConstraints.PAGE_START;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 0);
        add(selectButtonsPanel, gridBagConstraints);

        // add Avaliable label
        JLabel label = new javax.swing.JLabel();
        label.setText(BundleHolder.bundle.getMessage("Available")); //NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 10);
        add(label, gridBagConstraints);

        // add Selected label
        label = new javax.swing.JLabel();
        label.setText(BundleHolder.bundle.getMessage("sel")); //NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 10);
        add(label, gridBagConstraints);

        // add the selected list control
        Component component;
        component = initializeSelectedListComponent();

        // wrap the list control in scrolling pane
        selectedJListScrollPane = new JScrollPane(component);
        selectedJListScrollPane.setPreferredSize(new Dimension(200, 200));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 10);
        add(selectedJListScrollPane, gridBagConstraints);

        if (getCompositeReferenceData().canOrderItems()){
            // add move Up, Down buttons
            moveButtonsPanel = new JPanel();
            moveButtonsPanel.setLayout(new BoxLayout(moveButtonsPanel, BoxLayout.Y_AXIS));

            upJButton = new JButton(BundleHolder.bundle.getMessage("up")); //NOI18N
          //  upJButton.setMaximumSize(new Dimension(200, 200));
            upJButton.setActionCommand(UP_ACTION);
            upJButton.addActionListener(this);
            upJButton.setEnabled(false);
            moveButtonsPanel.add(upJButton);
            moveButtonsPanel.add(Box.createRigidArea(new Dimension(0, 5)));

            downJButton = new JButton(BundleHolder.bundle.getMessage("down")); //NOI18N
          //  downJButton.setMaximumSize(new Dimension(200, 200));
            downJButton.setActionCommand(DOWN_ACTION);
            downJButton.setEnabled(false);
            downJButton.addActionListener(this);
            moveButtonsPanel.add(downJButton);

            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 5;
            gridBagConstraints.gridy = 3;
            gridBagConstraints.gridwidth = 1;
            gridBagConstraints.anchor = GridBagConstraints.PAGE_START;
            gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
            gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 10);
            add(moveButtonsPanel, gridBagConstraints);
        }
    }

    protected Component initializeSelectedListComponent() {

        selectedJListModel = new DefaultListModel();
        selectedJList = new JList(selectedJListModel);
        selectedJList.setSelectionMode(getListSelectionStyle());
        selectedJList.setLayoutOrientation(JList.VERTICAL);
        selectedJList.setVisibleRowCount( -1);
        selectedJList.setCellRenderer(new ReferenceDataTwoColumnListCellRenderer());
        populateSelectedJListModel();
        selectedJList.addListSelectionListener(this);
        return selectedJList;
    }

    protected void populateSelectedJListModel() {
        ReferenceDataItem items[];

        selectedJListModel.clear();
        ReferenceDataTwoColumnListCellRenderer renderer = (ReferenceDataTwoColumnListCellRenderer)
            selectedJList.getCellRenderer();
        renderer.resetLeftColumnWidth();
        items = getChooseManyReferenceDataPropertyEditor().getValueReferenceDataItems();
        if (items != null) {
            for (int i = 0; i < items.length; i++) {
                ReferenceDataItem item = items[i];
                selectedJListModel.addElement(item);
                renderer.getListCellRendererComponent(choicesJList, item, -1, false, false);
                renderer.adjustLeftColumnWidthIfNecessary();
            }
        }
    }

    protected void updateButtonsState() {

        super.updateButtonsState();
        boolean hasSelections = choicesJList.getSelectedIndices().length > 0;
        selectJButton.setEnabled(hasSelections);

        if (selectedJList != null) {
            hasSelections = selectedJList.getSelectedIndices().length > 0;
            deselectJButton.setEnabled(hasSelections);
            deselectAllJButton.setEnabled(selectedJListModel.size() > 0);
            if (getCompositeReferenceData().canOrderItems()){
                upJButton.setEnabled(selectedJList.getMinSelectionIndex() > 0);
                downJButton.setEnabled(selectedJList.getMaxSelectionIndex() >= 0 &&
                    selectedJList.getMaxSelectionIndex() < (selectedJListModel.size() - 1));
            }
        }
    }

    public void valueChanged(ListSelectionEvent event) {

        super.valueChanged(event);
        if (event.getSource() == selectedJList) {
            handleSelectedJListSelectionChanged(event);
        }
    }
}
