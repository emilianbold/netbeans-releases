package org.netbeans.modules.uml.core.generativeframework;

import java.io.File;

import org.dom4j.Node;

import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.ModuleUnitTestSuiteBuilder;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IClass;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPackage;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IInterface;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 * Test cases for TemplateManager.
 * 
 */
public class TemplateManagerTestCase extends AbstractUMLTestCase {
	public static void main(String[] args) {
		junit.textui.TestRunner.run(TemplateManagerTestCase.class);
	}

	private ITemplateManager man = product.getTemplateManager();

	private IClass newClass;

	IPackage newPack;

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		man = new TemplateManager();

		newPack = createType("Package");
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
	}

	public void testBasicQuery() {
		String doc = "This is the documentation for the class";
		newClass.setDocumentation(doc);
		assertEquals(doc, newClass.getDocumentation());

		// This is expecting the expansion variable definition to look like
		// this:
		//
		// <ExpansionVar name="documentation"
		// query="UML:Element.ownedElement/UML:TaggedValue[@name='documentation']/UML:TaggedValue.dataValue"
		// varType="text"/>
		assertEquals(doc, man.expandVariable("documentation", newClass));

		newClass.setName("ContextClass");

		// Expecting the definition to look like this:
		//
		// <ExpansionVar name="name" query="@name"/>
		assertEquals("ContextClass", man.expandVariable("name", newClass));

		// Check to see if the class is abstract or not. By default, it should
		// NOT be abstract
		// 
		// Definition: <ExpansionVar name="isAbstract" varType="boolean"
		// expand="abstract" trueValue="abstract"/>
		assertEquals("false", man.expandVariable("isAbstract", newClass));

		assertEquals("OuterPackage::ContextClass", man.expandVariable(
				"qualifiedName", newClass));

		assertEquals("Super", man.expandVariable("firstSuperClassName",
				newClass));
	}

	public void testIDLookups() {
		IAttribute attr = newClass.createAttribute("int", "m_NewAttr");
		newClass.addAttribute(attr);

		// This tests the id lookup, as well as the expansion of the "name"
		// variable.
		// Expecting the definition of the "typeName" variable to look like
		// this:
		//
		// <ExpansionVar name="typeName" idLookup="@type" expand="name"/>
		assertEquals("int", man.expandVariable("typeName", attr));
	}

	public void testComplexQueries() {
		IVariableFactory factory = man.getFactory();
		factory.setExecutionContext(man.createExecutionContext());

		IExpansionVariable var = factory
				.createVariableWithText("implementedInterfaces");
		var.expand(newClass);

		ETList<Node> resultNodes = var.getResultNodes();
		assertNotNull(resultNodes);
		// We made sure to make newClass implement 3 interfaces in setUp()
		assertEquals(3, resultNodes.size());

		var = factory.createVariableWithText("implementedInterfaceName");

		assertEquals("I1", var.expand(resultNodes.get(0)));
		assertEquals("I2", var.expand(resultNodes.get(1)));
		assertEquals("I3", var.expand(resultNodes.get(2)));

		assertNotNull(factory.getPopContext());
	}

	public void testExpandTemplate() {
		String conLoc = ModuleUnitTestSuiteBuilder.tempDotUmlDirName
				+ File.separator + "scripts" + File.separator + "java"
				+ File.separator + "java_attribute.gt";

		IClassifier c = createClass("NY");
		IAttribute attr = c.createAttribute("int", "harlem");
		c.addAttribute(attr);
		assertEquals("private int harlem;", man.expandTemplate(conLoc, attr));

	}

	public void testExpandTemplateWithNode() {
		String conLoc = ModuleUnitTestSuiteBuilder.tempDotUmlDirName
				+ File.separator + "scripts" + File.separator + "java"
				+ File.separator + "java_attribute.gt";

		IClassifier c = createClass("NY");
		IAttribute attr = c.createAttribute("int", "harlem");
		c.addAttribute(attr);
		assertEquals("private int harlem;", man.expandTemplateWithNode(conLoc,
				attr.getNode()));

	}

	public void testGetVariableExpander() {
		assertNull(man.getVariableExpander());
		VariableExpander ve = new VariableExpander();
		VariableFactory vf = new VariableFactory();
		vf.setExecutionContext(ve);
		ve.setManager(man);
		man.setFactory(vf);
		assertTrue(man.getVariableExpander().equals(ve));		
	}

	public void testSetConfigLocation() {
		String defaultLocation = man.getConfigLocation();
		String conLoc = ModuleUnitTestSuiteBuilder.tempDotUmlDirName
				+ File.separator + "config" + File.separator
				+ "ExpansionVar.etc";
		assertEquals(conLoc, defaultLocation);
		assertEquals(conLoc, man.getConfigLocation());
		String newConfigLocation = new File("ExpansionVar.etc")
				.getAbsolutePath();

		man.setConfigLocation(newConfigLocation);
		assertEquals(newConfigLocation, man.getConfigLocation());
	}

	public void testGetConfigLocation() {
		// Tested by testSetConfigLocation.
	}

	public void testSetFactory() {
		// Tested by testGetVariableExpander.
	}

	public void testGetFactory() {
		// Tested by testGetVariableExpander.
	}

	public void testSetWorkingDirectory() {
		// Tested by testSetConfigLocation

	}

	public void testGetWorkingDirectory() {
		// Tested by testSetWorkingDirectory.
	}

	public void testCreateExecutionContext() {
		assertTrue(man.createExecutionContext() instanceof IVariableExpander);
	}

	public void testExpandVariable() {
		IClassifier c = createClass("NY");
		assertEquals("NY", man.expandVariable("name", c));
	}

	public void testExpandVariableWithNode() {
		IClassifier c = createClass("NY");
		assertEquals("NY", man.expandVariableWithNode("name", c.getNode()));
	}
}