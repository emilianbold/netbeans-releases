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
 * File       : NestedPackageStateHandler.java
 * Created on : Dec 12, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.statehandlers;

import org.dom4j.Node;

import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.Identifier;

/**
 * @author Aztec
 */
public class NestedPackageStateHandler extends PackageStateHandler
{
    private Identifier m_ParentName = new Identifier();

    public NestedPackageStateHandler(String language)
    {
        super(language);
    }

    public NestedPackageStateHandler(Identifier parentID, String language)
    {
        super(language);
        m_ParentName = parentID;
    }
    
    public StateHandler createSubStateHandler(String stateName, String language) 
    {
        StateHandler retVal = super.createSubStateHandler(stateName, language);
        
        if(retVal == null)
        {
            if("Class Declaration".equals(stateName))
            {
                String packageName = getFullPackageName();
                retVal = new ClassStateHandler(language, packageName, false);
            }
            else if("Structure Declaration".equals(stateName))
            {
                String packageName = getFullPackageName();
                retVal = new StructureStateHandler(language, packageName, false);
            }
            else if("Enumeration Declaration".equals(stateName))
            {
                String packageName = getFullPackageName();
                retVal = new EnumStateHandler(language, packageName, false);
            }
            else if("Package".equals(stateName))
            {
                retVal = new NestedPackageStateHandler(m_ParentName, language);
            }
            else if("Dependency".equals(stateName))
            {
                retVal = new DependencyStateHandler(language);
            }
            else if("Interface Declaration".equals(stateName))
            {
                String packageName = getFullPackageName();
                retVal = new InterfaceStateHandler(language, packageName, false);
            }

            if(retVal != null && retVal != this)
            {
                Node pClassNode = getDOMNode();

                if(pClassNode != null)
                {
                    retVal.setDOMNode(pClassNode);
                }
            }
        }
        return retVal;
    }
    
    public Identifier getParentPackageName() 
    {
        return m_ParentName;
    }

    public void setParentPackageName(Identifier value) 
    {
        m_ParentName = value;
    }
    
    public String getPackageName() 
    {
        Identifier id = getPackageIdenfier();
        String retVal = m_ParentName.getIdentifierAsSource();     
      
        if(retVal != null)
        {
            retVal += ".";
        }
        retVal += id.getIdentifierAsSource();
        
        return retVal;
    }

    public String getUMLPackageName() 
    {
        Identifier id = getPackageIdenfier();
        String retVal = m_ParentName.getIdentifierAsUML();     
      
        if(retVal != null)
        {
            retVal += "::";
        }
        retVal += id.getIdentifierAsUML();
        
        return retVal;
    }
    
    protected void updateName() 
    {
        super.updateName();
        super.firePackageEvent();
    }

    protected void firePackageEvent() 
    {
        // No valid implementation in the C++ code base.
    }
}
