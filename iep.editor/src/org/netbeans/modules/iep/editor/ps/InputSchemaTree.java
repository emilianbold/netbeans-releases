/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.iep.editor.ps;

import org.netbeans.modules.iep.model.IEPModel;
import org.netbeans.modules.iep.model.OperatorComponent;

import java.awt.Component;
import java.awt.dnd.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
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
    
    public InputSchemaTree(IEPModel model, OperatorComponent component) {
        this(model, new InputSchemaTreeModel(new DefaultMutableTreeNode("root"), model, component));
    }
    
    public InputSchemaTree(IEPModel model, TreeModel treeModel) {
        super(treeModel);
        mDragAction = 1;
        mDragSource = DragSource.getDefaultDragSource();
        mDragSource.createDefaultDragGestureRecognizer(this, mDragAction, new MyDragGestureListener());
        setRootVisible(false);
        setEditable(false);
        setShowsRootHandles(true);
        getSelectionModel().setSelectionMode(1);
        setCellRenderer(new MyTreeCellRenderer());
        
        //accessibility
        getAccessibleContext().setAccessibleName(NbBundle.getMessage(InputSchemaTree.class,
                "ACSN_InputSchemaTree"));
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(InputSchemaTree.class,
                "ACSD_InputSchemaTree"));
        
    }
    
    public boolean isEmpty() {
	return ((InputSchemaTreeModel)getModel()).isEmpty();
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
                        lab.setText(ai.getColumnMetadata().getAttributeName());
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
	    if (e.getDragAction() > 0) {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) getLastSelectedPathComponent();
		if (node != null) {
		    Object o = node.getUserObject();
		    if (!(o instanceof AttributeInfo) && !(o instanceof String)) {
			return;
		    }
		    java.awt.datatransfer.Transferable transferable = new AttributeInfoTransferable(
			    new Object[] { o });
		    try {
			e.startDrag(DragSource.DefaultCopyNoDrop, transferable,
				new MyDragSourceListener());
		    } catch (InvalidDnDOperationException idoe) {
			InputSchemaTree.mLog.log(Level.SEVERE, idoe
				.getMessage(), idoe);
		    }
		}
	    }
	}

    }
    
}