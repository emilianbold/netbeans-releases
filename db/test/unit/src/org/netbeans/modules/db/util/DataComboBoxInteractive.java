/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
