/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
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
public abstract class ItemCheckBoxHelper implements ActionListener, Refreshable {
    private JCheckBox checkBox;
    private XmlMultiViewDataObject dataObject;

    /**
     * Constructor initializes object by combo box and data object which will be handled
     *
     * @param dataObject
     * @param checkBox   handled JComboBox.
     */
    public ItemCheckBoxHelper(XmlMultiViewDataObject dataObject, JCheckBox checkBox) {
        this.dataObject = dataObject;
        this.checkBox = checkBox;
        checkBox.addActionListener(this);
        setValue(getItemValue());
    }

    /**
     * Invoked when an action occurs on a combo box.
     */
    public final void actionPerformed(ActionEvent e) {
        final boolean value = getValue();
        if (value != getItemValue()) {
            setItemValue(value);
            dataObject.modelUpdatedFromUI();
        }
    }

    /**
     * Selects the item value in check box.
     *
     * @param itemValue value of item to be selected in check box
     */
    public void setValue(boolean itemValue) {
        checkBox.setSelected(itemValue);
    }

    /**
     * Check box getter
     *
     * @return handled check box
     */
    public JCheckBox getCheckBox() {
        return checkBox;
    }

    /**
     * Retrieves the text value selected in the check box.
     *
     * @return selected item of the check box
     */
    public boolean getValue() {
        return checkBox.isSelected();
    }

    /**
     * Called by the helper in order to retrieve the value of the item.
     *
     * @return value of the handled item.
     */
    public abstract boolean getItemValue();

    /**
     * Called by the helper in order to set the value of the item
     *
     * @param value new value of the hanlded item
     */
    public abstract void setItemValue(boolean value);

    public void refresh() {
        setValue(getItemValue());
    }
}
