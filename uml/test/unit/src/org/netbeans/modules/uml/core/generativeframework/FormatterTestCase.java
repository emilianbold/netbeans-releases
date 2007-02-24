package org.netbeans.modules.uml.core.generativeframework;

import org.netbeans.modules.uml.core.AbstractUMLTestCase;

/**
 * @author aztec
 */
public class FormatterTestCase extends AbstractUMLTestCase {

	public static void main(String[] args) {
		junit.textui.TestRunner.run(FormatterTestCase.class);
	}

	@Override
	protected void setUp() throws Exception {
		// TODO Auto-generated method stub
		super.setUp();

	}

	public void testConvertNewLines() {
		
		String str="private int i;\n   private String str;";
		assertEquals(str,Formatter.convertNewLines("   class k{\nint m;", str));
		
		assertEquals("private int i;\n\n   private String str;",Formatter.convertNewLines("   class k{\n int m;", str));
		
	}

}
