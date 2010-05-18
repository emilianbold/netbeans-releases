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
import org.netbeans.modules.uml.core.metamodel.core.constructs.IEnumerationLiteral;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPackage;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IInterface;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

public class JavaEnumerationChangeHandlerTestCase extends AbstractUMLTestCase
{
	IEnumeration enums;
			
	public void setUp()
	{
		
		// Creaing class, enumeration and setting name for enumeration
		enums = createType("Enumeration");
		enums.setName("TestEnum");
		
	}


/**
 * EnumCreateTestCase
 */

	public void testCreate()
	{
		
		// creating an attribute and a literal and adding it to the enumeration
		IAttribute enumAttr = enums.createAttribute("int","TestAttr");
		enums.addAttribute(enumAttr);
		IEnumerationLiteral enLit = enums.createLiteral("BoolLit");
		enums.addLiteral(enLit);
		
		// Testing whether the enumeration has created perfectly
		ETList <IOperation> operList = enums.getOperations();
		assertEquals("getTestAttr",operList.get(0).toString());
		assertEquals("setTestAttr",operList.get(1).toString());
		ETList <IEnumerationLiteral> literalList = enums.getLiterals();
		assertEquals(1,literalList.size());
	}


/**
 * EnumDeleteTestCase
 */
	
	public void testDelete()
	{
		
		// Creating an enum literal and adding it to the enum
		IEnumerationLiteral enLit = enums.createLiteral("BoolLit");
		enums.addLiteral(enLit);
		
		// Creating a class and adding the enum to the class.
		// Checking whether the enum exists in the class.
		IClass newCls = createClass("TestClass");
		newCls.addElement(enums);
		ETList <IElement> elemList = newCls.getElements();
		IEnumeration clsItmEnum = (IEnumeration)elemList.item(1);
		assertEquals("BoolLit",clsItmEnum.getLiterals().item(0).toString());
		ETList <IOperation> operList = newCls.getOperations();
		
		// Deleting the enum
		enums.delete();
		
		// Checking whether the enum has been deleted perfectly.
		elemList = newCls.getElements();
		assertEquals(1,elemList.size());
		boolean elemExist = false;
		for(IElement elems : elemList)
		{
			if(elems.toString().equals("TestEnum"))
			{
				elemExist = true;
			}
		}
		assertFalse(elemExist);
	}


/**
 * EnumNameChangeTestCase
 */
	public void testNameChange()
	{
		// Creating an attribute, literal and adding it to the enum.
		IAttribute enumAttr = enums.createAttribute("int","TestAttr");
		enums.addAttribute(enumAttr);
		IEnumerationLiteral enLit = enums.createLiteral("BoolLit");
		enums.addLiteral(enLit);
		
		// Checking whether the attribute and the literal exists in the enum
		ETList <IOperation> operList = enums.getOperations();
		assertEquals("getTestAttr",operList.get(0).toString());
		assertEquals("setTestAttr",operList.get(1).toString());
		ETList <IEnumerationLiteral> literalList = enums.getLiterals();
		assertEquals(1,literalList.size());
		
		// Renaming the literal
		enums.setName("NameChangeEnum");
		
		// Checking whether the renaming has happened perfectly.
		operList = enums.getOperations();
		assertEquals("getTestAttr",operList.get(0).toString());
		assertEquals("setTestAttr",operList.get(1).toString());
		literalList = enums.getLiterals();
		assertEquals(1,literalList.size());
		
	}


/**
 * TransformClassToEnumerationTestCase
 */
	public void testTransformClassToEnumeration()
	{
		// Creating attribute, operation, class and 
		// adding the attribute and the operation to the class.
		IClass newCls = createClass("TransformClassToEnumeration");
		IAttribute attr = newCls.createAttribute("int","TestAttr");
		IOperation oper = newCls.createOperation("int","TestOper");
		newCls.addAttribute(attr);
		newCls.addOperation(oper);
		
		// Transfrming the class to enumeration
		IEnumeration transEnum = (IEnumeration)newCls.transform("Enumeration");
		
		// Checking whether the transformation has happened perfectly.
		assertTrue(transEnum instanceof IEnumeration);
		ETList <IOperation> clsOpers = transEnum.getOperations();
		assertTrue(clsOpers.isInList(oper));
		assertEquals("getTestAttr",clsOpers.item(1).toString());
		assertEquals("setTestAttr",clsOpers.item(2).toString());
		ETList <IAttribute> clsAttr = transEnum.getAttributes();
		assertEquals(1,clsAttr.size());
		assertEquals("TestAttr",clsAttr.item(0).toString());
	}


/**
 * TransformInterfaceToEnumerationTestCase
 */
	
	public void testTransformInterfaceToEnumeration()
	{
		
		// Creating attribute, operation, interface and 
		// adding the attribute and the operation to the interface.
		IInterface intrfce = createInterface("TestInterface"); 
		IAttribute attr = intrfce.createAttribute("int","TestAttr");
		IOperation oper = intrfce.createOperation("int","TestOper");
		intrfce.addAttribute(attr);
		intrfce.addOperation(oper);
		
		// Transfrming the interface to enumeration
		IEnumeration transEnum = (IEnumeration)intrfce.transform("Enumeration");
		
		// Checking whether the transformation has happened perfectly.
		assertTrue(transEnum instanceof IEnumeration);
		ETList <IOperation> clsOpers = transEnum.getOperations();
		assertEquals(1,clsOpers.size());
		assertEquals("TestOper",clsOpers.item(0).toString());
		ETList <IAttribute> clsAttr = transEnum.getAttributes();
		assertEquals(1,clsAttr.size());
		assertEquals("TestAttr",clsAttr.item(0).toString());
	}

	
/**
 * EnumNamespaceChangeTestCase
 */
	public void testNameSpaceChange()
	{
		// Creating a package and name it
		IPackage tstPackg = createType("Package");
		tstPackg.setName("TestPackage");
		
		// Adding the enum to the package
		tstPackg.addOwnedElement(enums);
		
		// Creating a new package and name it
		IPackage newTestPackg = createType("Package");
		newTestPackg.setName("NewTestPackage");
		
		// Moving the enum to the new package
		newTestPackg.addOwnedElement(enums);
		
		// Checking whether the enum has been moved to the new package.
		assertEquals("NewTestPackage",enums.getOwner().toString());
		assertEquals(0,tstPackg.getOwnedElementCount());
		assertEquals(1,newTestPackg.getOwnedElementCount());
		
	}
}
