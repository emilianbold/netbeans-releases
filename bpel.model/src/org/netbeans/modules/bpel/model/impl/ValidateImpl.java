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
package org.netbeans.modules.bpel.model.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicReference;
import org.netbeans.modules.bpel.model.impl.references.BpelReferenceBuilder;

import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.Validate;
import org.netbeans.modules.bpel.model.api.VariableDeclaration;
import org.netbeans.modules.bpel.model.api.references.BpelReference;
import org.netbeans.modules.bpel.model.api.support.BpelModelVisitor;
import org.netbeans.modules.bpel.model.xam.BpelAttributes;
import org.netbeans.modules.bpel.model.xam.BpelElements;
import org.netbeans.modules.xml.xam.Reference;
import org.netbeans.modules.xml.xam.dom.Attribute;
import org.w3c.dom.Element;

/**
 * @author ads
 */
public class ValidateImpl extends ActivityImpl implements Validate {

    ValidateImpl(BpelModelImpl model, Element e) {
        super(model, e);
    }

    ValidateImpl(BpelBuilderImpl builder) {
        super(builder, BpelElements.VALIDATE.getName());
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.Validate#getVariables()
     */
    public List<BpelReference<VariableDeclaration>> getVariables() {
        return getBpelReferenceList(BpelAttributes.VARIABLES, VariableDeclaration.class);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.Validate#setVaraibles(java.util.List)
     */
    public void setVariables(List<BpelReference<VariableDeclaration>> list) {
        setBpelReferenceList(BpelAttributes.VARIABLES, VariableDeclaration.class, list);
    }

    public String getVariablesList() {
        List<BpelReference<VariableDeclaration>> references = getVariables();

        if (references == null) {
            return null;
        }
        StringBuilder builder = new StringBuilder();

        for (BpelReference<VariableDeclaration> reference : references) {
            builder.append(reference.getRefString());
            builder.append(" "); // NOI18N
        }
        if (builder.length() > 0) {
            return builder.substring(0, builder.length() - 1);
        }
        return builder.toString();
    }

    public void setVariablesList(String value) {
//System.out.println("SET VAR LIST: " + value);
        if (value == null) {
            removeVariablesList();
            return;
        }
        StringTokenizer stk = new StringTokenizer(value, " "); // NOI18N
        List<BpelReference<VariableDeclaration>> list = new ArrayList<BpelReference<VariableDeclaration>>();

        while (stk.hasMoreTokens()) {
            String next = stk.nextToken();
            BpelReference<VariableDeclaration> ref = BpelReferenceBuilder.getInstance().build(VariableDeclaration.class, this, next);
            BpelReferenceBuilder.getInstance().setAttribute(ref, BpelAttributes.VARIABLES);
            list.add(ref);
        }
        setVariables(Collections.unmodifiableList(list));
//System.out.println("        LIST: " + getVariablesList());
    }

    public void removeVariablesList() {
        setVariables(new ArrayList<BpelReference<VariableDeclaration>>());
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.BpelEntity#getElementType()
     */
    public Class<? extends BpelEntity> getElementType() {
        return Validate.class;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.references.ReferenceCollection#getReferences()
     */
    public Reference[] getReferences() {
        List<BpelReference<VariableDeclaration>> list = getVariables();
        if ( list== null ) {
            return EMPTY_REFERENCES; 
        }
        return list.toArray( new Reference[ list.size() ]);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.impl.BpelEntityImpl#acceptThis(org.netbeans.modules.soa.model.bpel20.api.support.BpelModelVisitor)
     */
    public void accept( BpelModelVisitor visitor ) {
        visitor.visit( this );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.impl.BpelEntityImpl#getDomainAttributes()
     */
    protected Attribute[] getDomainAttributes() {
        if ( myAttributes.get() == null ){
            Attribute[] attr = super.getDomainAttributes();
            Attribute[] ret = new Attribute[ attr.length + 1];
            System.arraycopy( attr , 0 , ret , 1 , attr.length );
            ret[ 0 ] = BpelAttributes.VARIABLES;
            myAttributes.compareAndSet( null, ret);
        }
        return myAttributes.get();
    }
    
    private static AtomicReference<Attribute[]> myAttributes = new AtomicReference<Attribute[]>();
    private static final Reference[] EMPTY_REFERENCES = new Reference[0];
}
