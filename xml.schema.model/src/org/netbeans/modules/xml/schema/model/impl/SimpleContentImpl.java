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
 * SimpleContentImpl.java
 *
 * Created on October 6, 2005, 9:18 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.schema.model.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.netbeans.modules.xml.schema.model.Annotation;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SimpleContent;
import org.netbeans.modules.xml.schema.model.SimpleContentDefinition;
import org.netbeans.modules.xml.schema.model.visitor.SchemaVisitor;
import org.w3c.dom.Element;

/**
 *
 * @author rico
 */
public class SimpleContentImpl extends SchemaComponentImpl implements SimpleContent{
    
    /** Creates a new instance of SimpleContentImpl */
    protected SimpleContentImpl(SchemaModelImpl model) {
        this(model, createNewComponent(SchemaElements.SIMPLE_CONTENT, model));
    }
    
    public SimpleContentImpl(SchemaModelImpl model, Element el){
        super(model,el);
    }

	/**
	 *
	 *
	 */
	public Class<? extends SchemaComponent> getComponentType() {
		return SimpleContent.class;
	}
    
    public void setLocalDefinition(SimpleContentDefinition definition) {
        List<Class<? extends SchemaComponent>> list = new ArrayList<Class<? extends SchemaComponent>>(1);
        list.add(Annotation.class);
        
        setChild(SimpleContentDefinition.class,
                LOCAL_DEFINITION_PROPERTY, definition, list);
    }
    
    public SimpleContentDefinition getLocalDefinition() {
        Collection<SimpleContentDefinition> elements = 
            getChildren(SimpleContentDefinition.class);
        if(!elements.isEmpty()){
            return elements.iterator().next();
        }
        return null;
    }
    
    /**
     * Visitor providing
     */
    public void accept(SchemaVisitor visitor) {
        visitor.visit(this);
    }
}
