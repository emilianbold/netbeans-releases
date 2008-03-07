/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.module.iep.editor.xsd.nodes;

/**
 *
 * @author radval
 */
public class SchemaElementNode extends AbstractSchemaArtifactNode implements SelectableTreeNode {

    private boolean mSelected;
    
    public SchemaElementNode(Object userObject) {
        super(userObject);
    }

    public boolean isSelected() {
        return mSelected;
    }

    public void setSelected(boolean selected) {
        this.mSelected = selected;
    }
}
