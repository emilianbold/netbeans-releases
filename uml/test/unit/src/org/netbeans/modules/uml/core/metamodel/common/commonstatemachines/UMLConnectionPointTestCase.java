
/*
 * Created on Sep 26, 2003
 *
 */
package org.netbeans.modules.uml.core.metamodel.common.commonstatemachines;

import java.util.Iterator;
import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
/**
 * @author aztec
 *
 */
public class UMLConnectionPointTestCase extends AbstractUMLTestCase
{
	private IUMLConnectionPoint connPoint = null;
	
	public UMLConnectionPointTestCase()
	{
		super();
	}
	
	protected void setUp()
	{
		connPoint = (IUMLConnectionPoint)FactoryRetriever.instance().createType("ConnectionPoint", null);
		//connPoint.prepareNode(DocumentFactory.getInstance().createElement(""));
		project.addElement(connPoint); 
	}
	
	public void testSetDefinition()
	{
		IUMLConnectionPoint defn = (IUMLConnectionPoint)FactoryRetriever.instance().createType("ConnectionPoint", null);
		//defn.prepareNode(DocumentFactory.getInstance().createElement(""));
		project.addElement(defn); 
		
		connPoint.setDefinition(defn);
		IUMLConnectionPoint defnGot = connPoint.getDefinition();
		assertEquals(defn.getXMIID(), defnGot.getXMIID()); 
	}
	
	public void testAddEntry()
	{
		IPseudoState pseudoState = factory.createPseudoState(null);
		project.addElement(pseudoState);
		
		//add and get
		connPoint.addEntry(pseudoState);
		ETList<IPseudoState> pseudoStates = connPoint.getEntries();
		assertNotNull(pseudoStates);
				
		Iterator iter = pseudoStates.iterator();
		while (iter.hasNext())
		{
			IPseudoState pseudoStateGot = (IPseudoState)iter.next();
			assertEquals(pseudoState.getXMIID(), pseudoStateGot.getXMIID());							
		}
		
		//Remove Input
		connPoint.removeEntry(pseudoState);
		pseudoStates = connPoint.getEntries();
		if (pseudoStates != null)
		{
			assertEquals(0,pseudoStates.size());
		}
	}
}



