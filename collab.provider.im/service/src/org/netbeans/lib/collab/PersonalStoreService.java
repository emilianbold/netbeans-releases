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
 * The PersonalStore Service
 * The service should be intialized by calling intialize() before using any
 * of the methods.
 *
 * @since version 0.1
 *
 */
public interface PersonalStoreService {
    /**
     * retrieves a personal store entry from its entry id
     * @param entryType entry type, as defined in PersonalStoreEntry
     * @param entryId entry's unique id
     * @return entry if found.
     */
    public PersonalStoreEntry getEntry(String entryType,
                                       String entryId)
                                                throws CollaborationException;

    /**
     * retrieves a personal store entry from its entry id
     * @param entryType entry type, as defined in PersonalStoreEntry
     * @param entryId entry's unique id
     * @param principal owner of the personal store to query
     * @return entry if found.
     */
    public PersonalStoreEntry getEntry(CollaborationPrincipal principal,
                                       String entryType,
                                       String entryId)
                                                throws CollaborationException;

    /**
     * retrieves all entries of a specific type from the personal
     * store
     * note: this assumes there aren't too many folders in the personal store
     * <p>
     * note: retrieves only the folders, not the entries.
     * @param entryType folderType type of folder as defined in PersonalStoreEntryType
     * @return a Collection of PersonalStoreFolder objects
     */
    public java.util.Collection getEntries(String entryType)
                                                throws CollaborationException;

    /**
     * retrieves all the folders in the personal store
     * note: this assumes there aren't too many folders in the personal store
     * <p>
     * note: retrieves only the folders, not the entries.
     * @param entryType folderType type of folder as defined in PersonalStoreEntryType
     * @return a Collection of PersonalStoreFolder objects
     */
    public java.util.Collection getFolders(String entryType)
                                                throws CollaborationException;

    /**
     * retrieves all the folders in the personal store of a specified principal
     * note: this assumes there aren't too many folders in the personal store
     * <p>
     * note: retrieves only the folders, not the entries.
     * @param principal owner of the personal store to query
     * @param entryType folderType type of folder as defined in PersonalStoreEntryType
     * @return a Collection of PersonalStoreFolder objects
     */
    public java.util.Collection getFolders(CollaborationPrincipal principal,
                                           String entryType)
                                                throws CollaborationException;

    /**
     * creates a new entry
     * This method does not necessarily commit the entry to permanent storage.
     * The other attributes of the created entry need to be set using the
     * methods in PersonalStoreEntry or subclass thereof.  In particular,
     * the folder(s) referencing this entry must be specified using
     * PersonalStoreEntry.addToFolder().
     * After the entry is complete, it must be committed with its save()
     * method.
     *
     * @param entryType type of entry as defined in PersonalStoreEntryType
     * @param displayName entry display name
     * @return a new entry, which class depends on the specified type.
     * Note that the returned entry may be a folder.
     */
    public PersonalStoreEntry createEntry(String entryType,
					  String displayName)
                                                throws CollaborationException;


    /**
     * creates a new entry on behalf of a specified user
     * This method does not necessarily commit the entry to permanent storage.
     * The other attributes of the created entry need to be set using the
     * methods in PersonalStoreEntry or subclass thereof.  In particular,
     * the folder(s) referencing this entry must be specified using
     * PersonalStoreEntry.addToFolder().
     * After the entry is complete, it must be committed with its save()
     * method.
     *
     * @param principal owner ofd the personal store in which to add the
     * new entry
     * @param entryType type of entry as defined in PersonalStoreEntryType
     * @param displayName entry display name
     * @return a new entry, which class depends on the specified type.
     * Note that the returned entry may be a folder.
     */
    public PersonalStoreEntry createEntry(CollaborationPrincipal principal,
                                          String entryType,
                                          String displayName)
                                                throws CollaborationException;

    /**
     * search directory for entry
     * This method search the corporate directory for entries that can then
     * be used to add as a PersonalContact to the PersonalStoreFolder.
     * There are four types of search criteria: BYUID, CONTAINNAME, STARTNAME,
     * ENDNAME.  When a particular one is choosed to be added to a folder,
     * pass that as the argument to the createEntry() method.
     *
     * @param searchType type of search to perform
     * @param pattern searchName name to search for
     * @return a array of principals from the search result,
     *         or null if none matched
     */
    public CollaborationPrincipal[] searchPrincipals(int searchType,
                                                     String pattern)
                                                 throws CollaborationException;


    /**
     * search directory for entry
     * This method search the corporate directory for entries that can
     * then be used to add as a PersonalContact to the PersonalStoreFolder.
     * There are four types of search criteria: BYUID, CONTAINNAME, STARTNAME,
     * ENDNAME.  When a particular one is choosed to be added to a folder,
     * pass that as the argument to the createEntry() method.
     * The attribute for the search can be NAME_ATTRIBUTE or UID_ATTRIBUTE
     * or MAIL_ATTRIBUTE
     *
     * @param searchType type of search to perform
     * @param pattern searchName name to search for
     * @param attribute Attribute on which to search for
     * @return a array of principals from the search result,
     *         or null if none matched
     */
    public CollaborationPrincipal[] searchPrincipals(int searchType,
                                                     String pattern,
                                                     int attribute)
                                                throws CollaborationException;

    /**
     * search directory for entry
     * This method search the corporate directory for entries that can then be
     * used to add as a PersonalContact to the PersonalStoreFolder.
     * There are four types of search criteria: BYUID, CONTAINNAME, STARTNAME,
     * ENDNAME.  When a particular one is choosed to be added to a folder,
     * pass that as the argument to the createEntry() method.
     *
     * @param searchType type of search to perform
     * @param pattern searchName name to search for
     * @return a array of principals from the search result,
     *         or null if none matched
     */
    public PersonalStoreEntry[] search(int searchType,
                                       String pattern,
                                       String entryType)
                                                throws CollaborationException;

    /**
     * @param searchType type of search to perform
     * @param pattern searchName name to search for
     * @param entryType type of the entry to search for
     * @param attribute Attribute on which to search for
     * @return a array of principals from the search result,
     *         or null if none matched
     */
    public PersonalStoreEntry[] search(int searchType,
                                       String pattern,
                                       String entryType,
                                       int attribute)
                                                throws CollaborationException;

    /**
     * get the profile info
     *
     * @return profile the profile info
     */
    public PersonalProfile getProfile() throws CollaborationException;

    /**
     * get the profile info
     *
     * @param principal owner of the personal store to query
     * @return profile the profile info
     */
    public PersonalProfile getProfile(CollaborationPrincipal principal)
                                                throws CollaborationException;

    /**
     * Commit unsaved personal store changes to the server
     */
    public void save() throws CollaborationException;


    /**
     * intialize the service by providing a PersonalStoreServiceListener.
     * Service should be initialized by calling this method before using
     * any of the services of PersonalStoreService
     * @param listener PersonalStoreServiceListener
     */
    public void initialize(PersonalStoreServiceListener listener)
                                                throws CollaborationException;

    /**
     * Add an additional PersonalStoreServiceListener to receive the event notifications.
     * To receive all the initial events the first PersonalStoreServiceListener should be
     * added while {@link #initialize initializing} PersonalStoreService.
     * @param listener PersonalStoreServiceListener The PersonalStoreServiceListener to be added.
     */
    public void addPersonalStoreServiceListener(PersonalStoreServiceListener listener);

    /**
     * Removes an already added ConfereneServiceListener. To prevent loss of any event
     * notification it is advised to have atleast one PersonalStoreServiceListener
     * @param listener PersonalStoreServiceListener The PersonalStoreServiceListener to be removed.
     */
    public void removePersonalStoreServiceListener(PersonalStoreServiceListener listener);

    /*
    //valid result is returned but there may be more that were past the limit
    public static final int SEARCH_LIMIT_EXCEEDED = 1;

    //normal returned results
    public static final int SEARCH_OK = 2;

    //the argument (filter) to use for the search was invalid
    public static final int SEARCH_BAD_ARGUMENT = 3;

    //search has been disabled
    public static final int SEARCH_DISABLED = 4;

    //an error has occured and we dont know why
    public static final int SEARCH_ERROR = 5;
    */

    // ----------------------------- //
    //  pre-defined search criteria  //
    // ----------------------------- //

    /**
     * search by contain name
     */
    public static final int SEARCHTYPE_CONTAINS = 1;

    /**
     * search by start with name
     */
    public static final int SEARCHTYPE_STARTSWITH = 2;

    /**
     * search by end with name
     */
    public static final int SEARCHTYPE_ENDSWITH = 3;

    /**
     * equals
     */
    public static final int SEARCHTYPE_EQUALS = 0;

    /**
     * search on name attribute
     */
    public static final int NAME_ATTRIBUTE = 1;

    /**
     * search on uid attribute
     */
    public static final int UID_ATTRIBUTE = 2;

    /**
     * search on mail attribute
     */
    public static final int MAIL_ATTRIBUTE = 4;
}
