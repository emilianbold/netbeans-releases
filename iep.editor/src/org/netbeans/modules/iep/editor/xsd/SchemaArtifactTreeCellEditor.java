/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.iep.editor.xsd;

import java.awt.Color;
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
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;
import org.netbeans.modules.iep.editor.xsd.nodes.SelectableTreeNode;
import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xml.axi.AXIType;


/**
 *
 * @author radval
 */
public class SchemaArtifactTreeCellEditor extends AbstractCellEditor implements TreeCellEditor { 

    private JTree mTree;
    
    private TreeCellRenderer mRenderer;
    
    private SelectableTreeNode mCellValue;
    
    private List mExistingArtificatNames = new ArrayList();
    
    public SchemaArtifactTreeCellEditor(JTree tree,
                                        TreeCellRenderer renderer,
                                        List existingArtificatNames) { 
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
            
            //AXIComponent comp = (AXIComponent) mNode.getUserObject();
            Object comp =  mNode.getUserObject();
            
            if(mNode.isSelected()) {
                if(!mExistingArtificatNames.contains(comp)) {
                    mExistingArtificatNames.add(comp);
                }
            } else {
                mExistingArtificatNames.remove(comp);
            }
                
            
          }
        
    }
}
