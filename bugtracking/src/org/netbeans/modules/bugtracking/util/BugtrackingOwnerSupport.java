/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.bugtracking.util;

import org.netbeans.modules.bugtracking.kenai.spi.KenaiUtil;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.bugtracking.BugtrackingManager;
import org.netbeans.modules.bugtracking.kenai.spi.KenaiProject;
import org.netbeans.modules.bugtracking.kenai.spi.OwnerInfo;
import org.netbeans.modules.bugtracking.spi.BugtrackingConnector;
import org.netbeans.modules.bugtracking.spi.Repository;
import org.netbeans.modules.bugtracking.ui.selectors.RepositorySelectorBuilder;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Stupka
 * @author Marian Petras
 */
public abstract class BugtrackingOwnerSupport {

    private static BugtrackingOwnerSupport instance;

    protected BugtrackingOwnerSupport() { }

    public static BugtrackingOwnerSupport getInstance() {
        if(instance == null) {
            instance = Lookup.getDefault().lookup(BugtrackingOwnerSupport.class);
            if (instance == null) {
                instance = new DefaultImpl();
            }
        }
        return instance;
    }

    public enum ContextType {
        MAIN_PROJECT_ONLY,
        MAIN_OR_SINGLE_PROJECT,
        ALL_PROJECTS,
        SELECTED_FILE_AND_ALL_PROJECTS,
    }

    //--------------------------------------------------------------------------

    public static Project getMainOrSingleProject() {
        final OpenProjects projects = OpenProjects.getDefault();

        Project[] openProjects = projects.getOpenProjects();
        if (openProjects.length == 1) {
            return openProjects[0];
        } else {
            return projects.getMainProject();
        }
    }

    //--------------------------------------------------------------------------

    public Repository getRepository(ContextType context) {
        switch (context) {
            case MAIN_PROJECT_ONLY:
                Project mainProject = OpenProjects.getDefault().getMainProject();
                if (mainProject != null) {
                    return getRepository(mainProject, false);
                }
                break;
            case MAIN_OR_SINGLE_PROJECT:
                Project mainOrSingleProject = getMainOrSingleProject();
                if (mainOrSingleProject != null) {
                    return getRepository(mainOrSingleProject, false);
                }
                break;
            case ALL_PROJECTS:
                return getRepository(OpenProjects.getDefault().getOpenProjects());
            case SELECTED_FILE_AND_ALL_PROJECTS:
                File contextFile = BugtrackingUtil.getLargerContext();
                if (contextFile != null) {
                    return getRepositoryForContext(contextFile, false);
                }
                break;
            default:
                assert false;
                break;
        }
        return null;
    }

    public Repository getRepository(Node... nodes) {
        if (nodes == null) {
            return null;
        }
        if (nodes.length == 0) {
            return null;
        }
        if (nodes.length == 1) {
            return getRepository(nodes[0]);
        }

        Repository chosenRepo = null;
        for (Node node : nodes) {
            Repository repo = getRepository(node);
            if (repo == null) {
                continue;
            }
            if (chosenRepo == null) {
                chosenRepo = repo;
            } else if (repo != chosenRepo) {    //various repositories assigned
                return null;
            }
        }
        return chosenRepo;
    }

    protected Repository getRepository(Node node) {
        final Lookup nodeLookup = node.getLookup();

        Project project = nodeLookup.lookup(Project.class);
        if (project != null) {
            return getRepository(project, false);
        }

        DataObject dataObj = nodeLookup.lookup(DataObject.class);
        if (dataObj != null) {
            return getRepository(dataObj);
        }

        return null;
    }

    protected abstract Repository getRepository(DataObject dataObj);

    public Repository getRepository(Project... projects) {
        if (projects.length == 0) {
            return null;
        }
        if (projects.length == 1) {
            return getRepository(projects[0], false);
        }

        Repository chosenRepo = null;
        for (Project project : projects) {
            Repository repo = getRepository(project, false);
            if (repo == null) {
                continue;
            }
            if (chosenRepo == null) {
                chosenRepo = repo;
            } else if (repo != chosenRepo) {
                return null;   //the projects have various repositories assigned
            }
        }
        return chosenRepo;
    }

    public abstract Repository getRepository(Project project, boolean askIfUnknown);

    public Repository getRepository(File file, boolean askIfUnknown) {
        return getRepository(file, null, askIfUnknown);
    }

    public abstract Repository getRepository(File file, String issueId, boolean askIfUnknown);

    protected Repository getRepositoryForContext(File context,
                                                 boolean askIfUnknown) {
        return getRepositoryForContext(context, null, askIfUnknown);
    }

    protected abstract Repository getRepositoryForContext(File context,
                                                          String issueId,
                                                          boolean askIfUnknown);

    public void setFirmAssociations(File[] files, Repository repository) {
        if (files == null) {
            throw new IllegalArgumentException("files is null");        //NOI18N
        }
        if (files.length == 0) {
            return;
        }

        FileToRepoMappingStorage.getInstance().setFirmAssociation(
                BugtrackingUtil.getLargerContext(files[0]),
                repository);
    }

    public void setFirmAssociation(File file, Repository repository) {
        FileToRepoMappingStorage.getInstance().setFirmAssociation(
                BugtrackingUtil.getLargerContext(file),
                repository);
    }

    public void setLooseAssociation(ContextType contextType, Repository repository) {
        final OpenProjects projects = OpenProjects.getDefault();

        File context = null;

        switch (contextType) {
            case MAIN_PROJECT_ONLY:
                Project mainProject = projects.getMainProject();
                if (mainProject != null) {
                    context = BugtrackingUtil.getLargerContext(mainProject);
                }
                break;
            case MAIN_OR_SINGLE_PROJECT:
                Project mainOrSingleProject = getMainOrSingleProject();
                if (mainOrSingleProject != null) {
                    context = BugtrackingUtil.getLargerContext(mainOrSingleProject);
                }
                break;
            case ALL_PROJECTS:
                context = BugtrackingUtil.getContextFromProjects();
                break;
            case SELECTED_FILE_AND_ALL_PROJECTS:
                context = BugtrackingUtil.getLargerContext();
                break;
            default:
                assert false;
                break;
        }

        if (context != null) {
            FileToRepoMappingStorage.getInstance().setLooseAssociation(
                    context,
                    repository);
        }
    }

    public void setLooseAssociation(File file, Repository repository) {
        FileToRepoMappingStorage.getInstance().setLooseAssociation(
                BugtrackingUtil.getLargerContext(file),
                repository);
    }

    /**
     * Returns all repository urls that appear in a <strong>firm</strong> association.
     * @return
     */
    public final Collection<String> getAllAssociatedUrls() {
        return FileToRepoMappingStorage.getInstance().getAllFirmlyAssociatedUrls();
    }

    //--------------------------------------------------------------------------

    private static class DefaultImpl extends BugtrackingOwnerSupport {

        private static Logger LOG = Logger.getLogger("org.netbeans.modules.bugtracking.bridge.BugtrackingOwnerSupport");   // NOI18N

        protected Repository getRepository(DataObject dataObj) {
            FileObject fileObj = dataObj.getPrimaryFile();
            if (fileObj == null) {
                return null;
            }

            Project project = FileOwnerQuery.getOwner(fileObj);
            if (project != null) {
                return getRepository(project, false);
            }

            Repository repo;

            try {
                repo = getKenaiBugtrackingRepository(fileObj);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
                repo = null;
            }

            return repo;
        }

        public Repository getRepository(Project project, boolean askIfUnknown) {
            Repository repo;

            FileObject fileObject = project.getProjectDirectory();
            try {
                repo = getKenaiBugtrackingRepository(fileObject);
                if (repo != null) {
                    return repo;
                }
            } catch (IOException ex) {
                return null;
            }

            File context = BugtrackingUtil.getLargerContext(project);
            if (context != null) {
                return getRepositoryForContext(context, null, askIfUnknown);
            } else {
                return askUserToSpecifyRepository(null, null);
            }
        }

        public Repository getRepository(File file, String issueId, boolean askIfUnknown) {
            //TODO - synchronization/threading
            FileObject fileObject = FileUtil.toFileObject(file);
            if (fileObject == null) {
                LOG.log(Level.WARNING, " did not find a FileObject for file {0}", new Object[] {file}); //NOI18N
            } else {
                try {
                    Repository repo = getKenaiBugtrackingRepository(fileObject);
                    if (repo != null) {
                        return repo;
                    }
                } catch (IOException ex) {
                    LOG.log(Level.WARNING,
                          " communication with Kenai failed while loading " //NOI18N
                              + "information about bugtracking repository", //NOI18N
                          ex);
                    return null;
                }
            }

            File context = BugtrackingUtil.getLargerContext(file, fileObject);
            if (context == null) {
                context = file;
            }

            return getRepositoryForContext(context, issueId, askIfUnknown);
        }

        protected Repository getRepositoryForContext(File context, String issueId,
                                                     boolean askIfUnknown) {
            Repository repo = FileToRepoMappingStorage.getInstance()
                              .getFirmlyAssociatedRepository(context);
            if (repo != null) {
                LOG.log(Level.FINER, " found stored repository [" + repo    //NOI18N
                                     + "] for directory " + context); //NOI18N
                return repo;
            }

            Repository suggestedRepository = FileToRepoMappingStorage.getInstance()
                                             .getLooselyAssociatedRepository(context);
            if (!askIfUnknown) {
                return suggestedRepository;
            }

            repo = askUserToSpecifyRepository(issueId, suggestedRepository);
            if (repo != null) {
                return repo;
            }

            return null;
        }

        private static Repository getKenaiBugtrackingRepository(FileObject fileObject) throws IOException {
            return getRepository(fileObject);
        }

        /**
         *
         * @param fileObject
         * @return
         * @throws IOException
         */
        private static Repository getRepository(FileObject fileObject) throws IOException {
            Object attValue = fileObject.getAttribute(
                                           "ProvidedExtensions.RemoteLocation");//NOI18N
            if (attValue instanceof String) {
                Repository repository = null;
                String url = (String) attValue;
                if(BugtrackingUtil.isNbRepository(url)) {
                    File file = FileUtil.toFile(fileObject);
                    if(file != null) {
                        OwnerInfo ownerInfo = KenaiUtil.getOwnerInfo(file);
                        if(ownerInfo != null) {
                            repository = KenaiUtil.getRepository(url, ownerInfo.getOwner());
                        }
                    }
                }
                if(repository != null) {
                    return repository;
                }
                try {
                    repository = KenaiUtil.getRepository(url);
                    if (repository != null) {
                        return repository;
                    }
                } catch (IOException ex) {
                    /* the remote location (URL) denotes a Kenai project */
                    if ("Not Found".equals(ex.getMessage())) {              //NOI18N
                        BugtrackingManager.LOG.log(Level.INFO,
                                "Kenai project corresponding to URL "       //NOI18N
                                        + attValue
                                        + " does not exist.");              //NOI18N
                    } else {
                        BugtrackingManager.LOG.throwing(
                                BugtrackingOwnerSupport.class.getName(),    //class name
                                "getRepository(String)",    //method name //NOI18N
                                ex);
                    }
                    throw ex;
                }
            }
            return null;
        }

        /**
         * Find a Kenai bug-tracking repository for the given URL.
         *
         * @param  string containing information about bug-tracking repository
         *         or versioning repository
         * @return  instance of an existing Kenai bug-tracking repository, if the
         *          given string denotes such a repository;
         *          {@code null} if the string format does not denote a Kenai
         *          bug-tracking repository (in other cases, an exception is thrown)
         * @throws  org.netbeans.modules.kenai.api.KenaiException
         *          if the URI denotes a Kenai project's repository but there was
         *          some problem getting the project's repository, e.g. because
         *          the given project does not exist on Kenai
         */
        private static Repository getKenaiBugtrackingRepository(String remoteLocation) throws IOException {
            return KenaiUtil.getRepository(remoteLocation);
        }

        private Repository askUserToSpecifyRepository(String issueId,
                                                      Repository suggestedRepo) {
            Repository[] repos = BugtrackingUtil.getKnownRepositories(true);
            BugtrackingConnector[] connectors = BugtrackingUtil.getBugtrackingConnectors();

            final RepositorySelectorBuilder selectorBuilder = new RepositorySelectorBuilder();
            selectorBuilder.setDisplayFormForExistingRepositories(true);
            selectorBuilder.setExistingRepositories(repos);
            selectorBuilder.setBugtrackingConnectors(connectors);
            selectorBuilder.setPreselectedRepository(suggestedRepo);
            selectorBuilder.setLabelAboveComboBox();

            final String dialogTitle = getMessage("LBL_BugtrackerSelectorTitle"); //NOI18N

            DialogDescriptor dialogDescriptor
                    = selectorBuilder.createDialogDescriptor(dialogTitle);

            Object selectedOption = DialogDisplayer.getDefault().notify(dialogDescriptor);
            if (selectedOption == NotifyDescriptor.OK_OPTION) {
                Repository repository = selectorBuilder.getSelectedRepository();
                try {
                    repository.getController().applyChanges();
                } catch (IOException ex) {
                    LOG.log(Level.SEVERE, null, ex);
                    repository = null;
                }
                return repository;
            } else {
                return null;
            }
        }

        private String getMessage(String msgKey) {
            return NbBundle.getMessage(BugtrackingOwnerSupport.class, msgKey);
        }

    }

}
