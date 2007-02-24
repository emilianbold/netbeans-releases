package org.netbeans.modules.uml.core.support.umlmessagingcore;

import java.util.Calendar;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

import junit.framework.TestCase;
/**
 *
 */

public class MessageDataTestCase extends TestCase
{
    
    public IMessageData subMsg = new MessageData();
    /**
     *
     */
    public MessageDataTestCase()
    {
        super();
    }
    
    protected void setUp() throws Exception
    {
        super.setUp();
    }
    
    protected void tearDown() throws Exception
    {
        super.tearDown();
    }
    
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(MessageDataTestCase.class);
    }
    
    //Setting attributes of MessageData. A single method(setDetails) is available
    // to set all the attributes, and it is verified with the getter methods.
    public void testSetters()
    {
        int msgType = MsgCoreConstants.MT_INFO;
        String facilityStr = "Unknown";
        String value = "sample";
        long time = Calendar.getInstance().getTime().getTime();
        if (subMsg != null)
        {
            subMsg.setDetails(msgType, facilityStr, value);
            subMsg.setTimeT(time);
        }
        assertEquals(msgType, subMsg.getMessageType());
        assertEquals(facilityStr, subMsg.getFacility());
        assertEquals(value, subMsg.getMessageString());
        assertEquals(time, subMsg.getTimeT());
    }
    
    public void testGetFormattedMessageString()
    {
        subMsg = getFilledInMsgData();
        
        String str = null;
        if (subMsg != null)
        {
            str = subMsg.getFormattedMessageString(true);
        }
        //verification
        String expected = subMsg.getTimestamp();
        expected  = expected  +  " [Unknown (Info)] sample";
        assertEquals(expected,str);
    }
    
    private IMessageData getFilledInMsgData()
    {
        IMessageData dat = new MessageData();
        int msgType = MsgCoreConstants.MT_INFO;
        String facilityStr = "Unknown";
        String value = "sample";
        long time = Calendar.getInstance().getTime().getTime();
        
        dat.setDetails(msgType, facilityStr, value);
        dat.setTimeT(time);
        
        return dat;
    }
    
    public void testAddMessage()
    {
        IMessageData msg = getFilledInMsgData();
        subMsg = getFilledInMsgData();
        subMsg.setMessageType(MsgCoreConstants.MT_WARNING);
        msg.addSubMessage(subMsg);
        ETList<IMessageData> subMessages = msg.getSubMessages();
        assertNotNull(subMessages);
        assertEquals(subMessages.get(0).getMessageType(),
            MsgCoreConstants.MT_WARNING);
    }
    
    public void testAddMessage2()
    {
        IMessageData msg = getFilledInMsgData();
        msg.addSubMessage(MsgCoreConstants.MT_DEBUG,"Unknown","Sample");
        ETList<IMessageData> subMessages = msg.getSubMessages();
        assertNotNull(subMessages);
        assertEquals(subMessages.get(0).getMessageType(),
            MsgCoreConstants.MT_DEBUG);
    }
}



