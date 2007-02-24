package org.netbeans.modules.uml.core.generativeframework;

import java.io.File;
import java.io.FileInputStream;

import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.ModuleUnitTestSuiteBuilder;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IClass;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPackage;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IInterface;

/**
 * @author aztec
 */
public class ExpansionVarLocatorTestCase extends AbstractUMLTestCase {
	private ExpansionVarLocator var = null;

	public static void main(String[] args) {
		junit.textui.TestRunner.run(ExpansionVarLocatorTestCase.class);
	}

	@Override
	protected void setUp() throws Exception {
		// TODO Auto-generated method stub
		super.setUp();

		String conLoc = ModuleUnitTestSuiteBuilder.tempDotUmlDirName
				+ File.separator + "scripts" + File.separator + "java"
				+ File.separator + "java_attribute.gt";

		IClassifier c = createClass("NY");
		IAttribute attr = c.createAttribute("int", "harlem");
		c.addAttribute(attr);
		// assertEquals("private int harlem;", man.expandTemplate(conLoc
		// + "java_attribute.gt", attr));
		ITemplateManager man = product.getTemplateManager();
		var = new ExpansionVarLocator(man, conLoc, readFile(conLoc), attr);
	}

	/**
	 * Reads the File and forms as String Data.
	 * 
	 * @param String
	 *            FileName to be read
	 * @return String
	 */
	public static String readFile(String fileName) {
		String str = "";
		try {
			FileInputStream p = new FileInputStream(fileName);
			int ch = -1;
			while ((ch = p.read()) != -1)
				str += (char) ch;
		} catch (Exception ewe) {
			ewe.printStackTrace();
		}
		return str;
	}

	public void testGetManager() {
		assertTrue(var.getManager() instanceof ITemplateManager);
	}

	public void testExpandVars() {
		assertEquals("private int harlem;", var.expandVars());
	}
}
