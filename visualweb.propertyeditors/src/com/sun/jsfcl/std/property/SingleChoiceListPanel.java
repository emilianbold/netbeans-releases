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
