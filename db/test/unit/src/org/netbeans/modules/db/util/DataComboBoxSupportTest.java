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

package org.netbeans.modules.db.util;

import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.ListModel;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Andrei Badea
 */
public class DataComboBoxSupportTest extends NbTestCase {

    public DataComboBoxSupportTest(String testName) {
        super(testName);
    }

    public boolean runInEQ() {
        return true;
    }

    public void testBasic() {
        JComboBox comboBox = new JComboBox();
        ListModelImpl listModel = new ListModelImpl();
        DataModelImpl dataModel = new DataModelImpl(listModel);
        DataComboBoxSupport support = new DataComboBoxSupport(comboBox, dataModel, true);

        assertSame(support.NEW_ITEM, comboBox.getItemAt(0));
        assertEquals("Add", comboBox.getItemAt(0).toString());
        assertEquals(-1, comboBox.getSelectedIndex());

        List items = new ArrayList();
        items.add("foo");
        items.add("bar");
        listModel.setItems(items);

        assertEquals("foo", comboBox.getItemAt(0));
        assertEquals("bar", comboBox.getItemAt(1));
        assertSame(support.SEPARATOR_ITEM, comboBox.getItemAt(2));
        assertEquals("Add", comboBox.getItemAt(3).toString());
        assertEquals("The old selected item was removed, nothing should be selected now", -1, comboBox.getSelectedIndex());

        comboBox.setSelectedIndex(1); // bar
        items.remove("foo");
        listModel.setItems(items);

        assertEquals("bar", comboBox.getItemAt(0));
        assertSame(support.SEPARATOR_ITEM, comboBox.getItemAt(1));
        assertEquals("Add", comboBox.getItemAt(2).toString());
        assertEquals("Bar should still be selected", 0, comboBox.getSelectedIndex());

        items.add("new");
        listModel.setItems(items, "new");

        assertEquals("bar", comboBox.getItemAt(0));
        assertEquals("new", comboBox.getItemAt(1));
        assertSame(support.SEPARATOR_ITEM, comboBox.getItemAt(2));
        assertEquals("Add", comboBox.getItemAt(3).toString());
        assertEquals("new", comboBox.getSelectedItem());
        assertEquals("New should be selected", 1, comboBox.getSelectedIndex());
    }

    public void testNoAddition() {
        JComboBox comboBox = new JComboBox();
        ListModelImpl listModel = new ListModelImpl();
        DataModelImpl dataModel = new DataModelImpl(listModel);
        DataComboBoxSupport support = new DataComboBoxSupport(comboBox, dataModel, false);

        assertEquals(0, comboBox.getItemCount());

        List items = new ArrayList();
        items.add("foo");
        items.add("bar");
        listModel.setItems(items);

        assertEquals(2, comboBox.getItemCount());
        assertEquals("foo", comboBox.getItemAt(0));
        assertEquals("bar", comboBox.getItemAt(1));
    }

    private static final class DataModelImpl implements DataComboBoxModel {

        private ComboBoxModel listModel;

        public DataModelImpl(ComboBoxModel listModel) {
            this.listModel = listModel;
        }

        public String getItemTooltipText(Object item) {
            return null;
        }

        public String getItemDisplayName(Object item) {
            return (String)item + "-display";
        }

        public void newItemActionPerformed() {
            System.out.println("action performed");
        }

        public String getNewItemDisplayName() {
            return "Add";
        }

        public ComboBoxModel getListModel() {
            return listModel;
        }
    }

    private static final class ListModelImpl extends AbstractListModel implements ComboBoxModel {

        Object[] items = new Object[0];
        Object selectedItem;

        public Object getElementAt(int index) {
            return items[index];
        }

        public int getSize() {
            return items.length;
        }

        public Object getSelectedItem() {
            return selectedItem;
        }

        public void setSelectedItem(Object selectedItem) {
            this.selectedItem = selectedItem;
        }

        private void setItems(List items) {
            this.items = (Object[])items.toArray(new Object[items.size()]);
            fireContentsChanged(this, 0, this.items.length);
        }

        private void setItems(List items, Object selectedItem) {
            this.items = (Object[])items.toArray(new Object[items.size()]);
            this.selectedItem = selectedItem;
            fireContentsChanged(this, 0, this.items.length);
        }
    }
}
