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
package org.netbeans.modules.wsdlextensions.sap.impl;

import org.netbeans.modules.xml.wsdl.model.WSDLModel;

import org.netbeans.modules.wsdlextensions.sap.SAPIDocOperation;
import org.netbeans.modules.wsdlextensions.sap.SAPComponent;
import org.netbeans.modules.wsdlextensions.sap.SAPQName;
import org.w3c.dom.Element;

public class SAPIDocOperationImpl extends SAPComponentImpl implements SAPIDocOperation {

    public SAPIDocOperationImpl(WSDLModel model, Element e) {
        super(model, e);
    }

    public SAPIDocOperationImpl(WSDLModel model) {
        this(model, createPrefixedElement(SAPQName.IDOCOPERATION.getQName(), model));
    }

    public void accept(SAPComponent.Visitor visitor) {
        visitor.visit(this);
    }
}
