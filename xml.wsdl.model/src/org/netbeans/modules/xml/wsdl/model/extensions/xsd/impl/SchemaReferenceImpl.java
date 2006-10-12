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

package org.netbeans.modules.xml.wsdl.model.extensions.xsd.impl;

import java.util.List;
import javax.xml.XMLConstants;
import org.netbeans.modules.xml.schema.model.ReferenceableSchemaComponent;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.SchemaModelFactory;
import org.netbeans.modules.xml.schema.model.visitor.FindGlobalReferenceVisitor;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.netbeans.modules.xml.xam.dom.AbstractNamedComponentReference;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;

/**
 *
 * @author Nam Nguyen
 */
public class SchemaReferenceImpl<T extends ReferenceableSchemaComponent> 
        extends AbstractNamedComponentReference<T> implements NamedComponentReference<T> {
    
    /** Creates a new instance of SchemaReferenceImpl */
    public SchemaReferenceImpl(
            T referenced, 
            Class<T> type, 
            AbstractDocumentComponent parent) {
        super(referenced, type, parent);
    }
    
    //for use by resolve methods
    public SchemaReferenceImpl(Class<T> type, AbstractDocumentComponent parent, String refString){
        super(type, parent, refString);
    }

    protected List<Schema> findSchemas(String namespace) {
        List<Schema> schemas = ((WSDLModel)getParent().getModel()).findSchemas(namespace);
        if (schemas.isEmpty() && XMLConstants.W3C_XML_SCHEMA_NS_URI.equals(namespace)) {
            SchemaModel primitiveModel = SchemaModelFactory.getDefault().getPrimitiveTypesModel();
            schemas.add(primitiveModel.getSchema());
        }
        return schemas;
    }
    
    public T get() {
        if (super.getReferenced() == null) {
            List<Schema> schemas = findSchemas(getEffectiveNamespace());
            for (Schema schema : schemas) {
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
        if (refString == null) {
            assert getReferenced() != null;
            return getReferenced().getModel().getSchema().getTargetNamespace();
        } else {
            return ((AbstractDocumentComponent)getParent()).lookupNamespaceURI(getPrefix());
        }
    }
}
