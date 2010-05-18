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


package org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpcomponent;

import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IClass;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IEnumeration;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPackage;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IInterface;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

public class JavaInterfaceChangeHandlerTestCase extends AbstractUMLTestCase 
{
	IInterface intrfce;
	IPackage packg ;
	ETList <INamedElement> elems;
	
	public void setUp()
	{
		// Creating the interface, package and adding th
		intrfce = createInterface("NewInterface");
		packg = createType("Package");
		packg.addOwnedElement(intrfce);
	}
	
	
/**
 * InterfaceNameChangeTestCase
 * 
 */
	
	public void testNameChange()
	{
		// Getting the elements owned by the package
		ETList <INamedElement> interfaceList = packg.getOwnedElements();
		
		// Testing whether interface exists in the package
		assertEquals(1,interfaceList.size());
		assertEquals("NewInterface",interfaceList.item(0).toString());
		
		// Rename the interface
		intrfce.setName("RenamedInterface");
		
		// Testing whether renamed interface exists in the package
		assertEquals(1,interfaceList.size());
		assertEquals("RenamedInterface",interfaceList.item(0).toString());
		
	}

/**
 * InterfaceNamespaceChangeTestCase
 * Moving an interface from one package to another
 */
	
	public void testNamespaceChange()
	{
		// Checking whether the interface exists in the package
		elems = packg.getOwnedElements();
		assertEquals("NewInterface",elems.item(0).toString());
		
		// Creating a new package and moving the interface to the new package
		IPackage newPackg = createType("Package");
		newPackg.addOwnedElement(intrfce);
		
		// Checking whether the interface exists in the new package
		elems = packg.getOwnedElements();
		assertEquals(0,elems.size());
		elems = newPackg.getOwnedElements();
		assertEquals("NewInterface",elems.item(0).toString());
	}


/**
 * TransformClassToInterfaceTestCase
 * Tests the transformation of a class to an interface.
 */
	public void testTransformClassToInterface()
	{
		
		// Creating a class, operation and adding the operation to the class. 
		IClass newCls = createClass("TransformClassToInterface");
		IOperation oper = createType("Operation");
		oper.setName("TestOper");
		newCls.addOperation(oper);
		
		// Checking the operaions exists in the class
		ETList<IOperation> opers = newCls.getOperations();
		assertEquals(2,opers.size());
		assertEquals("TransformClassToInterface",opers.item(0).toString());
		
		// Transforming the class to an interface 
		IInterface transCls = (IInterface)newCls.transform("Interface");
		
		// Checking whether the transformation happened perfectly
		assertTrue(transCls instanceof IInterface);
		opers = transCls.getOperations();
		assertEquals(1,opers.size());
		assertEquals("TestOper",opers.item(0).toString());
	}
	

/**
 * TransformEnumToInterfaceTestCase
 * Tests the transformation of an enum to an interface.
 */

	public void testTransformEnumToInterface()
	{
		// Creating an enumeration, attribute and adding the attribute to the enumeration.
		IEnumeration enums = createType("Enumeration");
		enums.setName("TestEnum");
		IAttribute newAttr = enums.createAttribute("int","NewAttr");
		enums.addAttribute(newAttr);
		
		// Checking the attribute modifiers
		assertFalse(newAttr.getIsStatic());
		assertFalse(newAttr.getIsFinal());
		
		// Transforming the enum to interface
		IInterface transEnum = (IInterface)enums.transform("Interface");
		
		// Checking whether the transformation happened properly.
		assertTrue(transEnum instanceof IInterface);
		ETList <IAttribute> attrList = transEnum.getAttributes();
		assertEquals(1,attrList.size());
		assertEquals("NewAttr",attrList.item(0).toString());
		assertTrue(attrList.item(0).getIsStatic());
		assertTrue(attrList.item(0).getIsFinal());
	}
	
}
