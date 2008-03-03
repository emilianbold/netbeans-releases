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
import java.util.List;
import javax.swing.Icon;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.queries.SharabilityQuery;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;


/**
 *
 * @author Martin Adamek
 */
public class GrailsSources implements Sources {
    
    private final FileObject projectDir;
    private final ChangeSupport changeSupport = new ChangeSupport(this);
    
    GrailsSources(FileObject projectDir) {
        this.projectDir = projectDir;
    }
    
    public SourceGroup[] getSourceGroups(String type) {
        List<SourceGroup> result = new ArrayList<SourceGroup>();
        
        if (Sources.TYPE_GENERIC.equals(type)) {
            result.add(new Group(projectDir, projectDir.getName(), null, null, SourceCategory.NONE));
        } else if ("grails-app/conf".equals(type)) {
            result.add(new Group(projectDir.getFileObject("grails-app/conf"), "Configuration", null, null, SourceCategory.CONFIGURATION));
        } else if ("grails-app/controllers".equals(type)) {
            result.add(new Group(projectDir.getFileObject("grails-app/controllers"), "Controllers", null, null, SourceCategory.CONTROLLERS));
        } else if ("grails-app/domain".equals(type)) {
            result.add(new Group(projectDir.getFileObject("grails-app/domain"), "Domain classes", null, null, SourceCategory.DOMAIN));
        } else if ("grails-app/i18n".equals(type)) {
            result.add(new Group(projectDir.getFileObject("grails-app/i18n"), "Message bundles", null, null, SourceCategory.MESSAGES));
        } else if ("grails-app/services".equals(type)) {
            result.add(new Group(projectDir.getFileObject("grails-app/services"), "Services", null, null, SourceCategory.SERVICES));
        } else if ("grails-app/taglib".equals(type)) {
            result.add(new Group(projectDir.getFileObject("grails-app/taglib"), "Tag libraries", null, null, SourceCategory.TAGLIB));
        } else if ("grails-app/utils".equals(type)) {
            result.add(new Group(projectDir.getFileObject("grails-app/utils"), "Utility classes", null, null, SourceCategory.UTIL));
        } else if ("grails-app/views".equals(type)) {
            result.add(new Group(projectDir.getFileObject("grails-app/views"), "Views and layouts", null, null, SourceCategory.VIEWS));
        } else if ("web-app".equals(type)) {
            result.add(new Group(projectDir.getFileObject("web-app"), "Web Application", null, null, SourceCategory.VIEWS));
        } else if ("lib".equals(type)) {
            result.add(new Group(projectDir.getFileObject("lib"), "Libraries", null, null, SourceCategory.LIB));
        } else if ("test".equals(type)) {
            result.add(new Group(projectDir.getFileObject("test"), "Tests", null, null, SourceCategory.VIEWS));
        } else if ("src".equals(type)) {
            result.add(new Group(projectDir.getFileObject("src"), "Source Packages", null, null, SourceCategory.VIEWS));
        } else if ("scripts".equals(type)) {
            result.add(new Group(projectDir.getFileObject("scripts"), "Scripts", null, null, SourceCategory.VIEWS));
        } else {
            if(!type.startsWith(".") && !type.startsWith("grails-app/.")) { // we have to filter-out hidden directories like .settings etc.
                FileObject fileObject = projectDir.getFileObject(type);
                if (fileObject != null) {
                    result.add(new Group(fileObject, fileObject.getName(), null, null, SourceCategory.NONE));
                }
            }
        }
        
        return result.toArray(new SourceGroup[result.size()]);
    }
    
    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }
    
    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }
    
    
    private final class Group implements SourceGroup {
        
        private final FileObject loc;
        private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
 
        private final String displayName;
        private final Icon icon;
        private final Icon openedIcon;
        private final SourceCategory category;
        
        public Group(FileObject loc, String displayName, Icon icon, Icon openedIcon, SourceCategory category) {
            this.loc = loc;
            this.displayName = displayName;
            this.icon = icon;
            this.openedIcon = openedIcon;
            this.category = category;
        }
        
        public SourceCategory getSourceCategory(){
            return category;
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
            return opened ? icon : openedIcon;
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
