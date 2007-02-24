package org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
/**
 * Test cases for Signal.
 */
public class SignalTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(SignalTestCase.class);
    }
    
    private ISignal sig;
    
    protected void setUp() throws Exception
    {
        super.setUp();
        
        sig = factory.createSignal(null);
        project.addElement(sig);
    }
    
    protected void tearDown() throws Exception
    {
        super.tearDown();
        sig.delete();
    }

    public void testAddContext()
    {
        IClassifier c = createClass("Z");
        IOperation op = c.createOperation("int", "corsiva");
        c.addOperation(op);
        
        sig.addContext(op);
        ETList<IBehavioralFeature> feats = sig.getContexts();
        assertEquals(1, feats.size());
        assertEquals(op.getXMIID(), feats.get(0).getXMIID());
    }

    public void testRemoveContext()
    {
        testAddContext();
        sig.removeContext(sig.getContexts().get(0));
        assertEquals(0, sig.getContexts().size());
    }
    
    public void testGetContexts()
    {
        // Tested by testAddContext
    }
    
    public void testAddHandler()
    {
        IClassifier c = createClass("Z");
        IOperation op = c.createOperation("int", "corsiva");
        c.addOperation(op);
        
        sig.addHandler(op);
        ETList<IBehavioralFeature> feats = sig.getHandlers();
        assertEquals(1, feats.size());
        assertEquals(op.getXMIID(), feats.get(0).getXMIID());
    }
    
    public void testRemoveHandler()
    {
        testAddHandler();
        sig.removeHandler(sig.getHandlers().get(0));
        assertEquals(0, sig.getHandlers().size());
    }
    
    public void testGetHandlers()
    {
        // Tested by testAddHandler
    }
}