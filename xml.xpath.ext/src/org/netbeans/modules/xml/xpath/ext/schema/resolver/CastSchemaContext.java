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
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.XPathSchemaContext;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.xpath.ext.LocationStep;

/**
 * The schema context, which relates to a Type Cast step. 
 * 
 * @author nk160297
 */
public class CastSchemaContext implements XPathSchemaContext {

    // The context, which this Cast context based on. 
    private XPathSchemaContext mBaseContext; 
    private XPathCast mXPathCast;
    
    private Set<SchemaCompPair> mCompPairSet;
    
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
                    Utilities.getSchemaCompHolder(parentContext);
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

    @Override
    public boolean equals(Object obj)  {
        if (obj instanceof CastSchemaContext) {
            //
            CastSchemaContext other = (CastSchemaContext)obj;
            if (this.mBaseContext.equals(other.mBaseContext) && 
                    this.mXPathCast.getType().equals(other.mXPathCast.getType())) {
                return true;
            }
            //
            return false;
        } else if (obj instanceof XPathSchemaContext) {
            return XPathSchemaContext.Utilities.equals(
                    this, (XPathSchemaContext)obj);
        }
        //
        return false;
    }
    
    public boolean equalsChain(XPathSchemaContext other) {
        if (equals(other)) {
            //
            // Compare parent contexts
            XPathSchemaContext parentCont1 = this.getParentContext();
            XPathSchemaContext parentCont2 = other.getParentContext();
            if (parentCont1 != null && parentCont2 != null) {
                boolean result = parentCont1.equalsChain(parentCont2);
                if (!result) {
                    return false;
                }
            } else if ((parentCont1 == null && parentCont2 != null) || 
                    (parentCont1 != null && parentCont2 == null)) {
                return false;
            } 
            //
            return true;
        }
        return false;
    }
}
