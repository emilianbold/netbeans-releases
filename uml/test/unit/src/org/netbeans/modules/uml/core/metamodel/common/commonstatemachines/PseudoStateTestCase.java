
/*
 * Created on Sep 25, 2003
 *
 */
package org.netbeans.modules.uml.core.metamodel.common.commonstatemachines;
import org.netbeans.modules.uml.core.metamodel.core.primitivetypes.IPseudostateKind;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;
/**
 * @author aztec
 *
 */
public class PseudoStateTestCase extends AbstractUMLTestCase
{
	private IPseudoState pseudoState = null;
	public PseudoStateTestCase()
	{
		super();
	}
	
	public static void main(String args[])
	{
		junit.textui.TestRunner.run(PseudoStateTestCase.class);
	}
	protected void setUp()
	{
		pseudoState = factory.createPseudoState(null);
		project.addElement(pseudoState);
	}
	
	public void testSetKind()
	{
		pseudoState.setKind(IPseudostateKind.PK_JUNCTION);
		assertEquals(IPseudostateKind.PK_JUNCTION, pseudoState.getKind());
		assertEquals("JunctionState",pseudoState.getExpandedElementType());
		
		pseudoState.setKind(IPseudostateKind.PK_INITIAL);
		assertEquals(IPseudostateKind.PK_INITIAL, pseudoState.getKind());
		assertEquals("InitialState",pseudoState.getExpandedElementType());
	}

}



