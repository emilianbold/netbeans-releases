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
package org.netbeans.modules.xml.xpath.ext.schema;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.netbeans.modules.xml.schema.model.AnyAttribute;
import org.netbeans.modules.xml.schema.model.Attribute;
import org.netbeans.modules.xml.schema.model.AttributeGroupReference;
import org.netbeans.modules.xml.schema.model.AttributeReference;
import org.netbeans.modules.xml.schema.model.ComplexContentRestriction;
import org.netbeans.modules.xml.schema.model.ComplexExtension;
import org.netbeans.modules.xml.schema.model.GlobalAttribute;
import org.netbeans.modules.xml.schema.model.GlobalAttributeGroup;
import org.netbeans.modules.xml.schema.model.GlobalComplexType;
import org.netbeans.modules.xml.schema.model.LocalAttribute;
import org.netbeans.modules.xml.schema.model.LocalAttributeContainer;
import org.netbeans.modules.xml.schema.model.LocalComplexType;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SimpleContentRestriction;
import org.netbeans.modules.xml.schema.model.SimpleExtension;
import org.netbeans.modules.xml.schema.model.visitor.DefaultSchemaVisitor;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;

/**
 * The special visitor for looking for attributes only. 
 * It is necessary for ComplexContentRestriction only. 
 * 
 * This visitor looks only for attributes defined direclty inside of parent.
 * Inherited attributes aren't taken into consideration.
 * 
 * @author nk160297
 */
public class CollectLocalAttributesVisitor extends DefaultSchemaVisitor {
    
    private boolean mSkipXsdAny;
    private AnyAttribute mAnyAttribute;
    private ArrayList<Attribute> mAttributes = new ArrayList<Attribute>();

    /**
     * 
     * @param lookForElements
     * @param lookForAttributes
     * @param supportAny indicates if it is necessary to add 
     * AnyElement and AnyAttribute to result list. 
     */
    public CollectLocalAttributesVisitor(boolean skipXsdAny) {
        super();
        mSkipXsdAny = skipXsdAny;
    }
    
    public List<Attribute> getAttributes() {
        return mAttributes;
    }
    
    public AnyAttribute getAnyAttribute() {
        return mAnyAttribute;
    }
    
    /**
     * Collects all attributes from the specified owner.
     * Collected attributes are accessible with the method getAttributes() and 
     * getAnyAttribute().
     * @param owner
     */
    public void collectFrom(SchemaComponent owner) {
        mAnyAttribute = null;
        mAttributes.clear();
        //
        if (owner instanceof LocalAttributeContainer) {
            visitAttrContainer((LocalAttributeContainer)owner);
        }
    }

    @Override
    public void visit(AttributeReference attrRef) {
        NamedComponentReference<GlobalAttribute> gAttrRef = attrRef.getRef();
        if (gAttrRef != null) {
            GlobalAttribute gAttr = gAttrRef.get();
            if (gAttr != null) {
                checkAttribute(gAttr);
            }
        }
    }

    @Override
    public void visit(AttributeGroupReference agr) {
        NamedComponentReference<GlobalAttributeGroup> gagRef = agr.getGroup();
        if (gagRef != null) {
            GlobalAttributeGroup gag = gagRef.get();
            if (gag != null) {
                visitAttrContainer(gag);
            }
        }
    }
    
    @Override
    public void visit(AnyAttribute aa) {
        if (!mSkipXsdAny && mAnyAttribute == null) {
            mAnyAttribute = aa;
            mSkipXsdAny = true; // any already found 
        }
    }

    //---------------------------------------------------------------
    
    private void visitAttrContainer(LocalAttributeContainer lac) {
	Collection<LocalAttribute> localAttrList = lac.getLocalAttributes();
        for (LocalAttribute localAttr : localAttrList) {
            checkAttribute(localAttr);
        }
        //
        Collection<AttributeReference> attrRefList = lac.getAttributeReferences();
        for (AttributeReference attrRef : attrRefList) {
            visit(attrRef);
        }
        //
        Collection<AttributeGroupReference> attrRefGroupList = 
                lac.getAttributeGroupReferences();
        for (AttributeGroupReference attrRefGroup : attrRefGroupList) {
            visit(attrRefGroup);
        }
        //
        if (mSkipXsdAny) {
            AnyAttribute anyAttr = lac.getAnyAttribute();
            if (anyAttr != null) {
                visit(anyAttr);
            }
        }
    }
    
    protected void checkAttribute(Attribute sc) {
        if (sc instanceof Attribute) {
            Attribute attr = (Attribute)sc;
            mAttributes.add(attr);
        }
    }
    
}
