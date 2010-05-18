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
 * Created on Sep 15, 2003
 *
 */
package org.netbeans.modules.uml.core.metamodel.basic.basicactions;

import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import java.util.Iterator;
import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IValueSpecification;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
/**
 * @author aztec
 *
 */
public class ActionTestCase extends AbstractUMLTestCase
{
	private IValueSpecification vSpec = null;
	private IAction action = null;
	public ActionTestCase()
	{
		super();				
	}
	
	protected void setUp() throws Exception
	{
		action = factory.createCreateAction(null);		
		project.addElement(action);
	}
	
	public static void main(String args[])
	{
		junit.textui.TestRunner.run(ActionTestCase.class);
	}
	
	public void testAddInput()
	{
		vSpec = factory.createExpression(null);
		project.addElement(vSpec);
		
		//Add & get Inputs
		action.addInput(vSpec);
		ETList<IValueSpecification> specs = action.getInputs();
		assertNotNull(specs);
				
		Iterator iter = specs.iterator();
		while (iter.hasNext())
		{
			IValueSpecification vspecGot = (IValueSpecification)iter.next();
			assertEquals(vSpec.getXMIID(), vspecGot.getXMIID());							
		}
		
		//Remove Input
		action.removeInput(vSpec);
		specs = action.getInputs();
		if (specs != null)
		{
			assertEquals(0,specs.size());
		}
	}
	
	public void testAddOutput()
	{
		IOutputPin outPin = (IOutputPin)FactoryRetriever.instance().createType("OutputPin", null);
		//outPin.prepareNode(DocumentFactory.getInstance().createElement(""));
		project.addElement(outPin);
		
		//Add & get Outputs
		action.addOutput(outPin);
		ETList<IOutputPin> outPins = action.getOutputs();		
		assertNotNull(outPins);
				
		Iterator iter = outPins.iterator();
		while (iter.hasNext())
		{
			IOutputPin outPinGot = (IOutputPin)iter.next();
			assertEquals(outPin.getXMIID(), outPinGot.getXMIID());							
		}
		
		//Remove Output
		action.removeOutput(outPin);
		outPins = action.getOutputs();
		if (outPins != null)
		{
			assertEquals(0,outPins.size());
		}			
	}	
	
	public void testAddJumpHandler()
	{
		IJumpHandler handler = (IJumpHandler)FactoryRetriever.instance().createType("JumpHandler", null);
		//handler.prepareNode(DocumentFactory.getInstance().createElement(""));
		project.addElement(handler);
		
		//Add & get Outputs
		action.addJumpHandler(handler);
		ETList<IJumpHandler> handlers = action.getJumpHandlers();		
		assertNotNull(handlers);
				
		Iterator iter = handlers.iterator();
		while (iter.hasNext())
		{
			IJumpHandler handlerGot = (IJumpHandler)iter.next();
			assertEquals(handler.getXMIID(), handlerGot.getXMIID());							
		}
		
		//Remove Output
		action.removeJumpHandler(handler);
		handlers = action.getJumpHandlers();	
		if (handlers != null)
		{
			assertEquals(0,handlers.size());
		}			
	}	
	
	public void testAddPredecessor()
	{
		IAction predecessor = factory.createCreateAction(null);
		project.addElement(predecessor);
		
		//Add & get Outputs
		action.addPredecessor(predecessor);
		ETList<IAction> predecessors = action.getPredecessors();		
		assertNotNull(predecessors);
				
		Iterator iter = predecessors.iterator();
		while (iter.hasNext())
		{
			IAction predecessorGot = (IAction)iter.next();
			assertEquals(predecessor.getXMIID(), predecessorGot.getXMIID());							
		}
		
		//Remove Output
		action.removePredecessor(predecessor);
		predecessors = action.getPredecessors();	
		if (predecessors != null)
		{
			assertEquals(0,predecessors.size());
		}			
	}
	
	public void testAddSuccessor()
	{
		IAction successor = factory.createCreateAction(null);
		project.addElement(successor);
		
		//Add & get Outputs
		action.addSuccessor(successor);
		ETList<IAction> successors = action.getSuccessors();		
		assertNotNull(successors);
				
		Iterator iter = successors.iterator();
		while (iter.hasNext())
		{
			IAction successorGot = (IAction)iter.next();
			assertEquals(successor.getXMIID(), successorGot.getXMIID());							
		}
		
		//Remove Output
		action.removeSuccessor(successor);
		successors = action.getSuccessors();	
		if (successors != null)
		{
			assertEquals(0,successors.size());
		}			
	}
	
	public void testSetIsReadOnly()
	{
		action.setIsReadOnly(true);
		assertTrue(action.getIsReadOnly());
	}
}



