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
import org.netbeans.modules.xml.axi.AXIModel;
import org.netbeans.modules.xml.schema.model.ReferenceableSchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public interface TransformationDesc extends XsltMapEntity {
    
    TransformationDescType getType();
    
    TransformationUC getParent();

    XsltMapModel getModel();
    
    String getPartnerLink();

    void setPartnerLink(String partnerLink);

    String getRoleName();

    void setRoleName(String roleName);

    String getPortType();

    void setPortType(String portType);

    void setOperation(String operation);

    String getOperation();

    String getMessageType();

    void setMessageType(String messageType);

    void setMessageType(Operation inputOperation, Operation outputOperation);

    String getFile();

    void setFile(String file);

    String getTransformJBI();

    void setTransformJBI(boolean transformJBI);
    
    void setTransformJBI(String transformJBI);

    boolean isTransformJBI();
    
    boolean isEqualInputFile(FileObject xsltFile);

    ReferenceableSchemaComponent getTargetType(FileObject projectRoot);
    
    ReferenceableSchemaComponent getSourceType(FileObject projectRoot);

    AXIComponent getTargetAXIType(FileObject projectRoot);
    
    AXIComponent getSourceAXIType(FileObject projectRoot);

    AXIComponent getPltTargetAXIType(FileObject projectRoot);
    
    AXIComponent getPltSourceAXIType(FileObject projectRoot);

    AXIModel getSourceAxiModel(FileObject projectRoot);
    
    SchemaModel getSourceSchema(FileObject projectRoot);
    
    SchemaModel getTargetSchema(FileObject projectRoot);

    AXIModel getTargetAxiModel(FileObject projectRoot);
    
}
