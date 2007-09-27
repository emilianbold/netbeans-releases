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

package org.netbeans.modules.iep.editor.ps;

import org.netbeans.modules.iep.editor.model.AttributeMetadata;
import org.netbeans.modules.iep.editor.model.Plan;
import org.netbeans.modules.iep.editor.tcg.model.TcgComponent;
import java.awt.Component;
import java.awt.dnd.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.*;
import org.openide.util.NbBundle;

/**
 * InputSchemaTree.java
 *
 * Created on November 1, 2006, 1:52 PM
 *
 * @author Bing Lu
 */
public class InputSchemaTree extends JTree {
    private static final Logger mLog = Logger.getLogger(InputSchemaTree.class.getName());
    private DragSource mDragSource;
    private int mDragAction;
    
    private void expandTree() {
        int k = getRowCount() - 1;
        for(int i = k; i >= 0; i--) {
            expandRow(i);
        }
    }
    
    public void addNotify() {
        super.addNotify();
        expandTree();
    }
    
    public InputSchemaTree(Plan plan, TcgComponent component) {
        super(new InputSchemaTreeModel(new DefaultMutableTreeNode("root"), plan, component));
        mDragAction = 1;
        mDragSource = DragSource.getDefaultDragSource();
        mDragSource.createDefaultDragGestureRecognizer(this, mDragAction, new MyDragGestureListener());
        setRootVisible(false);
        setEditable(false);
        setShowsRootHandles(true);
        getSelectionModel().setSelectionMode(1);
        setCellRenderer(new MyTreeCellRenderer());
    }
    
    class MyTreeCellRenderer extends DefaultTreeCellRenderer {
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            JLabel lab = (JLabel)super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
            if(value instanceof DefaultMutableTreeNode) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
                Object obj = node.getUserObject();
                if(obj instanceof String) {
                    lab.setText((String)obj);
                } else {
                    if(obj instanceof AttributeInfo) {
                        AttributeInfo ai = (AttributeInfo)obj;
                        lab.setText(ai.getColumnMetadata().getName());
                    } else {
                        lab.setText(node.toString());
                    }
                }
            }
            return lab;
        }
    }
    
    class MyDragSourceListener extends DragSourceAdapter {
        public void dragDropEnd(DragSourceDropEvent e) {
            if(!e.getDropSuccess()) {
                mLog.warning(NbBundle.getMessage(InputSchemaTree.class,
                             "InputSchemaTree.DRAG_FAILED", 
                             "" + e.getDropAction(), 
                             "" + e.getDragSourceContext().getSourceActions()));
                return;
            } else {
                return;
            }
        }
        
        public void dragEnter(DragSourceDragEvent e) {
            DragSourceContext context = e.getDragSourceContext();
            if((e.getDropAction() & mDragAction) != 0) {
                context.setCursor(DragSource.DefaultCopyDrop);
            } else {
                context.setCursor(DragSource.DefaultCopyNoDrop);
            }
        }
        
        public void dropActionChanged(DragSourceDragEvent e) {
            DragSourceContext context = e.getDragSourceContext();
            context.setCursor(DragSource.DefaultCopyNoDrop);
        }
        
    }
    
    class MyDragGestureListener implements DragGestureListener {
        
        public void dragGestureRecognized(DragGestureEvent e) {
            if(e.getDragAction() > 0) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode)getLastSelectedPathComponent();
                Object o = node.getUserObject();
                if(!(o instanceof AttributeInfo) && !(o instanceof String)) {
                    return;
                }
                java.awt.datatransfer.Transferable transferable = new AttributeInfoTransferable(new Object[] {
                    o
                });
                try {
                    e.startDrag(DragSource.DefaultCopyNoDrop, transferable, new MyDragSourceListener());
                } catch(InvalidDnDOperationException idoe) {
                    InputSchemaTree.mLog.log(Level.SEVERE, idoe.getMessage(), idoe);
                }
            }
        }
        
    }
    
}