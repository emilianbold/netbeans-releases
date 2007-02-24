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
public class DependencyEvent extends ParserData implements IDependencyEvent
{
    /**
     * Retrieves the supplier of the dependency.  The supplier will be
     * specified using the UML fully scoped name of the supplier model element.
     * @param pVal [out] The name of the supplier.
     */
    public String getSupplier()
    {
        return XMLManip.getAttributeValue(getEventData(), "supplier");
    }

    /**
     * Retrieves the client of the dependency.  The client will be specified 
     * using the UML fully scoped name of the supplier model element.
     * @param pVal [out] The name of the client.
     */
    public String getClient()
    {
        return XMLManip.getAttributeValue(getEventData(), "client");
    }

    /**
     * Determines if the dependency is a package dependency or a class dependency.
     * 
     * @param pVal [out] True if the dependency is a class dependency.
     */
    public boolean getIsClassDependency()
    {
        ITokenDescriptor desc = getTokenDescriptor("Class Dependency");
        if (desc instanceof IXMLTokenDescriptor)
        {
            Node node = ((IXMLTokenDescriptor) desc).getTokenDescriptorNode();
            if (node != null)
                return XMLManip.getAttributeBooleanValue(node, "value");
        }
        return false;
    }

    public boolean isStaticDependency()
    {
        ITokenDescriptor desc = getTokenDescriptor("Static Dependency");
        if (desc instanceof IXMLTokenDescriptor)
        {
            Node node = ((IXMLTokenDescriptor) desc).getTokenDescriptorNode();
            if (node != null)
                return XMLManip.getAttributeBooleanValue(node, "value");
        }
        return false;
    }
    
    /**
     * Retrieves the package name that is the reciever of the dependency.  If the dependency is 
     * a class dependency then the package name is the package that contains the class.
     * 
     * @param pVal [out] The name of the package.
     */
    public String getSupplierPackage()
    {
        String ret = "";
        String supplierName = getSupplier();
        boolean isClassDep = getIsClassDependency();
        if (isClassDep)
        {
            int pos = supplierName.lastIndexOf("::");
            // If the "::" seperator was not found in the supplier name then the supplier
            // name is the name of a class.  Therefore return an empty string.
            if (pos != -1)
                ret = supplierName.substring(0, pos);
        }
        else
        {
            // Since the dependency event does not represent a class dependency 
            // the package name is the supplier name.
            ret = supplierName;
        }
        return ret;
    }

    /**
     * Retrieves the name of the class that is the reciever of the dependency.  The class name 
     * property is only valid when the dependeny is a class dependency.
     * 
     * @param pVal [out] The name of the class.
     */
    public String getSupplierClassName()
    {
        String ret = null;
        String supplierName = getSupplier();
        boolean isClassDep = getIsClassDependency();
        
        // I can only retrieve the class name if the dependency event is a class dependency.
        // Therefore, If I do not have a class dependency return null
        if (isClassDep)
        {
            int pos = supplierName.lastIndexOf("::");
            ret = pos != -1? supplierName.substring(pos + 2) : supplierName;
        }
        
        return ret;
    }

    /**
     * Test if the specified class name is the same as the supplier of the dependency.  
     * This method is only valid if the dependency is a class dependency.
     * 
     * @param className [in] The name of the class to test.
     * @param pVal [out] True if the class is the same class.
     */
    public boolean isSameClass(String className)
    {
        boolean isClassDep = getIsClassDependency();
        if (isClassDep)
        {
            if (className != null && className.length() > 0)
            {    
                // First is the simple test.  Test if the name that was wanted is the same
                // as the supplier name.
                if (className.equals(getSupplier()))
                    return true;
                
                // Now check if the wanted name is the same as the supplier class name.
                String supplierClass = getSupplierClassName();
                if (className.equals(supplierClass))
                    return true;
            }
        }
        else
            throw new IllegalStateException("DependencyEvent is not " +
                    "a class dependency");
        return false;
    }
}
