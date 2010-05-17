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
package org.netbeans.modules.collab.ui.switcher;

import java.awt.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

/**
 *
 * @author  Todd Fast, todd.fast@sun.com
 */
public class ViewSwitcherPane extends JPanel implements ListSelectionListener {
    ////////////////////////////////////////////////////////////////////////////
    // Instance variables
    ////////////////////////////////////////////////////////////////////////////
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel contentPanel;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JPanel switcherPanel;
    private javax.swing.JPanel tollbarPanel;
    private javax.swing.ButtonGroup toolbarButtonGroup;

    // End of variables declaration//GEN-END:variables
    private ViewSwitcherItemList switcher;
    private Map items = new HashMap();

    /**
     *
     *
     */
    public ViewSwitcherPane() {
        super();
        intialize();
    }

    /**
     *
     *
     */
    private void intialize() {
        initComponents();

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        switcher = new ViewSwitcherItemList();
        switcher.setShowNames(true);
        switcher.addListSelectionListener(this);
        ToolTipManager.sharedInstance().registerComponent(switcher);
        panel.add(switcher, BorderLayout.NORTH);

        switcherPanel.add(panel, BorderLayout.NORTH);
	
        // hide the switcher initially, show on demand
        tollbarPanel.setVisible(false);
    }

    /**
     *
     *
     */
    public void addItem(String title, Icon icon, Component component, String tip) {
        if (items.containsKey(title)) {
            throw new IllegalArgumentException(
                "A component with the " + "name \"" + title + "\" is already present in this component"
            );
        }

        // Create a new item
        Item item = new Item();
        item.setTitle(title);
        item.setIcon(icon);
        item.setComponent(component);
        item.setDescription(tip);
        ((DefaultListModel) switcher.getModel()).addElement(item);

        // Select the first one
        if (items.size() == 0) {
            switcher.setSelectedIndex(0);
        }

        // Add the component
        contentPanel.add(component, title);

        // Remember the button, since we may need to select it.  We can get
        // the component from the button.
        items.put(title, item);
        if (items.size() > 1) tollbarPanel.setVisible(true);
    }

    /**
     *
     *
     */
    public String[] getItemNames() {
        return (String[]) items.keySet().toArray(new String[items.keySet().size()]);
    }

    /**
     *
     *
     */
    public int getNumItems() {
        return items.keySet().size();
    }

    /**
     *
     *
     */
    public Component getComponent(String name) {
        Item item = (Item) items.get(name);

        return (item != null) ? item.getComponent() : null;
    }

    /**
     *
     *
     */
    protected Item getSelectedItem() {
        return (Item) switcher.getSelectedValue();
    }

    /**
     *
     *
     */
    public void setSelectedItem(String name) {
        ((CardLayout) contentPanel.getLayout()).show(contentPanel, name);

        Item item = (Item) items.get(name);

        if (item != null) {
            switcher.setSelectedValue(item, true);
        }
    }

    /**
     *
     *
     */
    public void removeItem(String name) {
        Item item = (Item) items.get(name);

        if (item != null) {
            items.remove(name);
            ((DefaultListModel) switcher.getModel()).removeElement(item);
            contentPanel.remove(item.getComponent());
        }
        if (items.size() <= 1) tollbarPanel.setVisible(false);
    }

    /**
         * This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents

        java.awt.GridBagConstraints gridBagConstraints;

        toolbarButtonGroup = new javax.swing.ButtonGroup();
        tollbarPanel = new javax.swing.JPanel();
        switcherPanel = new javax.swing.JPanel();
        jSeparator1 = new javax.swing.JSeparator();
        contentPanel = new javax.swing.JPanel();

        setLayout(new java.awt.BorderLayout());

        tollbarPanel.setLayout(new java.awt.BorderLayout());

        switcherPanel.setLayout(new java.awt.BorderLayout());

        tollbarPanel.add(switcherPanel, java.awt.BorderLayout.NORTH);

        tollbarPanel.add(jSeparator1, java.awt.BorderLayout.SOUTH);

        add(tollbarPanel, java.awt.BorderLayout.NORTH);

        contentPanel.setLayout(new java.awt.CardLayout());

        add(contentPanel, java.awt.BorderLayout.CENTER);
    }//GEN-END:initComponents

    /**
     *
     *
     */
    public void valueChanged(ListSelectionEvent event) {
        ViewSwitcherItemList list = (ViewSwitcherItemList) event.getSource();
        Item item = (Item) list.getSelectedValue();

        if (item != null) {
            setSelectedItem(item.getTitle());
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Inner class
    ////////////////////////////////////////////////////////////////////////////

    /**
     *
     *
     */
    protected class Item extends Object {
        private String title;
        private String description;
        private Icon icon;
        private Component component;

        /**
         *
         *
         */
        public Item() {
            super();
        }

        /**
         *
         *
         */
        public String getTitle() {
            return title;
        }

        /**
         *
         *
         */
        public void setTitle(String value) {
            title = value;
        }

        /**
         *
         *
         */
        public String getDescription() {
            return description;
        }

        /**
         *
         *
         */
        public void setDescription(String value) {
            description = value;
        }

        /**
         *
         *
         */
        public Icon getIcon() {
            return icon;
        }

        /**
         *
         *
         */
        public void setIcon(Icon value) {
            icon = value;
        }

        /**
         *
         *
         */
        public Component getComponent() {
            return component;
        }

        /**
         *
         *
         */
        public void setComponent(Component value) {
            component = value;
        }
    }
}
