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
