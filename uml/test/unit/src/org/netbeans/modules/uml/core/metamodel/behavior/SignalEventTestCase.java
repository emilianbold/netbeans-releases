
/*
 * Created on Oct 17, 2003
 *
 */
package org.netbeans.modules.uml.core.metamodel.behavior;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ISignal;
/**
 * @author aztec
 *
 */
public class SignalEventTestCase extends AbstractUMLTestCase
{
	public SignalEventTestCase()
	{
	}
	
	public static void main(String args[])
	{
		junit.textui.TestRunner.run(SignalEventTestCase.class);
	}
	
	public void testSetSignal()
	{
		ISignalEvent signalEvent = factory.createSignalEvent(null);
		project.addElement(signalEvent);
		
		ISignal signal = factory.createSignal(null);
		project.addElement(signal);
		
		signalEvent.setSignal(signal);
		ISignal signalGot = signalEvent.getSignal();
		
		assertEquals(signal.getXMIID(), signalGot.getXMIID());
	}

}



