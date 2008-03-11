/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.module.iep.editor.xsd.nodes;

import javax.swing.Icon;
import javax.swing.tree.DefaultMutableTreeNode;
/**
 *
 * @author radval
 */
public abstract class AbstractSchemaArtifactNode extends DefaultMutableTreeNode  {

    protected Icon mIcon;
    
    public AbstractSchemaArtifactNode(Object userObject) {
        super(userObject);
    }
   
   public Icon getIcon() {
        return mIcon;
    }
    
   
}
