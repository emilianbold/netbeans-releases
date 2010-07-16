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

package org.netbeans.modules.xml.xpath.ext.schema.resolver;

import org.netbeans.modules.xml.xpath.ext.spi.*;
import java.util.Collections;
import java.util.Set;
import javax.xml.namespace.NamespaceContext;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.xpath.ext.LocationStep;
import org.netbeans.modules.xml.xpath.ext.XPathUtils;
import org.netbeans.modules.xml.xpath.ext.schema.SchemaModelsStack;

/**
 * The schema context, which relates to a Type Cast step. 
 * 
 * @author nk160297
 */
public class CastSchemaContext implements WrappingSchemaContext {

    // The context, which this Cast context based on. 
    private XPathSchemaContext mBaseContext; 
    private XPathCast mXPathCast;
    
    private Set<SchemaCompPair> mCompPairSet;

    private boolean lastInChain = false;

    public CastSchemaContext(XPathSchemaContext baseContext, 
            XPathCast xPathCast) {
        mBaseContext = baseContext;
        mXPathCast = xPathCast;
    }

    public XPathSchemaContext getBaseContext() {
        return mBaseContext;
    }
    
    public XPathCast getTypeCast() {
        return mXPathCast;
    }
    
    public XPathSchemaContext getParentContext() {
        return mBaseContext.getParentContext();
    }

    public Set<SchemaCompPair> getSchemaCompPairs() {
        if (mCompPairSet == null) {
            XPathSchemaContext parentContext = getParentContext();
            SchemaCompHolder parentCompHolder = 
                    Utilities.getSchemaCompHolder(parentContext, false);
            //
            GlobalType type = mXPathCast.getType();
            SchemaCompPair sCompPair = new SchemaCompPair(type, parentCompHolder);
            mCompPairSet = Collections.singleton(sCompPair);
        }
        //
        return mCompPairSet;
    }
    
    public Set<SchemaCompPair> getUsedSchemaCompPairs() {
        return getSchemaCompPairs();
    }

    public void setUsedSchemaCompH(Set<SchemaCompHolder> compSet) {
        // Ignore the set because there is only one schema component 
        // in this context and it always is implied as used!
    }

    public String toStringWithoutParent() {
        String type = mXPathCast.getType().getName();
        return "(" + type + ")" + mBaseContext.toStringWithoutParent();
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        //
        XPathSchemaContext parentContext = getParentContext();
        if (parentContext != null) {
            sb.append(parentContext.toString());
            sb.append(LocationStep.STEP_SEPARATOR);
        }
        //
        sb.append(toStringWithoutParent());
        //
        return sb.toString();
    }

    public String getExpressionString(NamespaceContext nsContext, SchemaModelsStack sms) {
        // Doesn't change anything here. The TypeCast is invisible in XPath!
        return mBaseContext.getExpressionString(nsContext, sms);
    }

    @Override
    public boolean equals(Object obj)  {
        if (obj instanceof CastSchemaContext) {
            CastSchemaContext other = (CastSchemaContext)obj;
            return XPathUtils.equal(
                    this.mBaseContext, other.mBaseContext) &&
                    XPathUtils.equal(
                    this.mXPathCast.getType(), other.mXPathCast.getType());
        }
        //
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + (this.mBaseContext != null ? this.mBaseContext.hashCode() : 0);
        hash = 89 * hash + (this.mXPathCast != null ? this.mXPathCast.hashCode() : 0);
        return hash;
    }
    
    public boolean equalsChain(XPathSchemaContext other) {
        return XPathSchemaContext.Utilities.equalsChain(this, other);
    }

    public boolean isLastInChain() {
        return lastInChain;
    }

    public void setLastInChain(boolean value) {
        lastInChain = value;
    }
}
