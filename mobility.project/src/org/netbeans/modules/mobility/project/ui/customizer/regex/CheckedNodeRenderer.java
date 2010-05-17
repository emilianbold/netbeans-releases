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

package org.netbeans.modules.mobility.project.ui.customizer.regex;
import org.openide.explorer.view.Visualizer;
import org.openide.nodes.Node;

import javax.swing.*;
import javax.swing.tree.TreeCellRenderer;
import java.awt.*;
import java.awt.event.ItemListener;
import java.beans.BeanInfo;

/**
 * User: suchys
 * Date: Dec 20, 2003
 * Time: 12:30:14 PM
 */
public class CheckedNodeRenderer implements TreeCellRenderer {
    private RendererComponent customRenderer = null;
    private Object defaultRenderer = null;
    private CheckedTreeBeanView storage;
    private Color selectionForeground, selectionBackground, textForeground;//, textBackground;
    
    protected RendererComponent getRenderer(){
        return customRenderer;
    }
    
    public CheckedNodeRenderer(Object defaultRenderer) {
        this.defaultRenderer = defaultRenderer;
        selectionForeground = UIManager.getColor("Tree.selectionForeground"); //NOI18N
        selectionBackground = UIManager.getColor("Tree.selectionBackground"); //NOI18N
        textForeground = UIManager.getColor("Tree.textForeground"); //NOI18N
//        textBackground = UIManager.getColor("Tree.textBackground");
        customRenderer = new RendererComponent();
        Font fontValue = UIManager.getFont("Tree.font"); //NOI18N
        if (fontValue != null){
            customRenderer.setFont(fontValue);
        }
        Boolean booleanValue = (Boolean)UIManager.get("Tree.drawsFocusBorderAroundIcon"); //NOI18N
        customRenderer.setFocusPainted((booleanValue != null) && (booleanValue.booleanValue()));
    }
    
    public CheckedNodeRenderer() {
        this(null);
    }
    
    public void setContentStorage(final CheckedTreeBeanView storage) {
        this.storage = storage;
    }
    
    public Component getTreeCellRendererComponent(final JTree tree, final Object value,
            final boolean selected, final boolean expanded,
            final boolean leaf, final int row, final boolean hasFocus) {
        Component returnValue = null;
        final Node node = Visualizer.findNode(value);
        final FileObjectCookie doj = (FileObjectCookie) node.getCookie(FileObjectCookie.class);
        if (doj != null){
            //String stringValue = tree.convertValueToText(value, selected, expanded, leaf, row, false);
            customRenderer.setEnabled(tree.isEnabled());
            
            if (selected){
                customRenderer.setForeground(selectionForeground, textForeground);
                customRenderer.setBackground(selectionBackground, tree.getBackground());
            } else {
                customRenderer.setForeground(textForeground);
                customRenderer.setBackground(tree.getBackground());
            }
            customRenderer.setText(node.getDisplayName());
            if (doj != null){
                if (storage == null)
                    customRenderer.setState(CheckedTreeBeanView.UNSELECTED);
                else {
                    final Object state = storage.getState(doj.getFileObject());
                    customRenderer.setState(state == null ? CheckedTreeBeanView.UNSELECTED : state);
                }
            }
            
            customRenderer.setIcon(new ImageIcon(node.getIcon(BeanInfo.ICON_COLOR_16x16)));
            return customRenderer;
        } 
        if (defaultRenderer != null)
            returnValue = ((TreeCellRenderer)defaultRenderer).getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
        return returnValue;
    }
    
    static class RendererComponent extends JPanel{
        MultiStateCheckBox jCheckBox1;
        final private JLabel jLabel1;
        final private JLabel jLabel2;
        
        public RendererComponent() {
            jCheckBox1 = new MultiStateCheckBox();
            jCheckBox1.setBorder(null);
            jLabel1 = new JLabel();
            jLabel2 = new JLabel();
            //todo badges around icons !!!
            setLayout(new BorderLayout(5, 0));
            add(jCheckBox1, BorderLayout.WEST);
            add(jLabel1, BorderLayout.CENTER);
            add(jLabel2, BorderLayout.EAST);
        }
        
        public void setForeground(final Color fg){
            setForeground(fg, fg);
        }
        
        public void setBackground(final Color bg){
            setBackground(bg, bg);
        }
        
        public void setForeground(final Color selection, final Color text) {
            if ( jCheckBox1 == null || jLabel1 == null || jLabel2 == null) return;
            jCheckBox1.setForeground(text);
            jLabel1.setForeground(text);
            jLabel2.setForeground(selection);
            super.setForeground(selection);
        }
        
        public void setBackground(final Color selection, final Color text) {
            if ( jCheckBox1 == null || jLabel1 == null || jLabel2 == null) return;
            //System.err.println(jCheckBox1);
            jCheckBox1.setBackground(text);
            jLabel1.setBackground(text);
            jLabel2.setBackground(selection);
            super.setBackground(selection);
        }
        
        public void setFocusPainted(final boolean painted) {
            jCheckBox1.setFocusPainted(painted);
        }
        
        public void setText(final String text) {
            jLabel2.setText(text);
        }
        
        public String getText(){
            return jLabel2.getText();
        }
        
        public void setState(final Object state) {
            jCheckBox1.setState(state);
        }
        
        public void setIcon(final Icon icon){
            //System.err.println(icon);
            jLabel1.setIcon(icon);
        }
        
        public void addItemListener(final ItemListener itemListener) {
            jCheckBox1.addItemListener(itemListener);
        }
        
        public void removeItemListener(final ItemListener itemListener) {
            jCheckBox1.removeItemListener(itemListener);
        }
    }
    
}
