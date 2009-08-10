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

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.bugtracking.spi.BugtrackingConnector;
import org.netbeans.modules.bugtracking.spi.Repository;
import org.netbeans.modules.bugtracking.ui.selectors.RepositorySelectorBuilder;
import org.netbeans.modules.kenai.api.KenaiException;
import org.netbeans.modules.kenai.api.KenaiProject;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Stupka
 * @author Marian Petras
 */
public class BugtrackingOwnerSupport {

    private static Logger LOG = Logger.getLogger("org.netbeans.modules.bugtracking.bridge.BugtrackingOwnerSupport");   // NOI18N

    private Map<File, Repository> fileToRepo = new HashMap<File, Repository>(10);
    private static BugtrackingOwnerSupport instance;

    private BugtrackingOwnerSupport() { }

    public static BugtrackingOwnerSupport getInstance() {
        if(instance == null) {
            instance = new BugtrackingOwnerSupport();
        }
        return instance;
    }

    public enum ContextType {
        MAIN_PROJECT_ONLY,
        MAIN_OR_SINGLE_PROJECT,
        ALL_PROJECTS,
        SELECTED_FILE_AND_ALL_PROJECTS,
    }

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

    public static Project getMainOrSingleProject() {
        final OpenProjects projects = OpenProjects.getDefault();

        Project[] openProjects = projects.getOpenProjects();
        if (openProjects.length == 1) {
            return openProjects[0];
        } else {
            return projects.getMainProject();
        }
    }

    public Repository getRepository(Project[] projects) {
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
                break;        //the projects have various repositories assigned
            }
        }
        return chosenRepo;
    }

    public Repository getRepository(Project project, boolean askIfUnknown) {
        Repository repo;

        FileObject fileObject = project.getProjectDirectory();
        try {
            repo = getKenaiBugtrackingRepository(fileObject);
            if (repo != null) {
                return repo;
            }
        } catch (KenaiException ex) {
            return null;
        }

        File context = BugtrackingUtil.getLargerContext(project);
        return getRepositoryForContext(context, null, askIfUnknown);
    }

    public Repository getRepository(File file, boolean askIfUnknown) {
        return getRepository(file, null, askIfUnknown);
    }

    public Repository getRepository(File file, String issueId, boolean askIfUnknown) {
        //TODO - synchronization/threading
        Repository repo = fileToRepo.get(file);
        if(repo != null) {
            LOG.log(Level.FINER, " found cached repository [{0}] for file {1}", new Object[] {repo, file}); // NOI18N
            return repo;
        }

        LOG.log(Level.FINER, " no repository cached for file {1}", new Object[] {file}); // NOI18N

        FileObject fileObject = FileUtil.toFileObject(file);
        if (fileObject == null) {
            LOG.log(Level.WARNING, " did not find a FileObject for file {0}", new Object[] {file}); //NOI18N
            return null;
        }

        try {
            repo = getKenaiBugtrackingRepository(fileObject);
            if (repo != null) {
                return repo;
            }
        } catch (KenaiException ex) {
            return null;
        }

        File context = BugtrackingUtil.getLargerContext(file, fileObject);
        if (context == null) {
            return null;
        }

        return getRepositoryForContext(context, issueId, askIfUnknown);
    }

    private Repository getRepositoryForContext(File context,
                                               boolean askIfUnknown) {
        return getRepositoryForContext(context, null, askIfUnknown);
    }

    private Repository getRepositoryForContext(File context, String issueId,
                                               boolean askIfUnknown) {
        Repository repo;

        repo = fileToRepo.get(context);
        if (repo != null) {
            LOG.log(Level.FINER, " found cached repository [" + repo    //NOI18N
                                 + "] for directory " + context); //NOI18N
            return repo;
        }

        repo = FileToRepoMappingStorage.getInstance()
               .getFirmlyAssociatedRepository(context);
        if (repo != null) {
            LOG.log(Level.FINER, " found stored repository [" + repo    //NOI18N
                                 + "] for directory " + context); //NOI18N
            fileToRepo.put(context, repo);
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

    private static Repository getKenaiBugtrackingRepository(FileObject fileObject) throws KenaiException {
        Object attValue = fileObject.getAttribute(
                                   "ProvidedExtensions.RemoteLocation");//NOI18N
        if (attValue instanceof String) {
            Repository repository;
            try {
                repository = getKenaiBugtrackingRepository((String) attValue);
                if (repository != null) {
                    return repository;
                }
            } catch (KenaiException ex) {
                /* the remote location (URL) denotes a Kenai project */
                if ("Not Found".equals(ex.getMessage())) {              //NOI18N
                    LOG.log(Level.INFO,
                            "Kenai project corresponding to URL "       //NOI18N
                                    + attValue
                                    + " does not exist.");              //NOI18N
                } else {
                    LOG.throwing(
                            BugtrackingOwnerSupport.class.getName(),    //class name
                            "getKenaiBugtrackingRepository(String)",    //method name //NOI18N
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
    private static Repository getKenaiBugtrackingRepository(String remoteLocation) throws KenaiException {
        KenaiProject project = KenaiProject.forRepository(remoteLocation);//throws KenaiException
        return (project != null)
               ? KenaiUtil.getKenaiBugtrackingRepository(project)
               : null;        //not a Kenai project repository
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

    public void setFirmAssociations(File[] files, Repository repository) {
        if (files == null) {
            throw new IllegalArgumentException("files is null");        //NOI18N
        }
        if (files.length == 0) {
            return;
        }
        
        fileToRepo.clear();
        for (int i = 0; i < files.length; i++) {
            fileToRepo.put(files[i], repository);
        }

        FileToRepoMappingStorage.getInstance().setFirmAssociation(
                BugtrackingUtil.getLargerContext(files[0]),
                repository);
    }

    public void setFirmAssociation(File file, Repository repository) {
        fileToRepo.clear();
        fileToRepo.put(file, repository);

        FileToRepoMappingStorage.getInstance().setFirmAssociation(
                BugtrackingUtil.getLargerContext(file),
                repository);
    }

    private Repository askUserToSpecifyRepository(String issueId,
                                                  Repository suggestedRepo) {
        Repository[] repos = BugtrackingUtil.getKnownRepositories();
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
