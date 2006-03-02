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
