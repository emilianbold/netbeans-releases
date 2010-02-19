/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.bugtracking.kenai.spi;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.PasswordAuthentication;
import java.util.Collection;
import javax.swing.JLabel;
import org.netbeans.modules.bugtracking.spi.RepositoryUser;
import org.openide.nodes.Node;

/**
 * Implementation provides access to kenai API
 * 
 * @author Tomas Stupka
 */
public abstract class KenaiAccessor {

    public static final String PROP_LOGIN = "kenai.login.changed";              // NOI18N

    /**
     * Returns all projects from the kenai dashboard
     * @return
     */
    public abstract KenaiProject[] getDashboardProjects();

    /**
     * Returns a KenaiProject for the given kenai vcs repository url
     *
     * @param url
     * @return
     * @throws IOException
     */
    public abstract KenaiProject getKenaiProjectForRepository(String repositoryUrl) throws IOException;

    /**
     * Returns a KenaiProject for the given kenai url
     * 
     * @param url a kenai url, might be one of the following:<br>
     *          <ul>
     *              <li>kenai host url</li>
     *              <li>kenai vcs repository url</li>
     *              <li>kenai issuetracker url</li>
     *          </ul>
     * @param projectName
     * @return
     * @throws IOException
     */
    public abstract KenaiProject getKenaiProject(String url, String projectName) throws IOException;

    /**
     * Determines wheter the given kenai instance is logged or not
     * @param url
     * @return
     */
    public abstract boolean isLoggedIn(String url);

    /**
     * Opens the kenai login dialog
     *
     * @return returns true if a login was confirmed
     */
    public abstract boolean showLogin();

    /**
     * Determines wheter the given url belongs to a kenai project or not
     *
     * @param url
     * @return true if the given url belongs to a kenai project, otherwise false
     */
    public abstract Collection<RepositoryUser> getProjectMembers(KenaiProject kp) throws IOException;

    /**
     * User credentials in the given kenai site
     * @param url a kenai site url
     * @param forceLogin force a user login in case no credentials are available at the moment
     * @return
     */
    public abstract PasswordAuthentication getPasswordAuthentication(String url, boolean forceLogin);

    /**
     * Returns a ui widget representing the given user on the given kenai site.
     *
     * @param userName user
     * @param host kenai site host
     * @param chatMessage text will be addded to the chat window in case
     *                    a chat session is activated was the widged
     * @return
     */
    public abstract JLabel createUserWidget(String userName, String host, String chatMessage);

    /**
     * Determines wheter the netbeans.org kenai site is registered in the IDE or not
     * @return
     */
    public abstract boolean isNetbeansKenaiRegistered();

    /**
     * Returns OwnerInfo for the given file
     * @param file
     * @return
     */
    public abstract OwnerInfo getOwnerInfo(File file);

    /**
     * Returns OwnerInfo for the given node
     * @param node
     * @return {@link OwnerInfo}
     */
    public abstract OwnerInfo getOwnerInfo(Node node);

    /**
     * Logs kenai usage. You should know what you do when calling this.
     *
     * @param parameters
     */
    public abstract void logKenaiUsage(Object... parameters);

    /**
     * Registers a listener on a kenai instance with the given url if such is available
     *
     * @param listener
     * @param kenaiHostUrl
     */
    public abstract void addPropertyChangeListener(PropertyChangeListener listener, String kenaiHostUrl);

    /**
     * Unregisters a listener on a kenai instance with the given url if such is available
     * @param listener
     * @param kenaiHostUrl
     */
    public abstract void removePropertyChangeListener(PropertyChangeListener listener, String kenaiHostUrl);

}
