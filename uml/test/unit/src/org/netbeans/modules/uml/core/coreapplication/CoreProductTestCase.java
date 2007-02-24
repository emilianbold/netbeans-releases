
package org.netbeans.modules.uml.core.coreapplication;

import junit.framework.TestCase;
import org.netbeans.modules.uml.core.IApplication;
import org.netbeans.modules.uml.core.eventframework.EventDispatchController;
import org.netbeans.modules.uml.core.eventframework.EventDispatchNameKeeper;
import org.netbeans.modules.uml.core.eventframework.EventDispatchRetriever;
import org.netbeans.modules.uml.core.eventframework.IEventDispatchController;
import org.netbeans.modules.uml.core.roundtripframework.RTMode;
import org.netbeans.modules.uml.core.workspacemanagement.IWorkspace;
/**
 *
 */
public class CoreProductTestCase extends TestCase
{
    private static final String wsLoc = "TestWS.etw";
    private static final String wsName = "TestWS";
    private TestCoreProductChangeListener m_ProdListener =
        new TestCoreProductChangeListener();
    private ICoreProductEventDispatcher m_Dispatcher = null;
    
    public static boolean callingPreInit = false;
    public static boolean callingInit = false;
    public static boolean callingPreQuit = false;
    
    public CoreProductTestCase()
    {
        super();
    }
    
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(CoreProductTestCase.class);
    }
    
    public void testCreateWorkspace()
    {
        CoreProduct prod = new CoreProduct();
        try
        {
            prod.createWorkspace(wsLoc,wsName);
            IWorkspace space = prod.openWorkspace(wsLoc);
            assertEquals(space.getName(),wsName);
            assertEquals(wsLoc, space.getLocation());
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    
    //The initialize() method of CoreProduct sets all the attributes of
    //the class, so by calling that single method, we can assert all the
    //attributes of CoreProduct at once.
    public void testInitialize() throws Exception
    {
        CoreProduct prod = new CoreProduct();
        prod.createWorkspace(wsLoc,wsName);
        
        IApplication application = prod.initialize();
        
        //We have to initialize it again to test whether the init event is getting fired or not.
        //as the event is fired when the product is getting initialized and
        //inialization is required to register the events.
        prod.initialize();
        
        assertNotNull(application);
        
        IWorkspace space = prod.getCurrentWorkspace();
        assertNotNull(space);
        assertEquals(wsLoc, space.getLocation());
        
        assertNotNull(prod.getLanguageManager());
        
        assertNotNull(prod.getFacilityManager());
        assertNotNull(prod.getPreferenceManager());
        assertNotNull(prod.getNavigatorFactory());
        assertNotNull(prod.getTemplateManager());
        assertNotNull(prod.getConfigManager());
        assertNotNull(prod.getRoundTripController());
        
        // conover - default is OFF now that RT is being disabled
        assertEquals(prod.getRoundTripController().getMode(),RTMode.RTM_OFF);
        
        assertTrue(CoreProductTestCase.callingInit);
        assertTrue(CoreProductTestCase.callingPreInit);
    }
    
    //quit() method releases all the resources of CoreProduct
    public void testQuit()
    {
        CoreProduct prod = new CoreProduct();
        IApplication application = prod.initialize();
        prod.quit();
        assertNull(prod.getApplication());
        IWorkspace space = prod.getCurrentWorkspace();
        assertNull(space);
    }
    
    protected void setUp()
    {
        EventDispatchRetriever ret = EventDispatchRetriever.instance();
        IEventDispatchController cont = ret.getController();
        if (cont == null)
        {
            cont = new EventDispatchController();
        }
        m_Dispatcher =  (ICoreProductEventDispatcher)
        cont.retrieveDispatcher(EventDispatchNameKeeper.EDT_COREPRODUCT_KIND);
        if (m_Dispatcher == null)
        {
            m_Dispatcher =  new CoreProductEventDispatcher();
            cont.addDispatcher(EventDispatchNameKeeper.EDT_COREPRODUCT_KIND,
                m_Dispatcher);
        }
        m_Dispatcher.registerForInitEvents(m_ProdListener);
        ret.setController(cont);
    }
    
    protected void tearDown()
    {
        m_Dispatcher.revokeInitSink(m_ProdListener);
    }
    
}


