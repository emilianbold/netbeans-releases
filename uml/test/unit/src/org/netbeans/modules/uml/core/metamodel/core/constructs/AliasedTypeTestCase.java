package org.netbeans.modules.uml.core.metamodel.core.constructs;
import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;
/**
 * Test cases for AliasedType.
 */
public class AliasedTypeTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(AliasedTypeTestCase.class);
    }

    private IAliasedType aliasedType;
    private IClass       type;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        
        aliasedType = (IAliasedType)FactoryRetriever.instance().createType("AliasedType", null);
        //aliasedType.prepareNode(DocumentFactory.getInstance().createElement(""));
        project.addElement(aliasedType);
        
        type = createClass("Type");
    }
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        super.tearDown();
        aliasedType.delete();
        type.delete();
    }

    public void testSetActualType()
    {
        aliasedType.setActualType(type);
        assertEquals(type.getXMIID(), aliasedType.getActualType().getXMIID());
    }
    
    public void testSetActualType2()
    {
        aliasedType.setActualType2("I");
        assertEquals("I", aliasedType.getActualType().getName());
    }

    public void testGetActualType()
    {
        // Tested by testSetActualType.
    }

    public void testSetAliasedName()
    {
        aliasedType.setAliasedName("Alias");
        assertEquals("Alias", aliasedType.getAliasedName());
    }

    public void testGetAliasedName()
    {
        // Tested by testSetAliasedName.
    }

    public void testSetTypeDecoration()
    {
        aliasedType.setTypeDecoration("Interface");
        assertEquals("Interface", aliasedType.getTypeDecoration());
    }

    public void testGetTypeDecoration()
    {
        // Tested by testSetTypeDecoration.
    }
}