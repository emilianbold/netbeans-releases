package org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 */
public class AllParsingFrameworkTests 
{
    public static void main(String[] args)
    {
        TestRunner.run(suite());
    }
    
	public static Test suite()
	{
        TestSuite suite = new TestSuite("ParsingFramework Tests");

        suite.addTest(new TestSuite(CodeGenerationScriptTestCase.class));
        suite.addTest(new TestSuite(ErrorEventTestCase.class));
        suite.addTest(new TestSuite(FacilityManagerTestCase.class));
        suite.addTest(new TestSuite(FacilityTestCase.class));
        suite.addTest(new TestSuite(LanguageManagerTestCase.class));
        suite.addTest(new TestSuite(LanguageParserSettingsTestCase.class));
        suite.addTest(new TestSuite(LanguageSyntaxTestCase.class));
        suite.addTest(new TestSuite(LanguageTestCase.class));

        return suite;
	}
}
