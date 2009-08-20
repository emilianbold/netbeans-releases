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
package org.netbeans.modules.maven.execute;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import org.apache.maven.project.MavenProject;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.netbeans.modules.maven.api.execute.RunConfig;
import org.netbeans.modules.maven.embedder.EmbedderFactory;
import org.netbeans.modules.maven.options.MavenSettings;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;



/**
 *
 * @author mkleint
 */
public class BeanRunConfig implements RunConfig {
    
    private File executionDirectory;
    private WeakReference<Project> project;
    private FileObject projectDirectory;
    private List<String> goals;
    private String executionName;
    private Properties properties;
    //for these delegate to default options for defaults.
    private boolean showDebug = MavenSettings.getDefault().isShowDebug();
    private boolean showError = MavenSettings.getDefault().isShowErrors();
    private Boolean offline = MavenSettings.getDefault().isOffline();
    private boolean updateSnapshots = MavenSettings.getDefault().isUpdateSnapshots();
    private boolean interactive = MavenSettings.getDefault().isInteractive();
    private List<String> activate;
    private boolean recursive = true;
    private String taskName;
    private RunConfig parent;
    private String actionName;
    private FileObject selectedFO;
    private MavenProject mp;
    private RunConfig preexecution;
    private String preactionname;
    private ReactorStyle reactor = ReactorStyle.NONE;
    
    /** Creates a new instance of BeanRunConfig */
    public BeanRunConfig() {
    }

    /**
     * create a new instance that wraps around the parent instance, allowing
     * to change values while delegating to originals if not changed.
     * @param parent
     */
    public BeanRunConfig(RunConfig parent) {
        this.parent = parent;
        //boolean props need to be caried over
        setRecursive(parent.isRecursive());
        setInteractive(parent.isInteractive());
        setOffline(parent.isOffline());
        setShowDebug(parent.isShowDebug());
        setShowError(parent.isShowError());
        setUpdateSnapshots(parent.isUpdateSnapshots());
        setReactorStyle(parent.getReactorStyle());
    }

    public final File getExecutionDirectory() {
        if (parent != null && executionDirectory == null) {
            return parent.getExecutionDirectory();
        }
        return executionDirectory;
    }

    public final void setExecutionDirectory(File executionDirectory) {
        this.executionDirectory = executionDirectory;
    }

    public final Project getProject() {
        if (parent != null && project == null) {
            return parent.getProject();
        }
        if (project != null) {
            Project prj = project.get();
            if (prj == null && projectDirectory.isValid()) {
                try {
                    prj = ProjectManager.getDefault().findProject(projectDirectory);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (IllegalArgumentException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            return prj;
        }
        return null;
    }

    public final synchronized MavenProject getMavenProject() {
        if (mp != null) {
            return mp;
        }
        Project prj = getProject();
        if (prj != null) {
            NbMavenProjectImpl impl = prj.getLookup().lookup(NbMavenProjectImpl.class);
            List<String> profiles = new ArrayList<String>();
            profiles.addAll(impl.getCurrentActiveProfiles());
            if (getActivatedProfiles() != null) {
                profiles.addAll(getActivatedProfiles());
            }
            Properties props = new Properties();
            if (getProperties() != null) {
                props.putAll(getProperties());
            }
            //#168036 use it's own embedder to prevent caching
            mp = impl.loadMavenProject(EmbedderFactory.createProjectLikeEmbedder(), profiles, props);
        }
        return mp;
     }

    public final synchronized void setProject(Project project) {
        if (project != null) {
            this.project = new WeakReference<Project>(project);
            projectDirectory  = project.getProjectDirectory();
        } else {
            this.project = null;
            projectDirectory = null;
        }
        mp = null;
    }

    public final List<String> getGoals() {
        if (parent != null && goals == null) {
            return parent.getGoals();
        }
        return goals;
    }

    public final void setGoals(List<String> goals) {
        this.goals = goals;
    }

    public final String getExecutionName() {
        if (parent != null && executionName == null) {
            return parent.getExecutionName();
        }
        return executionName;
    }

    public final void setExecutionName(String executionName) {
        this.executionName = executionName;
    }

    public final Properties getProperties() {
        if (parent != null && properties == null) {
            return parent.getProperties();
        }
        Properties newProperties = new Properties();
        if (properties != null) {
            newProperties.putAll(properties);
        }
        return newProperties;
    }

    public final String removeProperty(String key) {
        if (properties == null) {
            properties = new Properties();
            if (parent != null) {
                properties.putAll(parent.getProperties());
            }
        }
        String toRet = (String) properties.remove(key);
        synchronized (this) {
            mp = null;
        }
        return toRet;
    }

    public final String setProperty(String key, String value) {
        if (properties == null) {
            properties = new Properties();
            if (parent != null) {
                properties.putAll(parent.getProperties());
            }
        }
        String toRet = (String) properties.setProperty(key, value);
        synchronized (this) {
            mp = null;
        }
        return toRet;
    }

    public final void setProperties(Properties props) {
        if (properties == null) {
            properties = new Properties();
        }
        properties.clear();
        properties.putAll(props);
        synchronized (this) {
            mp = null;
        }
    }

    public final boolean isShowDebug() {
        return showDebug;
    }

    public final void setShowDebug(boolean showDebug) {
        this.showDebug = showDebug;
    }

    public final boolean isShowError() {
        return showError;
    }

    public final void setShowError(boolean showError) {
        this.showError = showError;
    }

    public final Boolean isOffline() {
        return offline;
    }

    public final void setOffline(Boolean offline) {
        this.offline = offline;
    }

    public final List<String> getActivatedProfiles() {
        if (parent != null && activate == null) {
            return parent.getActivatedProfiles();
        }
        if (activate != null) {
            return Collections.unmodifiableList(activate);
        }
        return Collections.<String>emptyList();
    }

    public final void setActivatedProfiles(List<String> activeteProfiles) {
        activate = new ArrayList<String>();
        activate.addAll(activeteProfiles);
        synchronized (this) {
            mp = null;
        }
    }

    public final boolean isRecursive() {
        return recursive;
    }
    
    public final void setRecursive(boolean rec) {
        recursive = rec;
    }

    public final boolean isUpdateSnapshots() {
        return updateSnapshots;
    }
    
    public final void setUpdateSnapshots(boolean set) {
        updateSnapshots = set;
    }

    public final String getTaskDisplayName() {
        if (parent != null && taskName == null) {
            return parent.getTaskDisplayName();
        }
        return taskName;
    }
    
    public final void setTaskDisplayName(String name) {
        taskName = name;
    }

    public final boolean isInteractive() {
        return interactive;
    }
    
    public final void setInteractive(boolean ia) {
        interactive = ia;
    }


    public void setActionName(String actionName) {
        this.actionName = actionName;
    }

    public String getActionName()
    {
        if (parent != null && actionName == null) {
            return parent.getActionName();
        }
        return actionName;
    }

    public FileObject getSelectedFileObject() {
        if (parent != null && selectedFO == null) {
            return parent.getSelectedFileObject();
        }
        return selectedFO;
    }

    public void setFileObject(FileObject selectedFile) {
        this.selectedFO = selectedFile;
    }

    public RunConfig getPreExecution() {
        if (parent != null && preexecution == null) {
            return parent.getPreExecution();
        }
        return preexecution;
    }

    public void setPreExecution(RunConfig config) {
        preexecution = config;
    }

    public void setPreExecutionActionName(String preactionname) {
        this.preactionname = preactionname;
    }

    public String getPreExecutionActionName() {
        //not worth inheriting I guess.
        return preactionname;
    }

    public ReactorStyle getReactorStyle() {
        return reactor;
    }

    public void setReactorStyle(ReactorStyle style) {
        reactor = style;
    }
}

