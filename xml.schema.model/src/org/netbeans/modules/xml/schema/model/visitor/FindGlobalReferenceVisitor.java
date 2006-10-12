/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.schema.model.visitor;

import java.util.Collection;
import java.util.List;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.GlobalAttribute;
import org.netbeans.modules.xml.schema.model.GlobalAttributeGroup;
import org.netbeans.modules.xml.schema.model.GlobalComplexType;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.schema.model.GlobalGroup;
import org.netbeans.modules.xml.schema.model.Notation;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.xam.NamedReferenceable;

/**
 *
 * @author rico
 */
public class  FindGlobalReferenceVisitor <T extends NamedReferenceable> extends DefaultSchemaVisitor{
    private Class<T> elementType;
    private String localName;
    private Schema schema;
    private T refType;
    private boolean found;
    
    public T find(Class<T> elementType, String localName, Schema schema){
        if (elementType == null || localName == null || schema == null) {
            throw new IllegalArgumentException("elementType == null");
        }

        this.elementType = elementType;
        this.localName = localName;
        this.schema = schema;
        found = false;
        schema.accept(this);
        return refType;
    }
    
    public void visit(Schema schema) {
        List<SchemaComponent> ch = schema.getChildren();
        for (SchemaComponent c : ch) {
            c.accept(this);
            if(found) return;
        }
    }
    
    public void  visit(GlobalAttributeGroup e) {
        findReference(GlobalAttributeGroup.class, e);
    }
    
    public void visit(GlobalGroup e) {
        findReference(GlobalGroup.class, e);
    }
    
    public void visit(GlobalAttribute e) {
        findReference(GlobalAttribute.class, e);
    }
    
    public void visit(GlobalElement e) {
        findReference(GlobalElement.class, e);
    }
    
    public void visit(GlobalSimpleType e) {
        findReference(GlobalSimpleType.class, e);
    }
    
    public void visit(GlobalComplexType e) {
        findReference(GlobalComplexType.class, e);
    }
    
    public void visit(Notation e) {
        findReference(Notation.class, e);
    }
    
    private void findReference(Class<? extends NamedReferenceable> refClass,
	NamedReferenceable n) {
	if(elementType.isAssignableFrom(refClass)){
	    if(localName.equals(n.getName())){
		refType = elementType.cast(n);
		found = true;
	    }
	}
    }
    
}
