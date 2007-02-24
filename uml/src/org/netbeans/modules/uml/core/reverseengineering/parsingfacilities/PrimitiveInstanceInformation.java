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

/**
 */
public class PrimitiveInstanceInformation extends InstanceInformation
{
    public PrimitiveInstanceInformation()
    {
    }

    public PrimitiveInstanceInformation(String name, String type)
    {
        super(name, type);
    }

    /**
     * Checks if the instance has been initialized.  If the instance
     * information has not been initailized then the information is
     * not valid.
     *
     * @param *pVal [in] The value.
     */
    public boolean isValid()
    {
        String name = getInstanceName();
        String type = getInstanceTypeName();
        // If the name of the instance and the type of the instance
        // has been set then the CPrimitiveInstanceInformation is valid.  Otherwise
        // The CPrimitiveInstanceInformation is not valid.
        return name != null && type != null && name.length() > 0 &&
                type.length() > 0;
    }

    /**
     * Retrieves the name of the type that was used to instantiate 
     * instance.
     *
     *
     * @return The type name.
     */
    public String getInstantiatedTypeName()
    {
        return m_TypeName;
    }

    /**
     * Sets the type name used to instantiate the instance.;
     *
     * @param typeName [in] The type name.
     * @param pClassLoader [in] The class loader used to locate the type.
     */
    public void setInstantiatedType(String typeName, 
                                    IREClassLoader pClassLoader)
    {
        if (typeName != null && typeName.length() > 0)
            m_TypeName = typeName;
    }

    /**
     * Check if a instance is derived from a specified class.  The class hiearchy of the 
     * instance will be traced until the desiredType is located.
     *
     * @param desiredType [in] The type to find.
     * @param pLoader [in] The class loader to use when searching for type information.
     *
     * @return true if the desired type is found.
     */
    public boolean isDerivedFrom(String desiredType, IREClassLoader pLoader)
    {
        String primitiveType = getPrimitiveType();
        return ("Integer Constant".equals(primitiveType) &&
                        ("short".equals(desiredType) ||
                         "char".equals(desiredType) ||
                         "int".equals(desiredType) ||
                         "long".equals(desiredType) ||
                         "float".equals(desiredType) ||
                         "double".equals(desiredType)))
            || ("Character Constant".equals(primitiveType) &&
                        ("int".equals(desiredType) ||
                         "long".equals(desiredType) ||
                         "float".equals(desiredType)  ||
                         "double".equals(desiredType)))
            || ("Long Constant".equals(primitiveType) &&
                        ("float".equals(desiredType) ||
                         "double".equals(desiredType)))
            || ("Float Constant".equals(primitiveType) &&
                        "double".equals(desiredType))
            || ("Double Constant".equals(primitiveType) &&
                        "double".equals(desiredType))
            || "String Constant".equals(primitiveType);
    }

    public boolean isPrimitive()
    {
        return true;
    }

    /**
     * Initializes the creation event data.
     *
     * @param pEvent [in] The event to initialize.
     */
    public void setCreationEventData(Node event)
    {
        if (event == null) return;
        
        super.setCreationEventData(event);
        
        Node descs = ensureElementExists(event, "TokenDescriptors",
                                    "TokenDescriptors");
        if (descs != null)
            createDescriptor(descs, "IsPrimitive", "true");
    }
    
    /**
     * Initializes the IDestroyEvent before it is sent to the operation 
     * detail listeners.  InitializeDestroyEvent is virtual so decendents
     * can perform additional intialization.  This version will initialize
     * the properties InstanceName, InstanceTypeName, and IsPrimitive.  The
     * IsPrimitive will be set to true.
     *
     * @param pEvent [in] The event to initalize.
     */
    protected void initializeDestroyEvent(Node event)
    {
        if (event == null) return;
        
        super.initializeDestroyEvent(event);
        Node descs = ensureElementExists(event, "TokenDescriptors",
                "TokenDescriptors");
        if (descs != null)
            createDescriptor(descs, "IsPrimitive", "true");
    }

    /**
     * Sets the primitive type.
     *
     * @param type [in] The name of the primitive.
     */
    public void setPrimitiveType(String type)
    {
        setInstanceTypeName(type);
    }

    /**
     * Retrieves the primitive type.
     *
     * @return The type name.
     */
    public String getPrimitiveType()
    {
        return getInstanceTypeName();
    }
    
    private String m_TypeName;
}