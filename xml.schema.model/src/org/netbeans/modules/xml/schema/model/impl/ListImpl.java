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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * This implements interface List, represents the xs:list element, which is a whitespace 
 * separated list of values.
 *
 * @author Nam Nguyen
 */

package org.netbeans.modules.xml.schema.model.impl;

import java.util.Collection;
import java.util.Collections;
import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.schema.model.List;
import org.netbeans.modules.xml.schema.model.LocalSimpleType;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.visitor.SchemaVisitor;
import org.netbeans.modules.xml.xam.GlobalReference;
import org.w3c.dom.Element;

public class ListImpl extends SchemaComponentImpl  implements List {
    
    public ListImpl(SchemaModelImpl model) {
        this(model, createNewComponent(SchemaElements.LIST,model));
    }
    
    /** Creates a new instance of ListImpl */
    public ListImpl(SchemaModelImpl model, Element e) {
        super(model, e);
    }

	/**
	 *
	 *
	 */
	public Class<? extends SchemaComponent> getComponentType() {
		return List.class;
	}
    
    public void accept(SchemaVisitor visitor) {
        visitor.visit(this);
    }
    
    public GlobalReference<GlobalSimpleType> getType() {
        return resolveGlobalReference(GlobalSimpleType.class, SchemaAttributes.ITEM_TYPE);
    }
	
    public void setType(GlobalReference<GlobalSimpleType> type) {
        setGlobalReference(TYPE_PROPERTY, SchemaAttributes.TYPE, type );
    }
	
    public LocalSimpleType getInlineType() {
        Collection<LocalSimpleType> types = getChildren(LocalSimpleType.class);
        if (types.size() > 1 || types.size() < 0) {
            throw new IllegalArgumentException("'" + SchemaElements.LIST + "' can only local simpleType child");
        }
        LocalSimpleType[] typesA = types.toArray(new LocalSimpleType[1]);
        if (typesA.length == 0) {
            return null;
        } else {
            return typesA[0];
        }
    }
	
    public void setInlineType(LocalSimpleType st) {
        java.util.List<Class<? extends SchemaComponent>> classes = Collections.emptyList();
        setChild(LocalSimpleType.class, INLINE_TYPE_PROPERTY, st, classes);
    }
}