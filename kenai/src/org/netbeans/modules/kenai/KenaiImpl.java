/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.kenai;

import java.util.Collection;
import org.netbeans.modules.kenai.api.KenaiException;

/**
 * SPI entry point.
 *
 * @author Maros Sandor
 * @author Jan Becicka
 */
public abstract class KenaiImpl {

    public abstract Collection<LicensesListData.LicensesListItem> getLicenses() throws KenaiException;

    public abstract Collection<ServicesListData.ServicesListItem> getServices() throws KenaiException;

    public abstract String checkName(String name) throws KenaiException;


    /**
     * Searches kenai for projects.
     *
     * @param pattern pattern to search for. Currenlty only substring match is supported.
     * @return list of Kenai projects
     * @throws org.netbeans.modules.kenai.api.KenaiException
     */
    public abstract Collection<ProjectData> searchProjects(String pattern) throws KenaiException;

    public abstract Collection<ProjectData> getMyProjects() throws KenaiException;

    /**
     * Retrieves all available information about a Kenai project.
     *
     * @param name name of the proejct to query
     * @return KenaiProjectImpl or null if the project does not exist
     * @throws org.netbeans.modules.kenai.api.KenaiException
     */
    public abstract ProjectData getProject(String name) throws KenaiException;

    public abstract Collection<UserData> getProjectMembers(String name) throws KenaiException;

    /**
     * Asks whether a person is authorized to perform an activity on a particular project.
     *
     * @param projectName name of a project
     * @param feature feature to check
     * @param activity activity to check
     * @return true if the person is authorized to perform the activity on the project, false otherwise
     * @throws org.netbeans.modules.kenai.api.KenaiException
     */
    public abstract boolean isAuthorized(String projectName, String feature, String activity) throws KenaiException;

    /**
     * Verifies that the supplied credentials are valid.
     *
     * @param username
     * @param password
     * @return users short name
     * @throws org.netbeans.modules.kenai.api.KenaiException if credentials are not valid or some other error occurrs
     */
    public abstract String verify(String username, char[] password) throws KenaiException;

    /**
     *
     * @param username
     * @param password
     * @throws org.netbeans.modules.kenai.api.KenaiException
     */
    public abstract void register(String username, char[] password) throws KenaiException ;

    /**
     * 
     * @param name
     * @param displayName
     * @param description
     * @param licenses
     * @param tags
     * @return
     * @throws org.netbeans.modules.kenai.api.KenaiException
     */
    public abstract ProjectData createProject (
            String name,
            String displayName,
            String description,
            String[] licenses,
            String tags
            ) throws KenaiException;

    /**
     * 
     * @param projectName
     * @param name
     * @param display_name
     * @param description
     * @param url
     * @param repository_url
     * @param browse_url
     * @param service
     * @return
     * @throws org.netbeans.modules.kenai.api.KenaiException
     */
    public abstract FeatureData createProjectFeature(
            String projectName,
            String name,
            String display_name,
            String description,
            String url,
            String repository_url,
            String browse_url,
            String service
            ) throws KenaiException;

}
