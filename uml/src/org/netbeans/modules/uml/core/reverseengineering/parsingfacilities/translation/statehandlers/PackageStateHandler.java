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
