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
 * Created on Nov 24, 2003
 *
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpcomponent;

import java.util.ArrayList;
import java.util.List;

import org.netbeans.modules.uml.core.metamodel.core.constructs.IClass;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IGeneralization;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameter;
import org.netbeans.modules.uml.core.roundtripframework.ChangeKind;
import org.netbeans.modules.uml.core.roundtripframework.IChangeRequest;
import org.netbeans.modules.uml.core.roundtripframework.IParameterChangeRequest;
import org.netbeans.modules.uml.core.roundtripframework.IRoundTripController;
import org.netbeans.modules.uml.core.roundtripframework.IRoundTripEventDispatcher;
import org.netbeans.modules.uml.core.roundtripframework.IRoundTripOperationEventsSink;
import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
/**
 * @author schandra
 *
 */
public class JavaGeneralizationChangeHandlerTestCase
    extends AbstractUMLTestCase
    implements IRoundTripOperationEventsSink
{
    
    public static void main(String args[])
    {
        junit.textui.TestRunner.run(JavaGeneralizationChangeHandlerTestCase.class);
    }
    
    protected void setUp()
    {
        subc = createClass("Subclass");
        superc = createClass("Superclass");
        IOperation op;
        superc.addOperation(op = superc.createOperation("int", "washington"));
        op.setIsAbstract(true);
        
        gen = relFactory.createGeneralization(superc, subc);
        
        IRoundTripController rt = product.getRoundTripController();
        IRoundTripEventDispatcher disp = rt.getRoundTripDispatcher();
        
        disp.registerForRoundTripOperationEvents(this, "Java");
        
        methEvents.clear();
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.AbstractUMLTestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        super.tearDown();
        IRoundTripController rt = product.getRoundTripController();
        IRoundTripEventDispatcher disp = rt.getRoundTripDispatcher();
        disp.revokeRoundTripOperationSink(this);
    }
 
/**
 * GeneralizationSpecificEndMovesTestCase
 */
    public void testSpecificEndMoves()
    {
        IClass newSub = createClass("NewSub");
        
        genMoved = true;
        
        assertEquals(1, newSub.getOperations().size());
        
        gen.setSpecific(newSub);
        
        assertEquals(2, newSub.getOperations().size());
        assertEquals("washington", newSub.getOperations().get(1).getName());
        assertEquals(1, newSub.getOperations().get(1).getRedefinedElementCount());
        
        assertEquals(1, superc.getOperations().get(1).getRedefiningElementCount());
        
        assertEquals(2, subc.getOperations().size());
        assertEquals(0, subc.getOperations().get(1).getRedefinedElementCount());
    }

/**
 * GeneralizationCreateTestCase
 */
    public void testCreate()
    {
        assertEquals(2, subc.getOperations().size());
        assertEquals("washington", subc.getOperations().get(1).getName());
    }
    
/**
 * GeneralizationDeleteTestCase
 */

	public void testDelete()
    {
// TODO: conover - temporary until fixed            
//        assertEquals(1, superc.getOperations().get(1).getRedefiningElementCount());
//        assertEquals(1, subc.getOperations().get(1).getRedefinedElementCount());
//        gen.delete();
//        assertEquals(0, superc.getOperations().get(1).getRedefiningElementCount());
//        assertEquals(0, subc.getOperations().get(1).getRedefinedElementCount());
    }
    
/**
 * GeneralizationEndMovesTestCase
 */

	public void testGeneralEndMoves()
    {
        IClass newSuper = createClass("NewSuper");
        IOperation op;
        newSuper.addOperation(op = newSuper.createOperation("char", "ike"));
        op.setIsAbstract(true);
        
        gen.setGeneral(newSuper);
        assertEquals(3, subc.getOperations().size());
        assertEquals(0, subc.getOperations().get(1).getRedefinedElementCount());
        assertEquals(0, superc.getOperations().get(1).getRedefiningElementCount());
        assertEquals("ike", subc.getOperations().get(2).getName());
        assertEquals(1, subc.getOperations().get(2).getRedefinedElementCount());
        assertEquals(1, newSuper.getOperations().get(1).getRedefiningElementCount());
    }
    
/**
 * GeneralizationAddParameterTestCase
 */

	public void testAddParameter()
    {
        IOperation op;
        IParameter pm;
        superc.addOperation(op = superc.createOperation("int", "delhi"));
        
        op.addParameter(pm = op.createParameter("int","intType"));
        assertEquals(2, op.getParameters().size());
        gen = relFactory.createGeneralization(superc, subc);
        assertEquals(2, op.getParameters().size());
        assertEquals(3, subc.getOperations().size());
        
        ETList<IOperation> superOps = superc.getOperationsByName("delhi");
        IOperation superOp  = superOps.get(0);
        
        ETList<IOperation> childOps = subc.getOperationsByName("delhi");
        IOperation childOp  = childOps.get(0);
        
        assertEquals(2,superOp.getParameters().size());
        assertEquals(2,childOp.getParameters().size());
        
        superOp.addParameter(superOp.createParameter("double","doubleType"));
        
        assertEquals(3,superOp.getParameters().size());
        assertEquals(3,childOp.getParameters().size());
        
        methEvents.clear();
        superOp.addParameter(superOp.createParameter("char","charType"));
        assertEquals(4,superOp.getParameters().size());
        assertEquals(4,childOp.getParameters().size());
        
        ETList<IParameter> plist = new ETArrayList<IParameter>();
        ETList<IParameter> curr  = superOp.getParameters();
        for (int i = 0, count = curr.size(); i < count; ++i)
        {
            IParameter c = curr.get(i);
            plist.add(superOp.createParameter(c.getTypeName(), c.getName()));
        }
        plist.add(superOp.createParameter("float", "f"));
        methEvents.clear();
        superOp.setParameters(plist);
        assertEquals(5,superOp.getParameters().size());
        assertEquals(5,childOp.getParameters().size());
    }
    



/**
 * GeneralizationDeleteParameterTestCase
 */
    public void testDeleteParameter()
    {
        IOperation op;
        IParameter pm1,pm2;
        superc.addOperation(op = superc.createOperation("int", "delhi"));
        op.removeAllParameters();
        op.addParameter(pm1 = op.createParameter("int","intType"));
        op.addParameter(pm2 = op.createParameter("double","doubleType"));
        assertEquals(2, op.getParameters().size());
        gen = relFactory.createGeneralization(superc, subc);
        assertEquals(2, op.getParameters().size());
        assertEquals(3, subc.getOperations().size());
        
        ETList<IOperation> superOps = superc.getOperationsByName("delhi");
        IOperation superOp  = superOps.get(0);
        
        ETList<IOperation> childOps = subc.getOperationsByName("delhi");
        IOperation childOp  = childOps.get(0);
        
        assertEquals(2,superOp.getParameters().size());
        assertEquals(2,childOp.getParameters().size());
        superOp.removeParameter(pm1);
        assertEquals(1,superOp.getParameters().size());
// TODO: conover - temporary until fixed            
//        assertEquals(1,childOp.getParameters().size());
        
        superOp.removeParameter(pm2);
        assertEquals(0,superOp.getParameters().size());
        assertEquals(0,childOp.getParameters().size());
    }
    

/**
 * GeneralizationRenameOperationTestCase
 */
    public void testRenameOperation()
    {
        IOperation op;
        superc.addOperation(op = superc.createOperation("int", "delhi"));
        op.removeAllParameters();
        op.addParameter(op.createParameter("int","intType"));
        gen = relFactory.createGeneralization(superc, subc);
        assertEquals(3, subc.getOperations().size());
        
        ETList<IOperation> superOps = superc.getOperationsByName("delhi");
        IOperation superOp  = superOps.get(0);
        assertNotNull(superOps);
        assertEquals(1, superOps.size());
        
        ETList<IOperation> childOps = subc.getOperationsByName("delhi");
        IOperation childOp  = childOps.get(0);
        assertNotNull(childOps);
        assertEquals(1, childOps.size());
        
        assertEquals(superOps.size(), childOps.size());
        assertEquals("delhi", superOp.getName());
        assertEquals("delhi", childOp.getName());
        
        superOp.setName("bombay");
        
        assertEquals("bombay", superOp.getName());
        assertEquals("bombay", childOp.getName());
        
        superOp.setName("london");
        assertEquals("london", superOp.getName());
        assertEquals("london", childOp.getName());
    }
    
    
/**
 * GeneralizationChangeParameterTestCase
 */
    public void testChangeParameter()
    {
        IOperation op;
        superc.addOperation(op = superc.createOperation("int", "delhi"));
        op.removeAllParameters();
        op.addParameter(op.createParameter("int","intType"));
        op.addParameter(op.createParameter("String","stringType"));
        op.addParameter(op.createParameter("double","doubleType"));
        gen = relFactory.createGeneralization(superc, subc);
        assertEquals(3, subc.getOperations().size());
        
        ETList<IOperation> ops = subc.getOperationsByName("delhi");
        assertNotNull(ops);
        assertEquals(1, ops.size());
        ETList<IParameter> listOfAttribute = ops.get(0).getParameters();
        assertEquals(3,listOfAttribute.size());
        // Change in type of first attribute of delhi operation in superClass
        ETList<IOperation> superops = superc.getOperationsByName("delhi");
        
        assertEquals(1,superops.get(0).getRedefiningElementCount());
        
        ETList<IParameter> slistOfAttribute = superops.get(0).getParameters();
        IParameter param  = slistOfAttribute.get(0);
        
        assertEquals("int",ops.get(0).getParameters().get(0).getTypeName());
        param.setTypeName("String");
        assertEquals("String",ops.get(0).getParameters().get(0).getTypeName());
    }
    
    public void onPreOperationChangeRequest(IChangeRequest newVal, IResultCell cell)
    {
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRoundTripOperationEventsSink#onOperationChangeRequest(org.netbeans.modules.uml.core.roundtripframework.IChangeRequest, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onOperationChangeRequest(IChangeRequest req, IResultCell cell)
    {
        int chgk = req.getState();
        int rdt  = req.getRequestDetailType();
        
        IElement eBefore = req.getBefore(),
            eAfter  = req.getAfter();
        
        String event = null;
        String owner = null;
        try
        {
            IOperation before = (IOperation) req.getBefore(),
                after  = (IOperation) req.getAfter();
            
            switch (chgk)
            {
                case ChangeKind.CT_CREATE:
                    event = "+" + after.getName();
                    owner = ((INamedElement) after.getOwner()).getName();
                    break;
                    
                case ChangeKind.CT_MODIFY:
                {
                    event = getMethodDesc(before) + "->" +
                        getMethodDesc(after);
                    owner = ((INamedElement) before.getOwner()).getName();
                    break;
                }
                case ChangeKind.CT_DELETE:
                    event = "-" + before.getName();
                    owner = ((INamedElement) before.getOwner()).getName();
                    break;
            }
        }
        catch (ClassCastException e)
        {
            IParameterChangeRequest preq = (IParameterChangeRequest) req;
            IOperation beforeOp = preq.getBeforeOperation(),
                afterOp  = preq.getAfterOperation();
            
            IParameter before = (IParameter) req.getBefore(),
                after  = (IParameter) req.getAfter();
            switch (chgk)
            {
                case ChangeKind.CT_CREATE:
                    event = "+[" + after.getTypeName() + ":"
                        + after.getName() + "]";
                    owner = ((INamedElement) after.getOwner()).getName()
                    + " in "
                        + ((INamedElement) after.getOwner().getOwner())
                        .getName();
                    break;
                    
                case ChangeKind.CT_MODIFY:
                {
                    event = "[" + before.getTypeName() + ":"
                        + before.getName() + "->"
                        + after.getTypeName() + ":"
                        + after.getName() + "]";
                    owner = ((INamedElement) before.getOwner()).getName()
                    + " in "
                        + ((INamedElement) before.getOwner().getOwner())
                        .getName();
                    
                    break;
                }
                case ChangeKind.CT_DELETE:
                    event = "-[" + before.getTypeName() + ":"
                        + before.getName() + "]";
                    owner = ((INamedElement) before.getOwner()).getName()
                    + " in "
                        + ((INamedElement) before.getOwner().getOwner())
                        .getName();
                    break;
            }
            
            event += " (before: " + getMethodDesc(beforeOp)
            + " after: " + getMethodDesc(afterOp) + ")";
        }
        if (event != null && owner != null)
            event += " in " + owner;
        if (event != null)
            methEvents.add(event);
    }
    
    private String getMethodDesc(IOperation op)
    {
        if (op == null) return null;
        return (op.getIsAbstract()? "abstract " : "")
        + op.getReturnType2() + " " + op.getName();
    }
    
    public static boolean genMoved = false;
    private IClass subc, superc;
    private IGeneralization gen;
    private List<String> methEvents = new ArrayList<String>();
}

