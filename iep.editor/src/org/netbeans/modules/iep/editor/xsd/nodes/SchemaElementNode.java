/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.iep.editor.xsd.nodes;

import org.netbeans.modules.iep.editor.xsd.nodes.images.NodeIcons;
import org.netbeans.modules.xml.axi.Element;

/**
 *
 * @author radval
 */
public class SchemaElementNode extends AbstractSchemaArtifactNode implements SelectableTreeNode {

    private boolean mSelected;
    
    public SchemaElementNode(Element element) {
        super(element);
        
        boolean repeating = false;
        boolean optional = false;
        this.mIcon = NodeIcons.ELEMENT.getIcon();
        if(element.getMinOccurs() != null && element.getMinOccurs().equals("0")) {
            this.mIcon = NodeIcons.ELEMENT_OPTIONAL.getIcon();
            optional = true;
        } 
        
        if (element.getMaxOccurs() != null) {
            
            if(element.getMaxOccurs().equalsIgnoreCase("UNBOUNDED")) {
                repeating = true;
            } else if (!element.getMaxOccurs().equals("0") && !element.getMaxOccurs().equals("1") ) {
                repeating = true;
            }
            
            if(repeating) {
                this.mIcon = NodeIcons.ELEMENT_REPEATING.getIcon();
            }
            
            if(repeating && optional) {
                this.mIcon = NodeIcons.ELEMENT_OPTIONAL_REPEATING.getIcon();
            }
            
            
        }
    }

    public boolean isSelected() {
        return mSelected;
    }

    public void setSelected(boolean selected) {
        this.mSelected = selected;
    }
    
    
}
