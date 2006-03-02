/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */


package org.netbeans.modules.xml.schema.model.impl;

import java.util.Collections;
import java.util.List;
import org.netbeans.modules.xml.schema.model.*;
import org.netbeans.modules.xml.schema.model.visitor.SchemaVisitor;
import org.w3c.dom.Element;

/**
 * 
 * @author Chris Webster
 */
public class GlobalGroupImpl extends NamedImpl 
implements GlobalGroup {
     public GlobalGroupImpl(SchemaModelImpl model) {
        this(model,createNewComponent(SchemaElements.GROUP,model));
    }
    
    public GlobalGroupImpl(SchemaModelImpl model, Element e) {
        super(model,e);
    }

	/**
	 *
	 *
	 */
	public Class<? extends SchemaComponent> getComponentType() {
		return GlobalGroup.class;
	}

    public void accept(SchemaVisitor v) {
        v.visit(this);
    }
    
    public void setDefinition(LocalGroupDefinition definition) {    
        List<Class<? extends SchemaComponent>> classes = Collections.emptyList();
        setChild(LocalGroupDefinition.class, DEFINITION_PROPERTY, 
            definition, classes);
    }

    public LocalGroupDefinition getDefinition() {
        List<LocalGroupDefinition> ld = getChildren(LocalGroupDefinition.class);
        return ld.isEmpty() ? null : ld.get(0);
    }
}
