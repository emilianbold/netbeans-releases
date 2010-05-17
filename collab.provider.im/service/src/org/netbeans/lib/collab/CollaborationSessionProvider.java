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

/**
 * Interface defining a Collaboratio Session Provider.
 * implementations of this interface are loaded by
 * CollaborationSessionFactory.
 *
 *
 * @since version 0.1
 *
 */
public interface CollaborationSessionProvider {

    /**
     * creates a collaboration session.
     *
     * @param serviceUrl service access point.  May include hostname
     *   domain name, port number, or other parameter to be interpreted
     *   by API implementations to establish a connection with
     *   the service.
     * @param loginName login name
     * @param password user password
     * @param listener session listener to convey asynchronous errors
     *    and events.
     *
     * @return an authenticated collaboration services session.
     */
    public CollaborationSession getSession(String serviceUrl, String loginName, String password, CollaborationSessionListener listener) throws CollaborationException;

    /**
     * creates a collaboration session.
     *
     * @param serviceUrl service access point.  May include hostname
     *   domain name, port number, or other parameter to be interpreted
     *   by API implementations to establish a connection with
     *   the service.
     * @param destination address to be used by others
     *   to identify the created session. 
     *   In the case of XMPP, this is the full JID
     *   (resource included) used by this session.
     * @param loginName login name
     * @param password user password
     * @param listener session listener to convey asynchronous errors
     *    and events.
     *
     * @return an authenticated collaboration services session.
     */
    public CollaborationSession getSession(String serviceUrl,
                                           String destination,
                                           String loginName,
                                           String password,
                                           CollaborationSessionListener listener)
        throws CollaborationException;

     /**
     * registers the user with the server    
     * @param serviceURL  hostname of the IM server
     * @param listener registration listener to convey 
     * asynchronous registration events.
     *
     * @return true if registration is successful
     */
    
    public void register(String serviceURL, RegistrationListener listener) throws CollaborationException;
    
     /**
     * registers the user with the server    
     * @param serviceURL  hostname of the IM server
     * @param domain - explictly specify the domain to which the user wants to register.
     * @param listener registration listener to convey 
     * asynchronous registration events.
     *
     * @return true if registration is successful
     */
    
    public void register(String serviceURL, String domain , RegistrationListener listener) throws CollaborationException;
    
    /**
     * @return application info
     */
    public ApplicationInfo getApplicationInfo() throws CollaborationException;
    
    /**
     * @deprecated use getApplicationInfo.  it will instantiate as needed
     * @param ai update application information based on information provided 
     */
    public void setApplicationInfo(ApplicationInfo ai) throws CollaborationException;
    
    /**
     * Register a SASL client side provider factory with the session provider.
     * If multiple provider factories are registered and they support same subset of 
     * mechanism's , then the last factory registered will override the previous ones.
     *
     */
    public void registerProvider(SASLClientProviderFactory providerfac);
    
    /**
     * Find out if there is a provider factory  registered for the mechanism specified.
     * There will always be a provider for old jabber auth , and usually
     * SASL PLAIN and SASL DIGEST-MD5 should also be always present by default.
     *
     * @return 
     * true if there is a provider registered to handle the specified mechanism.
     */
    public boolean isSASLProviderRegistered(String mechanism);

    /**
     * gets an existing client session.
     * This method is used when the application creates many session 
     * objects and relies on the API to keep track of them.
     *
     * @param uid fully-qualified user id
     *
     * @return a collaboration services session.
     */
    /*
    public CollaborationSession getExistingSession(String uid) throws CollaborationException;
    */

    /**
     * tells the provider to release any resources.
     */
    public void close();
}

