/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */

package org.netbeans.modules.xml.wsdl.model.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.wsdl.model.Types;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.xsd.WSDLSchema;
import org.netbeans.modules.xml.wsdl.model.spi.WSDLComponentBase;
import org.netbeans.modules.xml.wsdl.model.visitor.WSDLVisitor;
import org.w3c.dom.Element;

/**
 *
 * @author Nam Nguyen
 */
public class TypesImpl extends WSDLComponentBase implements Types{
    
    /** Creates a new instance of TypesImpl */
    public TypesImpl(WSDLModel model, Element e) {
        super(model, e);
    }
    public TypesImpl(WSDLModel model) {
        this(model, createNewElement(WSDLQNames.TYPES.getQName(), model));
    }
    
    public void accept(WSDLVisitor visitor) {
        visitor.visit(this);
    }
    
    public Collection<Schema> getSchemas(){
        //get list of WSDLSchemas
        List<Schema> schemas = new ArrayList<Schema>();
        List<WSDLSchema> wsdlSchemas = getExtensibilityElements(WSDLSchema.class);
        
        for(WSDLSchema wsdlSchema : wsdlSchemas){
            schemas.add(wsdlSchema.getSchemaModel().getSchema());
        }
        return schemas;
    }

}
