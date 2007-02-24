package org.netbeans.modules.uml.core.reverseengineering.reframework;

import org.dom4j.Element;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IClass;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPackage;
/**
 * Test cases for REClass.
 */
public class REClassTestCase extends AbstractRETestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(REClassTestCase.class);
    }

    private REClass rec;
    private IClass  c;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        rec = new REClass();
        
        c = createClass("P");
        IClass a = createClass("A");
        IClass b = createClass("B");
        c.addOwnedElement(a);
        c.addOwnedElement(b);
        
        c.addAttribute(c.createAttribute("int", "a"));
        c.addAttribute(c.createAttribute("char", "b"));
        
        c.setIsAbstract(true);
        
        rec.setEventData(c.getNode());
        
        Element e = c.getElementNode();
        element = e;
        
        addToken("Package", "cucumber::anemone");
        
        Element tds = XMLManip.createElement(e, "TokenDescriptors");
        Element r  = XMLManip.createElement(tds, "TRealization");
        Element i  = XMLManip.createElement(r, "Interface");
        i.addAttribute("value", "Real");
       
        r  = XMLManip.createElement(tds, "TGeneralization");
        i  = XMLManip.createElement(r, "SuperClass");
        i.addAttribute("value", "Unreal");
    }
    
    public void testGetGeneralizations()
    {
        IREGeneralization regs = rec.getGeneralizations();
        assertEquals(1, regs.getCount());
        assertEquals("Unreal", regs.item(0).getName());
    }
    
    public void testGetRealizations()
    {
        assertEquals("Real", rec.getRealizations().item(0).getName());
    }

    public void testGetPackage()
    {
        IPackage p = createType("Package");
        p.addOwnedElement(c);
        assertEquals("cucumber::anemone", rec.getPackage());
    }

    public void testGetAllInnerClasses()
    {
        ETList<IREClass> cs = rec.getAllInnerClasses();
        assertEquals(2, cs.size());
        assertEquals("A", cs.get(0).getName());
        assertEquals("B", cs.get(1).getName());
    }

    public void testGetAttributes()
    {
        ETList<IREAttribute> cs = rec.getAttributes();
        assertEquals(2, cs.size());
        assertEquals("a", cs.get(0).getName());
        assertEquals("b", cs.get(1).getName());
    }

    public void testGetIsAbstract()
    {
        assertTrue(rec.getIsAbstract());
    }

    public void testGetIsInterface()
    {
        assertFalse(rec.getIsInterface());
    }

    public void testGetIsLeaf()
    {
        c.setIsAbstract(false);
        c.setIsLeaf(true);
        assertTrue(rec.getIsLeaf());
    }

    public void testGetOperations()
    {
        c.addOperation(c.createOperation("float", "a"));
        c.addOperation(c.createOperation("double", "b"));
        
        ETList<IREOperation> ops = rec.getOperations();
        // 7 = 4 accessors for the two existing attributes, plus constructor and
        //     the two operations we created above.
        assertEquals(7, ops.size());
        assertEquals("a", ops.get(5).getName());
        assertEquals("b", ops.get(6).getName());
    }
}