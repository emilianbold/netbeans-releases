/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */

package org.netbeans.modules.xml.schema.model.impl;

import java.util.Collection;
import org.netbeans.modules.xml.schema.model.ReferenceableSchemaComponent;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.visitor.FindGlobalReferenceVisitor;
import org.netbeans.modules.xml.xam.AbstractGlobalReference;
import org.netbeans.modules.xml.xam.GlobalReference;

/**
 *
 * @author Vidhya Narayanan
 * @author rico
 *
 * Represents global references. Provides additional information for referenced elements
 * such as its broken state and its changeability.
 */
public class GlobalReferenceImpl<T extends ReferenceableSchemaComponent> extends AbstractGlobalReference<T>
        implements GlobalReference<T> {
    
    //factory uses this
    public GlobalReferenceImpl(T target, Class<T> cType, SchemaComponentImpl parent) {
        super(target, cType, parent);
    }
    
    //used by resolve methods
    public GlobalReferenceImpl(Class<T> classType, SchemaComponentImpl parent, String refString){
        super(classType, parent, refString);
    }
    
    private Collection<Schema> getSchemas() {
        SchemaModel currentModel = (SchemaModel) parent.getModel();
        Collection<Schema> schemas = null;
        String namespace = getQName().getNamespaceURI();
        // TODO: this is needed for loanApplication.xsd to pass.  Need review to be stricter????
        if (namespace == null || namespace.length() == 0) {
            namespace = ((SchemaModel)parent.getModel()).getSchema().getTargetNamespace();
        }
        schemas = currentModel.findSchemas(namespace);
        return schemas;
    }
    
    public T get() {
        if (super.getReferenced() == null) {
            for (Schema schema : getSchemas()) {
                String localName = getLocalName();
                T target = getType().cast(new FindGlobalReferenceVisitor<T>().find(getType(), localName, schema));
                if (target != null) {
                    setReferenced(target);
                    break;
                }
            }
        }
        return getReferenced();
    }
    
    public String getEffectiveNamespace() {
        return ((SchemaModel)parent.getModel()).getEffectiveNamespace(get());
    }
}
