package org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure;

import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IClass;

/**
 * Test cases for Generalization.
 */
public class GeneralizationTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(GeneralizationTestCase.class);
    }

    private IGeneralization gen;
    private IClass general, specific;
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        
        general  = createClass("General");
        specific = createClass("Specific");
        
        gen = relFactory.createGeneralization(general, specific);
    }
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        super.tearDown();
        
        gen.delete();
        general.delete();
        specific.delete();
    }
    
    public void testSetGeneral()
    {
        assertEquals(general.getXMIID(), gen.getGeneral().getXMIID());
        IClass newGen = createClass("NewGeneral");
        gen.setGeneral(newGen);
        assertEquals(newGen.getXMIID(), gen.getGeneral().getXMIID());
    }
    
    public void testGetGeneral()
    {
        // Tested by setGeneral.
    }
    
    public void testSetSpecific()
    {
        assertEquals(specific.getXMIID(), gen.getSpecific().getXMIID());
        IClass newSpec = createClass("NewSpecific");
        gen.setSpecific(newSpec);
        assertEquals(newSpec.getXMIID(), gen.getSpecific().getXMIID());
    }
    
    public void testGetSpecific()
    {
        // Tested by setSpecific.
    }
}