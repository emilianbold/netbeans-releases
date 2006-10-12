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

package org.netbeans.modules.xml.schema.model.impl;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.xml.schema.model.SimpleType;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SimpleTypeDefinition;
import org.w3c.dom.Element;
/**
 *
 * @author rico
 */
public abstract class CommonSimpleTypeImpl extends SchemaComponentImpl implements SimpleType{

    /** Creates a new instance of CommonSimpleTypeImpl */
    public CommonSimpleTypeImpl(SchemaModelImpl model, Element e) {
        super(model, e);
    }
    
    public void setDefinition(SimpleTypeDefinition def) {
        if(def == null){
            throw new IllegalArgumentException(
                    "Element 'simpleType' must have either 'restriction' or 'list' or 'union'");
        }
        List<Class<? extends SchemaComponent>> classes = Collections.emptyList();
        setChild(SimpleTypeDefinition.class, DEFINITION_PROPERTY, def, classes);
    }
    
    public SimpleTypeDefinition getDefinition() {
        Collection<SimpleTypeDefinition> elements = getChildren(SimpleTypeDefinition.class);
        if(!elements.isEmpty()){
            return elements.iterator().next();
        }
        //TODO should we throw exception if there is no definition?
        return null;
    }
    
}
