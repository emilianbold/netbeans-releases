/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.iep.editor.xsd.nodes;

import javax.swing.tree.TreeNode;

/**
 *
 * @author radval
 */
public interface SelectableTreeNode extends TreeNode {

    boolean isSelected();
    
    void setSelected(boolean selected);
    
    Object getUserObject();
}
