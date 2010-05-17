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
 * This class is a Factory class, which can be used to create
 * CollborationSessions. It uses an implementation of
 * CollaborationSessionProvider to create Collaborationsessions.
 * The no argument constructor uses the {@link #systemProperty java system property}
 * to identify the class name of the CollaborationSessionProvider
 * implementation. Alternatively the other constructor takes the name of the
 * class implementing the CollaborationSessionProvider interface.
 *
 * For a list of supported CollaborationSessionProvier implementations refer 
 * to your API provider's documentation. 
 *  
 * For a descripton of serviceURL refer to your API provider's documentation.
 *
 * Starting point used to create sessions.
 * 
 * @see <a href="README">Default API provider's documentation</a>
 * 
 * 
 * @since version 0.1
 * 
 */
public class CollaborationSessionFactory {

    /**
     * major version number
     */
    public static final int MAJOR_VERSION = 0;

    /**
     * minor version number
     */
    public static final int MINOR_VERSION = 1;

    private String defaultClassName = "org.netbeans.lib.collab.xmpp.XMPPSessionProvider";
    private CollaborationSessionProvider impl;

    /**
     * System property containing the name of the CollaborationSessionFactory
     * to use.  This system property must be set in order to use
     * a non-default implementation of CollaborationSessionFactory
     */
    public static final String systemProperty = "org.netbeans.lib.collab.CollaborationSessionFactory";


    public CollaborationSessionFactory() throws Exception
    {
	String className = defaultClassName;

	// get the factory class name
        try {
            String systemProp = System.getProperty(systemProperty);
            if (systemProp!=null) {
                className = systemProp;
            }
        }catch (SecurityException se) {
        }

	impl = (CollaborationSessionProvider)Class.forName(className).newInstance();
    }


    public CollaborationSessionFactory(String className) throws Exception
    {
	impl = (CollaborationSessionProvider)Class.forName(className).newInstance();
    }

    /**
     * creates a collaboration session.
     *
     * @param serviceUrl URL for the authentication service (hostname and port).
     * @param loginName login name
     * @param password user password
     * @param listener session listener to convey asynchronous errors and events.
     *
     * @return an authenticated collaboration services session.
     */
    public CollaborationSession getSession(String serviceUrl, String loginName, String password, CollaborationSessionListener listener) throws CollaborationException
    {
	return impl.getSession(serviceUrl, loginName, password, listener);
    }

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
        throws CollaborationException 
    {
        return impl.getSession(serviceUrl, destination,
                               loginName, password,
                               listener);
    }

    /**
     * return the CollaborationSessionProvider instance in use by this factory
     * @return CollaborationSessionProvider instance    
     */    
    public CollaborationSessionProvider getCollaborationSessionProvider() {
        return impl;
    }
    
    public void close()
    {
        impl.close();
    }
}

