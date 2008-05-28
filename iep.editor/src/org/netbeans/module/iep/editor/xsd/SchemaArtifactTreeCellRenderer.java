/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.module.iep.editor.xsd;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;


import org.netbeans.module.iep.editor.xsd.nodes.SelectableTreeNode;

/**
 *
 * @author radval
 */
public class SchemaArtifactTreeCellRenderer implements TreeCellRenderer  {

    private DefaultTreeCellRenderer mDefaultRenderer;
    
    //private JCheckBox checkBoxRenderer = new JCheckBox();
    private CheckBoxPanel checkBoxRenderer = new CheckBoxPanel();
    
    Color selectionBorderColor;
    Color selectionForeground;
    Color selectionBackground;
    Color textForeground;
    Color textBackground;
    
    public SchemaArtifactTreeCellRenderer() {
        mDefaultRenderer = new DefaultTreeCellRenderer();
        
        Font fontValue;
        fontValue = UIManager.getFont("Tree.font");
        if (fontValue != null) {
          checkBoxRenderer.setFont(fontValue);
        }
        Boolean booleanValue = (Boolean) UIManager
            .get("Tree.drawsFocusBorderAroundIcon");
        checkBoxRenderer.setFocusPainted((booleanValue != null)
            && (booleanValue.booleanValue()));

        selectionBorderColor = UIManager.getColor("Tree.selectionBorderColor");
        selectionForeground = UIManager.getColor("Tree.selectionForeground");
        selectionBackground = UIManager.getColor("Tree.selectionBackground");
        textForeground = UIManager.getColor("Tree.textForeground");
        textBackground = UIManager.getColor("Tree.textBackground");
    }
    
    
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        
        
        
        TreeNodeInterface nodeValue = null;
        Icon icon = null;
        
        if(value instanceof TreeNodeInterface) {
            nodeValue = (TreeNodeInterface) value;
            icon = nodeValue.getIcon();
        }
        
        
        String  stringValue = tree.convertValueToText(value, false,
                        expanded, leaf, row, false);
        
        
        Component comp = null;
        
        if(value instanceof SelectableTreeNode) {
            SelectableTreeNode node = (SelectableTreeNode) value;
            
            checkBoxRenderer.setSelected(node.isSelected());
            if(icon != null) {
                checkBoxRenderer.setIcon(icon);
            }
            checkBoxRenderer.setText(stringValue);
            checkBoxRenderer.setEnabled(tree.isEnabled());
            
            
          if (sel) {
            checkBoxRenderer.setForeground(selectionForeground);
            checkBoxRenderer.setBackground(selectionBackground);
          } else {
            checkBoxRenderer.setForeground(textForeground);
            checkBoxRenderer.setBackground(textBackground);
          }
            
            
            comp = checkBoxRenderer;
          
        } else {
            if(icon != null) {
                mDefaultRenderer.setOpenIcon(icon);
                mDefaultRenderer.setClosedIcon(icon);
            }
            comp = mDefaultRenderer.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
        }
        
        
      

        return comp;
    }

  
}
