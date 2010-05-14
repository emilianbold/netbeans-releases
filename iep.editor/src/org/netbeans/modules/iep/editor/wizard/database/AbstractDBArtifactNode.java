/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.iep.editor.wizard.database;

import org.netbeans.modules.iep.editor.xsd.TreeNodeInterface;
import javax.swing.Icon;
import javax.swing.tree.DefaultMutableTreeNode;
/**
 *
 * @author radval
 */
public abstract class AbstractDBArtifactNode extends DefaultMutableTreeNode implements TreeNodeInterface {

    protected Icon mIcon;
    
    public AbstractDBArtifactNode(Object userObject) {
        super(userObject);
    }

    public AbstractDBArtifactNode(Object userObject, boolean allowsToHaveChildren) {
        super(userObject, allowsToHaveChildren);
    }
   

    
   public Icon getIcon() {
        return mIcon;
    }
    
   
}
