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
package org.netbeans.modules.xslt.tmap.model.impl;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.OperationParameter;
import org.netbeans.modules.xml.xam.Reference;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.netbeans.modules.xslt.tmap.model.api.OperationReference;
import org.netbeans.modules.xslt.tmap.model.api.Variable;
import org.netbeans.modules.xslt.tmap.model.api.WSDLReference;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public abstract class AbstractVariable implements Variable {

    private static final Logger LOGGER = Logger.getLogger(Variable.class.getName());
    private TMapModelImpl myModel;
    private OperationReference myOperationReference;
    private Reference<Message> myMessage;
    private String myName;
    
    public AbstractVariable(TMapModelImpl model, String varName, OperationReference operationRef) {
        myModel = model;
        myOperationReference = operationRef;
        myName = varName;
    }

    public String getName() {
        return myName;
    }

    public void setName(String name) {
        myName = name;
    }

    public void setOperationReference(OperationReference operation) {
        myOperationReference = operation;
    }

    public TMapModelImpl getModel() {
        return myModel;
    }
    
    protected Reference<Message> getMessage(OperationParameter operationParam) {
        if (operationParam == null) {
            LOGGER.log(Level.INFO, "Variable operationParam is null");
            return null;
        }
        
        Reference<Message> messageRef = operationParam.getMessage();
        
        return messageRef;
    }

}
