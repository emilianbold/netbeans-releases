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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.mylyn.internal.bugzilla.core.BugzillaClient;
import org.eclipse.mylyn.internal.bugzilla.core.BugzillaCorePlugin;
import org.eclipse.mylyn.internal.bugzilla.core.BugzillaCustomField;
import org.eclipse.mylyn.internal.bugzilla.core.BugzillaRepositoryConnector;
import org.eclipse.mylyn.internal.bugzilla.core.RepositoryConfiguration;
import org.netbeans.modules.bugtracking.spi.BugtrackingController;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Tomas Stupka
 */
public class Bugzilla {

    private BugzillaRepositoryConnector brc;

    private static Bugzilla instance;

    public static Logger LOG = Logger.getLogger("org.netbeans.modules.bugzilla.Bugzilla"); // NOI18N

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

    // XXX private?
    public BugzillaRepositoryConnector getRepositoryConnector() {
        if(brc == null) {
            brc = new BugzillaRepositoryConnector();
        }
        return brc;
    }

    /**
     * Returns a BugzillaClient for the given repository
     * @param repository
     * @return
     * @throws java.net.MalformedURLException
     * @throws org.eclipse.core.runtime.CoreException
     */
    public BugzillaClient getClient(BugzillaRepository repository) throws MalformedURLException, CoreException {
        return getRepositoryConnector().getClientManager().getClient(repository.getTaskRepository(), new NullProgressMonitor());
    }

    /**
     * Returns all products defined in the given repository
     *
     * @param repository
     * @return
     * @throws java.io.IOException
     * @throws org.eclipse.core.runtime.CoreException
     */
    public List<String> getProducts(BugzillaRepository repo) throws CoreException, IOException {
        return getRepositoryConfiguration(repo).getProducts();
    }

    /**
     * Returns the componets for the given product or all known components if product is null
     *
     * @param repository
     * @param product
     * @return list of components
     * @throws java.io.IOException
     * @throws org.eclipse.core.runtime.CoreException
     */
    public List<String> getComponents(BugzillaRepository repository, String product) throws IOException, CoreException {
        if(product == null) {
            return getRepositoryConfiguration(repository).getComponents();
        } else {
            return getRepositoryConfiguration(repository). getComponents(product);
        }
    }

    /**
     * Returns all resolutions defined in the given repository
     *
     * @param repository
     * @return
     * @throws java.io.IOException
     * @throws org.eclipse.core.runtime.CoreException
     */
    public List<String> getResolutions(BugzillaRepository repository) throws IOException, CoreException {
        return getRepositoryConfiguration(repository).getResolutions();
    }

    /**
     * Returns versiones defined for the given product or all available versions if product is null
     *
     * @param repository
     * @param product
     * @return
     * @throws java.io.IOException
     * @throws org.eclipse.core.runtime.CoreException
     */
    public List<String> getVersions(BugzillaRepository repository, String product) throws IOException, CoreException {
        if(product == null) {
            return getRepositoryConfiguration(repository).getVersions();
        } else {
            return getRepositoryConfiguration(repository).getVersions(product);
        }
    }

    /**
     * Returns all status defined in the given repository
     * @param repository
     * @return
     * @throws java.io.IOException
     * @throws org.eclipse.core.runtime.CoreException
     */
    public List<String> getStatusValues(BugzillaRepository repository) throws IOException, CoreException {
        return getRepositoryConfiguration(repository).getStatusValues();
    }

    /**
     * Returns all open statuses defined in the given repository.
     * @param repository
     * @return all open statuses defined in the given repository.
     * @throws java.io.IOException
     * @throws org.eclipse.core.runtime.CoreException
     */
    public List<String> getOpenStatusValues(BugzillaRepository repository) throws IOException, CoreException {
        return getRepositoryConfiguration(repository).getOpenStatusValues();
    }

    /**
     * Returns all priorities defined in the given repository
     * @param repository
     * @return
     * @throws java.io.IOException
     * @throws org.eclipse.core.runtime.CoreException
     */
    public List<String> getPriorities(BugzillaRepository repository) throws IOException, CoreException {
        return getRepositoryConfiguration(repository).getPriorities();
    }

    /**
     * Returns all keywords defined in the given repository
     * @param repository
     * @return
     * @throws java.io.IOException
     * @throws org.eclipse.core.runtime.CoreException
     */
    public List<String> getKeywords(BugzillaRepository repository) throws IOException, CoreException {
        return getRepositoryConfiguration(repository).getKeywords();
    }

    /**
     * Returns all platforms defined in the given repository
     * @param repository
     * @return
     * @throws java.io.IOException
     * @throws org.eclipse.core.runtime.CoreException
     */
    public List<String> getPlatforms(BugzillaRepository repository) throws IOException, CoreException {
        return getRepositoryConfiguration(repository).getPlatforms();
    }

    /**
     * Returns all operating systems defined in the given repository
     * @param repository
     * @return
     * @throws java.io.IOException
     * @throws org.eclipse.core.runtime.CoreException
     */
    public List<String> getOSs(BugzillaRepository repository) throws IOException, CoreException {
        return getRepositoryConfiguration(repository).getOSs();
    }

    /**
     * Returns all severities defined in the given repository
     * @param repository
     * @return
     * @throws java.io.IOException
     * @throws org.eclipse.core.runtime.CoreException
     */
    public List<String> getSeverities(BugzillaRepository repository) throws IOException, CoreException {
        return getRepositoryConfiguration(repository).getSeverities();
    }

    /**
     * Returns all custom fields defined in the given repository
     * @param repository
     * @return
     * @throws java.io.IOException
     * @throws org.eclipse.core.runtime.CoreException
     */
    public List<BugzillaCustomField> getCustomFields(BugzillaRepository repository) throws IOException, CoreException {
        return getRepositoryConfiguration(repository).getCustomFields();
    }

    /**
     * Returns target milestones defined for the given product or all available 
     * milestones if product is null
     *
     * @param repository
     * @param product
     * @return
     * @throws java.io.IOException
     * @throws org.eclipse.core.runtime.CoreException
     */
    public List<String> getTargetMilestones(BugzillaRepository repository, String product) throws IOException, CoreException {
        if(product == null) {
            return getRepositoryConfiguration(repository).getTargetMilestones();
        } else {
            return getRepositoryConfiguration(repository).getTargetMilestones(product);
        }
    }

    /**
     * Returns the request processor for common tasks in bugzilla.
     * Do not use this when accesing a remote repository.
     * 
     * @return
     */
    public RequestProcessor getRequestProcessor() {
        if(rp == null) {
            rp = new RequestProcessor("Bugzilla"); // NOI18N
        }
        return rp;
    }

    private RepositoryConfiguration getRepositoryConfiguration(BugzillaRepository repository) throws CoreException, IOException {
        RepositoryConfiguration rc = repoToRepoconf.get(repository.getDisplayName());
        if(rc == null) {
            assert !SwingUtilities.isEventDispatchThread() : "Accessing remote host. Do not call in awt";
            rc = getRepositoryConnector().getClientManager().getClient(repository.getTaskRepository(), new NullProgressMonitor()).getRepositoryConfiguration(new NullProgressMonitor());

            repository.getController().addPropertyChangeListener(new RepositoryListener(repository));
            repoToRepoconf.put(repository.getDisplayName(), rc);
        }
        return rc;
    }

    public void removeRepository(BugzillaRepository repository) {
        repoToRepoconf.remove(repository.getDisplayName());
        getRepositoryConnector().getClientManager().repositoryRemoved(repository.getTaskRepository());
    }

    private class RepositoryListener implements PropertyChangeListener {
        private final BugzillaRepository repository;

        public RepositoryListener(BugzillaRepository repository) {
            this.repository = repository;
        }

        public void propertyChange(PropertyChangeEvent evt) {
            if(evt.getPropertyName().equals(BugtrackingController.EVENT_COMPONENT_DATA_APPLIED)) {
                repository.removePropertyChangeListener(this);
                removeRepository(repository);
            }
        }
    }

}
