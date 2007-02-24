package org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure;

import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IClass;

/**
 * Test cases for NavigableEnd.
 */
public class NavigableEndTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(NavigableEndTestCase.class);
    }

    private INavigableEnd nav;
    private IClass first, second;
    private IAssociation assoc;
        
    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        
        first = createClass("First");
        second = createClass("Second");
        assoc = relFactory.createAssociation(first, second, project);
        project.addElement(assoc);

        nav = assoc.getEnds().get(0).makeNavigable();
    }
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        super.tearDown();
        
        assoc.delete();
        first.delete();
        second.delete();
        nav.delete();
    }

    public void testMakeNonNavigable()
    {
        IAssociationEnd end = nav.makeNonNavigable();
        assertNotNull(end);
        assertFalse(end instanceof INavigableEnd);
    }
    
    public void testGetReferencingClassifier()
    {
        assertEquals(second.getXMIID(), nav.getReferencingClassifier().getXMIID());
    }
}