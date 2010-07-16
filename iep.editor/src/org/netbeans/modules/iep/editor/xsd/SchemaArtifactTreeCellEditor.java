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
package org.netbeans.modules.iep.editor.xsd;

import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;
import javax.swing.AbstractCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JTree;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;
import org.netbeans.modules.iep.editor.xsd.nodes.SelectableTreeNode;


/**
 *
 * @author radval
 */
public class SchemaArtifactTreeCellEditor extends AbstractCellEditor implements TreeCellEditor { 

    public static final String PROP_SELECTED_NODES = "PROP_SELECTED_NODES";
    
    private JTree mTree;
    
    private TreeCellRenderer mRenderer;
    
    private SelectableTreeNode mCellValue;
    
    private List mExistingArtificatNames = new ArrayList();
    
    private PropertyChangeEvent propChangeEvent;
    
    public SchemaArtifactTreeCellEditor(JTree tree,
                                TreeCellRenderer renderer,
                                List existingArtificatNames) { 
        this.mTree = tree;
        this.mRenderer = renderer;
        this.mExistingArtificatNames = existingArtificatNames;
        
        KeyListener keyListener = new TreeKeyListener();
        tree.addKeyListener(keyListener);
    }
    
    public void setExistingArtifactList(List existingArtifactList) {
        this.mExistingArtificatNames = existingArtifactList;
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
            mNode.setSelected(mCheckBox.isSelected());
            mCheckBox.removeItemListener(this);
            stopCellEditing();
            
            List oldNames = new ArrayList();
            oldNames.addAll(mExistingArtificatNames);
            
            //AXIComponent comp = (AXIComponent) mNode.getUserObject();
            Object comp =  mNode.getUserObject();
            
            if(mNode.isSelected()) {
                if(!mExistingArtificatNames.contains(comp)) {
                    mExistingArtificatNames.add(comp);
                }
            } else {
                mExistingArtificatNames.remove(comp);
            }
                
            firePropertyChangeListener(PROP_SELECTED_NODES, oldNames, mExistingArtificatNames);
            
          }
        
    }
    
    class TreeKeyListener implements KeyListener {
        
        public TreeKeyListener() {
        }
        
        public void keyPressed(KeyEvent e) {
            // TODO Auto-generated method stub
            
        }

        public void keyReleased(KeyEvent e) {
            if(e.getKeyCode() == KeyEvent.VK_SPACE) {
                
                TreePath path = mTree.getSelectionPath();
                if(path != null) {
                    Object node = path.getLastPathComponent();
                    if ((node != null) && !(node instanceof SelectableTreeNode)) {
                        return;
                    }
                    
                    SelectableTreeNode mNode = (SelectableTreeNode) node;
                    mNode.setSelected(!mNode.isSelected());
                    
                    List oldNames = new ArrayList();
                    oldNames.addAll(mExistingArtificatNames);
                    
                    //AXIComponent comp = (AXIComponent) mNode.getUserObject();
                    Object comp =  mNode.getUserObject();
                    
                    if(mNode.isSelected()) {
                        if(!mExistingArtificatNames.contains(comp)) {
                            mExistingArtificatNames.add(comp);
                        }
                    } else {
                        mExistingArtificatNames.remove(comp);
                    }
                    
                    mTree.repaint();
                    firePropertyChangeListener(PROP_SELECTED_NODES, oldNames, mExistingArtificatNames);
                }
            }
            
        }

        public void keyTyped(KeyEvent e) {
            // TODO Auto-generated method stub
            
        }
        
    }
    
    public void addPropertyChangeListener(PropertyChangeListener l) {
        listenerList.add(PropertyChangeListener.class, l);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener l) {
        listenerList.add(PropertyChangeListener.class, l);
    }
    
    protected void firePropertyChangeListener(String propName, Object oldVal, Object newVal) {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==PropertyChangeListener.class) {
            // Lazily create the event:
            if (changeEvent == null)
                propChangeEvent = new PropertyChangeEvent(this, propName, oldVal, newVal);
            ((PropertyChangeListener)listeners[i+1]).propertyChange(propChangeEvent);
            }          
        }
        }
}
