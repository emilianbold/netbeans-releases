/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.groovy.grailsproject;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.Icon;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.queries.SharabilityQuery;
import org.netbeans.api.queries.VisibilityQuery;
import org.netbeans.modules.groovy.support.api.GroovySources;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Martin Adamek
 */
public class GrailsSources extends FileChangeAdapter implements Sources {

    //  those are dirs in project root we already know and create specific source groups
    public static final List KNOWN_FOLDERS = Arrays.asList(
            "grails-app", // NOI18N
            "lib", // NOI18N
            "scripts", // NOI18N
            "src", // NOI18N
            "test", // NOI18N
            "web-app" // NOI18N
            );

    //  those are dirs in grails-app root we already know and create specific source groups
    public static final List KNOWN_FOLDERS_IN_GRAILS_APP = Arrays.asList(
            "conf", // NOI18N
            "controllers", // NOI18N
            "domain", // NOI18N
            "i18n", // NOI18N
            "services", // NOI18N
            "taglib", // NOI18N
            "utils", // NOI18N
            "views" // NOI18N
            );

    private final FileObject projectDir;
    private final ChangeSupport changeSupport = new ChangeSupport(this);

    private GrailsSources(FileObject projectDir) {
        this.projectDir = projectDir;
    }

    static GrailsSources create(FileObject projectDir) {
        GrailsSources sources = new GrailsSources(projectDir);
        sources.startFSListener();
        return sources;
    }

    private void startFSListener () {
        try {
            FileSystem fs = projectDir.getFileSystem();
            fs.addFileChangeListener(FileUtil.weakFileChangeListener(this, fs));
        } catch (FileStateInvalidException x) {
            Exceptions.printStackTrace(x);
        }
    }

    public SourceGroup[] getSourceGroups(String type) {
        if (Sources.TYPE_GENERIC.equals(type)) {
            return new SourceGroup[] {
                new Group(projectDir, projectDir.getName())
            };
        } else if (JavaProjectConstants.SOURCES_TYPE_JAVA.equals(type)) {
            return new SourceGroup[] {
                new Group(projectDir.getFileObject("src/java"), NbBundle.getMessage(GrailsSources.class, "LBL_SrcJava"))
            };
        } else if (GroovySources.SOURCES_TYPE_GROOVY.equals(type)) {
            return new SourceGroup[] {
                createGroup(SourceCategory.GRAILSAPP_CONF, "LBL_grails-app_conf"),
                createGroup(SourceCategory.GRAILSAPP_CONTROLLERS, "LBL_grails-app_controllers"),
                createGroup(SourceCategory.GRAILSAPP_DOMAIN, "LBL_grails-app_domain"),
                createGroup(SourceCategory.GRAILSAPP_SERVICES, "LBL_grails-app_services"),
                createGroup(SourceCategory.GRAILSAPP_TAGLIB, "LBL_grails-app_taglib"),
                createGroup(SourceCategory.GRAILSAPP_UTILS, "LBL_grails-app_utils"),
                createGroup(SourceCategory.SCRIPTS, "LBL_scripts"),
                createGroup(SourceCategory.SRC_GROOVY, "LBL_SrcGroovy"),
                createGroup(SourceCategory.TEST_INTEGRATION, "LBL_IntegrationTests"),
                createGroup(SourceCategory.TEST_UNIT, "LBL_UnitTests")
            };
        } else if (GroovySources.SOURCES_TYPE_GRAILS.equals(type)) {
            return new SourceGroup[] {
                createGroup(SourceCategory.LIB, "LBL_lib"),
                createGroup(SourceCategory.GRAILSAPP_I18N, "LBL_grails-app_i18n"),
                createGroup(SourceCategory.WEBAPP, "LBL_web-app"),
                createGroup(SourceCategory.GRAILSAPP_VIEWS, "LBL_grails-app_views")
            };
        } else if (GroovySources.SOURCES_TYPE_GRAILS_UNKNOWN.equals(type)) {
            List<SourceGroup> result = new ArrayList<SourceGroup>();
            for (FileObject child : projectDir.getChildren()) {
                if (child.isFolder() && VisibilityQuery.getDefault().isVisible(child) && !KNOWN_FOLDERS.contains(child.getName())) {
                    String name = child.getName();
                    result.add(new Group(child, Character.toUpperCase(name.charAt(0)) + name.substring(1)));
                }
            }
            FileObject grailsAppFo = projectDir.getFileObject("grails-app");
            if (grailsAppFo != null) {
                for (FileObject child : grailsAppFo.getChildren()) {
                    if (child.isFolder() && VisibilityQuery.getDefault().isVisible(child) && !KNOWN_FOLDERS_IN_GRAILS_APP.contains(child.getName())) {
                        String name = child.getName();
                        result.add(new Group(child, Character.toUpperCase(name.charAt(0)) + name.substring(1)));
                    }
                }
            }
            return result.toArray(new SourceGroup[result.size()]);
        }
        return new SourceGroup[] {};

    }

    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    @Override
    public void fileDeleted(FileEvent fe) {
        changeSupport.fireChange();
    }

    @Override
    public void fileFolderCreated(FileEvent fe) {
        changeSupport.fireChange();
    }

    @Override
    public void fileRenamed(FileRenameEvent fe) {
        changeSupport.fireChange();
    }

    private Group createGroup(SourceCategory SourceCategory, String label) {
        return new Group(projectDir.getFileObject(SourceCategory.getRelativePath()), NbBundle.getMessage(GrailsSources.class, label));
    }

    private final class Group implements SourceGroup {

        private final FileObject loc;
        private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
        private final String displayName;

        public Group(FileObject loc, String displayName) {
            this.loc = loc;
            this.displayName = displayName;
        }

        public FileObject getRootFolder() {
            return loc;
        }

        public String getName() {
            String location = loc.getPath();
            return location.length() > 0 ? location : "generic"; // NOI18N
        }

        public String getDisplayName() {
            return displayName;
        }

        public Icon getIcon(boolean opened) {
            return null;
        }

        public boolean contains(FileObject file) throws IllegalArgumentException {
            if (file == loc) {
                return true;
            }
            String path = FileUtil.getRelativePath(loc, file);
            if (path == null) {
                throw new IllegalArgumentException();
            }
            if (file.isFolder()) {
                path += File.separator; // NOI18N
            }
            if (file.isFolder() && file != projectDir && ProjectManager.getDefault().isProject(file)) {
                // #67450: avoid actually loading the nested project.
                return false;
            }
            File f = FileUtil.toFile(file);
            if (f != null && SharabilityQuery.getSharability(f) == SharabilityQuery.NOT_SHARABLE) {
                return false;
            } // else MIXED, UNKNOWN, or SHARABLE; or not a disk file
            return true;
        }

        public void addPropertyChangeListener(PropertyChangeListener l) {
            pcs.addPropertyChangeListener(l);
        }

        public void removePropertyChangeListener(PropertyChangeListener l) {
            pcs.removePropertyChangeListener(l);
        }

        @Override
        public String toString() {
            return "GrailsSources.Group[name=" + getName() + ",rootFolder=" + getRootFolder() + "]"; // NOI18N
        }

    }

}
