package org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.importtest;
import junit.textui.TestRunner;

import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.AbstractUmlParserTestCase;

public class ImportClassDependencyTest extends AbstractUmlParserTestCase {	
	public static void main(String[] args) {
		TestRunner.run(ImportClassDependencyTest.class);
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testImportClassDependency() {		
		execute(getClass().getSimpleName());
	}
}
