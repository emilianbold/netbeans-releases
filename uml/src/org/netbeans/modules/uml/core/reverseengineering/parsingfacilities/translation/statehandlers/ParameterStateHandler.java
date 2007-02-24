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
 * File       : ParameterStateHandler.java
 * Created on : Dec 11, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.statehandlers;

import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenDescriptor;

/**
 * @author Aztec
 */
public class ParameterStateHandler extends TypeElementStateHandler
{
    private String m_Direction = null;

    public ParameterStateHandler(String language)
    {
        super(language, "Parameter");
    }

    public ParameterStateHandler(String language, String direction)
    {
        super(language, "Parameter");
        m_Direction = direction;
    }
    
    public ParameterStateHandler(String language, String direction, String stateName)
    {
        super(language, stateName);
        m_Direction = direction;
    }
    
    public void initialize() 
    {
        super.initialize();

        if(m_Direction != null)
        {
            setNodeAttribute("direction", m_Direction);
        }
        setNodeAttribute("isQuery", false);
    }
    
    public void processToken(ITokenDescriptor pToken, String language) 
    {
        if(pToken == null) return;
        
        String tokenType = pToken.getType();
        
        // If the parameter is a return type I do not care if the parameter is a
        // primitive.  It will always  be a return type.
        if("result".equals(m_Direction) == false)
        {
           // If the token type is primitive type then the parameter will
           // always be in.  Other wise use the default.  My need to be overridden
           // by a language specific implememtation.
            if("Primitive Type".equals(tokenType))
            {
                setNodeAttribute("direction", "in");
                createTokenDescriptor("IsPrimitive", -1, -1, -1, "true", -1);
            }
        }

        super.processToken(pToken, language);
    }
    
    /**
     * Retrieves the name of the feature that is being added.  This method 
     * is used by TypeElementStateHandler during processing.
     *
     *
     * @return 
     */
    public String getFeatureName()
    {
        return "UML:Parameter";
    }

    
    protected void handleModifier(ITokenDescriptor pToken, String language) 
    {
        if(pToken == null) return;
        
        long   line = pToken.getLine();
        long   col = pToken.getColumn();
        long   pos = pToken.getPosition();
        long   length = pToken.getLength();
        String value = pToken.getValue();

        createTokenDescriptor("Modifier", line, col, pos, value, length);

        // In some langauges certian modifiers specify that a class is abstract or 
        // constant.  So, I will query if the modifier should be handlec as  abstract
        // or leaf modifiers.  The query methods are virtual so they can be overriden
        // when specifing a new langauge.
        if(isLeafModifier(value, language))
        {
            setNodeAttribute("isQuery", true);
        }
    }

    protected boolean isAbstractModifier(String value) 
    {
        // No valid implementation in the C++ code base.
        return false;
    }

    protected boolean isOwnerScopeModifier(String value) 
    {
        // No valid implementation in the C++ code base.
        return false;
    }

}
