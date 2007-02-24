package org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.modifiertest.attributemodifiertest;
import junit.textui.TestRunner;

import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.AbstractUmlParserTestCase;

public class TransientAttributeTest extends AbstractUmlParserTestCase {	
	public static void main(String[] args) {
		TestRunner.run(TransientAttributeTest.class);
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testTransientAttribute() {		
		execute(getClass().getSimpleName());
	}
}
