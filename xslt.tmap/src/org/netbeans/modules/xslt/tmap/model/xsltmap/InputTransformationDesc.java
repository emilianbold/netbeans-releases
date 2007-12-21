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
package org.netbeans.modules.xslt.tmap.model.xsltmap;

import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.OperationParameter;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.netbeans.modules.xslt.tmap.util.Util;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public class InputTransformationDesc extends AbstractTransformationDesc {
    
    public InputTransformationDesc(XsltMapModel model, TransformationUC transformUC) {
        super(model, transformUC);
    }

    public TransformationDescType getType() {
        return TransformationDescType.INPUT;
    }
    
    public AXIComponent getTargetAXIType(FileObject projectRoot) {
        TransformationUC tUC = getParent();
        if (tUC == null) {
            return null;
        }
        
        if (TransformationType.REQUEST_REPLY_SERVICE.equals(tUC.getTransformationType())) {
            return getPltTargetAXIType(projectRoot);
        }
        
        OutputTransformationDesc outtDesc = tUC.getOutputTransformationDesc();
        return outtDesc == null ? null : outtDesc.getPltSourceAXIType(projectRoot);
    }

    public AXIComponent getSourceAXIType(FileObject projectRoot) {
        return getPltSourceAXIType(projectRoot);
    }

    public void setMessageType(Operation inputOperation, Operation outputOperation) {
        TransformationUC tUC = getParent();
        if (tUC == null || (inputOperation == null && outputOperation == null)) {
            return;
        }
        
        String messageType = null;
        messageType = TransformationType.REQUEST_REPLY_SERVICE.equals(tUC.getTransformationType())
            ? Util.getMessageType(inputOperation, false) 
            : Util.getMessageType(outputOperation, true);
        
        setMessageType(messageType);
    }
    
}
