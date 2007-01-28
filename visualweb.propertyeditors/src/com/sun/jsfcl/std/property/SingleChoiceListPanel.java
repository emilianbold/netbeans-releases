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
package com.sun.jsfcl.std.property;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.util.Iterator;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import com.sun.rave.designtime.DesignProperty;

/**
 * @author eric
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class SingleChoiceListPanel extends AbstractPropertyJPanel {

    protected java.util.List choices;
    protected JList choicesJList;
    protected DefaultListModel choicesJListModel;
    protected JScrollPane choicesJListScrollPane;
    protected JLabel valueLabelControl;
    protected JTextField valueTextControl;

    /**
     *
     */
    public SingleChoiceListPanel(SingleChoiceListPropertyEditor propertyEditor,
        DesignProperty liveProperty) {

        super(propertyEditor, liveProperty);
    }

    public void doLayout() {

        super.doLayout();
        choicesJList.ensureIndexIsVisible(choicesJList.getSelectedIndex());
    }

    protected java.util.List getChoices() {

        return choices;
    }

    protected SingleChoiceListPropertyEditor getSingleChoiceListPropertyEditor() {

        return (SingleChoiceListPropertyEditor)getPropertyEditor();
    }

    protected void handleChoicesJListSelectionChanged(ListSelectionEvent event) {

        if (event.getValueIsAdjusting()) {
            return;
        }

        int index;
        Object selectedChoice;

        index = choicesJList.getSelectedIndex();
        if (index == -1) {
            selectedChoice = null;
        } else {
            selectedChoice = choices.get(index);
        }
        getSingleChoiceListPropertyEditor().setValueChoice(selectedChoice);
        if (valueTextControl != null) {
            valueTextControl.setText(getSingleChoiceListPropertyEditor().getStringForChoice(
                selectedChoice));
        }
    }

    protected void initializeChoices() {

        choices = getSingleChoiceListPropertyEditor().getChoices();
    }

    protected void initializeComponents() {
        GridBagConstraints gridBagConstraints;

        setLayout(new java.awt.GridBagLayout());

        // add Value label
        valueLabelControl = new javax.swing.JLabel();
        valueLabelControl.setText(BundleHolder.bundle.getMessage("value")); //NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.ipadx = 36;
        gridBagConstraints.ipady = 5;
//        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 0);
        add(valueLabelControl, gridBagConstraints);

        // add Value entry field
        valueTextControl = new javax.swing.JTextField();
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
//        gridBagConstraints.ipadx = 118;
        gridBagConstraints.insets = new java.awt.Insets(10, 20, 10, 0);
        add(valueTextControl, gridBagConstraints);

        int selectedIndex;

        // add the list control
        choicesJListModel = new DefaultListModel();
        populateChoicesJListModel();
        choicesJList = new JList(choicesJListModel);
        choicesJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        choicesJList.setLayoutOrientation(JList.VERTICAL);
        choicesJList.setVisibleRowCount( -1);
        choicesJList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent event) {
                handleChoicesJListSelectionChanged(event);
            }
        });
        selectedIndex = choices.indexOf(getSingleChoiceListPropertyEditor().getValueChoice());
        choicesJList.setSelectedIndex(selectedIndex);
        // wrap the list control in scrolling pane
        choicesJListScrollPane = new JScrollPane(choicesJList);
        choicesJListScrollPane.setPreferredSize(new Dimension(200, 200));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
//        gridBagConstraints.ipadx = 5;
//        gridBagConstraints.insets = new java.awt.Insets(10, 20, 10, 10);
        add(choicesJListScrollPane, gridBagConstraints);

        return;
    }

    protected void populateChoicesJListModel() {

        for (Iterator iterator = getChoices().iterator(); iterator.hasNext(); ) {
            Object object;
            String string;

            object = iterator.next();
            string = getSingleChoiceListPropertyEditor().getStringForChoice(object);
            if (string.length() == 0) {
                string = " "; //NOI18N
            }
            choicesJListModel.addElement(string);
        }
    }

    protected void setPropertyEditorAndDesignProperty(AbstractPropertyEditor propertyEditor,
        DesignProperty liveProperty) {

        super.setPropertyEditorAndDesignProperty(propertyEditor, liveProperty);
        initializeChoices();
    }

}
