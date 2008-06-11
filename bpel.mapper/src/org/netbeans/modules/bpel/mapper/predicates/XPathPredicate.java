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

package org.netbeans.modules.bpel.mapper.predicates;

import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.xpath.ext.LocationStep;
import org.netbeans.modules.xml.xpath.ext.XPathPredicateExpression;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.XPathSchemaContext;

/**
 *
 * @author nk160297
 */
public class XPathPredicate extends AbstractPredicate {

    // private RestartableIterator<Object> mPathItr;
    
    private LocationStep mLocationStep;
    
    public XPathPredicate(LocationStep locationStep) {
        mLocationStep = locationStep;
    }
    
    public XPathSchemaContext getSchemaContext() {
        return mLocationStep.getSchemaContext(); // TODO
    }
    
    public XPathPredicateExpression[] getPredicates() {
        return mLocationStep.getPredicates();
    }
    
    public SchemaComponent getSComponent() {
        XPathSchemaContext schemaContext = mLocationStep.getSchemaContext();
        SchemaComponent sComp = XPathSchemaContext.Utilities.
                getSchemaComp(schemaContext);
        return sComp;
    }
    
    public void setPredicates(XPathPredicateExpression[] newPArr) {
        mLocationStep.setPredicates(newPArr);
        mLocationStep.getModel().discardResolvedStatus();
    }
    
} 