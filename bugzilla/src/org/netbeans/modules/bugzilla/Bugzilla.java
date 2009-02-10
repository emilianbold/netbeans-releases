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

package org.netbeans.modules.bugzilla;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.mylyn.internal.bugzilla.core.BugzillaCorePlugin;
import org.eclipse.mylyn.internal.bugzilla.core.BugzillaCustomField;
import org.eclipse.mylyn.internal.bugzilla.core.BugzillaRepositoryConnector;
import org.eclipse.mylyn.internal.bugzilla.core.RepositoryConfiguration;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Tomas Stupka
 */
public class Bugzilla {

    private BugzillaRepositoryConnector brc;

    private static Bugzilla instance;

    public static Logger LOG = Logger.getLogger("org.netbeans.modules.jira.Jira");

    private RequestProcessor rp;
    private Map<String, RepositoryConfiguration> repoToRepoconf = new HashMap<String, RepositoryConfiguration>();
    
    private Bugzilla() {
        BugzillaCorePlugin bcp = new BugzillaCorePlugin();
        try {
            bcp.start(null);
        } catch (Exception ex) {
            throw new RuntimeException(ex); // XXX thisiscrap
        }
    }

    public static Bugzilla getInstance() {
        if(instance == null) {
            instance = new Bugzilla();
        }
        return instance;
    }

    public BugzillaRepositoryConnector getRepositoryConnector() {
        if(brc == null) {
            brc = new BugzillaRepositoryConnector();
        }
        return brc;
    }

    public List<String> getProducts(BugzillaRepository repo) throws MalformedURLException, CoreException, IOException {
        return getRepositoryConfiguration(repo).getProducts();
    }

    public List<String> getComponents(BugzillaRepository repository, String product) throws IOException, CoreException {
        if(product == null) {
            return getRepositoryConfiguration(repository).getComponents();
        } else {
            return getRepositoryConfiguration(repository). getComponents(product);
        }
    }

    public List<String> getResolutions(BugzillaRepository repository) throws IOException, CoreException {
        return getRepositoryConfiguration(repository).getResolutions();
    }

    public List<String> getVersions(BugzillaRepository repository, String product) throws IOException, CoreException {
        if(product == null) {
            return getRepositoryConfiguration(repository).getVersions();
        } else {
            return getRepositoryConfiguration(repository).getVersions(product);
        }
    }

    public List<String> getStatusValues(BugzillaRepository repository) throws IOException, CoreException {
        return getRepositoryConfiguration(repository).getStatusValues();
    }

    public List<String> getPriorities(BugzillaRepository repository) throws IOException, CoreException {
        return getRepositoryConfiguration(repository).getPriorities();
    }

    public List<BugzillaCustomField> getFields(BugzillaRepository repository) throws IOException, CoreException {
        return getRepositoryConfiguration(repository).getCustomFields();
    }

    public RequestProcessor getRequestProcessor() {
        if(rp == null) {
            rp = new RequestProcessor("Bugzilla Tasks"); // XXX is tp 1 enough?
        }
        return rp;
    }

    private RepositoryConfiguration getRepositoryConfiguration(BugzillaRepository repository) throws CoreException, IOException {
        RepositoryConfiguration rc = repoToRepoconf.get(repository.getDisplayName());
        if(rc == null) {
            rc = getRepositoryConnector().getClientManager().getClient(repository.getTaskRepository(), new NullProgressMonitor()).getRepositoryConfiguration(new NullProgressMonitor());
            repoToRepoconf.put(repository.getDisplayName(), rc);
        }
        return rc;
    }

}
