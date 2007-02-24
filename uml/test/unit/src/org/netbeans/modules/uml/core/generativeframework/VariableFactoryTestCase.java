package org.netbeans.modules.uml.core.generativeframework;

import java.io.File;

import org.dom4j.DocumentFactory;

import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.ModuleUnitTestSuiteBuilder;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 * @author aztec
 */
public class VariableFactoryTestCase extends AbstractUMLTestCase {
	private IVariableFactory factory = null;

	private ITemplateManager man = product.getTemplateManager();

	public static void main(String[] args) {
		junit.textui.TestRunner.run(VariableFactoryTestCase.class);
	}

	@Override
	protected void setUp() throws Exception {
		// TODO Auto-generated method stub
		super.setUp();

		factory = new VariableFactory();
	}

	public void testCreateVariable() {
		org.dom4j.Element el = DocumentFactory.getInstance().createElement("p");
		assertNotNull(factory.createVariable(el));
	}

	public void testSetExecutionContext() {
		assertNull(factory.getExecutionContext());
		assertNull(factory.getPopContext());

		IVariableExpander ive = new VariableExpander();
		factory.setExecutionContext(ive);

		// After adding one element
		assertNotNull(factory.getExecutionContext());
		assertNotNull(factory.getPopContext());

		// After removing element
		assertNull(factory.getExecutionContext());
		assertNull(factory.getPopContext());

	}

	public void testGetExecutionContext() {
		// Tested By testSetExecutionContext.
	}

	public void testGetPopContext() {
		// Tested By testSetExecutionContext
	}

	public void testSetConfigFile() {
		String conLoc = ModuleUnitTestSuiteBuilder.tempDotUmlDirName
				+ File.separator + "config" + File.separator
				+ "ExpansionVar.etc";
		assertNull(factory.getConfigFile());
		factory.setConfigFile("abcde");
		assertEquals("abcde", factory.getConfigFile());
		factory.setConfigFile(conLoc);
	}

	public void testGetConfigFile() {
		// Tested By testSetConfigFile.
	}

	public void testCreateVariableWithText() {		
		factory.setExecutionContext(man.createExecutionContext());
		IExpansionVariable var = factory
				.createVariableWithText("implementedInterfaces");
		assertNotNull(var);
	}

	public void testGetOverrideVariables() {
		assertEquals(0, factory.getOverrideVariables().size());
		ETList<IExpansionVariable> value = new ETArrayList<IExpansionVariable>();
		org.dom4j.Element el1 = DocumentFactory.getInstance()
				.createElement("p");
		org.dom4j.Element el2 = DocumentFactory.getInstance()
				.createElement("p");

		IExpansionVariable iev1 = new ExpansionVariable();
		iev1.setNode(el1);
		iev1.setName("First");

		IExpansionVariable iev2 = new ExpansionVariable();
		iev2.setNode(el2);
		iev2.setName("Second");

		value.add(iev1);
		factory.setOverrideVariables(value);
		assertEquals(1, factory.getOverrideVariables().size());

		factory.addOverride(iev1);
		assertEquals(1, factory.getOverrideVariables().size());

		factory.addOverride(iev2);
		assertEquals(2, factory.getOverrideVariables().size());

		factory.removeOverride(iev1);
		assertEquals(1, factory.getOverrideVariables().size());

	}

	public void testSetOverrideVariables() {
		// Tested By testGetOverrideVariables.
	}

	public void testAddOverride() {
		// Tested By testGetOverrideVariables.
	}

	public void testRemoveOverride() {
		// Tested By testGetOverrideVariables.
	}

}
