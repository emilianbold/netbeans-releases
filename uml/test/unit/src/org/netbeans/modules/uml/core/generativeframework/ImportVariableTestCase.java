package org.netbeans.modules.uml.core.generativeframework;

import java.io.File;

import org.dom4j.DocumentFactory;
import org.dom4j.Node;

import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.ModuleUnitTestSuiteBuilder;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IClass;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IInterface;
import org.netbeans.modules.uml.core.support.umlsupport.INamedCollection;
import org.netbeans.modules.uml.core.support.umlsupport.NamedCollection;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.products.ad.diagramengines.actions.OrthogonalLayoutAction;

/**
 * @author aztec
 */
public class ImportVariableTestCase extends AbstractUMLTestCase {
	private ImportVariable var = null;

	String varText = "ImportVar <test=\"nodeName\" result=\"1:UML:Interface\" resultAction=\"1:java_interface_head.gt\" var=\"java_class_head.gt\">;";

	public static void main(String[] args) {
		junit.textui.TestRunner.run(ImportVariableTestCase.class);
	}

	@Override
	protected void setUp() throws Exception {
		// TODO Auto-generated method stub
		super.setUp();
		var = new ImportVariable();
	}

	public void testSetTemplate() {
		assertNull(var.getTemplate());
		String conLoc = ModuleUnitTestSuiteBuilder.tempDotUmlDirName
				+ File.separator + "scripts" + File.separator + "java"
				+ File.separator + "java_import_statement.gt";

		var.setTemplate(conLoc);
		assertEquals(conLoc, var.getTemplate());
	}

	public void testGetTemplate() {
		// Tested by testSetTemplate.
	}

	public void testSetTestClause() {
		assertNull(var.getTestClause());
		var.setTestClause("nodeName");
		assertEquals("nodeName", var.getTestClause());		
	}

	public void testGetTestClause() {
		// Tested by testSetTestClause.
	}

	public void testSetConditions() {
		ETList<INamedCollection> resultActions = new ETArrayList<INamedCollection>();
		INamedCollection pair = new NamedCollection();
		pair.setName("UML:Interface");
		pair.setData("java_interface_head.gt");
		resultActions.add(pair);

		assertNull(var.getConditions());

		var.setConditions(resultActions);
		assertEquals(resultActions, var.getConditions());

		assertEquals("UML:Interface", var.getConditions().get(0).getName());
		assertEquals("java_interface_head.gt", var.getConditions().get(0)
				.getData());

	}

	public void testGetConditions() {
		// tested by testSetConditions.
	}

	public void testExpand() {

		String conLoc = ModuleUnitTestSuiteBuilder.tempDotUmlDirName
				+ File.separator + "scripts" + File.separator + "java"
				+ File.separator + "java_import_statement.gt";
		var.setTemplate(conLoc);

		IInterface newClass = createInterface("Clazz");

		var.setExecutionContext(product.getTemplateManager()
				.createExecutionContext());
		var.setNode(newClass.getNode());
		assertEquals(System.getProperty("line.separator") + "import Clazz.*;", var.expand(newClass.getNode()));
	}

}
