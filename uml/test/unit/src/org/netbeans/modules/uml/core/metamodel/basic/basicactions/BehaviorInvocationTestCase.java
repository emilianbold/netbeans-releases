
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


