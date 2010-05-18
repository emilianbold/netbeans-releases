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
import java.util.List;
import java.util.Set;

/**
 *
 *@author Rahul K Singh
 *
 * The major difference between this and ConferenceService is , ExtendedConferenceService
 * requires <component ID> for all of its operation. All the operation performed here are specfic 
 * to a particular <component ID>.
 * Conference Ids are of the form    [ <conference-name> "@" ] <component ID>
 * 
 */
public interface ExtendedConferenceService extends ConferenceService{
    /**
     * setup a new conference
     *
     * @param listener conference listener
     * @param accessLevel privilegdes to assign to the invited user.
     * @param component ID.
     * @return a new conference handle.  The only member so far is the owner
     * of this session.  The invite method in the Conference object can then
     * be used to invite other users to the conference.
     */
    public Conference setupConference(
                            ConferenceListener listener, int accessLevel , String component)
                                                throws CollaborationException;

    /**
     * join a public conference
     *
     * @param destination conference address
     * @param listener conference listener. This listener can also be instance of 
     * @param component ID.
     * ConferencePasswordListener or ConferenceEventListener
     */
    public Conference joinPublicConference(
                        String destination, ConferenceListener listener, String component)
                                                throws CollaborationException;

    /**
     * join a public conference
     *
     * @param nick The nick name to be used in conference room
     * @param history The detail about the history messages. It should be null if the default
     * behaviour is desired.
     * @param destination conference address
     * @param listener conference listener. This listener can also be instance of 
     * @param component ID.
     * ConferencePasswordListener or ConferenceEventListener
     */
    public Conference joinPublicConference(
                        String nick, ConferenceHistory history, String destination, ConferenceListener listener, String component)
                                                throws CollaborationException;

    
    /**
     * create a new public conference
     * A public conference is a conference which persists even when no
     * member is present.  It is generally used as a public instant discussion
     * forum, aka public chat room.  It differs from a bulletin board in that
     * messages are not persistant.
     * A public conference is joined by other users using the
     * joinPublicConference method.
     * @see #joinPublicConference joinPublicConference
     * @param destination identifier for this conference
     * @param listener conference listener. This listener can also be instance of 
     * ConferencePasswordListener or ConferenceEventListener
     * @param accessLevel default privilegdes to assign to the joining users.
     * @param component ID.
     */
    public Conference setupPublicConference(
            String destination, ConferenceListener listener, int accessLevel, String component)
                                                throws CollaborationException;
    /**
     * retrieve a public conference without joining
     * Verified that the conference exists
     * @param destination identifier for this conference
     * @param component id. 
     *      if null then search in all the components available.
     *      this operation might take more time depending on number of components.    
     */
    public Conference getPublicConference(String destination, String component)
                                                throws CollaborationException;

    /**
     * list the conference rooms with specified access
     * @param access The access level as defined in this class
     * @param component id. 
     *      if null then search in all the components available.
     *      this operation might take more time depending on number of components.    
     * @return An array of Conference objects
     */
    public Conference[] listConference(int access, String component) throws CollaborationException;
    
     /**
     * list the  MultiuserChat providers
     * @param domain domainname to search muc providers for.
     * if null lists all the component in all servers this server knows of.
     * @return list of names of MUC providers.
     */
    public Set getMUCProviders(String domain)throws CollaborationException;
    
    /**
     * sets the default MUC provider
     * @param component id
     */
    public void setDefaultMUCProvider(String component)throws CollaborationException;

    /**
     * @param access Access to check for : pass 0 if you are not interested in this.
     * @param searchType search type as used in PersonalStoreService.
     * @filter filter results based on this param.
     * @component Which component to apply this search on. If null, will search in all
     * components hosted for the users domain.
     */
    public Conference[] searchConferences(int access, int searchType, String filter, String component) 
        throws CollaborationException ;
}
