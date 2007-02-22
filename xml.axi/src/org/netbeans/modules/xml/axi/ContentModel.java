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
package org.netbeans.modules.xml.axi;

import org.netbeans.modules.xml.axi.visitor.AXIVisitor;
import org.netbeans.modules.xml.schema.model.GlobalAttributeGroup;
import org.netbeans.modules.xml.schema.model.GlobalComplexType;
import org.netbeans.modules.xml.schema.model.GlobalGroup;
import org.netbeans.modules.xml.schema.model.SchemaComponent;

/**
 * A content model represents various content models in XML Schema
 * language, e.g. ComplexType, Group, AttributeGroup etc.
 * These are few constructs for reusability and extension.
 *
 * @author Samaresh (Samaresh.Panda@Sun.Com)
 */
public class ContentModel extends AXIContainer implements AXIType {
    
    /**
     * Various types of content model.
     */
    public static enum ContentModelType {
        COMPLEX_TYPE,
        GROUP,
        ATTRIBUTE_GROUP
    }
    
    /**
     * Creates a new instance of ContentModel
     */
    public ContentModel(AXIModel model, ContentModelType type) {
        super(model);
        this.type = type;
    }
    
    /**
     * Creates a new instance of ContentModel
     */
    public ContentModel(AXIModel model, SchemaComponent schemaComponent) {
        super(model, schemaComponent);
        if(schemaComponent instanceof GlobalGroup)
            type = ContentModelType.GROUP;
        if(schemaComponent instanceof GlobalAttributeGroup)
            type = ContentModelType.ATTRIBUTE_GROUP;
        if(schemaComponent instanceof GlobalComplexType)
            type = ContentModelType.COMPLEX_TYPE;
    }

    /**
     * Allows a visitor to visit this Element.
     */
    public void accept(AXIVisitor visitor) {
        visitor.visit(this);
    }
    
    /**
     * Returns the type of this component.
     * @see ComponentType.
     */
    public ComponentType getComponentType() {
        return ComponentType.SHARED;
    }
    
    /**
     * Returns the type of this content model.
     */
    public ContentModelType getType() {
        return type;
    }
    
    public String toString() {
        return getName();
    }

    private ContentModelType type;
    public static final String PROP_CONTENT_MODEL = "contentModel";
}
