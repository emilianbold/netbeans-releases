package org.netbeans.modules.uml.core.support.umlmessagingcore;

import junit.framework.TestCase;

import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.coreapplication.CoreProductManager;
import org.netbeans.modules.uml.core.coreapplication.ICoreProduct;
import org.netbeans.modules.uml.core.coreapplication.ICoreProductManager;
import org.netbeans.modules.uml.core.eventframework.EventDispatchNameKeeper;
import org.netbeans.modules.uml.core.eventframework.IEventDispatchController;
import org.netbeans.modules.uml.ui.products.ad.applicationcore.ADProduct;
/**
 *
 */
public class UMLMessagingHelperTestCase extends AbstractUMLTestCase
{
    private IUMLMessagingEventDispatcher m_Dispatcher = null;
    private TestUMLMessageListener m_MsgListener = new TestUMLMessageListener();
    public static int msgStr = -1;
    /**
     *
     */
    public UMLMessagingHelperTestCase()
    {
        super();
    }
    
    public void testGetMessageService()
    {
        UMLMessagingHelper helper = new UMLMessagingHelper();
        IMessageService service = helper.getMessageService();
        assertNotNull(service);
    }
    
    public void testSendCriticalMessage()
    {
        UMLMessagingHelper helper = new UMLMessagingHelper();
        helper.sendCriticalMessage("Critical Message ");
        assertTrue(UMLMessagingHelperTestCase.msgStr == MsgCoreConstants.MT_CRITICAL);
    }
    
    public void testSendErrorMessage()
    {
        UMLMessagingHelper helper = new UMLMessagingHelper();
        helper.sendErrorMessage("Error Message ");
        assertTrue(UMLMessagingHelperTestCase.msgStr == MsgCoreConstants.MT_ERROR);
    }
    
    public void testSendWarningMessage()
    {
        UMLMessagingHelper helper = new UMLMessagingHelper();
        helper.sendWarningMessage("Warning Message ");
        assertTrue(UMLMessagingHelperTestCase.msgStr == MsgCoreConstants.MT_WARNING);
    }
    
    public void testSendInfoMessage()
    {
        UMLMessagingHelper helper = new UMLMessagingHelper();
        helper.sendInfoMessage("Info Message ");
        assertTrue(UMLMessagingHelperTestCase.msgStr == MsgCoreConstants.MT_INFO);
    }
    
    public void testSendDebugMessage()
    {
        UMLMessagingHelper helper = new UMLMessagingHelper();
        helper.sendDebugMessage("Debug Message ");
        assertTrue(UMLMessagingHelperTestCase.msgStr == MsgCoreConstants.MT_DEBUG);
    }
    
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(UMLMessagingHelperTestCase.class);
    }
    
    protected void setUp() throws Exception
    {
//        ADProduct product = new ADProduct();
//        ICoreProductManager pProductManager =  CoreProductManager.instance();
//        pProductManager.setCoreProduct(product);
//        ICoreProduct pCoreProduct = pProductManager.getCoreProduct();
//        pCoreProduct.initialize();
        
        IEventDispatchController cont = product.getEventDispatchController();
        m_Dispatcher =  (IUMLMessagingEventDispatcher)
        cont.retrieveDispatcher(EventDispatchNameKeeper.EDT_MESSAGING_KIND);
        m_Dispatcher.registerMessengerEvents(m_MsgListener);
    }
    
    protected void tearDown() throws Exception
    {
        m_Dispatcher.revokeMessengerSink(m_MsgListener);
    }
}


