/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.xslt.tmap.model.api;

import org.netbeans.modules.xml.wsdl.model.extensions.bpel.PartnerLinkType;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.Role;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.xam.dom.Attribute;
import org.netbeans.modules.xslt.tmap.model.impl.AttributesType;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public enum TMapAttributes implements Attribute {
//    PARTNER_LINK_TYPE(PartnerLinkTypeReference.PARTNER_LINK_TYPE, QName.class),
//    ROLE_NAME(PartnerLinkTypeReference.ROLE_NAME, String.class, AttributesType.AttrType.NCNAME),
//    OPERATION_NAME(OperationReference.OPERATION_NAME, String.class, AttributesType.AttrType.NCNAME),
    PARTNER_LINK_TYPE(PartnerLinkTypeReference.PARTNER_LINK_TYPE, PartnerLinkType.class, AttributesType.AttrType.QNAME),
    ROLE_NAME(PartnerLinkTypeReference.ROLE_NAME, Role.class, AttributesType.AttrType.NCNAME),
    OPERATION_NAME(OperationReference.OPERATION_NAME, Operation.class, AttributesType.AttrType.NCNAME),
    TRANSFORM_JBI(TransformerDescriptor.TRANSFORM_JBI, Boolean.class),
    INPUT_VARIABLE(VariableDeclarator.INPUT_VARIABLE, String.class),
    OUTPUT_VARIABLE(VariableDeclarator.OUTPUT_VARIABLE, String.class),
    SOURCE(Transform.SOURCE, String.class),
    RESULT(Transform.RESULT, String.class),
    TYPE(Param.TYPE, String.class),
    NAME(Param.NAME, String.class),
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
