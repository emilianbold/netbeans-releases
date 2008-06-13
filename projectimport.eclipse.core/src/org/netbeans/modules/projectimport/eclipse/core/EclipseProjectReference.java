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
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.projectimport.eclipse.core.spi.ProjectImportModel;
import org.netbeans.modules.projectimport.eclipse.core.spi.ProjectTypeUpdater;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author david
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
        this.eclipseProjectLocation = new File(eclipseProjectLocation);
        this.eclipseWorkspaceLocation = new File(eclipseWorkspaceLocation);
        this.timestamp = Long.parseLong(timestamp);
        this.key = key;
        this.project = project;
    }

    public File getEclipseProjectLocation() {
        return eclipseProjectLocation;
    }
    
    public static EclipseProjectReference read(Project project) {
        AuxiliaryConfiguration aux = ProjectUtils.getAuxiliaryConfiguration(project);
        Element el = aux.getConfigurationFragment("eclipse", NS, true);
        if (el == null) {
            return null;
        }
        return new EclipseProjectReference(project,
                Util.findText(Util.findElement(el, "project", NS)),
                Util.findText(Util.findElement(el, "workspace", NS)),
                Util.findText(Util.findElement(el, "timestamp", NS)),
                Util.findText(Util.findElement(el, "key", NS))
                );
        
    }
    
    public static void write(Project project, EclipseProjectReference ref) throws IOException {
        AuxiliaryConfiguration aux = ProjectUtils.getAuxiliaryConfiguration(project);
        Document doc = XMLUtil.createDocument("ignore", null, null, null);
        Element reference = doc.createElementNS(NS, "eclipse"); // NOI18N

        Element el = doc.createElementNS(NS, "project"); // NOI18N
        el.appendChild(doc.createTextNode(ref.eclipseProjectLocation.getAbsolutePath()));
        reference.appendChild(el);
        
        el = doc.createElementNS(NS, "workspace"); // NOI18N
        el.appendChild(doc.createTextNode(ref.eclipseWorkspaceLocation.getAbsolutePath()));
        reference.appendChild(el);
        
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

    public boolean isUpToDate() {
        if (getCurrentTimestamp() <= timestamp) {
            return true;
        }
        if (!(getEclipseProject(true).getProjectTypeFactory() instanceof ProjectTypeUpdater)) {
            assert false : "project with <eclipse> data in project.xml is upgradable: "+
                    project.getProjectDirectory()+" " +getEclipseProject(false).getProjectTypeFactory().getClass().getName();
        }
        ProjectTypeUpdater updater = (ProjectTypeUpdater)getEclipseProject(false).getProjectTypeFactory();
        return key.equals(updater.calculateKey(importModel));
    }

    void update() throws IOException {
        if (!(getEclipseProject(false).getProjectTypeFactory() instanceof ProjectTypeUpdater)) {
            assert false : "project with <eclipse> data in project.xml is upgradable";
        }
        ProjectTypeUpdater updater = (ProjectTypeUpdater)getEclipseProject(false).getProjectTypeFactory();
        updater.update(project, importModel, key);
        key = updater.calculateKey(importModel);
        write(project, this);
    }

    private long getCurrentTimestamp() {
        // use directly Files:
        File dotClasspath = new File(eclipseProjectLocation, ".classpath");
        File dotProject = new File(eclipseProjectLocation, ".project");
        return Math.max(dotClasspath.lastModified(), dotProject.lastModified());
    }
    
    boolean isEclipseProjectReachable() {
        return new File(eclipseProjectLocation, ".classpath").exists() &&
            new File(eclipseProjectLocation, ".project").exists();
    }

    private EclipseProject getEclipseProject(boolean forceReload) {
        if (forceReload || !initialized) {
            try {
                eclipseProject = ProjectFactory.getInstance().load(eclipseProjectLocation, eclipseWorkspaceLocation);
            } catch (ProjectImporterException ex) {
                Exceptions.printStackTrace(ex);
            }
            File f = FileUtil.toFile(project.getProjectDirectory());
            importModel = new ProjectImportModel(eclipseProject, f.getAbsolutePath(), /*TODO*/ JavaPlatform.getDefault());
            initialized = true;
        }
        return eclipseProject;
    }
}
