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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.collab.channel.filesharing.eventhandler;

import com.sun.collablet.CollabException;

import org.netbeans.modules.collab.channel.filesharing.FilesharingConstants;
import org.netbeans.modules.collab.channel.filesharing.context.JoinBeginContext;
import org.netbeans.modules.collab.channel.filesharing.event.PauseFilesharingEvent;
import org.netbeans.modules.collab.channel.filesharing.eventlistener.FilesharingTimerTask;
import org.netbeans.modules.collab.channel.filesharing.eventlistener.PauseTimerTask;
import org.netbeans.modules.collab.channel.filesharing.mdc.CollabContext;
import org.netbeans.modules.collab.channel.filesharing.mdc.EventContext;
import org.netbeans.modules.collab.channel.filesharing.msgbean.CCollab;
import org.netbeans.modules.collab.channel.filesharing.msgbean.JoinFilesharing;
import org.netbeans.modules.collab.channel.filesharing.msgbean.User;
import org.netbeans.modules.collab.channel.filesharing.util.FileshareUtil;


/**
 * SendMessageJoinBegin
 *
 * @author  Ayub Khan, ayub.khan@sun.com
 * @version 1.0
 */
public class JoinFilesharingBeginHandler extends FilesharingEventHandler implements FilesharingConstants {
    /**
     * constructor
     *
     */
    public JoinFilesharingBeginHandler(CollabContext context) {
        super(context);
    }

    ////////////////////////////////////////////////////////////////////////////
    // Event Handler methods
    ////////////////////////////////////////////////////////////////////////////

    /**
     * constructMsg
     *
     * @param        context                                        Context
     */
    public CCollab constructMsg(EventContext evContext) {
        CCollab collab = new CCollab();
        collab.setVersion(getVersion());

        JoinFilesharing joinFilesharing = new JoinFilesharing();
        collab.setChJoinFilesharing(joinFilesharing);

        //add this element to message when begin to join a conversation.
        joinFilesharing.setBeginJoin(true);
        getContext().setJoinFlag(true);

        User user = new User();
        String joinUser = getLoginUser();

        if (evContext instanceof JoinBeginContext) {
            joinUser = (String) ((JoinBeginContext) evContext).getSource();
        }

        user.setId(joinUser);
        joinFilesharing.setUser(user);

        return collab;
    }

    /**
     * handleMsg
     *
     * @param        collabBean
     * @param        messageOriginator
     * @param        isUserSame
     */
    public void handleMsg(CCollab collabBean, String messageOriginator, boolean isUserSame)
    throws CollabException {
        //diable explorer and file view
        getContext().onJoinBegin();

        JoinFilesharing joinFilesharing = collabBean.getChJoinFilesharing();
        String saveJoinUser = joinFilesharing.getUser().getId();
        getContext().setJoinUser(saveJoinUser);
        getContext().addUser(saveJoinUser);

        //create a pausetimertask
        PauseTimerTask sendPauseMessageTimerTask = new PauseTimerTask(
                getContext().getChannelEventNotifier(),
                new PauseFilesharingEvent(new EventContext(PauseFilesharingEvent.getEventID(), null)), getContext()
            );

        //send pause message if not join user and a moderator
        if (!saveJoinUser.equals(getLoginUser()) && getContext().isModerator(getLoginUser())) {
            //pause-filesharing, send to all
            getContext().addTimerTask(SEND_PAUSE_TIMER_TASK, sendPauseMessageTimerTask);
            sendPauseMessageTimerTask.schedule(
                FileshareUtil.getRandomCount(FilesharingTimerTask.PERIOD /*FilesharingTimerTask.PAUSE_DELAY*/)
            );
        }

        if (isUserSame) {
            //degenerate pause-filesharing, send to all usually happens when 
            //1) if same user and #participants==2 (including this user)
            //   and other user is readonly, send join-end message, or
            //2) pause message from other user is not send/blocked for some reason
            getContext().addTimerTask(SEND_PAUSE_TIMER_TASK, sendPauseMessageTimerTask);
            sendPauseMessageTimerTask.schedule(FileshareUtil.getRandomCount(FilesharingTimerTask.PAUSE_DELAY * 2));
        }
    }
}
