package org.netbeans.modules.uml.core.reverseengineering.reframework;


/**
 * Test cases for REClassFeature.
 */
public class REClassFeatureTestCase extends AbstractRETestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(REClassFeatureTestCase.class);
    }

    private REClassFeature recf;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        recf = new REClassFeature();
        
        createBaseElement(recf, "UML:ClassFeature");
        attr("isFinal", "true");
        attr("isStatic", "true");
    }
    
    public void testGetIsConstant()
    {
        assertTrue(recf.getIsConstant());
    }

    public void testGetOwnerScope()
    {
        assertEquals(ScopeKind.SK_CLASSIFIER, recf.getOwnerScope());
    }
}
