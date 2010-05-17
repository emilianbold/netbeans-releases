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

import org.netbeans.modules.uml.core.metamodel.core.constructs.IClass;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPackage;
import org.netbeans.modules.uml.core.metamodel.core.primitivetypes.IVisibilityKind;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;
import org.netbeans.modules.uml.core.roundtripframework.ChangeKind;
import org.netbeans.modules.uml.core.roundtripframework.IChangeRequest;
import org.netbeans.modules.uml.core.roundtripframework.IDependencyChangeRequest;
import org.netbeans.modules.uml.core.roundtripframework.IRequestProcessor;
import org.netbeans.modules.uml.core.roundtripframework.IRequestProcessorInitEventsSink;
import org.netbeans.modules.uml.core.roundtripframework.IRoundTripAttributeEventsSink;
import org.netbeans.modules.uml.core.roundtripframework.IRoundTripClassEventsSink;
import org.netbeans.modules.uml.core.roundtripframework.IRoundTripController;
import org.netbeans.modules.uml.core.roundtripframework.IRoundTripEventDispatcher;
import org.netbeans.modules.uml.core.roundtripframework.RequestDetailKind;
import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 */
public class JavaAttributeChangeHandlerTestCase extends AbstractUMLTestCase
    implements IRoundTripClassEventsSink, IRequestProcessorInitEventsSink,
    IRoundTripAttributeEventsSink
{
    public static void main(String args[])
    {
        junit.textui.TestRunner.run(JavaAttributeChangeHandlerTestCase.class);
    }
    
    private IClass      c;
    public static IClass newOwner;
    private IAttribute  attr;
    
    private String      expectedAddedDependencyPackage;
    
    private boolean dependencyAdded;
    private boolean defaultBodyModified;
    private boolean visibilityChanged;
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        
        dependencyAdded = false;
        
        IRoundTripController rt = product.getRoundTripController();
        IRoundTripEventDispatcher disp = rt.getRoundTripDispatcher();
        
        disp.registerForRequestProcessorInitEvents(this);
        disp.registerForRoundTripClassEvents(this, "Java");
        disp.registerForRoundTripAttributeEvents(this, "Java");
        
        c = createClass("Test");
        attr = c.createAttribute("int", "");
        
        IClassifier type = createClass("Whale");
        IPackage p = createType("Package");
        p.setName("xyz");
        p.addOwnedElement(type);
        
        expectedAddedDependencyPackage = "xyz";
        attr.setType(type);
        c.addAttribute(attr);
        attr.setName("a");
        
        dependencyAdded = false;
        defaultBodyModified = false;
        visibilityChanged = false;
        
        expectedAddedDependencyPackage = null;
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.AbstractUMLTestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        super.tearDown();
        
        IRoundTripController rt = product.getRoundTripController();
        IRoundTripEventDispatcher disp = rt.getRoundTripDispatcher();
        
        disp.revokeRequestProcessorInitEvents(this);
        disp.revokeRoundTripClassSink(this);
        disp.revokeRoundTripAttributeSink(this);
    }

//	AttributeCreateTestCase -- This method tests if the attribute created in Setup method has been working properly.    
    public void testCreate()
    {
//        // Check whether accessors are created.
//        assertEquals(1, c.getOperationsByName("getA").size());
//        assertEquals(1, c.getOperationsByName("setA").size());
//        
//        assertEquals(1, c.getOperationsByName("getA").get(0).getClientDependencies().size());
//        assertEquals(1, c.getOperationsByName("setA").get(0).getSupplierDependencies().size());
//        
//        attr = c.createAttribute("int", "");
//        c.addAttribute(attr);
//        
////	AttributeMultiplicityTestCase - tests for the association multiplicity range.
//
//        attr = c.createAttribute("int", "abc");
//        attr.getMultiplicity().setRangeThroughString("0..*");
//        
//        c.addAttribute(attr);
//        
//        assertEquals(1, c.getOperationsByName("getAbc").size());
//        assertEquals(1, c.getOperationsByName("setAbc").size());
//        
//        assertEquals(1, c.getOperationsByName("getAbc").get(0).getClientDependencies().size());
//        assertEquals(1, c.getOperationsByName("setAbc").get(0).getSupplierDependencies().size());
//        IOperation op = c.getOperationsByName("getAbc").get(0);
//        assertEquals(1, op.getReturnType().getMultiplicity().getRangeCount());
//        assertEquals("0..*", op.getReturnType().getMultiplicity().getRangeAsString());
//        op = c.getOperationsByName("setAbc").get(0);
//        assertEquals(1, op.getFormalParameters().get(0).getMultiplicity().getRangeCount());
//        assertEquals("0..*", op.getFormalParameters().get(0).getMultiplicity().getRangeAsString());
    }
    
    public void testCopied()
    {
// TEMPORARY
//        newOwner = createClass("Z");
//        attr.duplicateToClassifier(newOwner);
//
//        IAttribute duplicate = newOwner.getAttributeByName("a");
//        assertNotNull(duplicate);
//
//        // Check whether new accessors exist
//        assertEquals(1, newOwner.getOperationsByName("getA").size());
//		  assertEquals(1, newOwner.getOperationsByName("setA").size());
//
//        // ... and have the right dependencies
//        assertEquals(1, newOwner.getOperationsByName("getA").get(0)
//                                .getClientDependencies().size());
//        assertEquals(1, newOwner.getOperationsByName("setA").get(0)
//                                .getSupplierDependencies().size());
//
//        // ... and that those dependencies point at the right attribute
//        IDependency client = newOwner.getOperationsByName("getA").get(0)
//                                     .getClientDependencies().get(0),
//                    supplier = newOwner.getOperationsByName("setA").get(0)
//                                     .getSupplierDependencies().get(0);
//        assertEquals(duplicate.getXMIID(), client.getSupplier().getXMIID());
//        assertEquals(duplicate.getXMIID(), supplier.getClient().getXMIID());
//
//        // Check whether old accessors also exist ...
//        assertEquals(1, c.getOperationsByName("getA").size());
//        assertEquals(1, c.getOperationsByName("setA").size());
//
//        // ... and still have *their* dependencies
//        assertEquals(1, c.getOperationsByName("getA").get(0)
//                         .getClientDependencies().size());
//        assertEquals(1, c.getOperationsByName("setA").get(0)
//                         .getSupplierDependencies().size());
//
//        // ... and that their dependencies are still valid
//        client = c.getOperationsByName("getA").get(0)
//                                     .getClientDependencies().get(0);
//        supplier = c.getOperationsByName("setA").get(0)
//                                     .getSupplierDependencies().get(0);
//        assertEquals(attr.getXMIID(), client.getSupplier().getXMIID());
//        assertEquals(attr.getXMIID(), supplier.getClient().getXMIID());
    }
  
	
//	AttributeDeleteTestCase
    
    public void testDelete()
    {
        attr.delete();
        try
        {
            // We have to perform the sleep because round trip is sleeping for
            // 850 before deleting the getter and setter operations.
            Thread.sleep(1500);
        }
        catch(Exception eX)
        {
            
        }
        // Check whether accessor dependencies are deleted.
        assertEquals(0, c.getOperationsByName("getA").size());
        assertEquals(0, c.getOperationsByName("setA").size());
    }

//	AttributeNameChangeTestCase

    public void testNameChange()
    {
// TODO: conover - temporary until fixed            
//        attr.setName("b");
//        
//        // Check whether new accessors are created.
//        assertEquals(1, c.getOperationsByName("getB").get(0).getClientDependencies().size());
//        assertEquals(1, c.getOperationsByName("setB").get(0).getSupplierDependencies().size());
    }

//	AttributeTypeChangeTestCase
    
	public void testTypeChange()
    {
        IClass   c2 = createClass("C2");
        IPackage quentin = createType("Package");
        quentin.setName("quentin");
        quentin.addOwnedElement(c2);
        
        expectedAddedDependencyPackage   = "quentin";
        attr.setType(c2);
        
        assertTrue(dependencyAdded);
    }
//	AttributeInitialValueAddedTestCase    
    
	public void testInitialValueAdded()
    {
        assertFalse(defaultBodyModified);
        attr.setDefault2("new Whale()");
        assertTrue(defaultBodyModified);
    }
    
    public void testInitialValueRemoved()
    {
        attr.setDefault2("new Whale()");
        defaultBodyModified = false;
        attr.setDefault2("");
        assertTrue(defaultBodyModified);
    }
    
    public void testInitialValueChanged()
    {
        attr.setDefault2("new Whale()");
        defaultBodyModified = false;
        attr.setDefault2("new Whale() { }");
        assertTrue(defaultBodyModified);
    }
    
//	AttributeVisiblityChangeTestCase
    public void testVisibilityChanged()
    {
        assertFalse(visibilityChanged);
        attr.setVisibility(IVisibilityKind.VK_PUBLIC);
        assertTrue(visibilityChanged);
    }
    
// AttributeMoveTestCase -- moving an attribute from one class to another.

	public void testMoveAttribute()
    {
    	// Creating a class, attribute and adding the attribute to the class.
    	IClass firstCls = createClass("FirstTestClass");
    	IAttribute newAttr = firstCls.createAttribute("int","NewAttribute");
    	firstCls.addAttribute(newAttr);
    	
    	// Make sure that the attribute exists in the class.
    	IElement elem = newAttr.getOwner();
    	assertEquals("FirstTestClass",elem.toString());
    	ETList <IOperation> op = firstCls.getOperationsByName("setNewAttribute");
    	assertEquals(1,op.getCount());
    	assertEquals("FirstTestClass",newAttr.getOwner().getElements().item(0).toString());
    	
    	// Creating a second class and moving the attribute to the second class.
    	IClass secondCls = createClass("SecondTestClass");
    	newAttr.moveToClassifier(secondCls);
    	
    	// Make sure that the attribute exists in the second class only.
    	op = firstCls.getOperationsByName("setNewAttribute");
    	assertEquals(0,op.getCount());
    	op = secondCls.getOperationsByName("setNewAttribute");
    	assertEquals(1,op.getCount());
    	assertEquals("SecondTestClass",newAttr.getOwner().getElements().item(0).toString());
    }
    
  
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRoundTripClassEventsSink#onPreClassChangeRequest(org.netbeans.modules.uml.core.roundtripframework.IChangeRequest, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onPreClassChangeRequest(IChangeRequest newVal, IResultCell cell)
    {
        
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRoundTripClassEventsSink#onClassChangeRequest(org.netbeans.modules.uml.core.roundtripframework.IChangeRequest, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onClassChangeRequest(IChangeRequest req, IResultCell cell)
    {
        int changeType = req.getState();
        int rt = req.getRequestDetailType();
        if (changeType == ChangeKind.CT_MODIFY &&
            (rt == RequestDetailKind.RDT_DEPENDENCY_ADDED ||
            rt == RequestDetailKind.RDT_DEPENDENCY_REMOVED))
        {
            IDependencyChangeRequest dcr = (IDependencyChangeRequest) req;
            
            switch (rt)
            {
                case RequestDetailKind.RDT_DEPENDENCY_ADDED:
                    dependencyAdded = true;
                    if (expectedAddedDependencyPackage != null)
                        assertEquals(expectedAddedDependencyPackage,
                            ((IPackage)dcr.getIndependentElement()).getName());
                    
                    break;
            }
        }
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRequestProcessorInitEventsSink#onPreInitialized(java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onPreInitialized(String proc, IResultCell cell)
    {
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRequestProcessorInitEventsSink#onInitialized(org.netbeans.modules.uml.core.roundtripframework.IRequestProcessor, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onInitialized(IRequestProcessor proc, IResultCell cell)
    {
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRoundTripAttributeEventsSink#onPreAttributeChangeRequest(org.netbeans.modules.uml.core.roundtripframework.IChangeRequest, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onPreAttributeChangeRequest(IChangeRequest newVal, IResultCell cell)
    {
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRoundTripAttributeEventsSink#onAttributeChangeRequest(org.netbeans.modules.uml.core.roundtripframework.IChangeRequest, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onAttributeChangeRequest(IChangeRequest req, IResultCell cell)
    {
        int ct  = req.getState();
        int rdt = req.getRequestDetailType();
        if (ct == ChangeKind.CT_MODIFY)
        {
            if (rdt == RequestDetailKind.RDT_ATTRIBUTE_DEFAULT_BODY_MODIFIED)
                defaultBodyModified = true;
            if (rdt == RequestDetailKind.RDT_VISIBILITY_MODIFIED)
                visibilityChanged = true;
        }
    }
}
