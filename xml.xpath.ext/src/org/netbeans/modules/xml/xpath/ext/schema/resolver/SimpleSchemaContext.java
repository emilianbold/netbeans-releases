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

import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.XPathSchemaContext;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.xpath.ext.LocationStep;
import org.netbeans.modules.xml.xpath.ext.spi.XPathPseudoComp;

/**
 * The schema context, which contains only one Schema component. 
 * @author nk160297
 */
public class SimpleSchemaContext implements XPathSchemaContext {

    private XPathSchemaContext mParentContext;
    private SchemaCompPair mSchemaCompPair;
    
    /**
     * Constructs a simple schema context based on the list of 
     * SchemaComponent objects. 
     * @param parentContext can be null. It not null then is will 
     * be assigned as parent to the first SimpleSchemaContext in the chain.  
     * @param pathList
     * @return
     */
    public static XPathSchemaContext constructSimpleSchemaContext(
            XPathSchemaContext parentContext,
            List<SchemaComponent> pathList) {
        //
        XPathSchemaContext result = parentContext;
        for (SchemaComponent sComp : pathList) {
            result = new SimpleSchemaContext(result, sComp);
            }
        //
        return result;
    }
        
    public SimpleSchemaContext(XPathSchemaContext parentContext, 
            SchemaCompPair schemaCompPair) {
        mParentContext = parentContext;
        mSchemaCompPair = schemaCompPair;
    }

    /**
     * Creates context for a global Type or global Element.
     */ 
    public SimpleSchemaContext(SchemaComponent sComp) {
        this(null, sComp);
    }

    public SimpleSchemaContext(XPathSchemaContext parentContext, 
            SchemaComponent sComp) {
        //
        if (parentContext == null) {
            mParentContext = null;
            mSchemaCompPair = new SchemaCompPair(sComp, (SchemaCompHolder)null);
        } else {
            mParentContext = parentContext;
            SchemaCompHolder parentCompHolder = 
                    Utilities.getSchemaCompHolder(mParentContext);
            assert parentCompHolder != null;
            mSchemaCompPair = new SchemaCompPair(sComp, parentCompHolder);
        }
    }

    public SimpleSchemaContext(XPathSchemaContext parentContext, 
            XPathPseudoComp pseudoComp) {
        //
        assert parentContext != null;
        //
        mParentContext = parentContext;
        SchemaCompHolder parentCompHolder = 
                Utilities.getSchemaCompHolder(mParentContext);
        assert parentCompHolder != null;
        SchemaCompHolder sCompHolder = SchemaCompHolder.Factory.construct(pseudoComp);
        mSchemaCompPair = new SchemaCompPair(sCompHolder, parentCompHolder);
    }

    public XPathSchemaContext getParentContext() {
        return mParentContext;
    }

    public Set<SchemaCompPair> getSchemaCompPairs() {
        return Collections.singleton(mSchemaCompPair);
    }
    
    public Set<SchemaCompPair> getUsedSchemaCompPairs() {
        return getSchemaCompPairs();
    }

    public void setUsedSchemaCompH(Set<SchemaCompHolder> compSet) {
        // Ignore the set because there is only one schema component 
        // in this context and it always is implied as used!
    }

    public String toStringWithoutParent() {
        StringBuilder sb = new StringBuilder();
        if (mSchemaCompPair != null) {
            SchemaCompHolder sCompHolder = mSchemaCompPair.getCompHolder();
            SchemaCompPair.appendCompName(sb, sCompHolder);
        }
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
        if (mSchemaCompPair != null) {
            sb.append(toStringWithoutParent());
        }
        //
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj)  {
        if (obj instanceof SimpleSchemaContext) {
            //
            // Optimized comparison for this simple case
            SimpleSchemaContext other = (SimpleSchemaContext)obj;
            SchemaCompHolder sCompH1 = this.mSchemaCompPair.getCompHolder();
            SchemaCompHolder sCompH2 = other.mSchemaCompPair.getCompHolder();
            //
            if (!(sCompH1.equals(sCompH2))) {
                return false;
            }
            //
            return true;
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
            XPathSchemaContext parentCont1 = this.mParentContext;
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
