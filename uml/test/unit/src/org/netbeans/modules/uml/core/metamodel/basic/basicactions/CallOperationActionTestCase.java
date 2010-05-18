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
 * Created on Sep 24, 2003
 *
 */
package org.netbeans.modules.uml.core.metamodel.basic.basicactions;

import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import java.util.Iterator;
import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
/**
 * @author aztec
 *
 */
public class CallOperationActionTestCase extends AbstractUMLTestCase
{
	private ICallOperationAction action = null;
	private IOperation  op;
	private IClassifier cl;
	
	public CallOperationActionTestCase()
	{
		super();		
	}
	public static void main(String args[])
	{
		junit.textui.TestRunner.run(CallOperationActionTestCase.class);
	}
	protected void setUp() throws Exception
	{
		action = (ICallOperationAction)FactoryRetriever.instance().createType("CallOperationAction", null);
		//action.prepareNode(DocumentFactory.getInstance().createElement(""));
		project.addElement(action);
	}
	
	protected void tearDown() throws Exception
	{
		super.tearDown();
	}
	public void testSetOperation()
	{	
		cl = createClass("Trellis");
		op = cl.createOperation("int", "almond");
		cl.addOperation(op);
		
		action.setOperation(op);
		IOperation operGot = action.getOperation();
		assertNotNull(operGot);
		assertEquals(op.getXMIID(), operGot.getXMIID()); 
	}
	
	public void testAddToResult()
	{
		IOutputPin result = (IOutputPin)FactoryRetriever.instance().createType("OutputPin", null);
		//result.prepareNode(DocumentFactory.getInstance().createElement(""));
		project.addElement(result);	
		//Add Result
		action.addToResult(result);
		
		//Get Result
		ETList<IOutputPin> results = action.getResults();
		assertNotNull(results);
		
		Iterator iter = results.iterator();
		while (iter.hasNext())
		{
			IOutputPin resultGot = (IOutputPin)iter.next();
			assertEquals(result.getXMIID(), resultGot.getXMIID());							
		}
		
		//Remove Result
		action.removeFromResult(result);
		results = action.getResults();
		if (results != null)
		{
			assertEquals(0,results.size());
		}
	}
	

}



