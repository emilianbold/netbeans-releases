package org.netbeans.modules.uml.core.metamodel.dynamics;

import org.netbeans.modules.uml.core.eventframework.IEventPayload;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElement;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;
/**
 * Test cases for DynamicsEventDispatcher.
 */
public class DynamicsEventDispatcherTestCase extends AbstractUMLTestCase
    implements ILifelineModifiedEventsSink
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(DynamicsEventDispatcherTestCase.class);
    }

    private IDynamicsEventDispatcher dispatcher =
        new DynamicsEventDispatcher();
    private ILifeline life;
    private boolean onPreChangeRepresentingClassifier,
                    onChangeRepresentingClassifier;
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        
        dispatcher.registerForLifelineModifiedEvents(this);
        onPreChangeRepresentingClassifier =
            onChangeRepresentingClassifier = false;
        
        // These won't be properly initialised, but that's not a worry.
        life = new Lifeline();
    }
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        // TODO Auto-generated method stub
        super.tearDown();
    }
    
    public void testFireChangeRepresentingClassifier()
    {
        IEventPayload payload = 
            dispatcher.createPayload("ChangeRepresentingClassifier");
        dispatcher.fireChangeRepresentingClassifier(life, 
                factory.createAttribute(null), payload);
        assertTrue(onChangeRepresentingClassifier);
    }

    public void testRegisterForLifelineModifiedEvents()
    {
        // Tested by testFire*
    }

    public void testRevokeLifelineModifiedSink()
    {
        dispatcher.revokeLifelineModifiedSink(this);
        IEventPayload payload = 
            dispatcher.createPayload("ChangeRepresentingClassifier");
        dispatcher.fireChangeRepresentingClassifier(life, 
                factory.createAttribute(null), payload);
        assertFalse(onChangeRepresentingClassifier);
    }

    public void testFirePreChangeRepresentingClassifier()
    {
        IEventPayload payload = 
            dispatcher.createPayload("PreChangeRepresentingClassifier");
        dispatcher.firePreChangeRepresentingClassifier(life, 
                factory.createAttribute(null), payload);
        assertTrue(onPreChangeRepresentingClassifier);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.ILifelineModifiedEventsSink#onPreChangeRepresentingClassifier(org.netbeans.modules.uml.core.metamodel.dynamics.ILifeline, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onPreChangeRepresentingClassifier(ILifeline pLifeline, ITypedElement pRepresents, IResultCell cell)
    {
        onPreChangeRepresentingClassifier = true;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.ILifelineModifiedEventsSink#onChangeRepresentingClassifier(org.netbeans.modules.uml.core.metamodel.dynamics.ILifeline, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onChangeRepresentingClassifier(ILifeline pLifeline, ITypedElement pRepresents, IResultCell cell)
    {
        onChangeRepresentingClassifier = true;
    }    
}