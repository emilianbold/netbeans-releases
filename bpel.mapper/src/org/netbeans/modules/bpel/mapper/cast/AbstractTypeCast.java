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

package org.netbeans.modules.bpel.mapper.cast;

import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.xam.Named;
import org.netbeans.modules.xml.xpath.ext.XPathSchemaContext;
import org.netbeans.modules.xml.xpath.ext.XPathSchemaContextHolder;

/**
 * The base class for different kind of Type Cast objects.
 * @author nk160297
 */
public abstract class AbstractTypeCast implements XPathSchemaContextHolder {

    private GlobalType mCastTo;

    public AbstractTypeCast(GlobalType castTo) {
        assert castTo != null;
        mCastTo = castTo;
    }
    
    public GlobalType getCastTo() {
        return mCastTo;
    }
    
    public abstract XPathSchemaContext getSchemaContext();

    public void setSchemaContext(XPathSchemaContext newContext) {
        throw new UnsupportedOperationException("Not supported"); // NOI18N
    }
    
    public void setCastTo(GlobalType newValue) {
        mCastTo = newValue;
    }
    
    public SchemaComponent getSComponent() {
        return XPathSchemaContext.Utilities.getSchemaComp(getSchemaContext());
    }
    
    public boolean hasSameContext(XPathSchemaContext context) {
        XPathSchemaContext myContext = getSchemaContext();
        if (myContext == null) {
            return false;
        }
        return myContext.equalsChain(context);
    }
    
    @Override
    public boolean equals(Object obj) {
        // Compare class
        if (!(obj instanceof AbstractTypeCast)) {
            return false;
        }
        //
        AbstractTypeCast comp2 = (AbstractTypeCast)obj;
        //
        XPathSchemaContext mySContext = getSchemaContext();
        if (mySContext == null || comp2.getSchemaContext() == null) {
            // 
            // Compare Schema component
            SchemaComponent mySchemaComp = getSComponent();
            if (mySchemaComp != comp2.getSComponent()) {
                return false;
            }
        } else {
            // Compare context
            if (!(comp2.getSchemaContext().equalsChain(mySContext))) {
                return false;
            }
        }
        //
        // Compare cast to
        GlobalType gType = comp2.getCastTo();
        return getCastTo() == gType;        
    }

    public String getDisplayName() {
        String sCompName = ((Named)getSComponent()).getName();
        return "(" + getCastTo().getName() + ")" + sCompName;
    }
    
    @Override
    public String toString() {
        return getDisplayName();
    }

} 