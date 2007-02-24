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

package org.netbeans.modules.uml.core.reverseengineering.reframework;

import org.dom4j.Node;

import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenDescriptor;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;

/**
 */
public class RECallAction extends REAction implements IRECallAction
{
    /**
     * Retrieves the package that contains the return types class.
     *
     * @param pVal [out] The package name.
     */
    public String getReturnTypePackage()
    {
        ITokenDescriptor desc = getTokenDescriptor("ReturnTypePackage");
        return desc != null? desc.getValue() : null;
    }

    /**
     * Retrieves the returns type class name.  The class name will be the short
     * version of the name.  The class name will not contain the package that 
     * contains the class.
     * 
     * @param pVal [out[ The class name.
     */
    public String getReturnTypeClass()
    {
        ITokenDescriptor desc = getTokenDescriptor("ReturnTypeClass");
        return desc != null? desc.getValue() : null;
    }

    /**
     * Retrieves if the the operation that is being called is a static method.
     *
     * @param pVal [out] True if the operation is static, false otherwise.
     */
    public boolean getIsOperationStatic()
    {
        ITokenDescriptor desc = getTokenDescriptor("StaticOperation");
        if (desc instanceof IXMLTokenDescriptor)
        {
            Node n = ((IXMLTokenDescriptor) desc).getTokenDescriptorNode();
            return Boolean.valueOf(
                    XMLManip.getAttributeValue(n, "value")).booleanValue();
        }
        return false;
    }

    /**
     * Retrieves if the receiving instance is a static instance.
     *
     * @param pVal [out] True if the instance is static, false otherwise.
     */
    public boolean getIsInstanceStatic()
    {
        ITokenDescriptor desc = getTokenDescriptor("StaticInstance");
        if (desc instanceof IXMLTokenDescriptor)
        {
            Node n = ((IXMLTokenDescriptor) desc).getTokenDescriptorNode();
            return Boolean.valueOf(
                    XMLManip.getAttributeValue(n, "value")).booleanValue();
        }
        return false;
    }

    /**
     * The called opertions return type.  The fully scoped name of the return type
     * is given when the the file that contains the return type is found.
     * 
     * @param pVal [out] The return type.
     */
    public String getReturnType()
    {
        ITokenDescriptor desc = getTokenDescriptor("ReturnType");
        return desc != null? desc.getValue() : null;
    }

    /**
     * The fully scoped name of the class that implements the operation.
     * When the operation is implemented by the super class the class that 
     * implements the class may be different than the recieving class.
     * 
     * @param pVal [out] The name of the implementing class.
     */
    public String getImplementingClass()
    {
        ITokenDescriptor desc = getTokenDescriptor("OperationOwner");
        return desc != null? desc.getValue() : null;
    }

    /**
     * Retrieves the name of the class that contains the instance of the object 
     * that is being called.
     * 
     * @param pVal [out] The name the class that owns the instance.
     */
    public String getInstanceOwner()
    {
        ITokenDescriptor desc = getTokenDescriptor("ContainingClass");
        return desc != null? desc.getValue() : null;
    }

    /**
     * Retrieves the name of the object that will recieve the method call.
     * @param pVal [out] The instance name.
     */
    public String getInstanceName()
    {
        ITokenDescriptor desc = getTokenDescriptor("Name");
        return desc != null? desc.getValue() : null;
    }

    /**
     * Retrieves the name of the operation that will be called.  Use 
     * the Arguments property to retrieve the calls parameters.
     * @param pVal [out] The operations name.
     */
    public String getOperationName()
    {
        return XMLManip.getAttributeValue(getEventData(), "operation");
    }
}