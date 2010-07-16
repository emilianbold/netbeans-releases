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

package org.netbeans.modules.wsdlextensions.jdbc.impl;

import java.util.Collection;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.wsdlextensions.jdbc.JDBCOperationOutput;
import org.netbeans.modules.wsdlextensions.jdbc.JDBCComponent;
import org.netbeans.modules.wsdlextensions.jdbc.JDBCQName;
import org.w3c.dom.Element;

/**
 * @author 
 */
public class JDBCOperationOutputImpl extends JDBCComponentImpl implements JDBCOperationOutput {
    
    public JDBCOperationOutputImpl(WSDLModel model, Element e) {
        super(model, e);
    }
    
    public JDBCOperationOutputImpl(WSDLModel model){
        this(model, createPrefixedElement(JDBCQName.OUTPUT.getQName(), model));
    }
    
    public void accept(JDBCComponent.Visitor visitor) {
        visitor.visit(this);
    }

	// need to work on this for getting the values

	public int getReturnPartName() {
        String interval = getAttribute(JDBCAttribute.JDBC_RETPARTNAME_PROPERTY);
        int intervalVal = -1;
        if ( interval != null ) {
            try {
                intervalVal = Integer.parseInt(interval);
            }
            catch (Exception e) {
                // just ignore
            }
        }
        return intervalVal;
    }

  	public void setReturnPartName(String opType) {
        setAttribute(JDBC_RETPARTNAME_PROPERTY, JDBCAttribute.JDBC_RETPARTNAME_PROPERTY, "" + opType);
    }
}