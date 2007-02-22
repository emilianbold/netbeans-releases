/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.xml.axi.impl;

import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xml.axi.AXIDocument;
import org.netbeans.modules.xml.axi.AnyAttribute;
import org.netbeans.modules.xml.axi.AnyElement;
import org.netbeans.modules.xml.axi.Attribute;
import org.netbeans.modules.xml.axi.Compositor;
import org.netbeans.modules.xml.axi.Element;
import org.netbeans.modules.xml.axi.ContentModel;
import org.netbeans.modules.xml.axi.visitor.DefaultVisitor;
import org.netbeans.modules.xml.schema.model.All;
import org.netbeans.modules.xml.schema.model.AttributeReference;
import org.netbeans.modules.xml.schema.model.Choice;
import org.netbeans.modules.xml.schema.model.ElementReference;
import org.netbeans.modules.xml.schema.model.GlobalAttribute;
import org.netbeans.modules.xml.schema.model.GlobalAttributeGroup;
import org.netbeans.modules.xml.schema.model.GlobalComplexType;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalGroup;
import org.netbeans.modules.xml.schema.model.LocalAttribute;
import org.netbeans.modules.xml.schema.model.LocalElement;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.Sequence;

/**
 * PeerValidator validates the peer in an AXIComponent.
 * It is possible that the code generator, sets arbitrary peer values
 * for various AXIComponent. AXI sync should treat those components as
 * invalid. For example if there was an ElementImpl but the peer was found
 * as an ElementReference then that ElementImpl is bad.
 *
 * @author Samaresh (Samaresh.Panda@Sun.Com)
 */
public class PeerValidator extends DefaultVisitor {

    private boolean result = true;
        
    /**
     * Creates a new instance of PeerValidator
     */
    public PeerValidator() {
    }
    
    public boolean validate(AXIComponent component) {
        result = true;
        component.accept(this);
        return result;
    }
    
    public void visit(AXIDocument root) {
        if(! (root.getPeer() instanceof Schema) )
            result = false;
    }
    
    public void visit(Element element) {
        SchemaComponent peer = element.getPeer();        
        if(element instanceof ElementImpl) {
            if( !(peer instanceof GlobalElement) &&
                !(peer instanceof LocalElement) )
                result = false;
        }
        if(element instanceof ElementRef) {
            if( !(peer instanceof ElementReference) )
                result = false;
        }        
    }
    
    public void visit(AnyElement element) {
        if(! (element.getPeer() instanceof org.netbeans.modules.xml.schema.model.AnyElement) )
            result = false;
    }
    
    public void visit(Attribute attribute) {        
        SchemaComponent peer = attribute.getPeer();        
        if(attribute instanceof AttributeImpl) {
            if( !(peer instanceof GlobalAttribute) &&
                !(peer instanceof LocalAttribute) )
                result = false;
        }
        if(attribute instanceof AttributeRef) {
            if( !(peer instanceof AttributeReference) )
                result = false;
        }        
    }
        
    public void visit(AnyAttribute attribute) {        
        if(! (attribute.getPeer() instanceof org.netbeans.modules.xml.schema.model.AnyAttribute) )
            result = false;
    }
    
    public void visit(Compositor compositor) {
        SchemaComponent peer = compositor.getPeer();
        if( !(peer instanceof Sequence) &&
            !(peer instanceof Choice) &&
            !(peer instanceof All) )
            result = false;
    }
    
    public void visit(ContentModel contentModel) {
        SchemaComponent peer = contentModel.getPeer();
        if( !(peer instanceof GlobalComplexType) &&
            !(peer instanceof GlobalGroup) &&
            !(peer instanceof GlobalAttributeGroup) )
            result = false;
    }    
}
