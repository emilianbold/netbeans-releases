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
import org.netbeans.modules.xml.schema.model.ReferenceableSchemaComponent;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.visitor.FindGlobalReferenceVisitor;
import org.netbeans.modules.xml.xam.dom.AbstractNamedComponentReference;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;

/**
 *
 * @author Vidhya Narayanan
 * @author rico
 *
 * Represents global references. Provides additional information for referenced elements
 * such as its broken state and its changeability.
 */
public class GlobalReferenceImpl<T extends ReferenceableSchemaComponent> extends AbstractNamedComponentReference<T>
        implements NamedComponentReference<T> {
    
    //factory uses this
    public GlobalReferenceImpl(T target, Class<T> cType, SchemaComponentImpl parent) {
        super(target, cType, parent);
    }
    
    //used by resolve methods
    public GlobalReferenceImpl(Class<T> classType, SchemaComponentImpl parent, String refString){
        super(classType, parent, refString);
    }
    
    private Collection<Schema> getSchemas() {
        SchemaModel currentModel = (SchemaModel) getParent().getModel();
        Collection<Schema> schemas = null;
        String namespace = getQName().getNamespaceURI();
        // TODO: this is needed for loanApplication.xsd to pass.  Need review to be stricter????
        if (namespace == null || namespace.length() == 0) {
            namespace = ((SchemaModel)getParent().getModel()).getSchema().getTargetNamespace();
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
        return ((SchemaComponentImpl)getParent()).getModel().getEffectiveNamespace(get());
    }
}
