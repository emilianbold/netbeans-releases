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
import java.util.List;
import java.util.Set;
import org.netbeans.modules.xml.xpath.ext.XPathModel;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.xpath.ext.XPathUtils;

/**
 * The special context for wildcard location steps - '*', '*@', 'node()'.
 * 
 * @author nk160297
 */
public class WildcardSchemaContext implements XPathSchemaContext {

    private XPathModel mXPathModel;
    private XPathSchemaContext mParentContext;
    private boolean lookForElements;
    private boolean lookForAttributes; 
    
    // TO DO replace to weak reference
    private Set<SchemaCompPair> mSchemaCompPair = null;
    private Set<SchemaCompHolder> mUsedSchemaCompSet;
    
    public WildcardSchemaContext(XPathSchemaContext parentContext, 
            XPathModel xPathModel, 
            boolean lookForElements, boolean lookForAttributes) {
        mXPathModel = xPathModel;
        mParentContext = parentContext; // it can be null in case of wildcard at root level
        //
        this.lookForElements = lookForElements;
        this.lookForAttributes = lookForAttributes;
    }

    public XPathSchemaContext getParentContext() {
        return mParentContext;
    }

    public synchronized Set<SchemaCompPair> getSchemaCompPairs() {
        if (mSchemaCompPair == null) {
            mSchemaCompPair = calculateSchemaComponents();
        }
        return mSchemaCompPair;
    }

    private Set<SchemaCompPair> calculateSchemaComponents() {
        HashSet<SchemaCompPair> result = new HashSet<SchemaCompPair>();
        //
        if (mParentContext == null) {
            //
            List<SchemaComponent> rootCompList = XPathUtils.findRootComponents(
                    mXPathModel, lookForElements, lookForAttributes, false);
            for (SchemaComponent foundComp : rootCompList) {
                SchemaCompPair newPair = new SchemaCompPair(
                        foundComp, (SchemaCompHolder)null);
                result.add(newPair);
            }
        } else {
            List<SchemaCompPair> scHolderList = XPathUtils.findSubcomponents(
                    mXPathModel, mParentContext, 
                    lookForElements, lookForAttributes, false);
            result.addAll(scHolderList);
        }
        //
        return result;
    }
    
    public Set<SchemaCompPair> getUsedSchemaCompPairs() {
        HashSet<SchemaCompPair> resultSet = new HashSet<SchemaCompPair>();
        //
        if (mUsedSchemaCompSet != null) {
            for (SchemaCompPair myCompPair : getSchemaCompPairs()) {
                SchemaCompHolder myCompHolder = myCompPair.getCompHolder();
                for (SchemaCompHolder usdCompHolder : mUsedSchemaCompSet) {
                    if (myCompHolder.equals(usdCompHolder)) {
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
        return "";
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
