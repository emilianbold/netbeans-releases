/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */

/*
 * KeyRefImpl.java
 *
 * Created on October 6, 2005, 2:55 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.schema.model.impl;

import java.net.URI;
import java.net.URISyntaxException;
import org.netbeans.modules.xml.schema.model.CommonElement;
import org.netbeans.modules.xml.schema.model.Constraint;
import org.netbeans.modules.xml.schema.model.KeyRef;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.visitor.FindReferredConstraintVisitor;
import org.netbeans.modules.xml.schema.model.visitor.SchemaVisitor;
import org.w3c.dom.Element;
/**
 *
 * @author Vidhya Narayanan
 * @author rico
 */
public class KeyRefImpl extends ConstraintImpl implements KeyRef {
    
    public KeyRefImpl(SchemaModelImpl model) {
        this(model,createNewComponent(SchemaElements.KEYREF,model));
    }
    
    /**
     * Creates a new instance of KeyRefImpl
     */
    public KeyRefImpl(SchemaModelImpl model, Element el) {
        super(model, el);
    }
    
    /**
     *
     *
     */
    public Class<? extends SchemaComponent> getComponentType() {
        return KeyRef.class;
    }
    
    /**
     *
     */
    public void setReferer(Constraint c) {
        this.setAttribute(REFERER_PROPERTY, SchemaAttributes.REFER,
                new ConstraintWrapper(c));
    }
    
    /**
     *
     */
    public void accept(SchemaVisitor visitor) {
        visitor.visit(this);
    }
    
    
    /**
     *
     */
    public Constraint getReferer() {
        String referValue = this.getAttribute(SchemaAttributes.REFER);
        if(referValue == null)
             return null;
        //remove prefix, if any
        String localName = getLocalName(referValue);
        SchemaComponent parent = findOutermostParentElement();
        FindReferredConstraintVisitor visitor = 
                new FindReferredConstraintVisitor();
        
        return visitor.findReferredConstraint(parent, localName);
    }
    
    /**
     * Adapter class to enable the use of Constraint in setAttribute()
     */
    private static class ConstraintWrapper{
        private Constraint c;
        
        public ConstraintWrapper(Constraint c){
            this.c = c;
        }
        
        public String toString(){
            return c.getName();
        }
    }
    
    private String getLocalName(String uri) {
        String localName = null;
        try {
            URI u = new URI(uri);
            localName = u.getSchemeSpecificPart();
        } catch (URISyntaxException ex) {
        }
        return localName;
    }
    
    /**
     * Look for the outermost <element> that encloses this keyRef. This is 
     * required to determine the effective scope where valid keys and uniques
     * may be obtained. That is, the refer attribute may only refer to keys or
     * uniques that are contained within the same element scope.
     */
    private SchemaComponent findOutermostParentElement(){
        SchemaComponent element = null;
        //go up the tree and look for the last instance of <element>
	SchemaComponent sc = getParent();
        while(sc != null){
            if(sc instanceof CommonElement){
                element = sc;
            }
	    sc = sc.getParent();
        }
        return element;
    }
}
