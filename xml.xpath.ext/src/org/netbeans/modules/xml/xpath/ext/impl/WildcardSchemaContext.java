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

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.xml.xpath.ext.XPathModel;
import org.netbeans.modules.xml.xpath.ext.XPathSchemaContext;
import org.netbeans.modules.xml.xpath.ext.spi.ExternalModelResolver;
import org.netbeans.modules.xml.xpath.ext.schema.FindAllChildrenSchemaVisitor;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaModel;

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
    
    // TODO replace to weak reference
    private Set<SchemaCompPair> mSchemaCompPair = null;
    private Set<SchemaComponent> mUsedSchemaCompSet;
    
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
            ExternalModelResolver exModelResolver = 
                    mXPathModel.getExternalModelResolver();
            //
            if (exModelResolver != null) {
                //
                // Look for all available root elements (attributes) 
                // in all available models
                Collection<SchemaModel> sModels = exModelResolver.getVisibleModels();
                for (SchemaModel sModel : sModels) {
                    Schema schema = sModel.getSchema();
                    FindAllChildrenSchemaVisitor visitor = 
                            new FindAllChildrenSchemaVisitor(
                            lookForElements, lookForAttributes);
                    visitor.lookForSubcomponents(schema);
                    //
                    List<SchemaComponent> foundComps = visitor.getFound();
                    for (SchemaComponent foundComp : foundComps) {
                        SchemaCompPair newPair = new SchemaCompPair(foundComp, null);
                        result.add(newPair);
                    }
                }
            }
        } else {
            Set<SchemaCompPair> parentCompPairSet = mParentContext.getSchemaCompPairs(); 
            for (SchemaCompPair parentCompPair : parentCompPairSet) {
                SchemaComponent parentComp = parentCompPair.getComp();
                FindAllChildrenSchemaVisitor visitor = 
                        new FindAllChildrenSchemaVisitor(
                        lookForElements, lookForAttributes);
                visitor.lookForSubcomponents(parentComp);
                //
                List<SchemaComponent> foundComps = visitor.getFound();
                for (SchemaComponent foundComp : foundComps) {
                    SchemaCompPair newPair = new SchemaCompPair(foundComp, parentComp);
                    result.add(newPair);
                }
            }
        }
        //
        return result;
    }
    
    public Set<SchemaCompPair> getUsedSchemaCompPairs() {
        HashSet<SchemaCompPair> resultSet = new HashSet<SchemaCompPair>();
        //
        if (mUsedSchemaCompSet != null) {
            for (SchemaCompPair myCompPair : getSchemaCompPairs()) {
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
