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

package org.netbeans.modules.xml.multiview;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * The class simplifies use of a combo box to show/set value of an item
 *
 * @author pfiala
 */
public abstract class ItemComboBoxHelper implements ActionListener, Refreshable {
    private XmlMultiViewDataSynchronizer synchronizer;
    private JComboBox comboBox;

    /**
     * Constructor initializes object by combo box and data object which will be handled
     *
     * @param synchronizer
     * @param comboBox   handled JComboBox.
     */
    public ItemComboBoxHelper(XmlMultiViewDataSynchronizer synchronizer, JComboBox comboBox) {
        this.synchronizer = synchronizer;
        this.comboBox = comboBox;
        comboBox.addActionListener(this);
        setValue(getItemValue());
    }

    /**
     * Invoked when an action occurs on a combo box.
     */
    public final void actionPerformed(ActionEvent e) {
        final String value = (String) comboBox.getSelectedItem();
        if (value == null || !value.equals(getItemValue())) {
            setItemValue(value);
            synchronizer.requestUpdateData();
        }
    }

    /**
     * Selects the item value in combo box.
     *
     * @param itemValue value of item to be selected in combo box
     */
    public void setValue(String itemValue) {
        comboBox.setSelectedItem(itemValue);
    }

    /**
     * Combo box getter
     *
     * @return handled combo box
     */
    public JComboBox getComboBox() {
        return comboBox;
    }

    /**
     * Retrieves the text value selected in the combo box.
     *
     * @return selected item of the combo box
     */
    public String getValue() {
        return (String) comboBox.getSelectedItem();
    }

    /**
     * Called by the helper in order to retrieve the value of the item.
     *
     * @return value of the handled item.
     */
    public abstract String getItemValue();

    /**
     * Called by the helper in order to set the value of the item
     *
     * @param value new value of the hanlded item
     */
    public abstract void setItemValue(String value);

    public void refresh() {
        setValue(getItemValue());
    }
}
