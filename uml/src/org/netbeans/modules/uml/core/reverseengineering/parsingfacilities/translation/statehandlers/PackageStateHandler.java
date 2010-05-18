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


/*
 * File       : PackageStateHandler.java
 * Created on : Dec 12, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.statehandlers;

import org.dom4j.Node;

import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParserEventDispatcher;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.Identifier;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IPackageEvent;
import org.netbeans.modules.uml.core.reverseengineering.reframework.PackageEvent;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenDescriptor;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;

/**
 * @author Aztec
 */
public class PackageStateHandler extends TopLevelStateHandler
{
    private Identifier m_PackageName = new Identifier(); 
    private int        m_NestedLevel;
    
    public PackageStateHandler(String language)
    {
        super(language);
        m_NestedLevel = 0;
    }
    
    public StateHandler createSubStateHandler(String stateName, String language) 
    {
        StateHandler retVal = null;
        
        if("Identifier".equals(stateName))
        {
            m_NestedLevel++;
            retVal = this;
        }
        return retVal;
    }
    
    public void initialize() 
    {
        createTopLevelNode("UML:Package");
    }
    
    public void processToken(ITokenDescriptor pToken, String language) 
    {
        if(pToken == null) return;
        
        String tokenType = pToken.getType();

        if("Keyword".equals(tokenType))
        {
           //CreateTopLevelNode(_T("UML:Package")) ;
            handleStartPosition(pToken);
            handleKeyword(pToken);
            handleFilename(pToken);
        }
        else if ("Identifier".equals(tokenType) || 
                  "Scope Operator".equals(tokenType))
        {
            m_PackageName.addToken(pToken);
        }
        else if("Statement Terminator".equals(tokenType))
        {
            handleTerminator(pToken);
            handleEndPostion(pToken);
        }
    }
    
    public void stateComplete(String stateName) 
    {
        if("Identifier".equals(stateName))
        {
            m_NestedLevel--;

            // Now if I am not still in a nested state I must move back to the 
            // general state.
            if(m_NestedLevel == 0)
            {
                updateName();  
            }
        }
        else if("Package".equals(stateName))
        {
            firePackageEvent();
        }
    }
    
    public String getFullPackageName() 
    {
        String retVal = null;
        Node pNode = getDOMNode();
      
        if(pNode != null)
        {
           retVal = XMLManip.getAttributeValue(pNode,"name");
        }
        return retVal;
    }

    public String getPackageName() 
    {
        return getPackageIdenfier().getIdentifierAsSource();
    }

    public String getUMLPackageName() 
    {
        return getPackageIdenfier().getIdentifierAsUML();
    }
    
    protected void updateName() 
    {
        String value = getPackageName();
        createTokenDescriptor("Name",
                                m_PackageName.getStartLine(), 
                                m_PackageName.getStartColumn(), 
                                m_PackageName.getStartPosition(), 
                                value,
                                m_PackageName.getLength());
      
        String umlName = getUMLPackageName();

        setNodeAttribute("name", umlName);
    }

    protected Identifier getPackageIdenfier() 
    {
        return m_PackageName;
    }

    protected void firePackageEvent()
    {
        IPackageEvent pEvent = new PackageEvent();
        if(pEvent != null)
        {
            Node pNode = getDOMNode();
         
            if(pNode != null)
            {
                pEvent.setEventData(pNode);
                IUMLParserEventDispatcher pDispatcher = getEventDispatcher();
            
                if(pDispatcher != null)
                {         
                    pDispatcher.firePackageFound("", pEvent, null);
                }
            }
        }
    }
}
