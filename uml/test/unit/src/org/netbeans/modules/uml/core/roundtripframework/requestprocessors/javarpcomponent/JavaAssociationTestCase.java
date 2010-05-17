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
import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.AssociationKindEnum;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociationEnd;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.INavigableEnd;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameter;
import org.netbeans.modules.uml.core.roundtripframework.ChangeKind;
import org.netbeans.modules.uml.core.roundtripframework.IChangeRequest;
import org.netbeans.modules.uml.core.roundtripframework.IRoundTripAttributeEventsSink;
import org.netbeans.modules.uml.core.roundtripframework.IRoundTripController;
import org.netbeans.modules.uml.core.roundtripframework.IRoundTripEventDispatcher;
import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
/**
 */
public class JavaAssociationTestCase extends AbstractUMLTestCase implements
    IRoundTripAttributeEventsSink
{
    public static void main(String args[])
    {
        //junit.textui.TestRunner.run(JavaAssociationTestCase.class);
        JavaAssociationTestCase tc = new JavaAssociationTestCase();
        tc.testMakeNonNavigable();
    }
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        
        IRoundTripController rt = product.getRoundTripController();
        IRoundTripEventDispatcher disp = rt.getRoundTripDispatcher();
        
        disp.registerForRoundTripAttributeEvents(this, "Java");
        
        createdAttributeName = null;
        
        a = createClass("A");
        b = createClass("B");
        c = createClass("C");
        
        assoc = relFactory.createAssociation2(
            a, b,
            AssociationKindEnum.AK_ASSOCIATION,
            false, true, project);
        project.addElement(assoc);
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.AbstractUMLTestCase#tearDown()
         */
    protected void tearDown() throws Exception
    {
        super.tearDown();
        
        IRoundTripController rt = product.getRoundTripController();
        IRoundTripEventDispatcher disp = rt.getRoundTripDispatcher();
        
        disp.revokeRoundTripAttributeSink(this);
    }
   
	
/**
 * AssociationMakeNonNavigableTestCase
 */
    public void testMakeNonNavigable()
    {
        IClass e = createClass("E"), f = createClass("F");
        IAssociation ass = relFactory.createAssociation2(
            e, f,
            AssociationKindEnum.AK_ASSOCIATION,
            false, true, project);
        
        assertEquals(1, e.getOperationsByName("getF").size());
        ((INavigableEnd) ass.getEndAtIndex(1)).makeNonNavigable();
        
        try
        {
            // We have to perform the sleep because round trip is sleeping for
            // 850 before deleting the getter and setter operations.
            Thread.sleep(1500);
        }
        catch(Exception eX)
        {
            
        }
        
        assertEquals(0, e.getOperationsByName("getF").size());
        assertEquals(0, e.getOperationsByName("setF").size());
    }

	
/**
 * AssociationMakeNavigableTestCase
 */
    public void testMakeNavigable()
    {
        IClass d = createClass("Y"),
            e = createClass("Z");
        IAssociation ass = relFactory.createAssociation2(
            d, e,
            AssociationKindEnum.AK_ASSOCIATION,
            false, false, project);
        project.addElement(ass);
        
        IAssociationEnd end = ass.getEnds().get(0).makeNavigable();
        assertEquals(1, e.getOperationsByName("getY").size());
        assertEquals(1, e.getOperationsByName("setY").size());
    }
    
/**
 * AssociationCreateTestCase
 */
	public void testAssociationCreate()
    {
        assertEquals("mB", createdAttributeName);
        ETList<IOperation> ops = a.getOperationsByName("setB");
        assertEquals(1, ops.size());
        IOperation op = ops.get(0);
        
        assertEquals("void", op.getReturnType2());
        assertEquals(2, op.getParameters().size());
        IParameter p = op.getParameters().get(1);
        assertEquals("B", p.getTypeName());
        
        assertEquals(1, op.getSupplierDependencies().size());
        
        ops = a.getOperationsByName("getB");
        assertEquals(1, ops.size());
        op = ops.get(0);
        
        assertEquals("B", op.getReturnType2());
        assertEquals(1, op.getParameters().size());
        
        assertEquals(1, op.getClientDependencies().size());
        
        ETList<IAssociation> assocs = a.getAssociations();
        assertEquals(1, assocs.size());
        assertEquals(2, assocs.get(0).getEnds().size());
        assertFalse(assocs.get(0).getEnds().get(0) instanceof INavigableEnd);
        assertTrue(assocs.get(0).getEnds().get(1) instanceof INavigableEnd);
        assertEquals("mB",
            ((INavigableEnd) assocs.get(0).getEnds().get(1)).getName());
    }

/**
 * AssociationDeleteTestCase
 */
    public void testAssociationDelete()
    {
        assoc.delete();
        
        try
        {
            // We have to perform the sleep because round trip is sleeping for
            // 850 before deleting the getter and setter operations.
            Thread.sleep(1500);
        }
        catch(Exception eX)
        {
            
        }
        
        assertEquals(0, a.getOperationsByName("setB").size());
        assertEquals(0, a.getOperationsByName("getB").size());
    }
   
/**
 * AssociationNavigableEndMovesTestCase
 */
    public void testNavigableEndMoves()
    {
        assoc.getEnds().get(1).setParticipant(c);
        ETList<IOperation> ops = a.getOperationsByName("setB");
        assertEquals(1, ops.size());
        IOperation op = ops.get(0);
        
        assertEquals("void", op.getReturnType2());
        assertEquals(2, op.getParameters().size());
        IParameter p = op.getParameters().get(1);
        assertEquals("C", p.getTypeName());
        
        assertEquals(1, op.getSupplierDependencies().size());
        
        ops = a.getOperationsByName("getB");
        assertEquals(1, ops.size());
        op = ops.get(0);
        
        assertEquals("C", op.getReturnType2());
        assertEquals(1, op.getParameters().size());
        assertEquals(1, op.getClientDependencies().size());
    }
/**
 * AssociationNonNavigableEndMovesTestCase
 */    
    public void testNonNavigableEndMoves()
    {
        assoc.getEnds().get(0).setParticipant(c);
        
        assertEquals(0, a.getAssociations().size());
        assertEquals(0, a.getOperationsByName("setB").size());
        assertEquals(0, a.getOperationsByName("getB").size());
        
        ETList<IOperation> ops = c.getOperationsByName("setB");
        assertEquals(1, ops.size());
        IOperation op = ops.get(0);
        
        assertEquals("void", op.getReturnType2());
        assertEquals(2, op.getParameters().size());
        IParameter p = op.getParameters().get(1);
        assertEquals("B", p.getTypeName());
        
        assertEquals(1, op.getSupplierDependencies().size());
        
        ops = c.getOperationsByName("getB");
        assertEquals(1, ops.size());
        op = ops.get(0);
        
        assertEquals("B", op.getReturnType2());
        assertEquals(1, op.getParameters().size());
        
        assertEquals(1, op.getClientDependencies().size());
        
        ETList<IAssociation> assocs = c.getAssociations();
        assertEquals(1, assocs.size());
        assertEquals(2, assocs.get(0).getEnds().size());
        assertFalse(assocs.get(0).getEnds().get(0) instanceof INavigableEnd);
        assertTrue(assocs.get(0).getEnds().get(1) instanceof INavigableEnd);
        assertEquals("mB",
            ((INavigableEnd) assocs.get(0).getEnds().get(1)).getName());
    }
    
    private IClass a, b, c;
    private IAssociation assoc;
    private String createdAttributeName;
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRoundTripAttributeEventsSink#onPreAttributeChangeRequest(org.netbeans.modules.uml.core.roundtripframework.IChangeRequest, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onPreAttributeChangeRequest(IChangeRequest newVal, IResultCell cell)
    {
        // TODO Auto-generated method stub
        
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRoundTripAttributeEventsSink#onAttributeChangeRequest(org.netbeans.modules.uml.core.roundtripframework.IChangeRequest, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onAttributeChangeRequest(IChangeRequest req, IResultCell cell)
    {
        int ct  = req.getState();
        int rdt = req.getRequestDetailType();
        if (ct == ChangeKind.CT_CREATE)
        {
            createdAttributeName = ((IAttribute)req.getAfter()).getName();
        }
    }
}
