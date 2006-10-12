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
import java.util.ArrayList;
import java.util.Collection;
import org.netbeans.modules.xml.schema.model.Annotation;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.ComplexExtension;
import org.netbeans.modules.xml.schema.model.ComplexExtensionDefinition;
import org.netbeans.modules.xml.schema.model.visitor.SchemaVisitor;

import org.w3c.dom.Element;
/**
 *
 * @author rico
 */
public class ComplexExtensionImpl extends CommonExtensionImpl implements ComplexExtension{
    
    /** Creates a new instance of ComplexExtensionImpl */
    protected ComplexExtensionImpl(SchemaModelImpl model) {
        this(model, createNewComponent(SchemaElements.EXTENSION, model));
    }
    
    public ComplexExtensionImpl(SchemaModelImpl model, Element el){
        super(model,el);
    }

	/**
	 *
	 *
	 */
	public Class<? extends SchemaComponent> getComponentType() {
		return ComplexExtension.class;
	}
    
    public void setLocalDefinition(ComplexExtensionDefinition content) {
        Collection<Class<? extends SchemaComponent>> list = new ArrayList<Class<? extends SchemaComponent>>();
        list.add(Annotation.class);
        setChild(ComplexExtensionDefinition.class, LOCAL_DEFINITION_PROPERTY, content, list);
    }
    
    public ComplexExtensionDefinition getLocalDefinition() {
        Collection<ComplexExtensionDefinition> elements = getChildren(ComplexExtensionDefinition.class);
        if(!elements.isEmpty()){
            return elements.iterator().next();
        }
        //TODO should we throw exception if there is no definition?
        return null;
    }
    
    public void accept(SchemaVisitor visitor) {
        visitor.visit(this);
    }
}
