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
