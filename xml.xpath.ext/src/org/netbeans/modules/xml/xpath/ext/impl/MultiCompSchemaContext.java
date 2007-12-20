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

package org.netbeans.modules.xml.xpath.ext.impl;

import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.xml.xpath.ext.XPathSchemaContext;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.xpath.ext.LocationStep;

/**
 * This schema context references to multiple schema components. 
 * 
 * @author nk160297
 */
public class MultiCompSchemaContext implements XPathSchemaContext {

    private XPathSchemaContext mParentContext;
    private Set<SchemaCompPair> mSchemaCompPairSet;
    private Set<SchemaComponent> mUsedSchemaCompSet;
    
    public MultiCompSchemaContext(XPathSchemaContext parentContext, 
            Set<SchemaCompPair> compPairSet) {
        mParentContext = parentContext;
        mSchemaCompPairSet = compPairSet;
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
                SchemaComponent myComponent = myCompPair.getComp();
                for (SchemaComponent usdComp : mUsedSchemaCompSet) {
                    if (myComponent.equals(usdComp)) {
                        resultSet.add(myCompPair);
                    }
                }
            }
        }
        //
        return resultSet;
    }

    public void setUsedSchemaComp(Set<SchemaComponent> compSet) {
        mUsedSchemaCompSet = compSet;
    }
    
    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        //
        if (mParentContext != null) {
            sb.append(mParentContext.toString());
        }
        sb.append(LocationStep.STEP_SEPARATOR);
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
                SchemaComponent parentComp = schemaCompPair.getParetnComp();
                if (parentComp != null) {
                    SchemaCompPair.appendCompName(sb, parentComp);
                    sb.append(">");
                }
                SchemaComponent schemaComp = schemaCompPair.getComp();
                SchemaCompPair.appendCompName(sb, schemaComp);
            }
        }
        //
        return sb.toString();
    }
    
    @Override
    public boolean equals(Object obj)  {
        if (obj instanceof XPathSchemaContext) {
            return XPathSchemaContext.Utilities.equals(
                    this, (XPathSchemaContext)obj);
        }
        //
        return false;
    }

    public boolean equalsChain(XPathSchemaContext obj) {
        if (obj instanceof XPathSchemaContext) {
            return XPathSchemaContext.Utilities.equalsChain(
                    this, (XPathSchemaContext)obj);
        }
        //
        return false;
    }
    
}
