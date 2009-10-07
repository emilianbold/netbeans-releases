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
import net.java.hulp.i18n.Logger;
import org.netbeans.modules.etl.logger.Localizer;


/**
 * @author Ritesh Adval
 * @version $Revision$
 */
public class GroupPanel extends JPanel {

    private static transient final Logger mLogger = Logger.getLogger(GroupPanel.class.getName());
    private static transient final Localizer mLoc = Localizer.get();
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
        String nbBundle30 = mLoc.t("BUND488: UP");
        upButton.setActionCommand(nbBundle30.substring(15));
        upButton.getAccessibleContext().setAccessibleName(nbBundle30.substring(15));
        upButton.setMnemonic(nbBundle30.substring(15).charAt(0));

        c.gridwidth = GridBagConstraints.REMAINDER;
        c.weighty = 1.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        buttonPanel.add(upButton, c);

        JButton downButton = new JButton(new ImageIcon(DOWN_IMG_URL));
        String nbBundle31 = mLoc.t("BUND488: UP");
        downButton.setActionCommand(nbBundle31.substring(15));
        downButton.getAccessibleContext().setAccessibleName(nbBundle31.substring(15));
        downButton.setMnemonic(nbBundle31.substring(15).charAt(0));

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

