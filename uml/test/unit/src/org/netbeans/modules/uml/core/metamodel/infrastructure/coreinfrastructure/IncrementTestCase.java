package org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure;

import org.netbeans.modules.uml.core.AbstractUMLTestCase;


/**
 * Test cases for Increment.
 */
public class IncrementTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(IncrementTestCase.class);
    }

    private IIncrement inc;
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        
        inc = factory.createIncrement(null);
        project.addElement(inc);
    }
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        super.tearDown();
        inc.delete();
    }
    
    public void testSetOtherPartialClassifier()
    {
        IClassifier otherP = createClass("OtherPartial");
        inc.setOtherPartialClassifier(otherP);
        assertEquals(otherP.getXMIID(), inc.getOtherPartialClassifier().getXMIID());
    }
    
    public void testGetOtherPartialClassifier()
    {
        // Tested by setOtherPartialClassifier.
    }
    
    public void testSetPartialClassifier()
    {
        IClassifier part = createClass("Partial");
        inc.setPartialClassifier(part);
        assertEquals(part.getXMIID(), inc.getPartialClassifier().getXMIID());
    }
    
    public void testGetPartialClassifier()
    {
        // Tested by setPartialClassifier.
    }
}