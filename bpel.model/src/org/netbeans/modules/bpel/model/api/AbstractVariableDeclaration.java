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

/**
 *
 */
package org.netbeans.modules.bpel.model.api;

import org.netbeans.modules.bpel.model.api.references.SchemaReference;
import org.netbeans.modules.bpel.model.api.references.WSDLReference;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.wsdl.model.Message;


/**
 * @author nk160297
 *
 * This interface is common interface for various variable
 * declarations.
 * It is used for referencing to such variable declaration 
 * from other entities. 
 */
public interface AbstractVariableDeclaration {
    
    /**
     * Gets the message type if varaible declaration have WSDL type.
     * 
     * Method returns reference that can point to read=-only Message. 
     * 
     * @return possible object is reference to WSDL model.
     */
    WSDLReference<Message> getMessageType();

    /**
     * Gets the element type if variable declaration have Schema element type.
     * 
     * Method returns reference that can point to read=-only Element.
     * 
     * @return possible object is reference to GlobalElement in schema model.
     */
    SchemaReference<GlobalElement> getElement();
    

    /**
     * Gets the type if variable declaration have Schema type.
     * 
     * Method returns reference that can point to read=-only GlobalType.
     * 
     * @return possible object is reference to GlobalType.
     */
    SchemaReference<GlobalType> getType();
    
    /**
     * Getter for variable name.
     * @return Variable name.
     */
    String getVariableName();
}
