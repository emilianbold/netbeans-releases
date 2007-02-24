package org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.jdk5.GenericsTestSuite;
import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.jdk5.StaticImportTest;

public class AllUMLParserTestSuite {
    
    public static void main(String[] args) {
        TestRunner.run(suite());
        
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite("UML Parser Tests");
        suite.addTest(AttributeTestSuite.suite());
        suite.addTest(ClassTestSuite.suite());
        suite.addTest(EnumTestSuite.suite());
        suite.addTest(ExceptionHandlingTestSuite.suite());
        suite.addTest(ExpressionTestSuite.suite());
        suite.addTest(ImportTestSuite.suite());
        suite.addTest(InterfaceTestSuite.suite());
        suite.addTest(ModifierTestSuite.suite());
        suite.addTest(OperationTestSuite.suite());
        suite.addTest(StatementTestSuite.suite());
        suite.addTest(GenericsTestSuite.suite());
        suite.addTest(new TestSuite(PackageTest.class));
        suite.addTest(new TestSuite(StaticImportTest.class));
        return suite;
    }
}
