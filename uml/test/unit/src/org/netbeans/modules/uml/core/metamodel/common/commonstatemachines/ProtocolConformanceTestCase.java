
/*
 * Created on Sep 25, 2003
 *
 */
package org.netbeans.modules.uml.core.metamodel.common.commonstatemachines;
import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;
/**
 * @author aztec
 *
 */
public class ProtocolConformanceTestCase extends AbstractUMLTestCase
{
	private IProtocolConformance protConf = null;
	public ProtocolConformanceTestCase()
	{
		super();
	}
	
	public static void main(String args[])
	{
		junit.textui.TestRunner.run(ProtocolConformanceTestCase.class);
	}
	protected void setUp()
	{
		protConf = factory.createProtocolConformance(null);
		project.addElement(protConf);
	}
	
	public void testSetGeneralMachine()
	{
		IProtocolStateMachine mach = (IProtocolStateMachine)FactoryRetriever.instance().createType("ProtocolStateMachine", null);
		//mach.prepareNode(DocumentFactory.getInstance().createElement(""));
		project.addElement(mach); 
		
		protConf.setGeneralMachine(mach);
		IProtocolStateMachine machGot = protConf.getGeneralMachine();
		assertEquals(mach.getXMIID(), machGot.getXMIID());
	}
	
	public void testSetSpecificMachine()
	{
		IStateMachine mach = (IStateMachine)FactoryRetriever.instance().createType("StateMachine", null);
		//mach.prepareNode(DocumentFactory.getInstance().createElement(""));
		project.addElement(mach); 
			
		protConf.setSpecificMachine(mach);
		IStateMachine machGot = protConf.getSpecificMachine();
		assertNotNull(machGot);
		assertEquals(mach.getXMIID(), machGot.getXMIID());
	}
}



