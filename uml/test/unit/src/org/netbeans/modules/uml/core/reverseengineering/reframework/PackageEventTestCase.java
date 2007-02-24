package org.netbeans.modules.uml.core.reverseengineering.reframework;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPackage;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;

/**
 * Test cases for PackageEvent.
 */
public class PackageEventTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(PackageEventTestCase.class);
    }

    private PackageEvent pe;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        pe = new PackageEvent();
        IPackage p = createType("Package");
        p.setName("foobar");
        pe.setEventData(p.getNode());
    }
    
    public void testGetPackageName()
    {
        assertEquals("foobar", pe.getPackageName());
    }
}