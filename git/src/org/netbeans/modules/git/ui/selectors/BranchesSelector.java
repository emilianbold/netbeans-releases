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
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeListener;
import org.netbeans.libs.git.GitBranch;
import org.openide.util.ChangeSupport;

/**
 *
 * @author Tomas Stupka
 */
public class BranchesSelector {
    private BranchesPanel panel;
    private ChangeSupport changeSupport = new ChangeSupport(this);

    public BranchesSelector(String title) {
        panel = new BranchesPanel();
        panel.titleLabel.setText(title);
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

    public void setBranches(List<Branch> branches) {
        final DefaultListModel model = new DefaultListModel();
        for (Branch branch : branches) {
            model.addElement(branch);
        }
        panel.branchesList.setModel(model);        
    }
    
    public List<Branch> getSelectedBranches() {
        List<Branch> ret = new ArrayList<Branch>(panel.branchesList.getModel().getSize());
        for (int i = 0; i < panel.branchesList.getModel().getSize(); i++) {
            Branch b = (Branch)panel.branchesList.getModel().getElementAt(i);
            if(b.isSelected()) {
                ret.add(b);
            }
        }
        return ret;
    }
   
    public static class Branch implements Comparable<Branch> {
        private static final String REF_SPEC_PATTERN = "+refs/heads/{0}:refs/remotes/{1}/{0}"; //NOI18N
        private final GitBranch branch;
        boolean selected = false;

        public Branch(GitBranch branch) {
            this.branch = branch;
        }

        @Override
        public int compareTo(Branch b) {
            if(b == null) return 1;
            return getName().compareTo(b.getName());
        }

        public String getName() {
            return branch.getName() + (isActive() ? "*" : "");
        }
        
        public String getRefSpec(String remoteName) {
            return MessageFormat.format(REF_SPEC_PATTERN, getName(), remoteName);
        }

        private boolean isSelected () {
           return selected;
        }
        
        public boolean isActive () {
           return branch.isActive();
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
                renderer.setText(b.getName());
                renderer.setSelected(b.isSelected());
            }
            return renderer;
        }
        
    }
    
}
