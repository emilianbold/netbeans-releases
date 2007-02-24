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
 * File       : MethodOptionGroupStateHandler.java
 * Created on : Dec 12, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.statehandlers;

import org.dom4j.Node;

/**
 * @author Aztec
 */
public class MethodOptionGroupStateHandler extends ElseConditionalStateHandler
{
    private String m_JumpCondition = "";
    private boolean m_FoundTest;


    /**
     * @param language
     */
    public MethodOptionGroupStateHandler(String language, String jumpCondition)
    {
        super(language);
        m_JumpCondition = jumpCondition;
    }
    
    public String writeTestXMI(Node pNode)
    {
        if(m_FoundTest)
        {
            m_JumpCondition += ", ";
        }

        m_JumpCondition +=  super.writeTestXMI(pNode); 
   
        m_FoundTest = true;
        return m_JumpCondition;   
    }

}
