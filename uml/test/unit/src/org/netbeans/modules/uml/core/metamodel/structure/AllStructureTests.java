package org.netbeans.modules.uml.core.metamodel.structure;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 *
 */
public class AllStructureTests
{
    
    public static Test suite()
    {
        TestSuite suite = new TestSuite("Structure Tests");
        
        //$JUnit-BEGIN$
        suite.addTest(new TestSuite(ProjectTestCase.class));
        suite.addTest(new TestSuite(ArtifactTestCase.class));
        suite.addTest(new TestSuite(CommentTestCase.class));
        suite.addTest(new TestSuite(ComponentAssemblyTestCase.class));
        suite.addTest(new TestSuite(ComponentTestCase.class));
        suite.addTest(new TestSuite(DeploymentSpecificationTestCase.class));
        suite.addTest(new TestSuite(DeploymentTestCase.class));
        suite.addTest(new TestSuite(NodeTestCase.class));
        suite.addTest(new TestSuite(SourceFileArtifactTestCase.class));
        suite.addTest(new TestSuite(SubsystemTestCase.class));
        
        //$JUnit-END$
        return suite;
    }
    
    public static void main(String args[])
    {
        junit.textui.TestRunner.run(suite());
    }
    
}


