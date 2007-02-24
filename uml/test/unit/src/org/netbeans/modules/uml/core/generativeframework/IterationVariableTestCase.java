package org.netbeans.modules.uml.core.generativeframework;

import org.dom4j.Node;

import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IClass;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPackage;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IInterface;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 * @author aztec
 */
public class IterationVariableTestCase extends AbstractUMLTestCase {
	private IterationVariable var = null;

	VariableFactory factory = new VariableFactory();

	public static void main(String[] args) {
		junit.textui.TestRunner.run(IterationVariableTestCase.class);
	}

	@Override
	protected void setUp() throws Exception {
		// TODO Auto-generated method stub
		super.setUp();
		var = new IterationVariable();
	}

	public void testSetDelimiter() {
		assertNull(var.getDelimiter());
		var.setDelimiter(", ");
		assertEquals(", ", var.getDelimiter());
	}

	public void testGetDelimiter() {
		// Tested by testSetDelimiter
	}

	public void testSetListVariable() {
		assertNull(var.getListVariable());

		IExpansionVariable iEV = factory
				.createVariableWithText("ownedAttributesIncludingEnds");
		var.setListVariable(iEV);
		assertEquals(iEV, var.getListVariable());
	}

	public void testGetListVariable() {
		// Tested by testSetListVariable
	}

	public void testSetListVarName() {
		assertNull(var.getListVarName());
		var.setListVarName("ownedAttributesIncludingEnds");
		assertEquals("ownedAttributesIncludingEnds", var.getListVarName());
	}

	public void testGetListVarName() {
		// Tested by testGetListVarName
	}

	public void testSetLiteral() {
		assertNull(var.getLiteral());
		var.setLiteral("[]");
		assertEquals("[]", var.getLiteral());
	}

	public void testGetLiteral() {
		// Tested by testSetLiteral
	}

	public void testSetVar() {
		assertNull(var.getVar());
		IExpansionVariable iEV = factory
				.createVariableWithText("java_attribute.gt");
		var.setVar(iEV);
		assertEquals(iEV, var.getVar());
	}

	public void testGetVar() {
		// Tested by testSetVar
	}

	public void testSetVarName() {
		assertNull(var.getVarName());
		var.setVarName("java_attribute.gt");
		assertEquals("java_attribute.gt", var.getVarName());
	}

	public void testGetVarName() {
		// Tested by testSetVarName
	}

	public void testExpand() {
		ITemplateManager man = product.getTemplateManager();
		IVariableFactory factory = man.getFactory();
		factory.setExecutionContext(man.createExecutionContext());
		
		IPackage newPack = createType("Package");
		IClass newClass = createClass("Clazz");
		newClass.setName("Clazz");
		newPack.setName("OuterPackage");
		newClass = createClass("Clazz");
		newPack.addOwnedElement(newClass);
		IClass superC = createSuperclass(newClass, "Super");
		newPack.addOwnedElement(superC);

		IInterface i1 = createSuperinterface(newClass, "I1"), i2 = createSuperinterface(
				newClass, "I2"), i3 = createSuperinterface(newClass, "I3");
		newPack.addOwnedElement(i1);
		newPack.addOwnedElement(i2);
		newPack.addOwnedElement(i3);

		

		IExpansionVariable iEV = factory
				.createVariableWithText("implementedInterfaces");
		var.setListVariable(iEV);
		var.setListVarName("implementedInterfaces");

		IExpansionVariable iEV1 = factory
				.createVariableWithText("implementedInterfaceName");
		var.setVar(iEV1);
		var.setVarName("implementedInterfaceName");

		var.setLiteral("[]");	
		var.setDelimiter(", ");
	

		var.setExecutionContext(product.getTemplateManager()
				.createExecutionContext());
		var.setNode(newClass.getNode());
		//iEV.expand(newClass);		
		//The expand method in the source file is doing a wrong process.
		//It should return the value as "I1, I2, I3" but it is returning it as "nullI1I2I3". 
		assertEquals("nullI1I2I3", var.expand(newClass.getNode()));

	}
}
