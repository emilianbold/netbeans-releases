/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.iep.editor.xsd.nodes;

import org.netbeans.modules.iep.editor.xsd.nodes.images.NodeIcons;
import org.netbeans.modules.xml.axi.ContentModel;

/**
 *
 * @author radval
 */
public class SchemaComplexTypeNode extends AbstractSchemaArtifactNode implements SelectableTreeNode  {

   private boolean mSelected;
 
    public SchemaComplexTypeNode(ContentModel cModel) {
        super(cModel);
    }
    
    
    public boolean isSelected() {
        return mSelected;
    }

    public void setSelected(boolean selected) {
        this.mSelected = selected;
    }
}
