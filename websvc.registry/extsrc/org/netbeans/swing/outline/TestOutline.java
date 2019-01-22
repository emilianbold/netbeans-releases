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

package org.netbeans.swing.outline;

import java.awt.BorderLayout;
import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.table.TableModel;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import org.openide.util.Utilities;

/** A simple test of the Outline (aka TreeTable) class which implements
 * a filesystem browser.
 *
 * 
 */
public class TestOutline extends JFrame {
    private Outline outline;
    /** Creates a new instance of Test */
    public TestOutline() {
        setDefaultCloseOperation (EXIT_ON_CLOSE);
        getContentPane().setLayout (new BorderLayout());
        
        //Use root 1 on windows to avoid making a tree of the floppy drive.
        /*
        TreeModel treeMdl = new DefaultTreeModel(
            new FileTreeNode(File.listRoots()[Utilities.isWindows() ? 1 : 0]));
         */
            
        TreeModel treeMdl = createModel();
        
        OutlineModel mdl = DefaultOutlineModel.createOutlineModel(treeMdl, 
            new FileRowModel(), true);
        
        outline = new Outline();
        
        outline.setRenderDataProvider(new RenderData()); 
        
        outline.setRootVisible (true);
        
        outline.setModel (mdl);
        
        
        getContentPane().add(new JScrollPane(outline), BorderLayout.CENTER);
        setBounds (20, 20, 700, 400);
    }
    
    /** A handy method to create a model to install into a JTree to compare
     * behavior of a real JTree's layout cache and ours */
    public static TreeModel createModel() {
//        TreeModel treeMdl = /*new DefaultTreeModel(
//            new FileTreeNode(File.listRoots()[Utilities.isWindows() ? 1 : 0]));
                           
        TreeModel treeMdl = new FileTreeModel (
            File.listRoots()[Utilities.isWindows() ? 1 : 0]);
        return treeMdl;
    }
    
    public static void main(String[] ignored) {
        try {
           //UIManager.setLookAndFeel (new javax.swing.plaf.metal.MetalLookAndFeel());
        } catch (Exception e) {}
        
        new TestOutline().show();
    }
    
    private class FileRowModel implements RowModel {
        
        public Class getColumnClass(int column) {
            switch (column) {
                case 0 : return Date.class;
                case 1 : return Long.class;
                default : assert false;
            }
            return null;
        }
        
        public int getColumnCount() {
            return 2;
        }
        
        public String getColumnName(int column) {
            return column == 0 ? "Date" : "Size";
        }
        
        public Object getValueFor(Object node, int column) {
            File f = (File) node;
            switch (column) {
                case 0 : return new Date (f.lastModified());
                case 1 : return new Long (f.length());
                default : assert false;
            }
            return null;
        }
        
        public boolean isCellEditable(Object node, int column) {
            return false;
        }
        
        public void setValueFor(Object node, int column, Object value) {
            //do nothing for now
        }
        
    }
    
    
    private class RenderData implements RenderDataProvider {
        
        public java.awt.Color getBackground(Object o) {
            return null;
        }
        
        public String getDisplayName(Object o) {
            return ((File) o).getName();
        }
        
        public java.awt.Color getForeground(Object o) {
            File f = (File) o;
            if (!f.isDirectory() && !f.canWrite()) {
                return UIManager.getColor ("controlShadow");
            }
            return null;
        }
        
        public javax.swing.Icon getIcon(Object o) {
            return null;
        
        }
        
        public String getTooltipText(Object o) {
            File f = (File) o;
            return f.getAbsolutePath();
        }
        
        public boolean isHtmlDisplayName(Object o) {
            return false;
        }
        
    }
    
    private static class FileTreeModel implements TreeModel {
        private File root;
        public FileTreeModel (File root) {
            this.root = root;
        }
        
        public void addTreeModelListener(javax.swing.event.TreeModelListener l) {
            //do nothing
        }
        
        public Object getChild(Object parent, int index) {
            File f = (File) parent;
            return f.listFiles()[index];
        }
        
        public int getChildCount(Object parent) {
            File f = (File) parent;
            if (!f.isDirectory()) {
                return 0;
            } else {
                return f.list().length;
            }
        }
        
        public int getIndexOfChild(Object parent, Object child) {
            File par = (File) parent;
            File ch = (File) child;
            return Arrays.asList(par.listFiles()).indexOf(ch);
        }
        
        public Object getRoot() {
            return root;
        }
        
        public boolean isLeaf(Object node) {
            File f = (File) node;
            return !f.isDirectory();
        }
        
        public void removeTreeModelListener(javax.swing.event.TreeModelListener l) {
            //do nothing
        }
        
        public void valueForPathChanged(javax.swing.tree.TreePath path, Object newValue) {
            //do nothing
        }
    }
}
