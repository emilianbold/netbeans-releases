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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.j2ee;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.maven.api.FileUtilities;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.project.support.GenericSources;
import org.openide.filesystems.FileObject;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;

/**
 * Implementation of Sources interface for Java EE Maven projects
 * @author  Milos Kleint
 */
@ProjectServiceProvider(
    service = {
        Sources.class
    },
    projectType = {
        "org-netbeans-modules-maven/" + NbMavenProject.TYPE_WAR,
        "org-netbeans-modules-maven/" + NbMavenProject.TYPE_EJB,
        "org-netbeans-modules-maven/" + NbMavenProject.TYPE_EAR,
        "org-netbeans-modules-maven/" + NbMavenProject.TYPE_APPCLIENT,
        "org-netbeans-modules-maven/" + NbMavenProject.TYPE_OSGI,
        "org-netbeans-modules-maven/" + NbMavenProject.TYPE_JAR // #233476
    }
)
public class J2eeMavenSourcesImpl implements Sources {
    
    public static final String TYPE_DOC_ROOT="doc_root"; //NOI18N
    public static final String TYPE_WEB_INF="web_inf"; //NOI18N
    
    private final Object lock = new Object();
    private final Project project;
    private final ChangeSupport cs = new ChangeSupport(this);
    private final PropertyChangeListener pcl;
    
    private SourceGroup webDocSrcGroup;

    
    public J2eeMavenSourcesImpl(Project project) {
        this.project = project;
        this.pcl = new PropertyChangeListener() {
            
            @Override
            public void propertyChange(PropertyChangeEvent event) {
                if (NbMavenProject.PROP_PROJECT.equals(event.getPropertyName())) {
                    checkChanges();
                }
            }
        };
    }
    
    private void checkChanges() {
        boolean changed;
        synchronized (lock) {
            changed = checkWebDocGroupCache(getWebAppDir());
        }
        if (changed) {
            cs.fireChange();
        }
    }
    
    @Override
    public void addChangeListener(ChangeListener changeListener) {
        // If no listener were registered until now, start listening at project changes
        if (!cs.hasListeners()) {
            NbMavenProject.addPropertyChangeListener(project, pcl);
        }
        cs.addChangeListener(changeListener);
    }
    
    @Override
    public void removeChangeListener(ChangeListener changeListener) {
        cs.removeChangeListener(changeListener);
        
        // If this was the last registered listener, stop listening at project changes
        if (!cs.hasListeners()) {
            NbMavenProject.removePropertyChangeListener(project, pcl);
        }
    }
    
    @Override
    public SourceGroup[] getSourceGroups(String str) {
        if (TYPE_DOC_ROOT.equals(str)) {
            return createWebDocRoot();
        }
        return new SourceGroup[0];
    }
    
    private SourceGroup[] createWebDocRoot() {
        FileObject folder = getWebAppDir();
        SourceGroup grp;
        synchronized (lock) {
            checkWebDocGroupCache(folder);
            grp = webDocSrcGroup;
        }
        if (grp != null) {
            return new SourceGroup[] {grp};
        } else {
            return new SourceGroup[0];
        }
    }
    
    private FileObject getWebAppDir() {
        NbMavenProject mavenproject = project.getLookup().lookup(NbMavenProject.class);
        return FileUtilities.convertURItoFileObject(mavenproject.getWebAppDirectory());
    }
    
    /**
     * consult the SourceGroup cache, return true if anything changed..
     */
    private boolean checkWebDocGroupCache(FileObject root) {
        if (root == null && webDocSrcGroup != null) {
            webDocSrcGroup = null;
            return true;
        }
        if (root == null) {
            return false;
        }
        boolean changed = false;
        if (webDocSrcGroup == null || !webDocSrcGroup.getRootFolder().equals(root)) {
            webDocSrcGroup = GenericSources.group(project, root, TYPE_DOC_ROOT, NbBundle.getMessage(J2eeMavenSourcesImpl.class, "LBL_WebPages"), null, null);
            changed = true;
        }
        return changed;
    }
    
}
