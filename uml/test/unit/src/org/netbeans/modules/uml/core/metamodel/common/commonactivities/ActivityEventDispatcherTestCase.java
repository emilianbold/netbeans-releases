package org.netbeans.modules.uml.core.metamodel.common.commonactivities;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;
/**
 * Test cases for ActivityEventDispatcher.
 */
public class ActivityEventDispatcherTestCase extends AbstractUMLTestCase
    implements IActivityEdgeEventsSink
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(ActivityEventDispatcherTestCase.class);
    }

    private IActivityEventDispatcher dispatcher;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        dispatcher = new ActivityEventDispatcher();
        dispatcher.registerForActivityEdgeEvents(this);
    }
    
    public void testRevokeActivityEdgeSink()
    {
        dispatcher.revokeActivityEdgeSink(this);
        IActivityEdge edge = new ControlFlow();
        dispatcher.fireGuardModified(edge, null);
        assertFalse(onGuardModified);
    }

    public void testRegisterForActivityEdgeEvents()
    {
        // Tested by other event fire methods.
    }

    public void testFireGuardModified()
    {
        IActivityEdge edge = new ControlFlow();
        dispatcher.fireGuardModified(edge, null);
        assertTrue(onGuardModified);
    }

    public void testFirePreGuardModified()
    {
        IActivityEdge edge = new ControlFlow();
        dispatcher.firePreGuardModified(edge, "xyzzy", null);
        assertTrue(onPreGuardModified);
    }

    public void testFirePreWeightModified()
    {
        IActivityEdge edge = new ControlFlow();
        dispatcher.firePreWeightModified(edge, "xyzzy", null);
        assertTrue(onPreWeightModified);
    }

    public void testFireWeightModified()
    {
        IActivityEdge edge = new ControlFlow();
        dispatcher.fireWeightModified(edge, null);
        assertTrue(onWeightModified);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityEdgeEventsSink#onPreWeightModified(org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityEdge, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onPreWeightModified(IActivityEdge pEdge, String newValue, IResultCell cell)
    {
        onPreWeightModified = true;
        assertNotNull(pEdge);
        assertNotNull(newValue);
        assertNotNull(cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityEdgeEventsSink#onWeightModified(org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityEdge, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onWeightModified(IActivityEdge pEdge, IResultCell cell)
    {
        onWeightModified = true;
        assertNotNull(pEdge);
        assertNotNull(cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityEdgeEventsSink#onPreGuardModified(org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityEdge, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onPreGuardModified(IActivityEdge pEdge, String newValue, IResultCell cell)
    {
        onPreGuardModified = true;
        assertNotNull(pEdge);
        assertNotNull(newValue);
        assertNotNull(cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityEdgeEventsSink#onGuardModified(org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityEdge, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onGuardModified(IActivityEdge pEdge, IResultCell cell)
    {
        onGuardModified = true;
        assertNotNull(pEdge);
        assertNotNull(cell);
    }
    private static boolean onGuardModified = false;
    private static boolean onPreGuardModified = false;
    private static boolean onPreWeightModified = false;
    private static boolean onWeightModified = false;
}