/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.xml.schema.model.impl;

import java.net.URI;
import java.net.URISyntaxException;
import org.netbeans.modules.xml.schema.model.Constraint;
import org.netbeans.modules.xml.schema.model.Element;
import org.netbeans.modules.xml.schema.model.KeyRef;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.visitor.FindReferredConstraintVisitor;
import org.netbeans.modules.xml.schema.model.visitor.SchemaVisitor;
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
    public KeyRefImpl(SchemaModelImpl model, org.w3c.dom.Element el) {
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
                c==null?null:new ConstraintWrapper(c));
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
            if(sc instanceof Element){
                element = sc;
            }
	    sc = sc.getParent();
        }
        return element;
    }
}
