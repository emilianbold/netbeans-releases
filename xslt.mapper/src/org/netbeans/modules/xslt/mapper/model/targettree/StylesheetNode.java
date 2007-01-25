/*
 * StylesheetNode.java
 *
 * Created on 22 Декабрь 2006 г., 13:56
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xslt.mapper.model.targettree;

import org.netbeans.modules.xslt.mapper.model.nodes.TreeNode;
import org.netbeans.modules.xslt.mapper.view.XsltMapper;
import org.netbeans.modules.xslt.model.XslComponent;

/**
 *
 * @author Alexey
 */
public abstract class StylesheetNode extends TreeNode {
    
  
    
    public StylesheetNode(XslComponent component, XsltMapper mapper) {
        super(component, mapper);
    }
    
    public XslComponent getComponent(){
        return (XslComponent) super.getDataObject();
    }
    
   

    
}
