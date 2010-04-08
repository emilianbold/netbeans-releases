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

import java.util.HashSet;
import java.util.Set;
import javax.xml.namespace.NamespaceContext;
import org.netbeans.modules.xml.xpath.ext.LocationStep;
import org.netbeans.modules.xml.xpath.ext.schema.SchemaModelsStack;

/**
 * This schema context references to multiple schema components.
 *
 * TODO: this class looks like unnecessary
 * 
 * @author nk160297
 */
public class MultiCompSchemaContext implements XPathSchemaContext {

    private XPathSchemaContext mParentContext;
    private Set<SchemaCompPair> mSchemaCompPairSet;
    private Set<SchemaCompHolder> mUsedSchemaCompSet;
    private boolean lastInChain = false;
    private boolean mIsAttribute;
    
    public MultiCompSchemaContext(XPathSchemaContext parentContext, 
            Set<SchemaCompPair> compPairSet, boolean isAttribute) {
        mParentContext = parentContext;
        mSchemaCompPairSet = compPairSet;
        mIsAttribute = isAttribute;
    }

    public XPathSchemaContext getParentContext() {
        return mParentContext;
    }

    public Set<SchemaCompPair> getSchemaCompPairs() {
        return mSchemaCompPairSet;
    }

    public Set<SchemaCompPair> getUsedSchemaCompPairs() {
        HashSet<SchemaCompPair> resultSet = new HashSet<SchemaCompPair>();
        //
        if (mUsedSchemaCompSet != null) {
            for (SchemaCompPair myCompPair : mSchemaCompPairSet) {
                SchemaCompHolder myComponent = myCompPair.getCompHolder();
                for (SchemaCompHolder usdCompHolder : mUsedSchemaCompSet) {
                    if (myComponent.equals(usdCompHolder)) {
                        resultSet.add(myCompPair);
                    }
                }
            }
        }
        //
        return resultSet;
    }

    public void setUsedSchemaCompH(Set<SchemaCompHolder> compSet) {
        mUsedSchemaCompSet = compSet;
    }
    
    public String toStringWithoutParent() {
        StringBuilder sb = new StringBuilder();
        //
        if (mSchemaCompPairSet != null) {
            boolean isFirst = true;
            for (SchemaCompPair schemaCompPair : mSchemaCompPairSet) {
                if (isFirst) {
                    isFirst = false;
                } else {
                    sb.append(" | ");
                }
                //
                SchemaCompHolder parentCompHolder = 
                        schemaCompPair.getParetnCompHolder();
                if (parentCompHolder != null) {
                    SchemaCompPair.appendCompName(sb, parentCompHolder);
                    sb.append(">");
                }
                SchemaCompHolder schemaCompHolder = schemaCompPair.getCompHolder();
                SchemaCompPair.appendCompName(sb, schemaCompHolder);
            }
        }
        //
        return sb.toString();
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        //
        if (mParentContext != null) {
            sb.append(mParentContext.toString());
        }
        sb.append(LocationStep.STEP_SEPARATOR);
        //
        sb.append(toStringWithoutParent());
        //
        return sb.toString();
    }
    
    public String getExpressionString(NamespaceContext nsContext, SchemaModelsStack sms) {
        String ending = mIsAttribute ? "/@*" : "/*";
        if (mParentContext == null) {
            return ending;
        } else {
            return mParentContext.getExpressionString(nsContext, sms) + ending;
        }
    }

    @Override
    public boolean equals(Object other)  {
        if (other instanceof MultiCompSchemaContext) {
            return XPathSchemaContext.Utilities.equalCompPairs(
                    this, XPathSchemaContext.class.cast(other));
        }
        //
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + (this.mParentContext != null ? this.mParentContext.hashCode() : 0);
        hash = 31 * hash + (this.mIsAttribute ? 1 : 0);
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
