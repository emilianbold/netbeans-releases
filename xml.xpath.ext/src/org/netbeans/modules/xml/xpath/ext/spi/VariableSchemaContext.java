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

package org.netbeans.modules.xml.xpath.ext.spi;

import java.util.Collections;
import java.util.Set;
import org.netbeans.modules.xml.xpath.ext.XPathSchemaContext;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.xpath.ext.XPathVariableReference;

/**
 *
 * @author nk160297
 */
public class VariableSchemaContext implements XPathSchemaContext {

    private Set<SchemaCompPair> mCompPairSet;
    private XPathVariable mXPathVar;
    
    public VariableSchemaContext(XPathVariable var) {
        assert var != null;
        mXPathVar = var;
    }

    public VariableSchemaContext(XPathVariableReference varRef) {
        assert varRef != null;
        XPathVariable var = varRef.getVariable();
        assert var != null;
        mXPathVar = var;
    }

    public XPathSchemaContext getParentContext() {
        return null;
    }

    public synchronized Set<SchemaCompPair> getSchemaCompPairs() {
        if (mCompPairSet == null) {
            SchemaComponent varType = mXPathVar.getType();
            SchemaCompPair sCompPair = new SchemaCompPair(varType, null);
            mCompPairSet = Collections.singleton(sCompPair);
        }
        //
        return mCompPairSet;
    }
    
    public Set<SchemaCompPair> getUsedSchemaCompPairs() {
        return getSchemaCompPairs();
    }

    public void setUsedSchemaComp(Set<SchemaComponent> compSet) {
        // Ignore the set because there is only one schema component 
        // in this context and it always is implied as used!
    }
    
    public XPathVariable getVariable() {
        return mXPathVar;
    }

    @Override
    public String toString() {
        return mXPathVar.toString();
    }

    @Override
    public boolean equals(Object obj)  {
        if (obj instanceof VariableSchemaContext) {
            if (this == obj) {
                return true;
            }
            //
            // Compare variables
            VariableSchemaContext other = (VariableSchemaContext)obj;
            XPathVariable var1 = this.getVariable();
            XPathVariable var2 = other.getVariable();
            //
            if (var1.equals(var2)) {
                return true;
            }
        }
        //
        return false;
    }

    public boolean equalsChain(XPathSchemaContext obj) {
        return equals(obj);
    }

}
