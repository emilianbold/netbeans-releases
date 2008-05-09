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
package org.netbeans.modules.xslt.tmap.model.api;

import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.xam.dom.Attribute;
import org.netbeans.modules.xslt.tmap.model.impl.AttributesType;
import org.netbeans.modules.xslt.tmap.model.impl.AttributesType.AttrType;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public enum TMapAttributes implements Attribute {
//    PARTNER_LINK_TYPE(PartnerLinkTypeReference.PARTNER_LINK_TYPE, QName.class),
//    ROLE_NAME(PartnerLinkTypeReference.ROLE_NAME, String.class, AttributesType.AttrType.NCNAME),
//    OPERATION_NAME(OperationReference.OPERATION_NAME, String.class, AttributesType.AttrType.NCNAME),
    TARGET_NAMESPACE( TransformMap.TARGET_NAMESPACE, String.class , AttrType.URI ),
    LOCATION( Import.LOCATION , String.class , AttrType.URI ),
    NAMESPACE( Import.NAMESPACE , String.class , AttrType.URI ),
//    PORT_TYPE(PortTypeReference.PORT_TYPE, PortType.class, AttributesType.AttrType.QNAME),
    PORT_TYPE(PortTypeReference.PORT_TYPE, PortType.class),
    OPERATION_NAME(OperationReference.OPERATION_NAME, Operation.class, AttributesType.AttrType.NCNAME),
    TRANSFORM_JBI(TransformerDescriptor.TRANSFORM_JBI, Boolean.class),
    INPUT_VARIABLE(VariableDeclarator.INPUT_VARIABLE, String.class),
    OUTPUT_VARIABLE(VariableDeclarator.OUTPUT_VARIABLE, String.class),
    SOURCE(Transform.SOURCE, String.class),
    RESULT(Transform.RESULT, String.class),
    TYPE(Param.TYPE, String.class),
    NAME(Nameable.NAME_PROPERTY, String.class),
    VALUE(Param.VALUE, String.class),
    FILE(Transform.FILE, String.class);

    private String myName;
    private Class myAttributeType;
    private Class myAttributeTypeInContainer;
    private AttributesType.AttrType myType;
    
    private TMapAttributes(String name, Class attrType, Class subtype) {
        myName = name;
        myAttributeType = attrType;
        myAttributeTypeInContainer = subtype;
    }

    private TMapAttributes(String name, Class attrType) {
        this(name, attrType, (Class)null);
    }
    
    private TMapAttributes(String name, Class attrType, AttributesType.AttrType type) {
        this(name, attrType);
        myType = type;
    }

    public String getName() {
        return myName;
    }

    public Class getType() {
        return myAttributeType;
    }

    public Class getMemberType() {
        return myAttributeTypeInContainer;
    }
    
    public AttributesType.AttrType getAttributeType() {
        return myType;
    }

}
