/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.cnd.remote.projectui.wizard.cnd;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.api.toolchain.CompilerSetManager;
import org.netbeans.modules.cnd.makeproject.api.wizards.IteratorExtension;
import org.netbeans.modules.dlight.libs.common.PathUtilities;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager.CancellationException;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils.ExitStatus;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Alexander Sinon
 */
public class CreateProjectFromBinary implements PropertyChangeListener {
    private static final boolean TRACE_REMOTE_CREATION = Boolean.getBoolean("cnd.discovery.trace.projectimport"); // NOI18N
    public static final Logger logger;
    static {
        logger = Logger.getLogger("org.netbeans.modules.cnd.makeproject.api.RunDialogPanel"); // NOI18N
        if (TRACE_REMOTE_CREATION) {
            logger.setLevel(Level.ALL);
        }
    }
    private static final RequestProcessor RP = new RequestProcessor("Create Remote Project Worker", 1); //NOI18N
    private final FileSystem fileSystem;
    private final String projectFolder;
    private final String executablePath;
    private final boolean sourcesUsed;
    private final String libraries;
    private final IteratorExtension.ProjectKind kind;
    
    private Project lastSelectedProject;
    
    public CreateProjectFromBinary(FileSystem fileSystem, String projectFolder, String executablePath, boolean sourcesUsed, String libraries, IteratorExtension.ProjectKind kind) {
        this.fileSystem = fileSystem;
        this.projectFolder = projectFolder;
        this.executablePath = executablePath;
        this.sourcesUsed = sourcesUsed;
        this.libraries = libraries;
        this.kind = kind;
    }
    
    public Project createRemoteProject() {
        RP.post(new Runnable() {

            @Override
            public void run() {
                ExecutionEnvironment executionEnvironment = FileSystemProvider.getExecutionEnvironment(fileSystem);
                ProgressHandle createHandle = ProgressHandleFactory.createHandle(NbBundle.getMessage(CreateProjectFromBinary.class, "RUN_PROJECT_CREATOR",executionEnvironment.getDisplayName()));
                createHandle.start();
                try {
                    createRemoteProjectImpl();
                } finally {
                    createHandle.finish();
                }
            }
        });
        return null;
    }

    private Project createRemoteProjectImpl() {
        ExecutionEnvironment executionEnvironment = FileSystemProvider.getExecutionEnvironment(fileSystem);
        FileObject projectFO = null;
        try {
            projectFO = FileUtil.createFolder(fileSystem.getRoot(), projectFolder);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        FileObject projectCreator = findProjectCreator();
        if (projectCreator == null) {
            DialogDisplayer.getDefault().notify(
                    new NotifyDescriptor.Message(
                            NbBundle.getMessage(CreateProjectFromBinary.class, "ERROR_FIND_PROJECT_CREATOR",executionEnvironment.getDisplayName()),
                            NotifyDescriptor.ERROR_MESSAGE));
            return null;
        }
        String sources;
        if (sourcesUsed) {
            sources = "--sources=used"; //NOI18N
        } else {
            sources = "--sources=all"; //NOI18N
        }
        if (TRACE_REMOTE_CREATION) {
            if (libraries == null) {
                logger.log(Level.INFO, "#{0} --netbeans-project={1} --project-create binary={2} "+sources, // NOI18N
                        new Object[]{projectCreator.getPath(), projectFolder, executablePath});
            } else {
                logger.log(Level.INFO, "#{0} --netbeans-project={1} --project-create binary={2} "+sources+" --additional-libraries="+libraries, // NOI18N
                        new Object[]{projectCreator.getPath(), projectFolder, executablePath});
            }
        }
        ExitStatus execute;
        if (libraries == null) {
            execute = ProcessUtils.execute(executionEnvironment, projectCreator.getPath()
                                     , "--netbeans-project="+projectFolder // NOI18N
                                     , "--project-create", "binary="+executablePath // NOI18N
                                     , sources 
                                     );
        } else {
            execute = ProcessUtils.execute(executionEnvironment, projectCreator.getPath()
                                     , "--netbeans-project="+projectFolder // NOI18N
                                     , "--project-create", "binary="+executablePath // NOI18N
                                     , sources 
                                     , "--additional-libraries="+libraries  // NOI18N
                                     );
        }
        if (TRACE_REMOTE_CREATION) {
            logger.log(Level.INFO, "#exitCode={0}", execute.exitCode); // NOI18N
            logger.log(Level.INFO, execute.error);
            logger.log(Level.INFO, execute.output);
        }
        if (!execute.isOK()) {
            // probably java does not found an
            // try to find java in environment variables
            String java = null; 
            try {
                java = HostInfoUtils.getHostInfo(executionEnvironment).getEnvironment().get("JDK_HOME"); // NOI18N
                if (java == null || java.isEmpty()) {
                    java = HostInfoUtils.getHostInfo(executionEnvironment).getEnvironment().get("JAVA_HOME"); // NOI18N
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            } catch (CancellationException ex) {
                Exceptions.printStackTrace(ex);
            }
            if (java != null) {
                if (libraries == null) {
                    execute = ProcessUtils.execute(executionEnvironment, projectCreator.getPath()
                                             , "--netbeans-project="+projectFolder // NOI18N
                                             , "--project-create", "binary="+executablePath // NOI18N
                                             , sources 
                                             );
                } else {
                    execute = ProcessUtils.execute(executionEnvironment, projectCreator.getPath()
                                             , "--netbeans-project="+projectFolder // NOI18N
                                             , "--project-create", "binary="+executablePath // NOI18N
                                             , sources 
                                             , "--additional-libraries="+libraries  // NOI18N
                                             );
                }
            }
            if (!execute.isOK()) {
                DialogDisplayer.getDefault().notify(
                        new NotifyDescriptor.Message(
                                NbBundle.getMessage(CreateProjectFromBinary.class, "ERROR_RUN_PROJECT_CREATOR",executionEnvironment.getDisplayName()),
                                NotifyDescriptor.ERROR_MESSAGE));
                return null;
            }
        }
        if (projectFO != null) {
            projectFO.refresh();
        } else {
            String baseDir = projectFolder;
            while (true) {
                baseDir = PathUtilities.getDirName(baseDir);
                if (baseDir == null || baseDir.length() <= 1) {
                    break;
                }
                FileObject toRefresh = fileSystem.findResource(baseDir);
                if (toRefresh != null) {
                    toRefresh.refresh();
                    break;
                }
            }
            projectFO = fileSystem.findResource(projectFolder);
        }
        if (projectFO == null) {
            return null;
        }
        Project project = null;
        try {
            project = ProjectManager.getDefault().findProject(projectFO);
            if (project == null) {
                return null;
            }
            lastSelectedProject = project;
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IllegalArgumentException ex) {
            Exceptions.printStackTrace(ex);
        }
        OpenProjects.getDefault().addPropertyChangeListener(this);
        OpenProjects.getDefault().open(new Project[]{project}, false);
        OpenProjects.getDefault().setMainProject(project);
        return project;
    }

    private FileObject findProjectCreator() {
        ExecutionEnvironment executionEnvironment = FileSystemProvider.getExecutionEnvironment(fileSystem);
        for(CompilerSet set : CompilerSetManager.get(executionEnvironment).getCompilerSets()) {
            if (set.getCompilerFlavor().isSunStudioCompiler()) {
                String directory = set.getDirectory();
                FileObject projectCreator = fileSystem.findResource(directory+"/../lib/ide_project/bin/ide_project"); // NOI18N
                if (projectCreator != null && projectCreator.isValid()) {
                    return projectCreator;
                }
            }
        }
        return null;
    }


    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(OpenProjects.PROPERTY_OPEN_PROJECTS)) {
            if (evt.getNewValue() instanceof Project[]) {
                Project[] projects = (Project[])evt.getNewValue();
                if (projects.length == 0) {
                    return;
                }
                OpenProjects.getDefault().removePropertyChangeListener(this);
                if (lastSelectedProject == null) {
                    return;
                }
                fillConfiguration();
            }
        }
    }
    
    private void fillConfiguration() {
        IteratorExtension extension = Lookup.getDefault().lookup(IteratorExtension.class);
        if (extension != null) {
            extension.discoverHeadersByModel(lastSelectedProject);
        }
    }
}
