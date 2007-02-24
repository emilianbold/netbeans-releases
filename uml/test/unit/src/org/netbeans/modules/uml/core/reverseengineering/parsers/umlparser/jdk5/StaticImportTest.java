package org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.jdk5;

import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.AbstractUmlParserTestCase;

import junit.textui.TestRunner;

/**
 * @author aztec
 */
public class StaticImportTest extends AbstractUmlParserTestCase {
	public static void main(String[] args) {
		TestRunner.run(StaticImportTest.class);
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testStaticImport() {		
		execute(getClass().getSimpleName());
	}
    
}
