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

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.beans.VetoableChangeListener;
import java.util.Iterator;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.ListSelectionEvent;
import com.sun.jsfcl.std.reference.CompositeReferenceData;
import com.sun.jsfcl.std.reference.ReferenceDataItem;
import com.sun.rave.designtime.DesignProperty;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import org.openide.explorer.propertysheet.PropertyEnv;

/**
 * @author eric
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ChooseOneReferenceDataPanel extends AbstractPropertyJPanel {

    protected static final String ADD_ACTION = "add"; //NOI18N
    protected static final String REMOVE_ACTION = "remove"; //NOI18N
    protected static final String CLEAR_FILTER_ACTION = "clearFilter"; //NOI18N
    protected static final int MIN_ITEMS_FOR_FILTER = 15;

    protected JButton addJButton;
    protected JList choicesJList;
    protected DefaultListModel choicesJListModel;
    protected JScrollPane choicesJListScrollPane;
    protected JButton clearFilterButton;
    protected Pattern filterPattern;
    protected JTextField filterTextControl;
    protected JButton removeJButton;
    protected ReferenceDataItem selectedChoice;
    
    private PanelSubmissionListener panelSubmissionListener;

    
    public ChooseOneReferenceDataPanel(ChooseOneReferenceDataPropertyEditor propertyEditor,
        DesignProperty liveProperty) {
        super(propertyEditor, liveProperty);
        PropertyEnv propertyEnv = propertyEditor.getEnv();
        if (propertyEnv != null) {
            propertyEnv.setState(PropertyEnv.STATE_NEEDS_VALIDATION);
            panelSubmissionListener = new PanelSubmissionListener(propertyEditor);
            propertyEnv.addVetoableChangeListener(panelSubmissionListener);
            this.propertyEditor = propertyEditor;
        }
    }

    public void actionPerformed(ActionEvent event) {
        super.actionPerformed(event);
        if (ADD_ACTION.equals(event.getActionCommand())) {
            handleAddAction(event);
            return;
        }
        if (REMOVE_ACTION.equals(event.getActionCommand())) {
            handleRemoveAction(event);
            return;
        }
        if (CLEAR_FILTER_ACTION.equals(event.getActionCommand())) {
            handleClearFilterAction(event);
            return;
        }
    }

    protected java.util.List getChoices() {
        return getChooseFromReferenceDataPropertyEditor().getItems();
    }

    protected ChooseOneReferenceDataPropertyEditor getChooseFromReferenceDataPropertyEditor() {
        return (ChooseOneReferenceDataPropertyEditor)getPropertyEditor();
    }

    protected CompositeReferenceData getCompositeReferenceData() {
        return getChooseFromReferenceDataPropertyEditor().getCompositeReferenceData();
    }

    protected int getListSelectionStyle() {
        return ListSelectionModel.SINGLE_SELECTION;
    }

    public Object getPropertyValue() {
        if (selectedChoice == null) {
            return null;
        } else {
            return selectedChoice.getValue();
        }
    }

    protected ReferenceDataItem getSelectedChoice() {
        return selectedChoice;
    }

    protected String getTopLabel() {
        return getCompositeReferenceData().getChooseOneTitle();
    }

    protected int getValueLabelGridWidth() {
        return 2;
    }

    protected void grabCurrentValueFromPropertyEditor() {
        selectedChoice = getChooseFromReferenceDataPropertyEditor().getValueReferenceDataItem();
    }

    protected void handleAddAction(ActionEvent event) {
        ReferenceDataItem item = (new NewReferenceDataItemDialog(getCompositeReferenceData())).
            showDialog(this);
        if (item == null) {
            return;
        }
        populateChoicesJListModel();
        choicesJList.setSelectedValue(item, true);
    }

    protected void handleChoicesJListSelectionChanged(ListSelectionEvent event) {
        if (event.getValueIsAdjusting()) {
            return;
        }
        if (choicesJList.getSelectedIndices().length == 0) {
            selectedChoice = null;
        } else {
            selectedChoice = (ReferenceDataItem)choicesJList.getSelectedValue();
        }
        updateButtonsState();
    }

    public void handleClearFilterAction(ActionEvent action) {
        filterTextControl.setText("");
    }

    public void handleFilterChanged() {
        String string = filterTextControl.getText().trim();
        clearFilterButton.setEnabled(string.length() > 0);
        if (string.length() == 0) {
            filterPattern = null;
        } else {
            try {
                filterPattern = Pattern.compile(".*" + string + ".*",
                    Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
            } catch (PatternSyntaxException e) {
                filterPattern = null;
            }
        }
        populateChoicesJListModel();
    }

    protected void handleRemoveAction(ActionEvent event) {
        int index = choicesJList.getMinSelectionIndex();
        getCompositeReferenceData().remove(getSelectedChoice());
        populateChoicesJListModel();
        if (choicesJListModel.size() > 0) {
            if (index >= choicesJListModel.size()) {
                index = choicesJListModel.size() - 1;
            }
            choicesJList.setSelectedIndex(index);
            choicesJList.ensureIndexIsVisible(index);
        }
    }

    protected boolean includeInChoicesJList(ReferenceDataItem item) {
        return filterPattern == null || item.matchesPattern(filterPattern);
    }

    protected void initializeComponents() {
        GridBagConstraints gridBagConstraints;

        setLayout(new java.awt.GridBagLayout());

        // add top label
        JLabel label = new javax.swing.JLabel();
        label.setText(getTopLabel());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = getValueLabelGridWidth();
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.PAGE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 10);
        add(label, gridBagConstraints);

        if (getChoices().size() > MIN_ITEMS_FOR_FILTER) {
            JPanel filterPanel = new JPanel();
            filterPanel.setLayout(new BoxLayout(filterPanel, BoxLayout.LINE_AXIS));

            label = new javax.swing.JLabel();
            label.setText(BundleHolder.bundle.getMessage("Filter")); //NOI18N
            filterPanel.add(label);
            filterPanel.add(Box.createRigidArea(new Dimension(5, 0)));

            // add filter entry field
            filterTextControl = new javax.swing.JTextField();
            filterTextControl.setColumns(10);
            filterTextControl.getDocument().addDocumentListener(this);
            filterPanel.add(filterTextControl);
            filterPanel.add(Box.createRigidArea(new Dimension(5, 0)));

            clearFilterButton = new JButton(BundleHolder.bundle.getMessage("CLEAR_BUTTON_TXT"));
            clearFilterButton.setEnabled(false);
            clearFilterButton.setActionCommand(CLEAR_FILTER_ACTION);
            clearFilterButton.addActionListener(this);
            filterPanel.add(clearFilterButton);

            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 2;
            gridBagConstraints.gridwidth = 2;
            gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
            gridBagConstraints.anchor = GridBagConstraints.CENTER;
            gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 10);
            add(filterPanel, gridBagConstraints);
        }

        // add the choices list control
        choicesJListModel = new DefaultListModel();
        choicesJList = new JList(choicesJListModel);
        choicesJList.setSelectionMode(getListSelectionStyle());
        choicesJList.setLayoutOrientation(JList.VERTICAL);
        choicesJList.setVisibleRowCount( -1);
        choicesJList.addListSelectionListener(this);
        choicesJList.setCellRenderer(new ReferenceDataTwoColumnListCellRenderer());
        populateChoicesJListModel();
        updateChoicesJListSelection();
        // wrap the list control in scrolling pane
        choicesJListScrollPane = new JScrollPane(choicesJList);
        choicesJListScrollPane.setPreferredSize(new Dimension(200, 200));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 10);
        add(choicesJListScrollPane, gridBagConstraints);

        if (getCompositeReferenceData().canAddRemoveItems()) {
            JPanel buttonPanel = new JPanel();
            buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));

            // Add add/remove buttons
            addJButton = new JButton(BundleHolder.bundle.getMessage("New")); //NOI18N
            addJButton.setActionCommand(ADD_ACTION);
            addJButton.addActionListener(this);
            buttonPanel.add(addJButton);
            buttonPanel.add(Box.createRigidArea(new Dimension(5, 0)));

            removeJButton = new JButton(BundleHolder.bundle.getMessage("remove")); //NOI18N
            removeJButton.setActionCommand(REMOVE_ACTION);
            removeJButton.addActionListener(this);
            buttonPanel.add(removeJButton);

            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 4;
            gridBagConstraints.gridwidth = 3;
            gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
            gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 0);
            add(buttonPanel, gridBagConstraints);
        }
    }

    public void documentEvent(DocumentEvent event) {
        if (event.getDocument() == filterTextControl.getDocument()) {
            handleFilterChanged();
        }
    }

    public void doLayout() {
        super.doLayout();
        choicesJList.ensureIndexIsVisible(choicesJList.getSelectedIndex());
        updateButtonsState();
    }

    protected void populateChoicesJListModel() {
        choicesJListModel.clear();
        ReferenceDataTwoColumnListCellRenderer renderer = (ReferenceDataTwoColumnListCellRenderer)
            choicesJList.getCellRenderer();
        renderer.resetLeftColumnWidth();
        for (Iterator iterator = getChoices().iterator(); iterator.hasNext(); ) {
            ReferenceDataItem item;

            item = (ReferenceDataItem)iterator.next();
            if (includeInChoicesJList(item)) {
                choicesJListModel.addElement(item);
                renderer.getListCellRendererComponent(choicesJList, item, -1, false, false);
                renderer.adjustLeftColumnWidthIfNecessary();
            }
        }
    }

    protected void updateButtonsState() {
        if (removeJButton != null) {
            ReferenceDataItem item;

            item = getSelectedChoice();
            removeJButton.setEnabled(item != null && item.isRemovable());
        }
    }

    protected void updateChoicesJListSelection() {
        choicesJList.setSelectedValue(getSelectedChoice(), true);
    }

    public void valueChanged(ListSelectionEvent event) {
        super.valueChanged(event);
        if (event.getSource() == choicesJList) {
            handleChoicesJListSelectionChanged(event);
        }
    }
    
    protected void finalize() throws Throwable {
        if (panelSubmissionListener != null)
            ((ChooseOneReferenceDataPropertyEditor) this.propertyEditor).getEnv().removeVetoableChangeListener(panelSubmissionListener);
        super.finalize();
    }
    
    
    class PanelSubmissionListener implements VetoableChangeListener {
        
        ChooseOneReferenceDataPropertyEditor propertyEditor;
        
        PanelSubmissionListener(ChooseOneReferenceDataPropertyEditor propertyEditor) {
            this.propertyEditor = propertyEditor;
        }
        
        public final void vetoableChange(PropertyChangeEvent event) throws PropertyVetoException {
            if (PropertyEnv.PROP_STATE.equals(event.getPropertyName())) {
                propertyEditor.setValue(getPropertyValue());
                propertyEditor.getEnv().setState(PropertyEnv.STATE_VALID);
            }
        }
    }
    

}
