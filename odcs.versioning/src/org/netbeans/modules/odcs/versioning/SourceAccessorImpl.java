/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.odcs.versioning;

import com.tasktop.c2c.server.scm.domain.ScmLocation;
import com.tasktop.c2c.server.scm.domain.ScmRepository;
import com.tasktop.c2c.server.scm.domain.ScmType;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.net.PasswordAuthentication;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.modules.odcs.api.ODCSProject;
import org.netbeans.modules.odcs.client.api.ODCSException;
import org.netbeans.modules.odcs.ui.api.ODCSUiServer;
import org.netbeans.modules.odcs.ui.spi.VCSAccessor;
import org.netbeans.modules.odcs.versioning.spi.ApiProvider;
import org.netbeans.modules.odcs.versioning.spi.ApiProvider.LocalRepositoryInitializer;
import org.netbeans.modules.team.ide.spi.IDEProject;
import org.netbeans.modules.team.ide.spi.IDEServices;
import org.netbeans.modules.team.ide.spi.ProjectServices;
import org.netbeans.modules.team.ui.spi.ProjectHandle;
import org.netbeans.modules.team.ui.spi.SourceAccessor;
import org.netbeans.modules.team.ui.spi.SourceHandle;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;

/**
 *
 * @author Milan Kubec, Jan Becicka, Tomas Stupka
 */

@ServiceProviders( { @ServiceProvider(service=SourceAccessor.class),
    @ServiceProvider(service=VCSAccessor.class)
})
public class SourceAccessorImpl extends VCSAccessor {

    public SourceAccessorImpl() { }
    
    @Override
    public Class<ODCSProject> type() {
        return ODCSProject.class;
    }
    
    @Override
    public List<SourceHandle> getSources(ProjectHandle<ODCSProject> prjHandle) {
        return getSources(prjHandle, null, false);
    }

    @Override
    public Action getOpenSourcesAction(SourceHandle srcHandle) {
        return getDefaultAction(srcHandle);
    }

    @Override
    public Action getDefaultAction(SourceHandle srcHandle) {
        assert srcHandle instanceof SourceHandleImpl;
        SourceHandleImpl impl = (SourceHandleImpl) srcHandle;
        return new GetSourcesFromODCSAction(new ProjectAndRepository(impl.getProjectHandle(), impl.getRepository()), impl);
    }

    @Override
    public Action getDefaultAction(final IDEProject ideProject) {
        return new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ProjectServices projects = Lookup.getDefault().lookup(ProjectServices.class);
                if (!projects.openProject(ideProject.getURL())) {
                    ideProject.notifyDeleted();
                }
            }
        };
    }

    @Override
    public Action getOpenOtherAction(final SourceHandle src) {
        return new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ProjectServices projects = Lookup.getDefault().lookup(ProjectServices.class);
                projects.openOtherProject(src.getWorkingDirectory());
            }
        };
    }

    @Override
    public Action getOpenFavoritesAction(final SourceHandle src) {
        final IDEServices ide = Lookup.getDefault().lookup(IDEServices.class);
        return ide.canOpenInFavorites() ?
            new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    ide.openInFavorites(src.getWorkingDirectory());
                 }
            } : null;
    }

    @Override
    public Action getOpenHistoryAction (ProjectHandle<ODCSProject> prjHandle, String repositoryName, String commitId) {
        assert !EventQueue.isDispatchThread();
        List<SourceHandle> sources = getSources(prjHandle, repositoryName, true);
        Action action = null;
        if (!sources.isEmpty()) {
            SourceHandleImpl sourceHandle = (SourceHandleImpl) sources.get(0);
            final File workdir = sourceHandle.getWorkingDirectory();
            if (workdir != null) {
                ApiProvider[] providers = getProvidersFor(ScmType.GIT); // support only git for now
                for (ApiProvider p : providers) {
                    action = p.createOpenHistoryAction(workdir, commitId);
                    if (action != null) {
                        break;
                    }
                }
            }
        }
        return action;
    }

    @Override
    public RepositoryInitializer getRepositoryInitializer (String repositoryKind) {
        RepositoryInitializer initializer = null;
        ScmType type = ScmType.valueOf(repositoryKind);
        ApiProvider[] providers = getProvidersFor(type);
        if (providers.length > 0) {
            ApiProvider provider = providers[0];
            LocalRepositoryInitializer localRepoInitializer = provider.getRepositoryInitializer();
            if (localRepoInitializer != null) {
                initializer = new RepositoryInitImpl(localRepoInitializer);
            }
        }
        return initializer;
    }

    private List<SourceHandle> getSources (ProjectHandle<ODCSProject> prjHandle, String repositoryName, boolean onlySupported) {
        ODCSProject project = prjHandle.getTeamProject();
        List<SourceHandle> handlesList = new ArrayList<SourceHandle>();
        
        try {
            if (project != null && project.hasScm()) {
                Collection<ScmRepository> repositories = project.getRepositories();
                for (ScmRepository repository : repositories) {
                    if (repositoryName != null && !repositoryName.equals(repository.getName())) {
                        continue;
                    }
                    boolean supported;
                    if (repository.getScmLocation() == ScmLocation.CODE2CLOUD) {
                        supported = isSupported(repository.getType());
                    } else {
                        supported = isSupported(null);
                    }
                    if (onlySupported && !supported) {
                        continue;
                    }
                    SourceHandleImpl srcHandle = new SourceHandleImpl((ProjectHandle<ODCSProject>)prjHandle, repository, supported);
                    handlesList.add(srcHandle);
                }
            }
        } catch (ODCSException ex) {
            Logger.getLogger(SourceAccessorImpl.class.getName()).log(ex instanceof ODCSException.ODCSCanceledException
                    ? Level.FINE
                    : Level.INFO, prjHandle.getId(), ex);
        }
        
        return handlesList.isEmpty() ? Collections.<SourceHandle>emptyList() : handlesList;
    }

    @Override
    public Action getOpenSourcesAction(ODCSUiServer server) {
        return new GetSourcesFromODCSAction(server);
    }

    @Override
    public boolean hasSources(ProjectHandle<ODCSProject> project) {
        return project.getTeamProject().hasScm();
    }

    public static class ProjectAndRepository {
        public ProjectHandle<ODCSProject> project;
        public ScmRepository repository;
        public String externalScmType;
        public ProjectAndRepository(ProjectHandle<ODCSProject> project, ScmRepository repository) {
            this.project = project;
            this.repository = repository;
        }
    }

    static boolean isSupported (ScmType type) {
        boolean supported;
        if (type == null) {
            supported = Lookup.getDefault().lookup(ApiProvider.class) != null;
        } else {
            supported = getProvidersFor(type).length > 0;
        }
        return supported;
    }

    static ApiProvider[] getProvidersFor (ScmType type) {
        Collection<? extends ApiProvider> allProviders = Lookup.getDefault().lookupAll(ApiProvider.class);
        List<ApiProvider> providers = new ArrayList<ApiProvider>(allProviders.size());
        for (ApiProvider prov : allProviders) {
            if (type == null || prov.accepts(type.name())) {
                providers.add(prov);
            }
        }
        return providers.toArray(new ApiProvider[providers.size()]);
    }
    
    private static class RepositoryInitImpl implements RepositoryInitializer {
        private final LocalRepositoryInitializer delegate;

        private RepositoryInitImpl (LocalRepositoryInitializer delegate) {
            this.delegate = delegate;
        }

        @Override
        public void initialize (File localFolder, String repositoryUrl, PasswordAuthentication credentials) throws IOException {
            delegate.initLocalRepository(localFolder, repositoryUrl, credentials);
        }
        
    }
}
