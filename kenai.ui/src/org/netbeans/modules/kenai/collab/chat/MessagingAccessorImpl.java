/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.kenai.collab.chat;

import java.awt.event.ActionEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.netbeans.modules.kenai.api.Kenai;
import org.netbeans.modules.kenai.api.Kenai.Status;
import org.netbeans.modules.kenai.api.KenaiFeature;
import org.netbeans.modules.kenai.api.KenaiProject;
import org.netbeans.modules.kenai.api.KenaiService;
import org.netbeans.modules.kenai.ui.api.KenaiServer;
import org.netbeans.modules.team.server.ui.spi.MessagingAccessor;
import org.netbeans.modules.team.server.ui.spi.MessagingHandle;
import org.netbeans.modules.team.server.ui.spi.ProjectHandle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * 
 */
@ServiceProvider(service=MessagingAccessor.class)
public class MessagingAccessorImpl extends MessagingAccessor<KenaiProject> {

    private static MessagingAccessor instance;
    
    public static MessagingAccessor getDefault() {
        if(instance == null) {
            instance = new MessagingAccessorImpl();
        }
        return instance;
    }

    @Override
    public MessagingHandle getMessaging(ProjectHandle<KenaiProject> project) {
        Kenai k = project.getTeamProject().getKenai();
        KenaiConnection kc = KenaiConnection.getDefault(k);
        //synchronized (kc) {
            try {
                final KenaiProject prj = project.getTeamProject();
                if (prj.isMyProject() && k.getStatus()==Status.ONLINE) {
                    MultiUserChat chat = kc.getChat(project.getId());
                    if (chat == null) {
                        KenaiFeature[] f;
                        f = prj.getFeatures(KenaiService.Type.CHAT);
                        if (f.length == 1) {
                            chat = kc.getChat(f[0]);
                            if (chat==null || !chat.isJoined()) {
                                throw new RuntimeException();
                            }
                        }
                    } else if (!chat.isJoined()) {
                        kc.tryJoinChat(chat);
                        if (!chat.isJoined())
                            throw new RuntimeException();
                    }
                }
            } catch (Exception ex) {
                Logger.getLogger(MessagingAccessorImpl.class.getName()).log(Level.INFO, ex.getMessage(), ex);
                    MessagingHandleImpl m = new MessagingHandleImpl(project.getTeamProject());
                    m.setMessageCount(0);
                    m.setOnlineCount(-3);
                    return m;
                }

            return ChatNotifications.getDefault().getMessagingHandle(project.getTeamProject());
        //}
    }



    @Override
    public Action getOpenMessagesAction(final ProjectHandle<KenaiProject> project) {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent arg0) {
                final ChatTopComponent chatTC = ChatTopComponent.findInstance();
                chatTC.open();
                chatTC.setActiveGroup(project.getId() + "@muc." + project.getTeamProject().getKenai().getUrl().getHost()); // NOI18N
                chatTC.requestActive(false);
            }
        };
    }

    @Override
    public Action getCreateChatAction(final ProjectHandle<KenaiProject> project) {
        return new CreateChatAction(project.getTeamProject());
    }

    @Override
    public Action getReconnectAction(final ProjectHandle<KenaiProject> project) {
        return new AbstractAction() {

            public void actionPerformed(ActionEvent e) {
                project.firePropertyChange(ProjectHandle.PROP_CONTENT, null, null);
            }
        };
    }
}
