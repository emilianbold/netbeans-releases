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

import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;
import javax.swing.AbstractCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JTree;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;

import org.netbeans.modules.iep.editor.xsd.CheckBoxPanel;
import org.netbeans.modules.iep.editor.xsd.nodes.SelectableTreeNode;
import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xml.axi.AXIType;


/**
 *
 * @author radval
 */
public class SchemaTreeCellEditor extends AbstractCellEditor implements TreeCellEditor { 

    private JTree mTree;
    
    private TreeCellRenderer mRenderer;
    
    private SelectableTreeNode mCellValue;
    
    private List<String> mExistingArtificatNames = new ArrayList<String>();
    private List<AXIComponent> mSelectedSchemas = new ArrayList<AXIComponent>();
    private List<AXIComponent> mRemovedSchemas = new ArrayList<AXIComponent>();
    
    public SchemaTreeCellEditor(JTree tree,
                                TreeCellRenderer renderer,
                                List<String> existingArtificatNames) { 
        this.mTree = tree;
        this.mRenderer = renderer;
        this.mExistingArtificatNames = existingArtificatNames;
    }

    
    public Component getTreeCellEditorComponent(JTree tree, Object value, boolean isSelected, boolean expanded, boolean leaf, int row) {
        Component editor = this.mRenderer.getTreeCellRendererComponent(tree, value,
        true, expanded, leaf, row, true);

        if(value instanceof SelectableTreeNode) {
            mCellValue = (SelectableTreeNode) value;
            
            if (editor instanceof CheckBoxPanel) {
                CheckBoxPanel checkBox = (CheckBoxPanel) editor;
                // editor always selected and focused
                ItemListener itemListener = new CheckBoxItemListener(mCellValue, checkBox.getCheckBox());
                checkBox.getCheckBox().addItemListener(itemListener);
            }
        }

        return editor;
    }

    public List<AXIComponent> getSelectedSchemas() {
	return mSelectedSchemas;
    }
    
    public List<AXIComponent> getRemovedSchemas() {
	return mRemovedSchemas;
    }
    
    @Override
    public boolean shouldSelectCell(EventObject anEvent) { 
    return true; 
    }

    @Override
    public boolean isCellEditable(EventObject event) {
        boolean returnValue = false;
        if (event instanceof MouseEvent) {
          MouseEvent mouseEvent = (MouseEvent) event;
          TreePath path = mTree.getPathForLocation(mouseEvent.getX(),
              mouseEvent.getY());
          if (path != null) {
            Object node = path.getLastPathComponent();
            if ((node != null) && (node instanceof SelectableTreeNode)) {
              returnValue = true;
            }
          }
        }
        
        return returnValue;
    }

    public Object getCellEditorValue() {
        return mCellValue.getUserObject();
    }
    
    
    class CheckBoxItemListener implements ItemListener {
        
        private SelectableTreeNode mNode;
        
        private JCheckBox mCheckBox;
        public CheckBoxItemListener(SelectableTreeNode node, JCheckBox checkBox) {
            this.mNode = node;
            this.mCheckBox = checkBox;
        }
        
        public void itemStateChanged(ItemEvent itemEvent) {
            mNode.setSelected(itemEvent.getStateChange() == ItemEvent.SELECTED ? true : false );
            mCheckBox.removeItemListener(this);
            stopCellEditing();
            
            AXIComponent comp = (AXIComponent) mNode.getUserObject();
            AXIType compType = (AXIType) comp;
            String name = compType.getName();
            //Object comp =  mNode.getUserObject();
            
            if(mNode.isSelected()) {
                if(!mExistingArtificatNames.contains(name)) {
                    //mExistingArtificatNames.add(comp);
                    mSelectedSchemas.add(comp);
                }
            } else {
        	mRemovedSchemas.add(comp);
            }
                
            
          }
        
    }
}
