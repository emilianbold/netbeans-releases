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
package org.netbeans.modules.collab.ui.actions;

import org.openide.nodes.Node;
import org.openide.util.*;
import org.openide.util.actions.NodeAction;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

import com.sun.collablet.*;
import org.netbeans.modules.collab.core.Debug;
import org.netbeans.modules.collab.ui.CollabSessionCookie;
import org.netbeans.modules.collab.ui.ConversationCookie;

public class DeletePublicConversationAction extends NodeAction {
    protected boolean enable(Node[] nodes) {
        if (nodes.length == 0) {
            return false;
        }

        // Sanity check to make sure the collab manager is present
        CollabManager manager = CollabManager.getDefault();

        if (manager == null) {
            return false;
        }

        for (int i = 0; i < nodes.length; i++) {
            CollabSessionCookie sessionCookie = (CollabSessionCookie) nodes[i].getCookie(CollabSessionCookie.class);

            if (sessionCookie == null) {
                return false;
            }

            ConversationCookie cookie = (ConversationCookie) nodes[i].getCookie(ConversationCookie.class);

            if (cookie == null) {
                return false;
            }

            CollabSession session = sessionCookie.getCollabSession();

            if (!session.canManagePublicConversation(nodes[i].getName())) {
                return false;
            }
        }

        return true;
    }

    public String getName() {
        return NbBundle.getMessage(SubscribeAction.class, "LBL_DeletePublicConversationAction_Name");
    }

    protected String iconResource() {
        return "org/openide/resources/actions/empty.gif";
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    protected boolean asynchronous() {
        return true;
    }

    protected void performAction(Node[] nodes) {
        NotifyDescriptor descriptor = new NotifyDescriptor.Confirmation(
                NbBundle.getMessage(DeletePublicConversationAction.class, "MSG_DeletePublicConversationAction_Confirm"),
                NotifyDescriptor.YES_NO_OPTION
            );

        if (DialogDisplayer.getDefault().notify(descriptor) == NotifyDescriptor.YES_OPTION) {
            for (int i = 0; i < nodes.length; i++) {
                try {
                    CollabSessionCookie sessionCookie = (CollabSessionCookie) nodes[i].getCookie(
                            CollabSessionCookie.class
                        );
                    assert sessionCookie != null : "CollabSessionCookie was null despite enable check";

                    CollabSession session = sessionCookie.getCollabSession();

                    ConversationCookie conversationCookie = (ConversationCookie) nodes[i].getCookie(
                            ConversationCookie.class
                        );

                    if (conversationCookie != null) {
                        Conversation conv = conversationCookie.getConversation();

                        if (conv != null) {
                            conv.leave();
                        }
                    }

                    session.deletePublicConversation(nodes[i].getName());
                    session.unsubscribePublicConversation(nodes[i].getName());
                } catch (CollabException e) {
                    Debug.errorManager.notify(e);
                }
            }
        }
    }
}
