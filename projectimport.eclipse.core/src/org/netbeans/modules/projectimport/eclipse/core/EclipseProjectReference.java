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

package org.netbeans.modules.projectimport.eclipse.core;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.prefs.Preferences;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.queries.CollocationQuery;
import org.netbeans.modules.projectimport.eclipse.core.spi.ProjectImportModel;
import org.netbeans.modules.projectimport.eclipse.core.spi.ProjectTypeUpdater;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbPreferences;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Represents reference to Eclipse project which consist of eclipse project location,
 * eclipse workspace location, eclipse files timestamp and key identifying relevant 
 * import data. File references are stored relative if collocated. If differnt user
 * opens NetBeans project and Eclipse reference cannot be resolved then UI asking 
 * for eclipse project location and eclipse workspace location is shown. These
 * are stored in NbPreferences in userdir for now.
 */
class EclipseProjectReference {

    private Project project;
    private File eclipseProjectLocation;
    private File eclipseWorkspaceLocation;
    private long timestamp;
    private String key;
    
    private boolean initialized;
    private EclipseProject eclipseProject;
    private ProjectImportModel importModel;
    
    private static final String NS = "http://www.netbeans.org/ns/eclipse-reference/1";
    
    public EclipseProjectReference(Project project, String eclipseProjectLocation, String eclipseWorkspaceLocation, String timestamp, String key) {
        this.eclipseProjectLocation = PropertyUtils.resolveFile(FileUtil.toFile(project.getProjectDirectory()), eclipseProjectLocation);
        if (eclipseWorkspaceLocation != null) {
            this.eclipseWorkspaceLocation = PropertyUtils.resolveFile(FileUtil.toFile(project.getProjectDirectory()), eclipseWorkspaceLocation);
        } else {
            this.eclipseWorkspaceLocation = null;
        }
        this.timestamp = Long.parseLong(timestamp);
        this.key = key;
        this.project = project;
    }

    public File getEclipseProjectLocation() {
        return eclipseProjectLocation;
    }

    public File getEclipseWorkspaceLocation() {
        return eclipseWorkspaceLocation;
    }
    
    File getFallbackEclipseProjectLocation() {
        String path = getPreferences().get(getEclipseProjectLocation().getPath(), null);
        if (path != null) {
            return new File(path);
        }
        return getEclipseProjectLocation();
    }

    File getFallbackWorkspaceProjectLocation() {
        if (eclipseWorkspaceLocation == null) {
            return null;
        }
        String path = getPreferences().get(getEclipseWorkspaceLocation().getPath(), null);
        if (path != null) {
            return new File(path);
        }
        return getEclipseWorkspaceLocation();
    }

    void updateReference(String eclipseLocation, String eclipseWorkspace) {
        if (eclipseLocation != null) {
            getPreferences().put(getEclipseProjectLocation().getPath(), eclipseLocation);
        }
        if (eclipseWorkspace != null) {
            getPreferences().put(getEclipseWorkspaceLocation().getPath(), eclipseWorkspace);
        }
    }
    
    private static Preferences getPreferences() {
        return NbPreferences.forModule(EclipseProjectReference.class);
    }

    public static EclipseProjectReference read(Project project) {
        AuxiliaryConfiguration aux = ProjectUtils.getAuxiliaryConfiguration(project);
        Element el = aux.getConfigurationFragment("eclipse", NS, true);
        if (el == null) {
            return null;
        }
        String workspace = null;
        Element e = Util.findElement(el, "workspace", NS);
        if (e != null) {
            workspace = Util.findText(e);
        }
        return new EclipseProjectReference(project,
                Util.findText(Util.findElement(el, "project", NS)),
                workspace,
                Util.findText(Util.findElement(el, "timestamp", NS)),
                Util.findText(Util.findElement(el, "key", NS))
                );
        
    }
    
    public static void write(Project project, EclipseProjectReference ref) throws IOException {
        AuxiliaryConfiguration aux = ProjectUtils.getAuxiliaryConfiguration(project);
        Document doc = XMLUtil.createDocument("ignore", null, null, null);
        Element reference = doc.createElementNS(NS, "eclipse"); // NOI18N

        Element el = doc.createElementNS(NS, "project"); // NOI18N
        File baseDir = FileUtil.toFile(project.getProjectDirectory());
        if (CollocationQuery.areCollocated(baseDir, ref.eclipseProjectLocation)) {
            el.appendChild(doc.createTextNode(PropertyUtils.relativizeFile(baseDir, ref.eclipseProjectLocation)));
        } else {
            el.appendChild(doc.createTextNode(ref.eclipseProjectLocation.getPath()));
        }
        reference.appendChild(el);
        
        if (ref.eclipseWorkspaceLocation != null) {
            el = doc.createElementNS(NS, "workspace"); // NOI18N
            if (CollocationQuery.areCollocated(baseDir, ref.eclipseWorkspaceLocation)) {
                el.appendChild(doc.createTextNode(PropertyUtils.relativizeFile(baseDir, ref.eclipseWorkspaceLocation)));
            } else {
                el.appendChild(doc.createTextNode(ref.eclipseWorkspaceLocation.getPath()));
            }
            reference.appendChild(el);
        }
        
        el = doc.createElementNS(NS, "timestamp"); // NOI18N
        el.appendChild(doc.createTextNode(""+ref.getCurrentTimestamp()));
        reference.appendChild(el);
        
        // TODO: key need to be normalzied or CDATA-ed
        el = doc.createElementNS(NS, "key"); // NOI18N
        el.appendChild(doc.createTextNode(ref.key));
        reference.appendChild(el);
        
        aux.putConfigurationFragment(reference, true);
        
        ProjectManager.getDefault().saveProject(project);
    }

    public boolean isUpToDate(boolean deepTest) {
        if (getCurrentTimestamp() <= timestamp && !deepTest) {
            return true;
        }
        EclipseProject ep = getEclipseProject(true);
        if (ep == null) {
            // an exception was thrown; pretend proj is uptodate
            return true;
        }
        if (!(ep.getProjectTypeFactory() instanceof ProjectTypeUpdater)) {
            assert false : "project with <eclipse> data in project.xml is upgradable: "+
                    project.getProjectDirectory()+" " +ep.getProjectTypeFactory().getClass().getName();
        }
        ProjectTypeUpdater updater = (ProjectTypeUpdater)ep.getProjectTypeFactory();
        return key.equals(updater.calculateKey(importModel));
    }

    void update(List<String> importProblems) throws IOException {
        EclipseProject ep = getEclipseProject(false);
        if (ep == null) {
            // an exception was thrown; pretend proj is uptodate
            return;
        }
        if (!(ep.getProjectTypeFactory() instanceof ProjectTypeUpdater)) {
            assert false : "project with <eclipse> data in project.xml is upgradable";
        }
        ProjectTypeUpdater updater = (ProjectTypeUpdater)getEclipseProject(false).getProjectTypeFactory();
        key = updater.update(project, importModel, key, importProblems);
        write(project, this);
    }

    private long getCurrentTimestamp() {
        // use directly Files:
        File dotClasspath = new File(getFallbackEclipseProjectLocation(), ".classpath");
        File dotProject = new File(getFallbackEclipseProjectLocation(), ".project");
        return Math.max(dotClasspath.lastModified(), dotProject.lastModified());
    }
    
    boolean isEclipseProjectReachable() {
        boolean b = EclipseUtils.isRegularProject(eclipseProjectLocation) &&
                (eclipseWorkspaceLocation == null || 
                 (eclipseWorkspaceLocation != null && EclipseUtils.isRegularWorkSpace(eclipseWorkspaceLocation)));
        if (b) {
            // if project/workspace are reachable remove fallback properties
            getPreferences().remove(eclipseProjectLocation.getPath());
            if (eclipseWorkspaceLocation != null) {
                getPreferences().remove(eclipseWorkspaceLocation.getPath());
            }
            return true;
        }
        return EclipseUtils.isRegularProject(getFallbackEclipseProjectLocation()) &&
                (eclipseWorkspaceLocation == null ||
                 (eclipseWorkspaceLocation != null && EclipseUtils.isRegularWorkSpace(getFallbackWorkspaceProjectLocation())));
    }

    private EclipseProject getEclipseProject(boolean forceReload) {
        if (forceReload || !initialized) {
            try {
                eclipseProject = ProjectFactory.getInstance().load(getFallbackEclipseProjectLocation(), getFallbackWorkspaceProjectLocation());
            } catch (ProjectImporterException ex) {
                Exceptions.printStackTrace(ex);
                eclipseProject = null;
                initialized = true;
                return null;
            }
            File f = FileUtil.toFile(project.getProjectDirectory());
            importModel = new ProjectImportModel(eclipseProject, f.getAbsolutePath(), 
                    JavaPlatformSupport.getJavaPlatformSupport().getJavaPlatform(eclipseProject, new ArrayList<String>()), Collections.<Project>emptyList());
            initialized = true;
        }
        return eclipseProject;
    }
}
