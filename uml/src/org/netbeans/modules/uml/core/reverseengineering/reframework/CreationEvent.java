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

import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;
import org.netbeans.modules.uml.core.support.umlsupport.Log;

/**
 */
public class CreationEvent extends MethodDetailParserData
    implements ICreationEvent
{

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.ICreationEvent#getREClass()
     */
    public IREClass getREClass()
    {
        Node n = getXMLNode("UML:Class|UML:Interface|UML:Enumeration");
        if (n != null)
        {    
            IREClass cl = new REClass();
            cl.setEventData(getEventData());
            return cl;
        }
        return null;
    }

    /**
     * Specifies whether or not the instance is a primitive data type.
     *
     * @param *pVal [out] true if a primitive, false otherwise.
     */
    public boolean getIsPrimitive()
    {
        return Boolean.valueOf(getTokenDescriptorValue("IsPrimitive"))
                    .booleanValue();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.ICreationEvent#getIsStatic()
     */
    public boolean getIsStatic()
    {
        // Ditto in C++ code.
        return false;
    }

    /**
     * The instance name of the object.
     *
     * @param pVal [out] The name.
     */
    public String getInstanceName()
    {
        Node outputPinNode = getXMLNode("UML:OutputPin");
        return XMLManip.getAttributeValue(outputPinNode, "value");
    }

    /**
     * The type name of the instance.  The instance type is the the 
     * type that was declared.  As opposed to the instantiated type 
     * name which is the type that was used to create the object.
     *
     * Example: Foo f = new Bar();
     *
     * The instance name is Foo, instantiated type name is Bar
     *
     * <I>The instance name is Foo, instantiated type name is Bar
     * Foo is the interface used when accessing the instance f.</I>
     *
     * @param pVal [out] The instance type name.
     */
    public String getInstanceTypeName()
    {
        return XMLManip.getAttributeValue(getEventData(), "classifier");
    }

    /**
     * The instantiated type name of the instance.  The instantiated 
     * type is the the type that was to create the instance.  
     * As opposed to the instance type name which is the type
     * that was used when declaring the object.
     *
     * Example: Foo f = new Bar();
     *
     * <I>The instance name is Foo, instantiated type name is Bar
     * Foo is the interface used when accessing the instance f.</I>
     *
     * @param *pVal [out] The instantiated type name.
     */
    public String getInstantiatedTypeName()
    {
        return getDescriptorValue("InstantiatedTypeName");
    }
    
    public IREClass getDeclaringClass()
	{
        IREClass  pClass = null;
        try
		{
       	    Node pParentNode = getXMLNode("DeclaringClass");   
       	    if(pParentNode != null)
       	    {
       	    	String query = "UML:Class|UML:Interface|UML:Enumeration"; 
       	    	Node  pNode = pParentNode.selectSingleNode(query);
       	    	if(pNode != null)
       	    	{
       	    		pClass = new REClass();
       	    		if(pClass != null)
       	    		{
       	    			pClass.setEventData(pNode);
       	    			return pClass;
       	    		}
       	    	}
       	    }
		}
        catch(Exception e)
		{
        	Log.stackTrace(e);
		}
        return pClass;
	}


    public IREOperation getConstructor()
    {
        IREOperation  pOper = null;
        try
		{
        	Node  pNode = getXMLNode("UML:Operation");   
        	if(pNode != null)
        	{
        		pOper = new REOperation();
        		if(pOper != null)
        		{
        			pOper.setEventData(pNode);
        			return pOper;
        		}
        	}
		}
        catch(Exception e)
		{
        	Log.stackTrace(e);
		}
        return  pOper;
    }

}
