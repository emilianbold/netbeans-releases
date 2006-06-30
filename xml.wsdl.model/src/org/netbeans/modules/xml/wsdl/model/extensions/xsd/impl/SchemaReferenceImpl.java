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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.xml.schema.model.ReferenceableSchemaComponent;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.visitor.FindGlobalReferenceVisitor;
import org.netbeans.modules.xml.wsdl.model.Types;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.xsd.WSDLSchema;
import org.netbeans.modules.xml.xam.AbstractComponent;
import org.netbeans.modules.xml.xam.AbstractGlobalReference;
import org.netbeans.modules.xml.xam.GlobalReference;
import org.openide.ErrorManager;

/**
 *
 * @author Nam Nguyen
 */
public class SchemaReferenceImpl<T extends ReferenceableSchemaComponent> 
        extends AbstractGlobalReference<T> implements GlobalReference<T> {
    
    /** Creates a new instance of SchemaReferenceImpl */
    public SchemaReferenceImpl(
            T referenced, 
            Class<T> type, 
            AbstractComponent parent) {
        super(referenced, type, parent);
    }
    
    //for use by resolve methods
    public SchemaReferenceImpl(Class<T> type, AbstractComponent parent, String refString){
        super(type, parent, refString);
    }

    private List<Schema> findSchema(WSDLModel wmodel, String namespace) {
        List<Schema> ret = new ArrayList<Schema>();
        Types types = wmodel.getDefinitions().getTypes();
        List<WSDLSchema> embeddedSchemas = Collections.emptyList();
        if (types != null) {
            embeddedSchemas = types.getExtensibilityElements(WSDLSchema.class);
        }
        for (WSDLSchema wschema : embeddedSchemas) {
            try {
                ret.addAll(wschema.getSchemaModel().findSchemas(namespace));
            } catch(Exception ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            }
        }
        return ret;
    }
    
    public T get() {
        WSDLModel wmodel = (WSDLModel) parent.getModel();
        if (super.getReferenced() == null) {
            List<Schema> schemas = findSchema(wmodel, getEffectiveNamespace());
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
        if (getRefString() == null) {
            assert getReferenced() != null;
            return getReferenced().getSchemaModel().getSchema().getTargetNamespace();
        } else {
            return parent.lookupNamespaceURI(getPrefix());
        }
    }
}
