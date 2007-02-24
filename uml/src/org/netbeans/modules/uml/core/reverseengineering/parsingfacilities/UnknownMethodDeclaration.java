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


package org.netbeans.modules.uml.core.reverseengineering.parsingfacilities;

import org.dom4j.Node;

import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.expression.EventExecutor;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 */
public class UnknownMethodDeclaration extends MethodDeclaration
{
    private String m_Name;

    public UnknownMethodDeclaration()
    {
        
    }
    

    public UnknownMethodDeclaration(String name)
    {
        m_Name = name;
    }
    
    
    /**
     * Generates the XMI fragment that represent the method call.
     *
     * @param pParentNode [in] The parent node of the result.
     * @param pVal [out] The result.
     */
    public Node generateXML(Node parentNode, 
                     int lineNumber, 
                     InstanceInformation instance, 
                     ETList<ETPairT<InstanceInformation, String>> arguments)
    {
        if (parentNode == null) return null;
        
        Node callNode = createNode(parentNode, "UML:CallBehaviorAction");
        if (callNode != null)
        {
            XMLManip.setAttributeValue(callNode, "name", m_Name);
            XMLManip.setAttributeValue(callNode, "instance", 
                    instance.getInstanceName());
            XMLManip.setAttributeValue(callNode, "line", 
                    String.valueOf(lineNumber));
            
            addArguments(callNode, arguments);
            
            String typename = instance.getInstanceTypeName();
            if (typename != null && typename.length() > 0)
                EventExecutor.createTokenDescriptor(callNode, typename, 
                        "DeclaringType", -1, -1, -1, -1);
        }
        return callNode;
    }

    /**
     * Adds the XML to represent the calls argumnts.
     *
     * @param pParentNode [in] The parent node.
     * @param arguments [in] The method call arguments .
     */
    public void addArguments(Node parentNode, 
                        ETList<ETPairT<InstanceInformation, String>> arguments)
    {
        if (parentNode == null) return;
        
        Node arg = createNode(parentNode, "UML:PrimitiveAction.argument");
        if (arg != null)
            for (int i = 0, count = arguments.size(); i < count; ++i)
                createInputPin(arg, null, arguments.get(i).getParamOne());
    }
}