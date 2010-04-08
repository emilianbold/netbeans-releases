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
import javax.xml.namespace.NamespaceContext;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.xpath.ext.LocationStep;
import org.netbeans.modules.xml.xpath.ext.StepNodeNameTest;
import org.netbeans.modules.xml.xpath.ext.XPathUtils;
import org.netbeans.modules.xml.xpath.ext.schema.SchemaModelsStack;
import org.netbeans.modules.xml.xpath.ext.spi.XPathPseudoComp;

/**
 * The schema context, which contains only one Schema component. 
 * @author nk160297
 */
public class SimpleSchemaContext implements XPathSchemaContext {

    private XPathSchemaContext mParentContext;
    private SchemaCompPair mSchemaCompPair;
    private boolean lastInChain = false;
    
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
                    Utilities.getSchemaCompHolder(mParentContext, false);
            assert parentCompHolder != null;
            mSchemaCompPair = new SchemaCompPair(sComp, parentCompHolder);
        }
    }

    public SimpleSchemaContext(XPathSchemaContext parentContext, 
            XPathPseudoComp pseudoComp) {
        this(parentContext, SchemaCompHolder.Factory.construct(pseudoComp));
    }

    public SimpleSchemaContext(XPathSchemaContext parentContext, 
            SchemaCompHolder sCompHolder) {
        //
        assert parentContext != null;
        //
        mParentContext = parentContext;
        SchemaCompHolder parentCompHolder = 
                Utilities.getSchemaCompHolder(mParentContext, false);
        assert parentCompHolder != null;
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

    public String getExpressionString(NamespaceContext nsContext, SchemaModelsStack sms) {
        assert nsContext != null;
        //
        StringBuilder sb = new StringBuilder();
        //
        if (mParentContext != null) {
            sb.append(mParentContext.getExpressionString(nsContext, sms)).
                    append(LocationStep.STEP_SEPARATOR);
        }
        //
        SchemaCompHolder sCompHilder =
                XPathSchemaContext.Utilities.getSchemaCompHolder(this, false);
        assert sCompHilder != null;
        //
        StepNodeNameTest newSnnt = new StepNodeNameTest(nsContext, sCompHilder, sms);
        assert newSnnt != null;
        //
        if (sCompHilder.isAttribute()) {
            sb.append("@"); // NOI18N
        }
        //
        sb.append(newSnnt.getExpressionString());
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
            return XPathUtils.equal(sCompH1, sCompH2);
        }
        //
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 67 * hash + (this.mParentContext != null ? this.mParentContext.hashCode() : 0);
        hash = 67 * hash + (this.mSchemaCompPair != null ? this.mSchemaCompPair.hashCode() : 0);
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
