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
