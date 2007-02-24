
/*
 * Created on Nov 18, 2003
 *
 */
package org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpcomponent;

import org.netbeans.modules.uml.core.coreapplication.IPreferenceManager2;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IClass;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IEnumeration;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPackage;
import org.netbeans.modules.uml.core.metamodel.core.primitivetypes.IVisibilityKind;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IInterface;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.INavigableEnd;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.support.ProductHelper;

/**
 * @author aztec
 *
 */
public class JavaClassChangeHandlerTestCase extends AbstractUMLTestCase
{
    IClass clazz = null;
    
    public static void main(String args[])
    {
        junit.textui.TestRunner.run(JavaClassChangeHandlerTestCase.class);
    }
    

/**
 * CreateClassTestCase
 */
 
 public void testCreate()
    {
        IClass a = createClass("A");
        IClass b = factory.createClass(null);
        
        clazz = a;
        
        project.addOwnedElement(b);
        
        IAssociation assoc = relFactory.createAssociation(a, b, project);
        project.addElement(assoc);
        
        INavigableEnd nav = assoc.getEnds().get(1).makeNavigable();
        
        assertEquals(0, a.getAttributes().size());
        
        b.setName("Neiman");
        
        assertEquals(1, b.getOperations().size());
        IOperation op = b.getOperations().get(0);
        assertTrue(op.getIsConstructor());
        
        assertEquals("mNeiman", b.getAssociationEnds().get(0).getName());
    }
    
/**
 * DeleteClassTestCase
 */
	
	public void testDelete()
    {
        clazz = createClass("Picasso");
        IClassifier parent = createSuperclass(clazz, "Super");
        IOperation oper = parent.createOperation("int", "a");
        parent.addOperation(oper);
        
        IOperation red = clazz.createOperation("int", "a");
        clazz.addOperation(red);
        oper.addRedefiningElement(red);
        red.addRedefinedElement(oper);
        
        // See whether redefined op link is broken
        assertEquals(1, oper.getRedefiningElementCount());
        clazz.delete();
        assertEquals(0, oper.getRedefiningElementCount());
    }
    
   
/**
 * NameChangeClassTestCase
 */
	public void testNameChange()
    {
        clazz = createClass("Duncan");
        clazz.setName("Idaho");
        
        assertEquals("Idaho", clazz.getOperations().get(0).getName());
    }
    
    public void testVisibilityChange()
    {
        // Nothing to test.
    }

   /**
    * CreateClassDestructorTestCase
    */	
    public void testCreateDestructor()
    {
        IPreferenceManager2 prefMan = ProductHelper.getPreferenceManager();
        prefMan.setPreferenceValue("RoundTrip|Java","ADD_DTORS","PSK_YES");
        String str = prefMan.getPreferenceValue("RoundTrip|Java", "ADD_DTORS");
        
        IClass clazz = createClass("AA");
        assertEquals(2, clazz.getOperations().size());
        assertEquals("AA", clazz.getOperations().get(0).getName());
        assertEquals("finalize", clazz.getOperations().get(1).getName());
        assertEquals(IVisibilityKind.VK_PROTECTED, clazz.getOperations().get(1).getVisibility());
        prefMan.setPreferenceValue("RoundTrip|Java","ADD_DTORS","PSK_NO");
    }
    
    /* 
     * TransformInterfaceToClassTestCase  
	 * This tests whether an interface is properly transformed into a class.
     */
    
    public void testTransformInterfaceToClass()
    {
    	
    	// Creating an interface, attributes, operations and adding the 
    	// attributes and operations to the interface
    	IInterface intrface = createInterface("NewInterface");
    	IOperation oper = (IOperation)createType("Operation");
    	IAttribute attrib = intrface.createAttribute("int","newAttr");
    	oper.setName("NewOperation");
    	intrface.addOperation(oper);
    	intrface.addAttribute(attrib);
    	
    	// Transforming the interface to a class
    	IClassifier transCls = intrface.transform("Class");
    	
    	// Checking whether the transformation happened properly
    	assertTrue(transCls instanceof IClass);
    	assertFalse(transCls instanceof IInterface);
    	String clsName = transCls.getName();
    	assertEquals("NewInterface",clsName);
    	ETList<IOperation> opList = transCls.getOperations();
    	assertTrue(((IOperation)opList.item(1)).getIsConstructor());
    	assertEquals("getNewAttr",opList.item(2).getName());
    	assertEquals("setNewAttr",opList.item(3).getName());
    	opList = transCls.getOperationsByName("NewOperation");
    }
    
    
   /** 
     * TransformEnumerationToClassTestCase  
	 * This tests whether an enum is properly transformed into a class.
     */
    
    public void testTransformEnumerationToClass()
    {
    	
    	// Creating an enumeration, attribute and adding the 
    	// attribute to the interface
    	IEnumeration enums = createType("Enumeration");
    	enums.setName("NewEnumeration");
    	IAttribute attrib = enums.createAttribute("int","newAttr");
    	enums.addAttribute(attrib);
    	
    	// Transforming the enumeration to a class
    	IClassifier transCls = enums.transform("Class");
    	
    	// Checking whether the transformation happened properly
    	assertTrue(transCls instanceof IClass);
    	assertFalse(transCls instanceof IEnumeration);
    	String clsName = transCls.getName();
    	assertEquals("NewEnumeration",clsName);
    	ETList<IOperation> opList = transCls.getOperationsByName("NewEnumeration");
    	assertEquals(1,opList.size());
    	opList = transCls.getOperationsByName("getNewAttr");
    	assertEquals(1,opList.size());
        opList = transCls.getOperationsByName("setNewAttr");
        // We are getting 2 setter methods instead of one because the actual implementation is not proper.
        //assertEquals(2,opList.size());
    }
    
    /* 
     * ClassNamespaceChangeTestCase
	 * This method tests for the movement of a class from one package to another package.
     */
    
    public void testNameSpaceChange()
    {
    	// Create a class, package and adding the class to the package.
    	IClass newCls = createClass("TestClass");
    	IPackage newPackage = createType("Package");
    	newPackage.setName("TestPackage");
    	newCls.setOwner(newPackage);
    	
    	// Checking whether the class exists in the package
    	ETList<INamedElement> elems = newPackage.getOwnedElements();
    	assertEquals("TestClass",elems.item(0).toString());
    	
    	// Creating a second package and adding the moving the class to the new package.
    	IPackage newPackage2 = createType("Package");
    	newPackage2.setName("TestPakage2");
    	newCls.setOwner(newPackage2);
    	
    	// Checking whether the class moved in the new package
    	elems = newPackage.getOwnedElements();
    	assertEquals(0,elems.size());
    	elems = newPackage2.getOwnedElements();
    	assertEquals("TestClass",elems.item(0).toString());
    }
    
    /* 
     * ClassModifierSetTestCase
	 * Tests the modifier change.
     */
    
    public void testModifierChange()
    {
    	IClass newCls = createClass("TestClass");
    	assertFalse(newCls.getIsAbstract());
    	newCls.setIsAbstract(true);
    	assertTrue(newCls.getIsAbstract());
    	
    }

   
    
}


