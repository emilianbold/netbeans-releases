
/*
 * Created on Sep 24, 2003
 *
 */
package org.netbeans.modules.uml.core.metamodel.basic.basicactions;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ISignal;
/**
 * @author aztec
 *
 */
public class BroadCastSignalActionTestCase extends AbstractUMLTestCase
{
	private IBroadCastSignalAction act = null;
	public BroadCastSignalActionTestCase()
	{
		super();		
	}
	
	protected void setUp()
	{
		act = (IBroadCastSignalAction)FactoryRetriever.instance().createType("BroadCastSignalAction", null);
		//act.prepareNode(DocumentFactory.getInstance().createElement(""));
		project.addElement(act);	
	}
	
	public static void main(String args[])
	{
		junit.textui.TestRunner.run(BroadCastSignalActionTestCase.class);
	}
	
	public void testSetSignal()
	{
		ISignal sig = (ISignal)FactoryRetriever.instance().createType("Signal", null);
		//sig.prepareNode(DocumentFactory.getInstance().createElement(""));
		project.addElement(sig);
		
		act.setSignal(sig);
		ISignal sigGot = act.getSignal();
		assertNotNull(sigGot);
		assertEquals(sig.getXMIID(), sigGot.getXMIID());
	}
}



