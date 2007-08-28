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

import java.util.HashMap;
import java.util.List;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.namespace.QName;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.SchemaModelFactory;
import org.netbeans.modules.xml.wsdl.model.Types;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.xsd.WSDLSchema;
import org.netbeans.modules.xml.wsdl.model.impl.WSDLAttribute;
import org.netbeans.modules.xml.wsdl.model.impl.WSDLModelImpl;
import org.netbeans.modules.xml.wsdl.model.spi.GenericExtensibilityElement;
import org.netbeans.modules.xml.wsdl.model.visitor.WSDLVisitor;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentModel;
import org.netbeans.modules.xml.xam.dom.DocumentModel;
import org.w3c.dom.Element;

/**
 *
 * @author rico
 */
public class WSDLSchemaImpl extends GenericExtensibilityElement implements WSDLSchema {
    public static final QName XSD_QNAME = new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "schema", "xsd");
    
    private WSDLModel model;
    private SchemaModel schemaModel;
    /** Creates a new instance of WSDLSchemaImpl */
    public WSDLSchemaImpl(WSDLModel model, Element e) {
        super(model, e);
        this.model = model;
    }
    
    public WSDLSchemaImpl(WSDLModel model){
        this(model, createPrefixedElement(XSD_QNAME, model));
    }
    
    public void accept(WSDLVisitor visitor) {
	visitor.visit(this);
    }
    
    public SchemaModel getSchemaModel() {
        if(schemaModel == null){
            schemaModel = SchemaModelFactory.getDefault().createEmbeddedSchemaModel(model, getPeer());
            schemaModel.getSchema().setForeignParent(getModel().getDefinitions().getTypes());
        }
        return schemaModel;
    }
    
    public DocumentModel getEmbeddedModel() {
        return getSchemaModel();
    }

    protected void populateChildren(List<WSDLComponent> children) {
        // just dummy node, make sure empty children
    }

}
