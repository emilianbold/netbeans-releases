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

/*
 * MethodCheckedNodeRenderer.java
 *
 */

package org.netbeans.modules.mobility.end2end.ui.treeview;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ItemListener;
import java.beans.BeanInfo;
import java.io.CharConversionException;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.tree.TreeCellRenderer;
import org.netbeans.modules.mobility.end2end.util.ServiceNodeManager;
import org.openide.explorer.view.Visualizer;
import org.openide.nodes.Node;
import org.openide.xml.XMLUtil;

/**
 *
 */
public class MethodCheckedNodeRenderer extends JPanel implements TreeCellRenderer {

    private static final Color selectionForeground = UIManager.getColor("Tree.selectionForeground"); //NOI18N
    private static final Color selectionBackground = UIManager.getColor("Tree.selectionBackground"); //NOI18N
    private static final Color textForeground = UIManager.getColor("Tree.textForeground"); //NOI18N

    /** Creates new form MethodCheckedNodeRenderer */
    public MethodCheckedNodeRenderer() {
        Font fontValue = UIManager.getFont("Tree.font"); //NOI18N
        if (fontValue != null) {
            setFont(fontValue);
        }
        initComponents();
        Boolean booleanValue = (Boolean) UIManager.get("Tree.drawsFocusBorderAroundIcon"); //NOI18N
        setFocusPainted((booleanValue != null) && (booleanValue.booleanValue()));
    }
 
    public Component getTreeCellRendererComponent(final JTree tree, final Object value, final boolean selected, final boolean expanded, final boolean leaf, final int row, final boolean hasFocus) {
        final Node node = Visualizer.findNode(value);
        setText(node.getDisplayName());
        setEnabled(tree.isEnabled());
        if (selected) {
            setForeground(selectionForeground, textForeground);
            setBackground(selectionBackground, tree.getBackground());
        } else {
            setForeground(textForeground);
            setBackground(tree.getBackground());
        }
        setState((MultiStateCheckBox.State)node.getValue(ServiceNodeManager.NODE_SELECTION_ATTRIBUTE));
        Boolean val = (Boolean)node.getValue(ServiceNodeManager.NODE_VALIDITY_ATTRIBUTE);
        if (val != null && !val) try {
            setText("<html><s>" + XMLUtil.toAttributeValue(node.getDisplayName()) + "</s></html>");
            setEnabled(false);
        } catch (CharConversionException cce) {} else {
            setEnabled(true);
        }

        setIcon(new ImageIcon(node.getIcon(BeanInfo.ICON_COLOR_16x16)));
        return this;
    }

    public void setForeground(final Color fg) {
        setForeground(fg, fg);
    }

    public void setBackground(final Color bg) {
        setBackground(bg, bg);
    }

    public int getCheckBoxWidth() {
        return jCheckBox1.getWidth();
    }
    
    public void setForeground(final Color selection, final Color text) {
        if (jCheckBox1 == null || jLabel1 == null) {
            return;
        }
        jCheckBox1.setForeground(text);
        jLabel1.setForeground(selection);
        super.setForeground(selection);
    }

    public void setBackground(final Color selection, final Color text) {
        if (jCheckBox1 == null || jLabel1 == null) {
            return;
        }
        jCheckBox1.setBackground(text);
        jLabel1.setBackground(selection);
        super.setBackground(selection);
    }

    public void setFocusPainted(final boolean painted) {
        jCheckBox1.setFocusPainted(painted);
    }

    public void setText(final String text) {
        jLabel1.setText(text);
    }
    
    public String getText() {
        return jLabel1.getText();
    }

    public MultiStateCheckBox.State getState() {
        return jCheckBox1.getState();
    }

    public void setState(final MultiStateCheckBox.State state) {
        jCheckBox1.setState(state);
    }

    public void setIcon(final Icon icon) {
        jLabel1.setIcon(icon);
    }

    @Override
    public void setEnabled(final boolean enabled) {        
        jCheckBox1.setEnabled(enabled);
        if (!enabled) {            
            jCheckBox1.setState(MultiStateCheckBox.State.UNSELECTED);
        }
    }

    public void addItemListener(final ItemListener itemListener) {
        jCheckBox1.addItemListener(itemListener);
    }

    public void removeItemListener(final ItemListener itemListener) {
        jCheckBox1.removeItemListener(itemListener);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jCheckBox1 = new org.netbeans.modules.mobility.end2end.ui.treeview.MultiStateCheckBox();
        jLabel1 = new javax.swing.JLabel();

        setLayout(new java.awt.BorderLayout(5, 0));

        jCheckBox1.setBorder(null);
        add(jCheckBox1, java.awt.BorderLayout.WEST);
        add(jLabel1, java.awt.BorderLayout.EAST);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private org.netbeans.modules.mobility.end2end.ui.treeview.MultiStateCheckBox jCheckBox1;
    private javax.swing.JLabel jLabel1;
    // End of variables declaration//GEN-END:variables
}