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

import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.xam.Reference;
import org.netbeans.modules.xslt.tmap.model.api.OperationReference;
import org.netbeans.modules.xslt.tmap.model.api.WSDLReference;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public class OutputVariableImpl extends AbstractVariable {

    OperationReference myOperationRef;

    public OutputVariableImpl(TMapModelImpl model, String varName, OperationReference operationRef) {
        super(model, varName, operationRef);
        myOperationRef = operationRef;
    }

    public Reference<Message> getMessage() {
        if (myOperationRef == null) {
            return null;
        }
        
        Reference<Operation> opRef = myOperationRef.getOperation();
        if (opRef == null) {
            return null;
        }
        
        Operation operation = opRef.get();
        if (operation == null) {
            return null;
        }
        return getMessage(operation.getOutput());
    }

}

