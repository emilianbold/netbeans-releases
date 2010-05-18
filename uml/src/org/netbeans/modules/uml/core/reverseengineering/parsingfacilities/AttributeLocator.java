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


// *****************************************************************************
package org.netbeans.modules.uml.core.reverseengineering.parsingfacilities;

import org.netbeans.modules.uml.core.reverseengineering.reframework.IREAttribute;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IREClass;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.support.umlsupport.Log;

/**
 */
public class AttributeLocator extends LocatorEvaluator<InstanceInformation>
{
    private String m_AttrName;
    
    public AttributeLocator(String attrName)
    {
        m_AttrName = attrName;
    }
    
    /**
     * Creates a CPrimitiveInstanceInformation and adds it to the symbol table.
     * If the name or type name is not valid then a CPrimitiveInstanceInformation
     * will not be created.
     *
     * @param name [in] The name of the instance.
     * @param typeName [in] The type of the instance.
     *
     * @return The instance that is added the table.  NULL if the instance
     *         name or type name is not valid.
     */
    public InstanceInformation createPrimitiveInstance(String name,
													   String instanceType, 
													   IREClass instanceOwner)
    {
        if (name != null && instanceType != null)
        {
            PrimitiveInstanceInformation info = 
                    new PrimitiveInstanceInformation();
            info.setPrimitiveType(instanceType);
            info.setInstanceName(name);
            info.setInstanceOwner(instanceOwner);
            return info;
        }
        return null;
    }

    /**
     * Creates a CObjectInstanceInformation and adds it to the symbol table.
     * If the name or type name is not valid then a CPrimitiveInstanceInformation
     * will not be created.
     *
     * @param name [in] The name of the instance.
     * @param typeName [in] The type of the instance.
     *
     * @return The instance that is added the table.  NULL if the instance
     *         name or type name is not valid.
     */
    public InstanceInformation createObjectInstance(String name, 
                                                    IREClass type,
													IREClass instanceOwner)
    {
        if (name != null && type != null)
        {
            String packageName = type.getPackage();
            String typeName    = type.getName();
            if (packageName != null && packageName.length() > 0)
                typeName = packageName + "::" + typeName;
            return createObjectInstance(name, typeName, type, instanceOwner);
        }
        return null;
    }

    /**
     * Creates a CObjectInstanceInformation and adds it to the symbol table.
     * If the name or type name is not valid then a CPrimitiveInstanceInformation
     * will not be created.
     *
     * @param name [in] The name of the instance.
     * @param typeName [in] The type of the instance.
     *
     * @return The instance that is added the table.  NULL if the instance
     *         name or type name is not valid.
     */
    public InstanceInformation createObjectInstance(String name, 
                                                    String typeName,
													IREClass type, 
													IREClass instanceOwner)
    {
        if (name != null && typeName != null)
        {
            InstanceInformation ii = 
                    new ObjectInstanceInformation(name, typeName, type);
            ii.setInstanceOwner(instanceOwner);
            return ii;
        }
        return null;
    }
    
    /**
     * Searches a specific class for an attribute declaration.
     *
     * @param pClass [in] The class to search.
     * @param pVal [out] The attribute instance information
     */
    public InstanceInformation findElement(IREClass receiver, 
                                           IREClassLoader classloader,
                                           InstanceInformation el)
    {
        if (receiver != null && m_AttrName != null)
        {
            ETList<IREAttribute> attrs = receiver.getAttributes();
            Log.out("Found attributes: " + attrs + " in " + receiver.getEventData().asXML());
            if (attrs != null)
            {
                for (int i = 0, count = attrs.size(); i < count; ++i)
                {
                    IREAttribute reat = attrs.get(i);
                    if (reat == null) continue;
                    
                    String attrName = reat.getName();
                    Log.out("Checking attribute name: " + attrName + " against " + m_AttrName);
                    if (m_AttrName.equals(attrName))
                    {
                        String typeName = reat.getType();
                        if (reat.getIsPrimitive())
                            return createPrimitiveInstance(attrName, typeName, 
                                                            receiver);
                        else
                        {    
                            IREClass typeC = 
                                    classloader.loadClass(typeName, receiver);
                            return typeC != null?
                               createObjectInstance(attrName, typeC, receiver)
                             : createObjectInstance(attrName, typeName, null,
                                                    receiver);
                        }
                    }
                }
            }
        }
        return null;
    }
}
