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
