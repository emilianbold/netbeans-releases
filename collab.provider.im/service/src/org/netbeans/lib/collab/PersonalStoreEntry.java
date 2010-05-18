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

package org.netbeans.lib.collab;

/**
 *
 *
 * @since version 0.1
 *
 */
public interface PersonalStoreEntry {

    /**
     * returns the entry id
     * @return entry id
     */
    public String getEntryId();

    /**
     * returns the display name
     * @return display name
     */
    public String getDisplayName();

    /**
     * sets the display name
     * @param name The display name to set
     */
    public void setDisplayName(String name);

    /**
     * returns the type of this personal store entry
     */
    public String getType();

    /**
     * adds a reference to this entry in the specified folder
     * @param folder folder in which to add the entry
     */
    public void addToFolder(PersonalStoreFolder folder) throws CollaborationException;

    /**
     * removes a reference to this entry from the specified folder
     * @param folder folder from which to remove the entry
     */
    public void removeFromFolder(PersonalStoreFolder folder) throws CollaborationException;

    /**
     * retrieve folders in which this entry is
     * @return a collection of PersonalFolder objects
     */
    public java.util.Collection getFolders() throws CollaborationException;

    /**
     * commit the current memory image of this entry to permanent
     * storage.
     */
    public void save() throws CollaborationException;

    /**
     * removes this entry from the personal store.  This operation is committed
     * immediately to permanent storage.
     */
    public void remove() throws CollaborationException;


    // ---------------------------- //
    //   pre-defined entry types    //
    // ---------------------------- //

    /**
     * an entity that can be contacted, e.g. a user, a group, 
     * a location, ...
     */
    public static final String CONTACT = "contact";

    /**
     * a bookmark
     */
    public static final String BOOKMARK = "bookmark";
    
    /**
     * a group.  Contact which lets you reach one to many users.
     */
    public static final String GROUP = "group";

    /**
     * a conference room
     */
    public static final String CONFERENCE = "conference";

    /**
     * a watcher
     */
    public static final String WATCHER = "watcher";
    
    /**
     * profile info - individually accessible element of 
     * application configuration information.
     */
    public static final String PROFILE = "profile";

    /**
     * an entry which purpose is to reference other personal store 
     * entry
     */
    public static final String FOLDER = "book";

    /**
     * a particular type of folder which usually contains contacts
     */
    public static final String CONTACT_FOLDER = "abook";

    /**
     * a particular type of folder which usually contains bookmarks
     */
    public static final String BOOKMARK_FOLDER = "bbook";

    /**
     * a particular type of folder which usually contains application
     * profiles.
     */
    public static final String PROFILE_FOLDER = "pbook";

    /**
     * an IM gateway.
     */
    public static final String GATEWAY = "gateway";

}

