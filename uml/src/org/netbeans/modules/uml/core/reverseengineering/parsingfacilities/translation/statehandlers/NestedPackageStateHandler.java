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
