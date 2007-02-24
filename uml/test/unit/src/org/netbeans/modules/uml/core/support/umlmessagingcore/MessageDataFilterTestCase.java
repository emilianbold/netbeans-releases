
/*
 * Created on Oct 8, 2003
 *
 */
package org.netbeans.modules.uml.core.support.umlmessagingcore;

import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import java.io.File;

import org.dom4j.Document;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;
/**
 * @author aztec
 *
 */
public class MessageDataFilterTestCase extends AbstractUMLTestCase
{
    
    IMessageDataFilter msgDataFilter = null;
    
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(MessageDataFilterTestCase.class);
    }
    protected void setUp() throws Exception
    {
        super.setUp();
        
        msgDataFilter = new MessageDataFilter();
        String loc = "DescribeMessageFilter.xml";
        
        Document pDoc = XMLManip.getDOMDocument();
        XMLManip.createElement(pDoc, "FACILITYFILTERS");
        XMLManip.save(pDoc, loc);
        
        IMessageService service = new MessageService();
        IMessageData data = new MessageData();
        data.setFacility("Facility1");
        
        data.setMessageType(MsgCoreConstants.MT_CRITICAL);
        service.addMessage(data);
        
        msgDataFilter.initialize(new File(loc).getAbsolutePath(),service);
    }
    
    public void testSetIsDisplayed()
    {
        IMessageData data = new MessageData();
        data.setFacility("Facility1");
        data.setMessageType(MsgCoreConstants.MT_CRITICAL);
        msgDataFilter.setIsDisplayed(data,true);
        
        assertTrue(msgDataFilter.getIsDisplayed(data));
    }
    
    public void testSetIsDisplayed1()
    {
        msgDataFilter.setIsDisplayed(MsgCoreConstants.MT_DEBUG,"Facility2",true);
        msgDataFilter.getIsDisplayed(MsgCoreConstants.MT_DEBUG,"Facility3");
        msgDataFilter.getIsDisplayed(MsgCoreConstants.MT_DEBUG,"Facility4");
        msgDataFilter.getIsDisplayed(MsgCoreConstants.MT_DEBUG,"Facility5");
        msgDataFilter.getIsDisplayed(MsgCoreConstants.MT_DEBUG,"Facility6");
        
        msgDataFilter.save();
        //assertTrue(msgDataFilter.getIsDisplayed(MsgCoreConstants.MT_DEBUG,"Facility2"));
    }
    
}



