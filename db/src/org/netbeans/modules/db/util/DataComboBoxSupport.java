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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

/**
 * This is an utility class for filling combo boxes with some data (usually
 * some items). The combo box has a separator item and a "Add item" item allowing
 * the user to invoke the adding of new items to the combo box. The client of
 * this class should provide a {@link DataComboBoxModel} and call
 * the {@link #connect} method.
 *
 * @author Andrei Badea
 */
public final class DataComboBoxSupport {

    private final DataComboBoxModel dataModel;
    private final boolean allowAdding;

    private Object previousItem = null;
    private Object previousNonSpecialItem = null;
    private int previousIndex = -1;

    private boolean performingNewItemAction = false;

    /**
     * Serves as the separator item.
     */
    private static final class Separator extends JSeparator {

        Separator() {
            setForeground(Color.BLACK);
        }

        public Dimension getPreferredSize() {
            Dimension size = super.getPreferredSize();
            return new Dimension(size.width, 1);
        }
    }

    /** Not private because used in tests. */
    static final Separator SEPARATOR_ITEM = new Separator();

    /**
     * Serves as the new item. Not private because used in tests.
     */
    final Object NEW_ITEM = new Object() {
        public String toString() {
            return dataModel.getNewItemDisplayName();
        }
    };

    /** Not private because used in tests. */
    DataComboBoxSupport(JComboBox comboBox, DataComboBoxModel dataModel, boolean allowAdding) {
        this.dataModel = dataModel;
        this.allowAdding = allowAdding;

        comboBox.setEditable(false);

        comboBox.setModel(new ItemComboBoxModel());

        comboBox.setRenderer(new ItemListCellRenderer());
        comboBox.addKeyListener(new ItemKeyListener());
        comboBox.addActionListener(new ItemActionListener());
        comboBox.addPopupMenuListener(new ItemPopupMenuListener());
    }

    /**
     * Connects a combo box with the specified combo box model.
     */
    public static void connect(JComboBox comboBox, DataComboBoxModel dataModel) {
        connect(comboBox, dataModel, true);
    }

    /**
     * Connects a combo box with the specified combo box model.
     */
    public static void connect(JComboBox comboBox, DataComboBoxModel dataModel, boolean allowAdding) {
        new DataComboBoxSupport(comboBox, dataModel, allowAdding);
    }

    private boolean isSpecialItem(Object item) {
        return item == SEPARATOR_ITEM || item == NEW_ITEM;
    }

    private void setPreviousNonSpecialItem(JComboBox comboBox) {
        if (comboBox.getSelectedItem() == NEW_ITEM) {
            // no new item added
            comboBox.setSelectedItem(previousNonSpecialItem);
        }
    }

    private class ItemComboBoxModel extends AbstractListModel implements ComboBoxModel, ListDataListener {

        // XXX intervalAdded() and intervalRemoved() are not implemented,
        // but it is enough for the connection and drivers combo boxes

        public ItemComboBoxModel() {
            getDelegate().addListDataListener(this);
        }

        public Object getElementAt(int index) {
            if (allowAdding) {
                if (getSize() == 1) {
                    // there is just NEW_ITEM
                    if (index == 0) {
                        return NEW_ITEM;
                    } else {
                        throw new IllegalStateException("Index out of bounds: " + index); // NOI18N
                    }
                }

                // there are the delegate items, SEPARATOR_ITEM, NEW_ITEM
                if (index >= 0 && index < getDelegate().getSize()) {
                    return getDelegate().getElementAt(index);
                } else if (index == getSize() - 2) {
                    return SEPARATOR_ITEM;
                } else if (index == getSize() - 1) {
                    return NEW_ITEM;
                } else {
                    throw new IllegalStateException("Index out of bounds: " + index); // NOI18N
                }
            } else {
                // there are no other items than those of the delegate
                return getDelegate().getElementAt(index);
            }
        }

        public int getSize() {
            // 1 = NEW_ITEM
            // 2 = SEPARATOR, NEW_ITEM
            if (allowAdding) {
                return getDelegate().getSize() == 0 ? 1 : getDelegate().getSize() + 2;
            } else {
                return getDelegate().getSize();
            }
        }

        public void setSelectedItem(Object anItem) {
            previousItem = getDelegate().getSelectedItem();
            previousIndex = getItemIndex(previousItem);

            if (!isSpecialItem(previousItem)) {
                previousNonSpecialItem = previousItem;
            }

            getDelegate().setSelectedItem(anItem);
        }

        public Object getSelectedItem() {
            return getDelegate().getSelectedItem();
        }

        public Object getPreviousItem() {
            return previousItem;
        }

        private ComboBoxModel getDelegate() {
            return dataModel.getListModel();
        }

        private int getItemIndex(Object item) {
            if (item == null) {
                return -1;
            }
            for (int i = 0; i < getSize(); i++ ) {
                if (getElementAt(i).equals(item)) {
                    return i;
                }
            }
            return -1;
        }

        public void intervalRemoved(ListDataEvent e) {
            throw new UnsupportedOperationException("This is currently not supported.");
        }

        public void intervalAdded(ListDataEvent e) {
            throw new UnsupportedOperationException("This is currently not supported.");
        }

        public void contentsChanged(ListDataEvent e) {
            fireContentsChanged(this, 0, getSize());
        }
    }

    private class ItemListCellRenderer extends DefaultListCellRenderer {

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {

            Component component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            JLabel label = (JLabel)component;

            if (value != null && !isSpecialItem(value)) {
                String displayName = dataModel.getItemDisplayName(value);
                label.setText(dataModel.getItemDisplayName(value));
                label.setToolTipText(dataModel.getItemTooltipText(value));
            } else if (value == SEPARATOR_ITEM) {
                return SEPARATOR_ITEM;
            } else if (value != null) {
                label.setText(value.toString());
                label.setToolTipText(null);
            }

            return label;
        }
    }

    private final class ItemKeyListener extends KeyAdapter {

        public void keyPressed(KeyEvent e) {
            JComboBox comboBox = (JComboBox)e.getSource();

            int keyCode = e.getKeyCode();
            if (KeyEvent.VK_ENTER == keyCode) {
                Object selectedItem = comboBox.getSelectedItem();
                if (selectedItem == NEW_ITEM) {
                    performingNewItemAction = true;
                    try {
                        comboBox.setPopupVisible(false);
                        e.consume();
                        dataModel.newItemActionPerformed();
                    } finally {
                        performingNewItemAction = false;
                    }

                    setPreviousNonSpecialItem(comboBox);
                }
            }
        }
    }

    private final class ItemActionListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            final JComboBox comboBox = (JComboBox)e.getSource();

            Object selectedItem = comboBox.getSelectedItem();
            if (selectedItem == SEPARATOR_ITEM) {
                int newIndex = -1;
                if (previousIndex != -1) {
                    // skipping the separator when moving up/down with the arrow keys
                    int selectedIndex = comboBox.getSelectedIndex();
                    if (selectedIndex > previousIndex) {
                        // moving down
                        newIndex = selectedIndex + 1;
                    } else {
                        // moving up
                        newIndex= selectedIndex - 1;
                    }
                }
                comboBox.setSelectedIndex(newIndex);
            } else if ((e.getModifiers() & InputEvent.BUTTON1_MASK) != 0) {
                // handling mouse click, see KeyEvent.getKeyModifiersText(e.getModifiers())
                if (selectedItem == NEW_ITEM) {
                    performingNewItemAction = true;
                    try {
                        comboBox.setPopupVisible(false);
                        dataModel.newItemActionPerformed();
                    } finally {
                        performingNewItemAction = false;
                    }

                    setPreviousNonSpecialItem(comboBox);
                    // we (or maybe the client) have just selected an item inside an actionPerformed event,
                    // which will not send another actionPerformed event for the new item. 
                    // We need to make sure all listeners get an event for the new item,
                    // thus...
                    final Object newSelectedItem = comboBox.getSelectedItem();
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            comboBox.setSelectedItem(newSelectedItem);
                        }
                    });
                }
            }
        }
    }

    private final class ItemPopupMenuListener implements PopupMenuListener {

        public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
        }

        public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
            if (!performingNewItemAction) {
                setPreviousNonSpecialItem((JComboBox)e.getSource());
            }
        }

        public void popupMenuCanceled(PopupMenuEvent e) {
            // without the check the previous non-special item would be displayed
            // while calling DataComboBoxModel.newItemActionPerformed() 
            // instead of NEW_ITEM, but this is unwanted. Same for
            // popupMenuWillBecomeImvisible().
            if (!performingNewItemAction) {
                setPreviousNonSpecialItem((JComboBox)e.getSource());
            }
        }
    }
}
