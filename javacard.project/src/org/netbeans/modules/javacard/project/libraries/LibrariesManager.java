/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
package org.netbeans.modules.javacard.project.libraries;

import org.netbeans.modules.javacard.project.*;
import java.awt.Toolkit;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.modules.javacard.constants.ProjectPropertyNames;
import org.netbeans.modules.javacard.project.JCProjectSourceNodeFactory.AddProjectAction;
import org.netbeans.spi.project.SubprojectProvider;
import org.netbeans.spi.project.ant.AntArtifactProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.WeakListeners;

/**
 * Centralizes all code that relates to fetching JAR files, class paths,
 * etc., so it can be changed later to use ReferenceHelper w/o requiring
 * code changes elsewhere
 *
 * @deprecated - remove after conversion to use new dependencies
 * @author Tim Boudreau
 */
@Deprecated
public final class LibrariesManager {

    private final JCProject project;
    private final PCL pcl = new PCL();
    private final ChangeSupport supp = new ChangeSupport(this);

    public void removeChangeListener(ChangeListener listener) {
        supp.removeChangeListener(listener);
    }

    public void addChangeListener(ChangeListener listener) {
        supp.addChangeListener(listener);
    }

    public LibrariesManager(JCProject project) {
        this.project = project;
        PropertyEvaluator e = project.evaluator();
        assert e != null;
        e.addPropertyChangeListener(WeakListeners.propertyChange(pcl, e));
    }

    private final class PCL implements PropertyChangeListener {

        public void propertyChange(PropertyChangeEvent evt) {
            if (ProjectPropertyNames.PROJECT_PROP_CLASS_PATH.equals(evt.getPropertyName())) {
                supp.fireChange();
            }
        }
    }

    public void addToProjectClasspath(final File[] files) {
        String cp = project.evaluator().getProperty(ProjectPropertyNames.PROJECT_PROP_CLASS_PATH);
        cp = cp == null ? "" : cp;
        Set<File> present = new HashSet<File>();
        for (String jar : cp.split(File.pathSeparator)) {
            present.add(new File(jar));
        }
        final StringBuilder sb = new StringBuilder(cp);
        for (File f : files) {
            if (!present.contains(f)) {
                if (sb.length() > 0) {
                    sb.append(File.pathSeparator);
                }
                sb.append(f.getAbsolutePath());
            }
        }
        saveProjectClasspath(sb);
    }

    private void saveProjectClasspath(final CharSequence sb) {
        ProjectManager.mutex().postWriteRequest(new Runnable() {

            public void run() {
                EditableProperties props = project.getAntProjectHelper().getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                props.setProperty(ProjectPropertyNames.PROJECT_PROP_CLASS_PATH, sb.toString());
                project.getAntProjectHelper().putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
                try {
                    ProjectManager.getDefault().saveProject(project);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (IllegalArgumentException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        });
    }

    public void addProjectsToClasspath(File[] files) {
        Set<File> artifacts = new HashSet<File>();
        for (File file : files) {
            Project p = FileOwnerQuery.getOwner(file.toURI());
            if (p != null) {
                AntArtifactProvider prov = p.getLookup().lookup(AntArtifactProvider.class);
                if (prov != null) {
                    for (AntArtifact a : prov.getBuildArtifacts()) {
                        if (!JavaProjectConstants.ARTIFACT_TYPE_JAR.equals(a.getType())) {
                            continue;
                        }
                        for (URI uri : a.getArtifactLocations()) {
                            try {
                                String path = URLDecoder.decode(uri.toString(), "UTF-8"); //NOI18N
                                if (path.startsWith("dist")) { //XXX hardcoding
                                    FileObject fo = p.getProjectDirectory();
                                    File pFile = FileUtil.toFile(fo);
                                    String pth = pFile.getPath();
                                    if (!pth.endsWith(File.separator)) {
                                        pth += File.separator;
                                    }
                                    pth += path;
                                    path = pth;
                                }
                                if (!path.startsWith("file:///")) {
                                    //NOI18N
                                    if (path.startsWith("file:/")) {
                                        path = path.replace("file:/", "file:///"); //NOI18N
                                    } else {
                                        path = "file:///" + path;
                                    }
                                }
                                URL url = new URL(path);
                                File f = FileUtil.normalizeFile(new File(url.getFile()));
                                artifacts.add(f);
                            } catch (MalformedURLException ex) {
                                Logger.getLogger(AddProjectAction.class.getName()).log(Level.INFO,
                                        "Bad URI in classpath for " + project.getProjectDirectory().getPath() +
                                        ": " + uri);
                            } catch (UnsupportedEncodingException ex) {
                                Exceptions.printStackTrace(ex);
                            }
                        }
                    }
                }
            }
        }
        if (artifacts.isEmpty()) {
            Toolkit.getDefaultToolkit().beep();
        } else {
            File[] f = artifacts.toArray(new File[artifacts.size()]);
            project.getLookup().lookup(LibrariesManager.class).addToProjectClasspath(f);
        }
    }

    public void removeFileFromClasspath(String toRemove) {
        List<File> l = new ArrayList<File>();
        String cp = project.evaluator().getProperty(ProjectPropertyNames.PROJECT_PROP_CLASS_PATH);
        for (String jar : cp.split(File.pathSeparator)) {
            l.add(new File(jar));
        }
        l.remove(new File(toRemove));
        StringBuilder sb = new StringBuilder();
        for (File f : l) {
            if (sb.length() > 0) {
                sb.append(File.pathSeparator);
            }
            sb.append(f.getAbsolutePath());
        }
        saveProjectClasspath(sb);
    }

    public static final class ErrFile extends File {

        public final String val;

        ErrFile(String val) {
            super(val);
            this.val = val;
        }
    }

    public List<File> getSubprojectArtifacts(boolean includeNonExistent,boolean includeLooseJars) {
        List<File> result = new ArrayList<File>(20);
        SubprojectProvider prov = project.getLookup().lookup(SubprojectProvider.class);
        if (prov != null) {
            for (Project p : prov.getSubprojects()) {
                AntArtifactProvider aprov = p.getLookup().lookup(AntArtifactProvider.class);
                if (aprov != null) {
                    for (AntArtifact a : aprov.getBuildArtifacts()) {
                        if (JavaProjectConstants.ARTIFACT_TYPE_JAR.equals(a.getType())) {
                            for (URI uri : a.getArtifactLocations()) {
                                try {
                                    String path = URLDecoder.decode(uri.toString(), "UTF-8"); //NOI18N
                                    if (!path.startsWith("file:///")) { //NOI18N
                                        path = path.replace("file:/", "file:///"); //NOI18N
                                        }
                                    URL url = new URL(path);
                                    File f = FileUtil.normalizeFile(new File(url.getFile()));
                                    result.add(f);
                                } catch (MalformedURLException ex) {
                                    Logger.getLogger(JCProjectSourceNodeFactory.class.getName()).log(Level.INFO,
                                            "Bad library reference in " //NOI18N
                                            + project.getProjectDirectory().getPath() + ":" + uri, //NOI18N
                                            ex);
                                    result.add(new ErrFile(uri.toString()));
                                } catch (UnsupportedEncodingException ex) {
                                    Logger.getLogger(JCProjectSourceNodeFactory.class.getName()).log(Level.INFO,
                                            "Bad library reference in " //NOI18N
                                            + project.getProjectDirectory().getPath() + ":" + uri, //NOI18N
                                            ex);
                                    result.add(new ErrFile(uri.toString()));
                                } catch (IllegalArgumentException ex) {
                                    Logger.getLogger(JCProjectSourceNodeFactory.class.getName()).log(Level.INFO,
                                            "Bad library reference in " //NOI18N
                                            + project.getProjectDirectory().getPath() + ":" + uri, //NOI18N
                                            ex);
                                    result.add(new ErrFile(uri.toString()));
                                }
                            }
                        }
                    }
                }
            }
        }
        if (includeLooseJars) {
            //Also iterate anything that belongs to no project
            String cp = project.evaluator().getProperty(ProjectPropertyNames.PROJECT_PROP_CLASS_PATH);
            if (cp != null && !"".equals(cp)) { //NOI18N
                //XXX DONT DEPEND ON File.separator - will cause problems if we ever support unix
                for (String jar : cp.split(File.pathSeparator)) {
                    if ("".equals(jar)) { //NOI18N
                        continue;
                    }
                    File f = new File(jar);
                    boolean known = result.contains(f);
                    if (f.exists() && !known) {
                        result.add(f);
                    } else if (!known) {
                        result.add(new ErrFile(jar));
                    }
                }
            }
        }
        return result;
    }

    public List<Project> getSubprojects(Project p, boolean transitiveClosure) {
        List<Project> projects = new ArrayList<Project>();
        getSubprojects(p, projects, transitiveClosure);
        return projects;
    }

    private void getSubprojects(Project p, List<Project> projects, boolean transitiveClosure) {
        if (!projects.contains(p)) {
            projects.add(p);
            SubprojectProvider provider = p.getLookup().lookup(SubprojectProvider.class);
            if (provider != null) {
                if (transitiveClosure) {
                    for (Project sub : provider.getSubprojects()) {
                        getSubprojects(sub, projects, true);
                    }
                } else {
                    for (Project sub : provider.getSubprojects()) {
                        projects.add(sub);
                    }
                }
            }
        }
    }

    public String getProjectLibraryClasspath(JCProject p) {
        List<File> allJars = getSubprojectArtifacts(false, true);
        StringBuilder sb = new StringBuilder();
        for (File f : allJars) {
            if (sb.length() > 0) {
                sb.append (File.pathSeparator);
            }
            sb.append (f.getAbsolutePath());
        }
        return sb.toString();
    }
}
