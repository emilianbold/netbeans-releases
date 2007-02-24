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
 * File       : VBParameterStateHandler.java
 * Created on : Dec 12, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.statehandlers;

import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenDescriptor;

/**
 * @author Aztec
 */
public class VBParameterStateHandler extends ParameterStateHandler
{

    public VBParameterStateHandler(String language)
    {
        super(language);
    }

    public VBParameterStateHandler(String language, String direction)
    {
        super(language, direction, "Parameter");
    }

    public VBParameterStateHandler(
        String language,
        String direction,
        String stateName)
    {
        super(language, direction, stateName);
    }
    
    public void initialize()
    {
        super.initialize();
        setNodeAttribute("type", "Variant");
    }
    
    public void processToken(ITokenDescriptor pToken, String language)
    {
        if(pToken == null) return;
        
        super.processToken(pToken, language);
        
        String tokenType = pToken.getType();
        if("Modifier".equals(tokenType))
        {
            String value = pToken.getValue();
            if(value != null)
            {
                if(isModifierSame("Optional", language, value) == true)
                {
                    setNodeAttribute("isOptional", "true");
                }
                else if(isModifierSame("ByRef", language, value) == true)
                {
                    setNodeAttribute("isByRef", "true");
                }
                else if(isModifierSame("ByVal", language, value) == true)
                {
                    setNodeAttribute("isByVal", "true");
                }
                else if(isModifierSame("AddressOf", language, value) == true)
                {
                    setNodeAttribute("isAddressOf", "true");
                }
            }
        }
    }
    
    protected void handleName(ITokenDescriptor pToken) 
    {
        if(pToken == null) return;
        
        handleName(pToken);

        String value = pToken.getValue();

        updateByTypeSpecifier(value);
    }

    protected void updateByTypeSpecifier(String typeName) 
    {
        if(typeName != null)
        {
            String value = null;
            String lastChar = typeName.substring(typeName.length() - 1);
            if("%".equals(lastChar))
            {
                value = "Integer";
            }
            else if("!".equals(lastChar))
            {
                value = "Single";
            }
            else if("$".equals(lastChar))
            {
                value = "String";
            }
            else if("@".equals(lastChar))
            {
               value = "Decimal";
            }
            else if("#".equals(lastChar))
            {
               value = "Double";
            }
            else if("&".equals(lastChar))
            {
               value = "Long";
            }

            if(value != null)
            {
                setNodeAttribute("type", value);
            }
        }
    }



}
