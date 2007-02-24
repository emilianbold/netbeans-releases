package org.netbeans.modules.uml.core.support.umlmessagingcore;

import junit.framework.TestCase;
/**
 *
 */
public class MessageFacilityFilterTestCase extends TestCase
{
    
    /**
     *
     */
    public MessageFacilityFilterTestCase()
    {
        super();
    }
    
    public void testIsDisplayed()
    {
        MessageFacilityFilter filter = new MessageFacilityFilter();
        filter.setIsDisplayed(MsgCoreConstants.MT_CRITICAL,false);
        boolean bool = filter.getIsDisplayed(MsgCoreConstants.MT_CRITICAL);
        assertEquals(bool,false);
    }
    
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(MessageFacilityFilterTestCase.class);
    }
}


