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

package org.netbeans.modules.soa.xpath.mapper.lsm;

import org.netbeans.modules.soa.xpath.mapper.utils.XPathMapperUtils;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.xam.Named;
import org.netbeans.modules.xml.xpath.ext.XPathPredicateExpression;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.CastSchemaContext;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.PredicatedSchemaContext;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.XPathSchemaContext;
import org.netbeans.modules.xml.xpath.ext.spi.XPathCast;
import org.netbeans.modules.xml.xpath.ext.spi.XPathVariable;

/**
 * Represents XPath Type Cast in BPEL Mapper
 *
 * @author Nikita Krjukov
 */
public class MapperTypeCast implements XPathCast, MapperLsm {

    protected GlobalType mCastTo;
    protected CastSchemaContext mCastedSContext;

    /**
     * Constructor is protected because the object has to be created either by
     * a derived class or by CastManager.
     * @param xPathCast
     */
    protected MapperTypeCast(XPathCast xPathCast) {
        GlobalType castTo = xPathCast.getType();
        assert castTo != null;
        mCastTo = castTo;
        mCastedSContext = xPathCast.getSchemaContext();
    }

    protected MapperTypeCast(XPathSchemaContext exprContext, GlobalType castTo) {
        assert castTo != null;
        mCastTo = castTo;
        mCastedSContext = new CastSchemaContext(exprContext, this);
    }

    public GlobalType getType() {
        return mCastTo;
    }
    
    public Object getCastedObject() {
        return XPathMapperUtils.getSchemaContextSubject(mCastedSContext);
    }

    public void setSchemaContext(XPathSchemaContext newContext) {
        // it has to be specified in a constructor
        throw new UnsupportedOperationException("Not supported"); // NOI18N
    }
    
    protected void setCastTo(GlobalType newValue) {
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
        //
        // Compare class
        if (!(obj instanceof MapperTypeCast)) {
            return false;
        }
        //
        MapperTypeCast comp2 = (MapperTypeCast)obj;
        //
        // Compare cast to
        if (getType() != comp2.getType()) {
            return false;
        }
        //
        XPathSchemaContext mySContext = getSchemaContext();
        XPathSchemaContext otherSContext = comp2.getSchemaContext();
        if (mySContext == null || otherSContext == null) {
            // 
            // Compare Schema component
            SchemaComponent mySchemaComp = getSComponent();
            if (mySchemaComp != comp2.getSComponent()) {
                return false;
            }
        } else {
            // Compare context
            if (!(otherSContext.equalsChain(mySContext))) {
                return false;
            }
        }
        //
        return true;
    }

    @Override
    public int hashCode() {
        XPathSchemaContext mySContext = getSchemaContext();
        if (mySContext != null) {
            return mySContext.toString().hashCode();
        } else {
            return super.hashCode();
        }
    }

    public String getDisplayName() {
        Object castedObj = getCastedObject();
        assert castedObj != null;
        //
        String castedObjName = null;
        if (castedObj instanceof Named) {
            castedObjName = ((Named)castedObj).getName();
        } else {
            castedObjName = castedObj.toString();
        }
        //
        return "(" + getType().getName() + ")" + castedObjName;
    }
    
    @Override
    public String toString() {
        return getSchemaContext().toString();
    }

    public XPathVariable getBaseVariable() {
        return XPathMapperUtils.getBaseVariable(getSchemaContext());
    }

    public CastSchemaContext getSchemaContext() {
        return mCastedSContext;
    }

    public void modifyPredicate(PredicatedSchemaContext template, 
            XPathPredicateExpression[] newExprArr) {
        //
        PredicateManager.modifyPredicateInSContext(
                mCastedSContext, template, newExprArr);
    }

} 