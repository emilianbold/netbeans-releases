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

package org.netbeans.modules.j2ee.earproject;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.modules.j2ee.common.project.classpath.ClassPathSupport;
import org.netbeans.modules.j2ee.earproject.ui.customizer.EarProjectProperties;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.util.ChangeSupport;

/**
 * TODO comments the whole class.
 *
 * @see org.netbeans.api.project.Project#getLookup
 * @author Martin Krauskopf
 */
public final class BrokenProjectSupport {
    
    private final EarProject project;
    private final ChangeSupport changeSupport = new ChangeSupport(this);
    private final FileChangeListener artifactListener;
    private final Collection<FileObject> watchedArtifacts = new HashSet<FileObject>();
    
    BrokenProjectSupport(final EarProject project) {
        this.project = project;
        this.artifactListener = new ArtifactListener();
    }
    
    public boolean hasBrokenArtifacts() {
        boolean brokenArtifacts = false;
        List<ClassPathSupport.Item> vcpis = EarProjectProperties.getJarContentAdditional(project);
        for (ClassPathSupport.Item vcpi : vcpis) {
            if (vcpi.getType() != ClassPathSupport.Item.TYPE_ARTIFACT) {
                continue;
            }
            FileObject script = vcpi.getArtifact().getScriptFile();
            if (script == null || !script.isValid()) {
                brokenArtifacts = true;
                break;
            }
        }
        return brokenArtifacts;
    }
    
    public void watchAntArtifact(final AntArtifact artifact) {
        FileObject artFile = artifact.getScriptFile();
        watchedArtifacts.add(artFile);
        artFile.addFileChangeListener(artifactListener);
    }
    
    public void cleanUp() {
        for (FileObject artFile : watchedArtifacts) {
            artFile.removeFileChangeListener(artifactListener);
        }
        watchedArtifacts.clear();
    }
    
    public void addChangeListener(ChangeListener l) {
        changeSupport.addChangeListener(l);
    }
    
    public void removeChangeListener(ChangeListener l) {
        changeSupport.removeChangeListener(l);
    }
    
    private void fireChangeEvent() {
        changeSupport.fireChange();
    }
    
    /**
     * Tries to fix/adjust broken artifacts' references.
     * <p>Acquires write access from {@link ProjectManager#mutex}.</p>
     */
    public void adjustReferences() {
        ProjectManager.mutex().writeAccess(new Runnable() {
            public void run() {
                for (ClassPathSupport.Item vcpi : EarProjectProperties.getJarContentAdditional(project)) {
                    if (vcpi.getType() == ClassPathSupport.Item.TYPE_ARTIFACT) {
                        String raw = vcpi.getReference();
                        if (raw.matches("^\\$\\{reference\\..*\\}")) { // NOI18N
                            String currEvaluated = project.evaluator().evaluate(raw);
                            FileObject currEvaluatedFO = project.getAntProjectHelper().resolveFileObject(currEvaluated);
                            if (currEvaluatedFO == null) {
                                String referenceKey = raw.substring(2, raw.length() - 1); // without $ and curly brackets // NOI18N
                                adjustReference(vcpi, referenceKey);
                            }
                        }
                    }
                }
            }
        });
    }
    
    private void adjustReference(final ClassPathSupport.Item vcpi, final String referenceKey) {
        EditableProperties prjProps = project.getAntProjectHelper().getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        String referenceValue = prjProps.getProperty(referenceKey);
        if (referenceValue.matches("^\\$\\{project\\..*\\}.+")) { // NOI18N
            String prjReference = referenceValue.substring(0, referenceValue.indexOf('}', 2) + 1);
            String relPath = project.evaluator().evaluate(vcpi.getReference());
            prjProps.setProperty(referenceKey, prjReference + '/' + relPath);
        }
        project.getAntProjectHelper().putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, prjProps);
        try {
            ProjectManager.getDefault().saveProject(project);
        } catch (IOException ioe) {
            Logger.getLogger("global").log(Level.WARNING, null, ioe);
        }
    }
    
    private class ArtifactListener extends FileChangeAdapter {
        
        @Override
        public void fileDeleted(FileEvent fe) {
            fireChangeEvent();
        }
        
        @Override
        public void fileRenamed(FileRenameEvent fe) {
            fireChangeEvent();
        }
        
    }
    
}
