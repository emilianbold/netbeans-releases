package org.netbeans.modules.uml.core.workspacemanagement;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 *
 * @author Trey Spiva
 */
public class AllWorkspaceTests
{
    public static void main(String[] args)
    {
        TestRunner.run(suite());
    }
    
    public static Test suite()
    {
        TestSuite suite = new TestSuite("Workspace Tests");
        
        //$JUnit-BEGIN$
        suite.addTest(new TestSuite(WorkspaceDispatcherTestCase.class));
        suite.addTest(new TestSuite(WSProjectTestCases.class));
        //$JUnit-END$
        return suite;
    }
}
