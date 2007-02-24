
/*
 * Created on Oct 17, 2003
 *
 */
package org.netbeans.modules.uml.core.metamodel.behavior;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IClass;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
/**
 * @author aztec
 *
 */
public class CreateActionTestCase extends AbstractUMLTestCase 
{

	public CreateActionTestCase()
	{
	}
	
	public static void main(String args[])
	{
		junit.textui.TestRunner.run(CreateActionTestCase.class);
	}
	
	public void testSetInstantiation()
	{
		ICreateAction createAction = factory.createCreateAction(null);
		project.addElement(createAction);
		
		IClass clazz = createClass("FirstClass");
		project.addElement(clazz);
		
		createAction.setInstantiation(clazz);
		IClassifier clazzGot = createAction.getInstantiation();
		
		assertEquals(clazz.getXMIID(), clazzGot.getXMIID());
	}

}



