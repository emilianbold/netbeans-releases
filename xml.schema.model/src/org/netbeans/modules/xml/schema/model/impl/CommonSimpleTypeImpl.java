/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * CommonSimpleTypeImpl.java
 *
 * Created on October 10, 2005, 10:19 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
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
