package org.netbeans.modules.uml.core.reverseengineering.reframework;

import org.netbeans.modules.uml.core.metamodel.core.primitivetypes.IVisibilityKind;

/**
 * Test cases for REClassElement.
 */
public class REClassElementTestCase extends AbstractRETestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(REClassElementTestCase.class);
    }

    private REClassElement rece;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        rece = new REClassElement();

        createBaseElement(rece, "UML:ClassElement");
        addToken("Comment", "This is a comment!");
        attr("name", "Name");
        attr("visibility", "private");
        
        createBaseElement(null, "UML:Class");
        attr("name", "Parent");
        
        rece.getEventData().detach();
        element.add(rece.getEventData());
    }
    
    public void testGetComment()
    {
        assertEquals("This is a comment!", rece.getComment());
    }

    public void testGetName()
    {
        assertEquals("Name", rece.getName());
    }

    public void testGetOwner()
    {
        assertEquals("Parent", rece.getOwner().getName());
    }

    public void testGetVisibility()
    {
        assertEquals(IVisibilityKind.VK_PRIVATE, rece.getVisibility());
    }
}