/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.iep.editor.xsd.nodes;

import javax.swing.Icon;
import javax.swing.tree.DefaultMutableTreeNode;

import org.netbeans.modules.iep.editor.xsd.TreeNodeInterface;
/**
 *
 * @author radval
 */
public abstract class AbstractSchemaArtifactNode extends DefaultMutableTreeNode implements TreeNodeInterface  {

    protected Icon mIcon;
    
    public AbstractSchemaArtifactNode(Object userObject) {
        super(userObject);
    }
   
   public Icon getIcon() {
        return mIcon;
    }
    
   
}
