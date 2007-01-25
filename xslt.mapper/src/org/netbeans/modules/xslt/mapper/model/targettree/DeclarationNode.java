/*
 * DeclarationNode.java
 *
 * Created on 22 Декабрь 2006 г., 13:57
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xslt.mapper.model.targettree;

import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xslt.mapper.view.XsltMapper;

import org.netbeans.modules.xslt.model.Attribute;
import org.netbeans.modules.xslt.model.Element;
import org.netbeans.modules.xslt.model.XslComponent;

/**
 *
 * @author Alexey
 */
public abstract class DeclarationNode  extends StylesheetNode {
    
    /** Creates a new instance of DeclarationNode */
    public DeclarationNode(XslComponent component,  XsltMapper mapper) {
        super(component, mapper);
    }
    
    
    public boolean isMappable() {
        return true;
    }

    
    
}
