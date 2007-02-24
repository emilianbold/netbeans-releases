package org.netbeans.modules.uml.core.generativeframework;

import java.io.File;

import org.dom4j.DocumentFactory;
import org.dom4j.Node;

import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.ModuleUnitTestSuiteBuilder;
import org.netbeans.modules.uml.core.metamodel.core.foundation.Element;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;

/**
 * @author aztec
 */
public class VariableExpanderTestCase extends AbstractUMLTestCase {
	private VariableExpander var = null;

	public static void main(String[] args) {
		junit.textui.TestRunner.run(VariableExpanderTestCase.class);
	}

	@Override
	protected void setUp() throws Exception {
		// TODO Auto-generated method stub
		super.setUp();
		var = new VariableExpander();
	}

	public void testSetConfigFile() {
		assertNull(var.getConfigFile());
		var.setConfigFile("abc");
		assertEquals("abc", var.getConfigFile());
	}

	public void testGetConfigFile() {
		// Tested by testSetConfigFile.
	}

	public void testRetrieveVarNode() {
		String conLoc = ModuleUnitTestSuiteBuilder.tempDotUmlDirName
				+ File.separator + "config" + File.separator
				+ "ExpansionVar.etc";
		var.setConfigFile(conLoc);

		// xyz node is not there in Expansionvar.etc.
		assertNull(var.retrieveVarNode("xyz"));

		// Pass existing the node name(abstract).
		Node n = var.retrieveVarNode("abstract");
		assertNotNull(n);
		assertEquals("ExpansionVar", n.getName());
		Element c = new Element();
		c.setNode(n);
		assertEquals("abstract", c.getAttributeValue("name"));
	}

	public void testSetManager() {
		assertNull(var.getManager());
		ITemplateManager man = product.getTemplateManager();
		var.setManager(man);
		assertEquals(man, var.getManager());
	}

	public void testGetManager() {
		// Tested by testSetManager.
	}

	public void testExpand() {

		assertFalse(var.endGathering());

		var.beginGathering();
		assertFalse(var.endGathering());

		/* Prepare Expansion variable and IFTests */
		VariableFactory vf = new VariableFactory();
		ITemplateManager man = product.getTemplateManager();
		vf.setExecutionContext(var);
		var.setManager(man);
		man.setFactory(vf);

		String conLoc = ModuleUnitTestSuiteBuilder.tempDotUmlDirName
				+ File.separator + "config" + File.separator
				+ "ExpansionVar.etc";
		var.setConfigFile(conLoc);

		IfVariable ifv = new IfVariable();
		ifv.setExecutionContext(man.getVariableExpander());

		org.dom4j.Element el = DocumentFactory.getInstance().createElement("p");
		XMLManip.setAttributeValue(el, "isVolatile", "true");
		XMLManip.setAttributeValue(el, "isStatic", "true");

		IfTest ift = new IfTest();
		ift.setTest("volatile");
		ift.setResultAction("static");

		ifv.addTest(ift);

		assertNotNull(el);

		var.beginGathering();
		String result = var.expand("", ifv, el);
		assertEquals("true", result);

		// After adding one result endGathering should return true.
		assertTrue(var.endGathering());

	}

	public void testBeginGathering() {
		// Tested by testExpand
	}

	public void testEndGathering() {
		// Tested by testExpand.
	}

	public void testAddResult() {
		// Not Implemented Stubbed in C++ code
	}

	public void testRemoveResult() {
		// Not Implemented Stubbed in C++ code
	}

	public void testAppendResults() {
		// Not Implemented Stubbed in C++ code
	}

	public void testSetExpansionResults() {
		// Not Implemented Stubbed in C++ code
	}

	public void testGetExpansionResults() {
		// Not Implemented Stubbed in C++ code
	}

}
