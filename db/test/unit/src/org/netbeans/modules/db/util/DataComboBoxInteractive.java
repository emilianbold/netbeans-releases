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

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import junit.framework.TestCase;

/**
 * Not a real test, just a JFrame to test DataComboBoxSupport interactively.
 * Looks like a test, however, in order to be able to invoke the Run File
 * action on it (otherwise, if it was e.g. a JFrame with a main() method, the
 * NetBeans build script would still run it as a TestCase...).
 *
 * @author Andrei Badea
 */
public class DataComboBoxInteractive extends TestCase {

    public DataComboBoxInteractive(String name) {
        super(name);
    }

    public void testInteractive() throws Exception {
        java.awt.EventQueue.invokeAndWait(new Runnable() {
            public void run() {
                new TestFrame().setVisible(true);
            }
        });
        synchronized (DataComboBoxInteractive.class) {
            DataComboBoxInteractive.class.wait();
        }
    }

    private static final class TestFrame extends JFrame {

        private javax.swing.JComboBox comboBox;

        public TestFrame() {
            initComponents();

            DataComboBoxSupport.connect(comboBox, new TestDataComboBoxModel());
        }

        private void initComponents() {
            comboBox = new javax.swing.JComboBox();

            addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    super.windowClosing(e);

                    setVisible(false);
                    synchronized (DataComboBoxInteractive.class) {
                        DataComboBoxInteractive.class.notifyAll();
                    }
                }
            });

            comboBox.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    comboBoxActionPerformed(evt);
                }
            });

            org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
            getContentPane().setLayout(layout);
            layout.setHorizontalGroup(
                layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(layout.createSequentialGroup()
                    .addContainerGap()
                    .add(comboBox, 0, 376, Short.MAX_VALUE)
                    .addContainerGap())
            );
            layout.setVerticalGroup(
                layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(layout.createSequentialGroup()
                    .addContainerGap()
                    .add(comboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(264, Short.MAX_VALUE))
            );
            pack();
        }

        private void comboBoxActionPerformed(java.awt.event.ActionEvent evt) {
            System.out.println("Action performed: " + comboBox.getSelectedItem());
        }
    }

    private static final class TestDataComboBoxModel implements DataComboBoxModel {

        private final TestComboBoxModel comboBoxModel = new TestComboBoxModel();

        public String getItemTooltipText(Object item) {
            return item + " tooltip";
        }

        public String getItemDisplayName(Object item) {
            return String.valueOf(item);
        }

        public void newItemActionPerformed() {
            String newItem = JOptionPane.showInputDialog("New item:");
            if (newItem != null) {
                comboBoxModel.addSelectedItem(newItem);
            }
        }

        public String getNewItemDisplayName() {
            return "Add item";
        }

        public ComboBoxModel getListModel() {
            return comboBoxModel;
        }
    }

    private static final class TestComboBoxModel extends AbstractListModel implements ComboBoxModel {

        private List items = new ArrayList();
        private Object selectedItem;

        public TestComboBoxModel() {
            items.add("first");
            items.add("second");
        }

        public void setSelectedItem(Object anItem) {
           selectedItem = anItem;
        }

        public Object getElementAt(int index) {
            return items.get(index);
        }

        public int getSize() {
            return items.size();
        }

        public Object getSelectedItem() {
            return selectedItem;
        }

        public void addSelectedItem(String item) {
            items.add(item);
            selectedItem = item;
            fireContentsChanged(this, 0, items.size());
        }
    }
}
