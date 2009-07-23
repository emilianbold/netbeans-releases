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

package org.netbeans.modules.maven.j2ee;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.apache.maven.project.MavenProject;
import org.netbeans.modules.maven.api.FileUtilities;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.spi.project.support.GenericSources;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

import org.openide.util.RequestProcessor;

/**
 * Implementation of Sources interface for maven projects in the j2ee area
 * @author  Milos Kleint
 */
public class J2eeMavenSourcesImpl implements Sources {
    
    public static final String TYPE_DOC_ROOT="doc_root"; //NOI18N
    public static final String TYPE_WEB_INF="web_inf"; //NOI18N
    
    private final Project project;
    private final List<ChangeListener> listeners;
    
    private SourceGroup webDocSrcGroup;
    
    private final Object lock = new Object();
    private NbMavenProject mavenproject;
    
    
    /** Creates a new instance of MavenSourcesImpl */
    public J2eeMavenSourcesImpl(Project proj) {
        project = proj;
        mavenproject = project.getLookup().lookup(NbMavenProject.class);
        listeners = new ArrayList<ChangeListener>();
        NbMavenProject.addPropertyChangeListener(project, new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent event) {
                if (NbMavenProject.PROP_PROJECT.equals(event.getPropertyName())) {
                    checkChanges(true);
                }
            }
        });
    }
    
    private void checkChanges(boolean synchronous) {
        boolean changed = false;
        synchronized (lock) {
            MavenProject mp = mavenproject.getMavenProject();
            FileObject fo = null;
            if (mp != null) {
                 fo = FileUtilities.convertURItoFileObject(mavenproject.getWebAppDirectory());
            }
            changed = checkWebDocGroupCache(fo);
        }
        if (changed) {
            if (synchronous) {
                fireChange();
            } else {
                RequestProcessor.getDefault().post(new Runnable() {
                    public void run() {
                        fireChange();
                    }
                });
            }
        }
    }
    
    private void fireChange() {
        List<ChangeListener> currList;
        synchronized (listeners) {
            currList = new ArrayList<ChangeListener>(listeners);
        }
        ChangeEvent event = new ChangeEvent(this);
        for (ChangeListener list : currList) {
            list.stateChanged(event);
        }
    }
    
    public void addChangeListener(ChangeListener changeListener) {
        synchronized (listeners) {
            listeners.add(changeListener);
        }
    }
    
    public void removeChangeListener(ChangeListener changeListener) {
        synchronized (listeners) {
            listeners.remove(changeListener);
        }
    }
    
    public SourceGroup[] getSourceGroups(String str) {
        if (TYPE_DOC_ROOT.equals(str)) {
            return createWebDocRoot();
        }
        return new SourceGroup[0];
    }
    
    
    private SourceGroup[] createWebDocRoot() {
        FileObject folder = FileUtilities.convertURItoFileObject(mavenproject.getWebAppDirectory());
        SourceGroup grp = null;
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
