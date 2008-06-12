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
import java.util.List;
import org.netbeans.modules.xml.schema.model.AnyAttribute;
import org.netbeans.modules.xml.schema.model.Attribute;
import org.netbeans.modules.xml.schema.model.SchemaComponent;

/**
 * The special visitor for looking for attributes only. 
 * It is necessary for ComplexContentRestriction only. 
 * 
 * @author nk160297
 */
public class CollectInheritedAttributesVisitor extends AbstractSchemaSearchVisitor {
    
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
    public CollectInheritedAttributesVisitor(boolean skipXsdAny) {
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
        owner.accept(this);
    }

    @Override
    public void visit(AnyAttribute aa) {
        if (!mSkipXsdAny && mAnyAttribute == null) {
            mAnyAttribute = aa;
            mSkipXsdAny = true;
        }
    }

    @Override
    protected void checkComponent(SchemaComponent sc) {
        if (sc instanceof Attribute) {
            Attribute attr = (Attribute)sc;
            mAttributes.add(attr);
        }
    }
}
