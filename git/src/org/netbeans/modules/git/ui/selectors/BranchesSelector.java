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
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
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
import org.netbeans.libs.git.GitBranch;
import org.openide.awt.Mnemonics;
import org.openide.util.ChangeSupport;

/**
 *
 * @author Tomas Stupka
 */
public class BranchesSelector implements ListSelectionListener {
    private BranchesPanel panel;
    private ChangeSupport changeSupport = new ChangeSupport(this);

    public BranchesSelector(String title) {
        panel = new BranchesPanel();
        Mnemonics.setLocalizedText(panel.titleLabel, title); 
        panel.branchesList.setCellRenderer(new BranchRenderer());
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

    public void setBranches(Collection<GitBranch> branches) {
        ArrayList<GitBranch> l = new ArrayList<GitBranch>(branches);
        Collections.sort(l, new Comparator<GitBranch>() {
            @Override
            public int compare (GitBranch b1, GitBranch b2) {
                return b1.getName().compareTo(b2.getName());
            }
        });
        DefaultListModel model = new DefaultListModel();
        for (GitBranch branch : l) {
            model.addElement(new Branch(branch));
        }
        panel.branchesList.setModel(model);        
    }
    
    public List<GitBranch> getSelectedBranches() {
        List<GitBranch> ret = new ArrayList<GitBranch>(panel.branchesList.getModel().getSize());
        for (int i = 0; i < panel.branchesList.getModel().getSize(); i++) {
            Branch b = (Branch)panel.branchesList.getModel().getElementAt(i);
            if(b.isSelected()) {
                ret.add(b);
            }
        }
        return ret;
    }

    @Override
    public void valueChanged (ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {
            if (e.getSource() == panel.branchesList.getSelectionModel()) {
                changeSupport.fireChange();
            }
        }
    }

    public void setEnabled(boolean b) {
        panel.branchesList.setEnabled(b);
        panel.titleLabel.setEnabled(b);
    }

    public boolean isEmpty() {
        return panel.branchesList.getModel().getSize() == 0;
    }
    
    public static class Branch implements GitBranch, Comparable<Branch> {
        private final GitBranch branch;
        boolean selected = false;

        private Branch(GitBranch branch) {
            this.branch = branch;
        }

        @Override
        public int compareTo(Branch b) {
            if(b == null) return 1;
            return getName().compareTo(b.getName());
        }

        public String getName() {
            return branch.getName();
        }

        @Override
        public boolean isActive () {
           return branch.isActive();
        }

        @Override
        public boolean isRemote() {
            return branch.isRemote();
        }

        @Override
        public String getId() {
            return branch.getId();
        }

        private boolean isSelected () {
           return selected;
        }

        private void setSelected(boolean selected) {
            this.selected = selected;
        }
    }
   
    private void attachListeners () {
        panel.branchesList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked (MouseEvent e) {
                switchSelection(panel.branchesList.locationToIndex(e.getPoint()));
            }
        });
        panel.branchesList.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased (KeyEvent e) {
                if(e.getKeyCode() == KeyEvent.VK_SPACE) {
                    switchSelection(panel.branchesList.getSelectedIndex());
                }
            }
        });
    };   
    
    private void switchSelection(int index) {
        if (index != -1) {
            Branch branch = (Branch) panel.branchesList.getModel().getElementAt(index);
            branch.setSelected(!branch.isSelected());
            panel.branchesList.repaint();
            changeSupport.fireChange();
        }
    }
    
    private static class BranchRenderer implements ListCellRenderer {
        private JCheckBox renderer = new JCheckBox();
        private static Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);
        
        public BranchRenderer() {
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
            
            if(value instanceof Branch) {
                Branch b = (Branch) value;
                renderer.setText(b.getName() + (b.isActive() ? "*" : ""));
                renderer.setSelected(b.isSelected());
            }
            renderer.setBorder(isSelected ? UIManager.getBorder("List.focusCellHighlightBorder") : noFocusBorder);
            return renderer;
        }
        
    }
    
}
