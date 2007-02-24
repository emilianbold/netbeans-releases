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