package org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.importtest;
import junit.textui.TestRunner;

import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.AbstractUmlParserTestCase;

public class ImportPackageDependencyTest extends AbstractUmlParserTestCase {	
	public static void main(String[] args) {
		TestRunner.run(ImportPackageDependencyTest.class);
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testImportPackageDependency() {		
		execute(getClass().getSimpleName());
	}
}
