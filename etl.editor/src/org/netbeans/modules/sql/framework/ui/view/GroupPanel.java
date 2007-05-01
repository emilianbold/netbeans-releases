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
package org.netbeans.modules.sql.framework.ui.view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

/**
 * @author Ritesh Adval
 * @version $Revision$
 */
public class GroupPanel extends JPanel {

    class ButtonActionListener implements ActionListener {

        /**
         * Invoked when an action occurs.
         */
        public void actionPerformed(ActionEvent e) {
            String actionCmd = e.getActionCommand();

            if (actionCmd == null) {
                return;
            }

            if (actionCmd.equalsIgnoreCase("UP")) {
                moveUp();
            } else if (actionCmd.equalsIgnoreCase("DOWN")) {
                moveDown();
            }
        }

        private void moveDown() {
            if (listModel.getSize() <= 1) {
                return;
            }

            int[] sel = list.getSelectedIndices();

            list.clearSelection();
            Arrays.sort(sel);
            for (int i = (sel.length - 1); i >= 0; i--) {
                int index = sel[i];
                if (index < 0 || index == listModel.size() - 1) {
                    continue;
                }

                Object obj = listModel.remove(index);
                if (obj != null) {
                    listModel.add(index + 1, obj);
                    list.addSelectionInterval(index + 1, index + 1);
                }
            }
        }

        private void moveUp() {
            if (listModel.getSize() <= 1) {
                return;
            }

            int[] sel = list.getSelectedIndices();

            list.clearSelection();
            Arrays.sort(sel);
            for (int i = 0; i < sel.length; i++) {
                int index = sel[i];
                if (index == 0 || index == listModel.size()) {
                    continue;
                }

                Object obj = listModel.remove(index);
                if (obj != null) {
                    listModel.add(index - 1, obj);
                    list.addSelectionInterval(index - 1, index - 1);
                }
            }
        }
    }

    class GroupListCellRenderer extends DefaultListCellRenderer {
        public Component getListCellRendererComponent(JList aList, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            return super.getListCellRendererComponent(aList, value.toString(), index, isSelected, cellHasFocus);
        }

    }

    private static final URL DOWN_IMG_URL = GroupPanel.class.getResource("/org/netbeans/modules/sql/framework/ui/resources/images/assignDown.png");

    private static final URL UP_IMG_URL = GroupPanel.class.getResource("/org/netbeans/modules/sql/framework/ui/resources/images/assignUp.png");
    private JList list;
    private DefaultListModel listModel;

    /**
     * New instance
     * 
     * @param title - title
     */
    public GroupPanel(String title) {
        super();
        listModel = new DefaultListModel();
        initGui(title);
    }

    /**
     * Creates a new instance of GroupByView
     * 
     * @param title - title
     * @param columns - columns
     */
    public GroupPanel(String title, Collection columns) {
        this(title);
        initializeListModel(columns);

    }

    /**
     * Add to list
     * 
     * @param aList - list
     */
    public void addToList(List aList) {
        Iterator it = aList.iterator();
        while (it.hasNext()) {
            Object obj = it.next();
            if (!contains(obj)) {
                listModel.addElement(obj);
            }
        }
    }

    /**
     * Contains
     * 
     * @param obj - object
     * @return true/false
     */
    public boolean contains(Object obj) {
        for (int i = 0; i < listModel.getSize(); i++) {
            Object listObj = listModel.get(i);
            if (listObj.equals(obj)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Get order list
     * 
     * @return list
     */
    public List getOrderedList() {
        ArrayList orderList = new ArrayList();
        for (Enumeration e = listModel.elements(); e.hasMoreElements();) {
            Object element = e.nextElement();
            orderList.add(element);
        }
        return orderList;
    }

    /**
     * Get selected item
     * 
     * @return list
     */
    public List getSelectItems() {
        ArrayList aList = new ArrayList();
        Object[] values = list.getSelectedValues();
        for (int i = 0; i < values.length; i++) {
            aList.add(values[i]);
        }

        return aList;
    }

    /**
     * Remove from list
     * 
     * @param aList - list
     */
    public void removeFromList(List aList) {
        Iterator it = aList.iterator();
        while (it.hasNext()) {
            Object obj = it.next();
            listModel.removeElement(obj);
        }
    }

    /**
     * Set data
     * 
     * @param data - data
     */
    public void setData(Collection data) {
        initializeListModel(data);
    }

    private void initGui(String title) {
        this.setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel();
        this.add(mainPanel, BorderLayout.CENTER);

        mainPanel.setLayout(new BorderLayout());

        list = new JList(listModel);
        list.setCellRenderer(new GroupListCellRenderer());

        list.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        JScrollPane sPane = new JScrollPane(list);
        mainPanel.add(sPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        GridBagLayout gl = new GridBagLayout();
        buttonPanel.setLayout(gl);
        mainPanel.add(buttonPanel, BorderLayout.EAST);

        GridBagConstraints c = new GridBagConstraints();

        JButton upButton = new JButton(new ImageIcon(UP_IMG_URL));
        upButton.setActionCommand("UP");

        c.gridwidth = GridBagConstraints.REMAINDER;
        c.weighty = 1.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        buttonPanel.add(upButton, c);

        JButton downButton = new JButton(new ImageIcon(DOWN_IMG_URL));
        downButton.setActionCommand("DOWN");

        c.gridwidth = GridBagConstraints.REMAINDER;
        c.weighty = 1.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        buttonPanel.add(downButton, c);

        ButtonActionListener aListener = new ButtonActionListener();
        upButton.addActionListener(aListener);
        downButton.addActionListener(aListener);

    }

    private void initializeListModel(Collection columns) {
        Iterator it = columns.iterator();
        while (it.hasNext()) {
            Object obj = it.next();
            listModel.addElement(obj);
        }
    }

}

