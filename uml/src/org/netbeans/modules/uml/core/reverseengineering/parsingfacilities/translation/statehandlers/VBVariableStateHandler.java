/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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


/*
 * File       : VBVariableStateHandler.java
 * Created on : Dec 12, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.statehandlers;

import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.Identifier;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.InstanceInformation;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenDescriptor;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.TokenDescriptor;

/**
 * @author Aztec
 */
public class VBVariableStateHandler extends MethodVariableStateHandler
{

    public VBVariableStateHandler(String language, boolean treatAsExpression)
    {
        super(language, treatAsExpression);
    }
    
    /**
     * Adds a new instance informaton to the symbol table.  If the 
     * instance information is not complete it will not be added to
     * the symbol table.  If the instance has already been added to
     * the symbol table it will not be added again.
     *
     * @return The instance that was added to the symbol table, NULL
     *         if the instance information is not complete.
     */
    public InstanceInformation addInstanceToSymbolTable()
    {
        Identifier typeID = getTypeIdentifier();
        if(typeID != null)
        {
            ITokenDescriptor pDescriptor = new TokenDescriptor();
            if(pDescriptor != null)
            {
                pDescriptor.setType("Identifier");
                pDescriptor.setValue("Variant");
                addTypeToken(pDescriptor);

                setIsPrimitive(true);
            }      
       }
       return super.addInstanceToSymbolTable();
    }

}
