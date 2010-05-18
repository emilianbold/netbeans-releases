/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
                createInputPin(arg, null, arguments.get(i).getParamOne(),arguments.get(i).getParamTwo());
    }
}
