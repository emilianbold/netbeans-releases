/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */


package org.netbeans.modules.jumpto.quicksearch;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;
import javax.swing.AbstractListModel;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.border.EmptyBorder;
import org.netbeans.modules.jumpto.quicksearch.CommandEval;
import org.netbeans.spi.jumpto.quicksearch.SearchResult;
import org.netbeans.spi.jumpto.quicksearch.SearchResultGroup;

/**
 *
 * @author  Jan Becicka
 */
public class QuickSearchPopup extends javax.swing.JPanel {

    
    /** Creates new form SilverPopup */
    public QuickSearchPopup() {
        initComponents();
        jList1.setCellRenderer(new CommandRenderer());
    }

    void invoke() {
        ((SearchResult) jList1.getModel().getElementAt(jList1.getSelectedIndex())).invoke();
    }

    void selectNext() {
        jList1.setSelectedIndex(jList1.getSelectedIndex()+1);
    }
    
    void selectPrev() {
        jList1.setSelectedIndex(jList1.getSelectedIndex()-1);
    }

    void update(String text) {
        jList1.setModel(new SearchModel(text));
    }
    
    public class CommandRenderer extends JLabel implements ListCellRenderer {

        public Component getListCellRendererComponent(JList list, Object value, int index,
                boolean isSelected, boolean cellHasFocus) {
            
            if (value==null) {
                JPanel c = new JPanel();
                c.setBackground(Color.GRAY);
                c.setPreferredSize(new Dimension(c.getPreferredSize().width, 1));
                return c;
            }
            JLabel jLabel1 = new javax.swing.JLabel();

            JPanel panel = new JPanel();
            jLabel1.setText("XXXXXXXXXXXXXX");
            jLabel1.setFont(jLabel1.getFont().deriveFont(Font.BOLD));
            jLabel1.setBorder(new EmptyBorder(0,5,0,0));
            
            panel.setLayout(new java.awt.BorderLayout());
            jLabel1.setOpaque(true);
            panel.add(jLabel1, BorderLayout.WEST);
            jLabel1.setPreferredSize(new JLabel("XXXXXXXXXXXXX").getPreferredSize());
            jLabel1.setForeground(Color.GRAY);

            Component c = null;
            if (value == null) {
                c = new JPanel();
            } else if (value instanceof Component) {
                c = (Component) value;
            } else if (value instanceof SearchResult) {
                JLabel p = new JLabel(((SearchResult) value).getDisplayName());
                p.setBorder(new EmptyBorder(0,5,0,0));
                if (isFirst((SearchResult) value)) {
                    jLabel1.setText(((SearchResult) value).getResultGroup().getCategory());
                } else {
                    jLabel1.setText("");
                }
                c=p;
            } else {
                c = new JLabel(value.toString());
            }

            panel.add(c, BorderLayout.CENTER);

            if (isSelected) {
                c.setBackground(list.getSelectionBackground());
                c.setForeground(list.getSelectionForeground());
            } else {
                c.setBackground(list.getBackground());
                c.setForeground(list.getForeground());
            }
            ((JComponent)c).setOpaque(true);

            panel.setOpaque(true);

            return panel;
        }
        
        private boolean isFirst(SearchResult result) {
            return result.equals(result.getResultGroup().getItems().iterator().next());
            //TODO: this is horrible hack. Should be rewritten.
        }
    }

    private class SearchModel extends AbstractListModel {
        private Iterable<? extends SearchResultGroup> results;
        private ArrayList ar = new ArrayList();
        public SearchModel(String text) {
            results = CommandEval.evaluate(text);
            for (SearchResultGroup cr:results) {
                for (SearchResult c:cr.getItems()) {
                    ar.add(c);
                }
                ar.add(null);
            }
        }

        public int getSize() {
            int size = 0;
            for (SearchResultGroup cr:results) {
                size+=cr.getSize();
                size++;
            }
            return size-1;
        }

        public Object getElementAt(int arg0) {
            return ar.get(arg0);
        }
    }
    

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList();

        jScrollPane1.setViewportView(jList1);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JList jList1;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables

}
