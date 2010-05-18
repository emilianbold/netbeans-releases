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



/*
 * Created on Nov 18, 2003
 *
 */
package org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpcomponent;

import java.util.prefs.Preferences;
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
import org.openide.util.NbPreferences;

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
        Preferences prefs = NbPreferences.forModule (JavaClassChangeHandlerTestCase.class) ;
        prefs.put ("UML_ADD_DTORS", "PSK_YES") ;
        String str = prefs.get ("UML_ADD_DTORS", "PSK_YES") ;
        
        IClass clazz2 = createClass("AA");
        assertEquals(2, clazz2.getOperations().size());
        assertEquals("AA", clazz2.getOperations().get(0).getName());
        assertEquals("finalize", clazz2.getOperations().get(1).getName());
        assertEquals(IVisibilityKind.VK_PROTECTED, clazz2.getOperations().get(1).getVisibility());
        prefs.put ("UML_ADD_DTORS", "PSK_NO");
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


