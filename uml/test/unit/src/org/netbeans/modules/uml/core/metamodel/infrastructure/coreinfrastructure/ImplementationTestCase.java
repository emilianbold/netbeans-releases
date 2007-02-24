package org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure;

import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IClass;

/**
 * Test cases for Implementation.
 */
public class ImplementationTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(ImplementationTestCase.class);
    }

    private IImplementation imp;
    private IClass     c;
    private IInterface i;
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        
        imp = factory.createImplementation(null);
        project.addElement(imp);
        
        c = createClass("Clazz");
        i = factory.createInterface(null);
        i.setName("IVoxDiabolica");
        project.addOwnedElement(i);
    }
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        super.tearDown();
        
        c.delete();
        i.delete();
        imp.delete();
    }
    
    public void testSetContract()
    {
        imp.setContract(i);
        assertEquals(i.getXMIID(), imp.getContract().getXMIID());
    }
    
    public void testGetContract()
    {
        // Tested by setContract.
    }
    
    public void testSetImplementingClassifier()
    {
        imp.setImplementingClassifier(c);
        assertNotNull(imp.getImplementingClassifier());
        assertEquals(c.getXMIID(), imp.getImplementingClassifier().getXMIID());
    }
    
    public void testGetImplementingClassifier()
    {
        // Tested by setImplementingClassifier.
    }
}