package org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.exceptionhandlingtest;
import junit.textui.TestRunner;

import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.AbstractUmlParserTestCase;

public class ThrowsKeywordInMethodDeclarationTest extends AbstractUmlParserTestCase {	
	public static void main(String[] args) {
		TestRunner.run(ThrowsKeywordInMethodDeclarationTest.class);
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testThrowsKeywordInMethodDeclaration() {		
		execute(getClass().getSimpleName());
	}
}
