package org.netbeans.modules.uml.core.generativeframework;

import java.io.File;

import org.dom4j.DocumentFactory;
import org.dom4j.Element;

import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.ModuleUnitTestSuiteBuilder;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;

/**
 * Test cases for IfVariable.
 * 
 */
public class IfVariableTestCase extends AbstractUMLTestCase {
	public static void main(String[] args) {
		junit.textui.TestRunner.run(IfVariableTestCase.class);
	}

	private IfVariable var;

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		var = new IfVariable();
	}

	public void testAddTest() {
		// Default tests should be 0.
		assertEquals(0, var.getTests().size());

		// Initialize and set name to IfTest
		IIfTest ifTest1 = new IfTest();
		ifTest1.setTest("FirstTest");
		IIfTest ifTest2 = new IfTest();
		ifTest2.setTest("SecondTest");

		// addTest with duplicate IfTest
		var.addTest(ifTest1);
		var.addTest(ifTest1);// Duplicate IFTests
		assertEquals(1, var.getTests().size());

		// add more IfTest
		var.addTest(ifTest2);
		assertEquals(2, var.getTests().size());
		assertEquals("FirstTest", var.getTests().get(0).getTest());
		assertEquals("SecondTest", var.getTests().get(1).getTest());

		// Remove Iftest
		var.removeTest(ifTest1);
		assertEquals(1, var.getTests().size());
		assertEquals("SecondTest", var.getTests().get(0).getTest());		
	}

	public void testRemoveTest() {
		// Tested by testAddTest.
	}

	public void testGetTests() {
		// Tested by testAddTest.
	}

	public void testExpand() {
		TemplateManager man = new TemplateManager();
		VariableFactory vf = new VariableFactory();
		VariableExpander ve = new VariableExpander();
		// String conLoc =
		// ProductHelper.getConfigManager().getDefaultConfigLocation();
		String conLoc = ModuleUnitTestSuiteBuilder.tempDotUmlDirName
				+ File.separator + "config" + File.separator; // NOI18N

		ve.setConfigFile(new File(conLoc + "ExpansionVar.etc").toString()); // NOI18N
		vf.setExecutionContext(ve);
		ve.setManager(man);
		man.setFactory(vf);
		var.setExecutionContext(man.getVariableExpander());

		Element el = DocumentFactory.getInstance().createElement("p"); // NOI18N
		XMLManip.setAttributeValue(el, "isVolatile", "true"); // NOI18N
		XMLManip.setAttributeValue(el, "isStatic", "true"); // NOI18N

		IfTest ift = new IfTest();
		ift.setTest("volatile"); // NOI18N
		ift.setResultAction("static"); // NOI18N

		var.addTest(ift);
		assertEquals("true", var.expand(el)); // NOI18N
	}

	public void testGetKind() {
		assertEquals(VariableKind.VK_NONE, var.getKind());
		var.setKind(VariableKind.VK_ATTRIBUTE);
		assertEquals(VariableKind.VK_ATTRIBUTE, var.getKind());
	}

	public void testSetKind() {
		// Tested by testGetKind.
	}
}