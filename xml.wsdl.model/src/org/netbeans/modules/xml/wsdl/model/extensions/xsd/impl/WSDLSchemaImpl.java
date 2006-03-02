/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */

package org.netbeans.modules.xml.wsdl.model.extensions.xsd.impl;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.SchemaModelFactory;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.xsd.WSDLSchema;
import org.netbeans.modules.xml.wsdl.model.spi.WSDLComponentBase;
import org.netbeans.modules.xml.wsdl.model.impl.WSDLModelImpl;
import org.netbeans.modules.xml.wsdl.model.visitor.WSDLVisitor;
import org.netbeans.modules.xml.xam.Model;
import org.w3c.dom.Element;

/**
 *
 * @author rico
 */
public class WSDLSchemaImpl extends WSDLComponentBase implements WSDLSchema {
    public static final QName XSD_QNAME = new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "schema", "xsd");
    
    private Element schemaElement;
    private WSDLModel model;
    private SchemaModel schemaModel;
    /** Creates a new instance of WSDLSchemaImpl */
    public WSDLSchemaImpl(WSDLModel model, Element e) {
        super(model, e);
        this.model = model;
        schemaElement = e;
    }
    
    public WSDLSchemaImpl(WSDLModel model){
        this(model, createPrefixedElement(XSD_QNAME, model));
    }
    
    public void accept(WSDLVisitor visitor) {
	visitor.visit(this);
    }
    
    public SchemaModel getSchemaModel() {
        if(schemaModel == null){
            schemaModel = SchemaModelFactory.getDefault().createEmbeddedSchemaModel(model, schemaElement);
        }
        return schemaModel;
    }

    public Model getEmbeddedModel() {
        return getSchemaModel();
    }
}
