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
package org.netbeans.modules.collab.ui;

import java.beans.*;
import java.util.*;

import org.openide.nodes.*;
import org.openide.util.*;

import com.sun.collablet.CollabSession;
import com.sun.collablet.Conversation;
import org.netbeans.modules.collab.core.Debug;


/**
 *
 *
 * @author        Todd Fast, todd.fast@sun.com
 */
public class ConversationsNodeChildren extends Children.Map implements NodeListener, PropertyChangeListener {
    ////////////////////////////////////////////////////////////////////////////
    // Class variables
    ////////////////////////////////////////////////////////////////////////////
    private static final String KEY_EMPTY_MESSAGE = "emptyMessage";

    ////////////////////////////////////////////////////////////////////////////
    // Instance variables
    ////////////////////////////////////////////////////////////////////////////
    private CollabSession session;

    /**
     *
     *
     */
    public ConversationsNodeChildren(CollabSession session) {
        super(createMap());
        this.session = session;

        getCollabSession().addPropertyChangeListener(this);
    }

    /**
     *
     *
     */
    private static java.util.Map createMap() {
        return new TreeMap(
            new Comparator() {
                public int compare(Object o1, Object o2) {
                    // Shouldn't happen
                    if ((o1 == null) || (o2 == null)) {
                        return 0;
                    }

                    String s1 = "";

                    if (o1 instanceof String) {
                        s1 = (String) o1;
                    } else if (o1 instanceof Conversation) {
                        s1 = ((Conversation) o1).getDisplayName();
                    }

                    String s2 = "";

                    if (o2 instanceof String) {
                        s2 = (String) o2;
                    } else if (o2 instanceof Conversation) {
                        s2 = ((Conversation) o2).getDisplayName();
                    }

                    if (s1.compareTo(s2) == 0) {
                        if (o1 instanceof Conversation) {
                            s1 = ((Conversation) o1).getIdentifier();
                        }

                        if (o2 instanceof Conversation) {
                            s2 = ((Conversation) o2).getIdentifier();
                        }

                        return s1.compareTo(s2);
                    }

                    return s1.compareTo(s2);
                }
            }
        );
    }

    /**
     *
     *
     */
    public CollabSession getCollabSession() {
        return session;
    }

    /**
     *
     *
     */
    protected void addNotify() {
        super.addNotify();
        refreshChildren();
    }

    /**
     *
     *
     */
    protected void removeNotify() {
        super.removeNotify();
        super.nodes.clear();
        refresh();
        getCollabSession().removePropertyChangeListener(this);
    }

    /**
     *
     *
     */
    public synchronized void refreshChildren() {
        try {
            // Clear old conversations
            boolean modified = false;

            for (Iterator i = nodes.keySet().iterator(); i.hasNext();) {
                Object key = i.next();
                Node node = (Node) nodes.get(key);

                if (node == null) {
                    i.remove();
                    modified = true;

                    continue;
                }

                ConversationCookie cookie = (ConversationCookie) node.getCookie(ConversationCookie.class);

                if (cookie != null) {
                    Conversation conv = cookie.getConversation();

                    if ((conv == null) || ((conv != null) && !conv.isValid())) {
                        i.remove();
                        modified = true;
                    }
                }

                //				if (next instanceof Conversation)
                //				{
                //					Conversation conversation=(Conversation)next;
                //					if (!conversation.isValid())
                //					{
                //						i.remove();
                //						modified=true;
                //					}
                //				}
                //				// temp: can we have a better way to determine which public
                //				// conversation needs to be deleted?
                //				else if (next instanceof String)
                //				{
                ////					i.remove();
                //					modified=true;
                //				}
            }

            if (modified) {
                refresh();
            }

            Conversation[] conversations = getCollabSession().getConversations();
            String[] subscribedConversations = getCollabSession().getSubscribedPublicConversations();

            List subs = Arrays.asList(subscribedConversations);

            if ((conversations.length == 0) && (subscribedConversations.length == 0)) {
                if (nodes.get(KEY_EMPTY_MESSAGE) == null) {
                    put(
                        KEY_EMPTY_MESSAGE,
                        new MessageNode(
                            NbBundle.getMessage(
                                ConversationsNodeChildren.class, "LBL_ConversationsNodeChildren_NoConferences"
                            )
                        )
                    );
                }
            } else {
                remove(KEY_EMPTY_MESSAGE);

                // Find new conversations
                for (int i = 0; i < conversations.length; i++) {
                    Conversation conversation = conversations[i];

                    if ((nodes.get(conversation) == null) && !subs.contains(conversation.getIdentifier())) {
                        //						Debug.out.println(" adding conv node: " + conversation.getIdentifier());
                        put(conversation, new ConversationNode(conversation));
                    }
                }

                for (int i = 0; i < subscribedConversations.length; i++) {
                    String conversationName = subscribedConversations[i];

                    if (nodes.get(conversationName) == null) {
                        put(conversationName, new PublicConversationNode(getCollabSession(), conversationName));
                    }
                }
            }
        } catch (Exception e) {
            Debug.errorManager.notify(e);
        }
    }

    /**
     *
     *
     */
    public void propertyChange(PropertyChangeEvent event) {
        if (event.getSource() instanceof CollabSession) {
            if (CollabSession.PROP_VALID.equals(event.getPropertyName())) {
                // TODO: Is there any reason to listen to valid=true?
                // I don't think there is.
                if (event.getNewValue().equals(Boolean.FALSE)) {
                    super.nodes.clear();
                    refresh();
                    getCollabSession().removePropertyChangeListener(this);
                }
            } else if (CollabSession.PROP_CONVERSATIONS.equals(event.getPropertyName())) {
                refreshChildren();
            }
        }
    }

    /**
     *
     *
     */
    public void childrenAdded(NodeMemberEvent ev) {
        // Ignore
    }

    /**
     *
     *
     */
    public void childrenRemoved(NodeMemberEvent ev) {
        // Ignore
    }

    /**
     *
     *
     */
    public void childrenReordered(NodeReorderEvent ev) {
        // Ignore
    }

    /**
     *
     *
     */
    public void nodeDestroyed(NodeEvent ev) {
        refreshChildren();
    }
}
