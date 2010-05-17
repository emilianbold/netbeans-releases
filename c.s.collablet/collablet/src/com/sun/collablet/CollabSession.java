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
package com.sun.collablet;

import java.beans.*;
import java.util.Collection;


/**
 * A single session to the collaboration server.  There may be multiple
 * sessions open at a time, but each one must have a different presentity.
 *
 * @author  Todd Fast <todd.fast@sun.com>
 */
public interface CollabSession {
    ////////////////////////////////////////////////////////////////////////////
    // Class variables
    ////////////////////////////////////////////////////////////////////////////
    public static final String PROP_VALID = "valid";
    public static final String PROP_CONTACT_GROUPS = "contactGroups";
    public static final String PROP_CONVERSATIONS = "conversations";
    public static final String PROP_PUBLIC_CONVERSATIONS = "publicConversations";
    public static final int SEARCHTYPE_EQUALS = 0;
    public static final int SEARCHTYPE_CONTAINS = 1;
    public static final int SEARCHTYPE_STARTSWITH = 2;
    public static final int SEARCHTYPE_ENDSWITH = 3;

    /**
     *
     *
     */
    public String getDisplayName();

    /**
     *
     *
     */
    public CollabManager getManager();

    /**
     *
     *
     */
    public CollabPrincipal getUserPrincipal();

    ////////////////////////////////////////////////////////////////////////////
    // Session management methods
    ////////////////////////////////////////////////////////////////////////////

    /**
     *
     *
     */
    public boolean isValid();

    /**
     *
     *
     */
    public boolean isLoggedIn();

    /**
     *
     *
     */
    public Account getAccount();

    /**
     *
     *
     */
    public void logout();

    ////////////////////////////////////////////////////////////////////////////
    // Conversation methods
    ////////////////////////////////////////////////////////////////////////////

    /**
     *
     *
     */
    public Conversation[] getConversations();

    /**
     *
     *
     */
    public String[] getSubscribedPublicConversations();

    /**
     *
     *
     */
    public Conversation createConversation() throws CollabException;

    /**
     *
     *
     */
    public Conversation createPublicConversation(String name)
    throws CollabException;

    //	/**
    //	 *
    //	 *
    //	 */
    //	public Conversation joinPublicConversation(String name)
    //		throws CollabException;

    /**
     *
     *
     */
    public String[] findPublicConversations(int searchType, String pattern)
    throws CollabException;

    /**
     *
     *
     */
    public String[] getPublicConversations() throws CollabException;

    /**
     *
     *
     */
    public ConversationPrivilege[] getPublicConversationPrivileges(String name)
    throws CollabException;

    /**
     *
     *
     */
    public void setPublicConversationPrivileges(String name, ConversationPrivilege[] privileges)
    throws CollabException;

    /**
     *
     *
     */
    public ConversationPrivilege getPublicConversationDefaultPrivilege(String name)
    throws CollabException;

    /**
     *
     *
     */
    public void setPublicConversationDefaultPrivilege(String name, ConversationPrivilege privilege)
    throws CollabException;
    
    /**
     *
     *
     */
    public Collection getParticipantsFromPublicConference(String destinationConference);

    ////////////////////////////////////////////////////////////////////////////
    // Contact methods
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Returns the canonical principal for the specified identifier
     *
     */
    public CollabPrincipal getPrincipal(String identifier)
    throws CollabException;

    /**
     *
     *
     */
    public CollabPrincipal[] findPrincipals(int searchType, String searchString)
    throws CollabException;

    /**
     *
     *
     */
    public ContactGroup[] getContactGroups();

    /**
     *
     *
     */
    public ContactGroup getContactGroup(String name);

    /*
     *
     *
     */
    public void publishStatus(int status, String reason)
    throws CollabException;

    /**
     *
     *
     */
    public ContactGroup createContactGroup(String groupName)
    throws CollabException;

    /**
     *
     *
     */
    public void setInvisibleToAll() throws CollabException;

    /**
     *
     *
     */
    public void setVisibleToAll() throws CollabException;

    /**
     *
     *
     */
    public void subscribePublicConversation(String name)
    throws CollabException;

    /**
     *
     *
     */
    public void unsubscribePublicConversation(String name)
    throws CollabException;

    /**
     *
     *
     */
    public boolean canManagePublicConversation(String name);

    /**
     *
     *
     */
    public void deletePublicConversation(String name) throws CollabException;

    /**
     *
     *
     */
    public void changePassword(String newPassword) throws CollabException;

    ////////////////////////////////////////////////////////////////////////////
    // Property change methods
    ////////////////////////////////////////////////////////////////////////////

    /**
     *
     *
     */
    public void addPropertyChangeListener(PropertyChangeListener listener);

    /**
     *
     *
     */
    public void removePropertyChangeListener(PropertyChangeListener listener);
}
