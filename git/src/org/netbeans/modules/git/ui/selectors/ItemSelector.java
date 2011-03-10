/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.git.ui.selectors;

import java.awt.Component;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.netbeans.modules.git.ui.selectors.ItemSelector.Item;
import org.openide.awt.Mnemonics;
import org.openide.util.ChangeSupport;

/**
 *
 * @author Tomas Stupka
 */
public class ItemSelector<I extends Item> implements ListSelectionListener {
    private ItemsPanel panel;
    private ChangeSupport changeSupport = new ChangeSupport(this);

    public ItemSelector(String title) {
        panel = new ItemsPanel();
        Mnemonics.setLocalizedText(panel.titleLabel, title); 
        panel.list.setCellRenderer(new ItemRenderer());
        attachListeners();
    }
    
    public JPanel getPanel() {
       return panel;
    }

    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    public void setBranches(List<I> branches) {
        Collections.sort(branches);
        DefaultListModel model = new DefaultListModel();
        for (I i : branches) {
            model.addElement(i);
        }
        panel.list.setModel(model);        
    }
    
    public List<I> getSelectedBranches() {
        List<I> ret = new ArrayList<I>(panel.list.getModel().getSize());
        for (int i = 0; i < panel.list.getModel().getSize(); i++) {
            I item = (I)panel.list.getModel().getElementAt(i);
            if(item.isSelected) {
                ret.add(item);
            }
        }
        return ret;
    }

    @Override
    public void valueChanged (ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {
            if (e.getSource() == panel.list.getSelectionModel()) {
                changeSupport.fireChange();
            }
        }
    }

    public void setEnabled(boolean b) {
        panel.list.setEnabled(b);
        panel.titleLabel.setEnabled(b);
    }

    public boolean isEmpty() {
        return panel.list.getModel().getSize() == 0;
    }
    
    private void attachListeners () {
        panel.list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked (MouseEvent e) {
                switchSelection(panel.list.locationToIndex(e.getPoint()));
            }
        });
        panel.list.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased (KeyEvent e) {
                if(e.getKeyCode() == KeyEvent.VK_SPACE) {
                    switchSelection(panel.list.getSelectedIndex());
                }
            }
        });
    };   
    
    private void switchSelection(int index) {
        if (index != -1) {
            Item item = (Item) panel.list.getModel().getElementAt(index);
            item.isSelected = !item.isSelected;
            panel.list.repaint();
            changeSupport.fireChange();
        }
    }
    
    public class ItemRenderer implements ListCellRenderer {
        private JCheckBox renderer = new JCheckBox();
        private Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);
        
        public ItemRenderer() {
            renderer.setBorder(noFocusBorder);
        }
        
        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            renderer.setBackground(list.getBackground());
            renderer.setForeground(list.getForeground());
            renderer.setEnabled(list.isEnabled());
            renderer.setFont(list.getFont());
            renderer.setFocusPainted(false);
            renderer.setBorderPainted(true);
            
            if(value instanceof ItemSelector.Item) {
                Item item = (Item) value;
                renderer.setText("<html>" + item.getText() + "</html>");
                renderer.setToolTipText(item.getTooltipText());
                renderer.setSelected(item.isSelected);
            }
            renderer.setBorder(isSelected ? UIManager.getBorder("List.focusCellHighlightBorder") : noFocusBorder);
            return renderer;
        }
    }
    
    
    public abstract static class Item implements Comparable<Item> {
        boolean isSelected;
        public abstract String getText();
        public abstract String getTooltipText();
    }
    
}
