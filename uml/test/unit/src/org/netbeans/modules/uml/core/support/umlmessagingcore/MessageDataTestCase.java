/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */


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



