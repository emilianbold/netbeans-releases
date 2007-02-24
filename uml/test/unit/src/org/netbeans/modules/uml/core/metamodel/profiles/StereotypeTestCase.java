
/*
 * Created on Sep 17, 2003
 *
 */
package org.netbeans.modules.uml.core.metamodel.profiles;

import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 * @author aztec
 *
 */
public class StereotypeTestCase extends AbstractUMLTestCase
{

	/**
	 * Constructor 
	 */
	public StereotypeTestCase()
	{
		super();
	}

	/**
	 * All the three methods are tested using this single method.
	 * since they are dependent on one another
	 */
	public void testAddApplicableMetaType()
	{		
		IStereotype sTy = (IStereotype)FactoryRetriever.instance().createType("Stereotype", null);
		//sTy.prepareNode(DocumentFactory.getInstance().createElement(""));
		project.addElement(sTy);
		
		String testData = "TestData";
		sTy.addApplicableMetaType(testData);
		
		ETList<String> strs = sTy.appliesTo();
		assertNotNull(strs);
		assertEquals(testData, strs.get(0));
		
		//After Remove, the list should be empty. 
		sTy.removeApplicableMetaType(testData);
		strs = sTy.appliesTo();
		if (strs != null)
		{
			assertEquals(0,strs.size());
		}		
	}

	public static void main(String args[])
	{
		junit.textui.TestRunner.run(StereotypeTestCase.class);
	}
}




