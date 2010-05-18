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
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavior;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
/**
 * @author aztec
 *
 */
public class BehaviorInvocationTestCase extends AbstractUMLTestCase
{
	private IBehaviorInvocation behaviorInvocation = null; 
	public static void main(String args[])
	{
		junit.textui.TestRunner.run(BehaviorInvocationTestCase.class);
	}
	
	protected void setUp()
	{		
		behaviorInvocation = (IBehaviorInvocation)FactoryRetriever.instance().createType("BehaviorInvocation", null);
//		 {			
//			public void establishNodePresence(Document doc, Node node)
//			{
//				super.buildNodePresence("UML:BehaviorInvocation", doc, node);
//			}
//		};
//		behaviorInvocation.prepareNode(DocumentFactory.getInstance().createElement(""));
		if (behaviorInvocation != null)
		{		
			project.addElement(behaviorInvocation);
		}
	}
	
	public void testAddArgument()
	{
		if (behaviorInvocation == null)
		{
			return;		
		}
		IOutputPin argument = (IOutputPin)FactoryRetriever.instance().createType("OutputPin", null);
		//argument.prepareNode(DocumentFactory.getInstance().createElement(""));
		project.addElement(argument);		
		
		//Add & get Argument
		behaviorInvocation.addBehaviorArgument(argument);
		ETList<IPin> arguments = behaviorInvocation.getBehaviorArguments();		
		assertNotNull(arguments);
						
		Iterator iter = arguments.iterator();
		while (iter.hasNext())
		{
			IOutputPin argumentGot = (IOutputPin)iter.next();
			assertEquals(argument.getXMIID(), argumentGot.getXMIID());							
		}
				
		//Remove Argument
		behaviorInvocation.removeBehaviorArgument(argument);
		arguments = behaviorInvocation.getBehaviorArguments();
		if (arguments != null)
		{
			assertEquals(0,arguments.size());
		}			
	}
	
	public void testAddResult()
	{		
		if (behaviorInvocation == null)
		{
			return;		
		}
		IOutputPin result = (IOutputPin)FactoryRetriever.instance().createType("OutputPin", null);
		//result.prepareNode(DocumentFactory.getInstance().createElement(""));
		project.addElement(result);		
		
		//Add & get Outputs
		behaviorInvocation.addResult(result);
		ETList<IPin> results = behaviorInvocation.getResults();		
		assertNotNull(results);
						
		Iterator iter = results.iterator();
		while (iter.hasNext())
		{
			IOutputPin resultGot = (IOutputPin)iter.next();
			assertEquals(result.getXMIID(), resultGot.getXMIID());							
		}
				
		//Remove Result
		behaviorInvocation.removeResult(result);
		results = behaviorInvocation.getResults();
		if (results != null)
		{
			assertEquals(0,results.size());
		}			
	}
	
	public void testSetBehavior()
	{
		if (behaviorInvocation == null)
		{
			return;		
		}
		IBehavior behavior = (IBehavior)FactoryRetriever.instance().createType("Behavior", null);
//		{
//			public void establishNodePresence(Document doc, Node node)
//			{
//				super.buildNodePresence("UML:Behavior", doc, node);
//			}
//	
//  	    };
//		behavior.prepareNode(DocumentFactory.getInstance().createElement(""));
		project.addElement(behavior);
		
		behaviorInvocation.setBehavior(behavior);
		assertNotNull(behaviorInvocation.getBehavior());
		
		assertEquals(behavior.getXMIID(), behaviorInvocation.getBehavior().getXMIID());		
	}
}


