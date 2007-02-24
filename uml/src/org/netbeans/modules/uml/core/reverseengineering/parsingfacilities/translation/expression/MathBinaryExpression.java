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
 * File       : MathBinaryExpression.java
 * Created on : Dec 9, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.expression;

import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenDescriptor;


public class MathBinaryExpression extends BinaryExpression
{
    private ITokenDescriptor  m_pPrecedenceStart = null;
    private ITokenDescriptor  m_pPrecedenceEnd = null;

    public void clear()
    {
        m_pPrecedenceStart = null;
        m_pPrecedenceEnd = null;
    }
    
    public MathBinaryExpression()
    {
        super();
    }
    
    public String toString()
    {
        String retVal = "";        
        if(getLeftHandPrecedenceTokenStart() != null)
        {
            String value = getLeftHandPrecedenceTokenStart().getValue();
            retVal += value;
        }
        
        retVal += getLeftHandSideString();
        
        if(getLeftHandPrecedenceTokenEnd() != null)
        {
            String value =  getLeftHandPrecedenceTokenEnd().getValue();
            retVal += value;
        }
        
        retVal += getOperatorAsString();
        
        if(getRightHandPrecedenceTokenStart() != null)
        {
            String value = getLeftHandPrecedenceTokenStart().getValue();
            retVal += value;
        }
        
        retVal += getRightHandSideString();
        
        if(getRightHandPrecedenceTokenEnd() != null)
        {
            String value =  getLeftHandPrecedenceTokenEnd().getValue();
            retVal += value;
        }
        return retVal;
    }
    
//    public void processToken(ITokenDescriptor pToken, String language)
//    {
//        if(pToken != null)
//        {
//            String type = pToken.getType();
//            if("Precedence Start".equals(type))
//            {
//                m_pPrecedenceStart = pToken;
//            }
//            else if("Precedence End".equals(type))
//            {
//                m_pPrecedenceEnd = pToken;
//            }
//            else
//            {
//                super.processToken(pToken, language);
//            }
//        }
//    }
    
    public void initialize()
    {
        // No any repective code in C++..
    }
    
}
