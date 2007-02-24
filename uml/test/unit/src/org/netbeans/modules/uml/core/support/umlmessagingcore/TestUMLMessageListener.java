package org.netbeans.modules.uml.core.support.umlmessagingcore;
import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;
/**
 *
 */
public class TestUMLMessageListener implements IMessengerEventsSink
{
    public void onMessageAdded(IMessageData pMessage, IResultCell cell)
    {
        //call back method to indicate that this method has been called successfully
        UMLMessagingHelperTestCase.msgStr = pMessage.getMessageType();
    }
}


