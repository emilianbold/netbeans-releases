/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.earproject;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.modules.j2ee.earproject.ui.customizer.VisualClassPathItem;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.ErrorManager;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;

/**
 * TODO comments the whole class.
 *
 * @see org.netbeans.api.project.Project#getLookup
 * @author Martin Krauskopf
 */
public final class BrokenProjectSupport {
    
    private final EarProject project;
    private final Set<ChangeListener> listeners;
    private final FileChangeListener artifactListener;
    private final Collection<FileObject> watchedArtifacts = new HashSet<FileObject>();
    
    BrokenProjectSupport(final EarProject project) {
        this.project = project;
        this.listeners = new HashSet<ChangeListener>(1);
        this.artifactListener = new ArtifactListener();
    }
    
    public boolean hasBrokenArtifacts() {
        boolean brokenArtifacts = false;
        List<VisualClassPathItem> vcpis = project.getProjectProperties().getJarContentAdditional();
        for (VisualClassPathItem vcpi : vcpis) {
            Object obj = vcpi.getObject();
            if (!(obj instanceof AntArtifact)) {
                continue;
            }
            AntArtifact aa = (AntArtifact) obj;
            FileObject script = aa.getScriptFile();
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
        synchronized (listeners) {
            listeners.add(l);
        }
    }
    
    public void removeChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }
    
    private void fireChangeEvent() {
        Iterator<ChangeListener> it;
        synchronized (listeners) {
            it = new HashSet<ChangeListener>(listeners).iterator();
        }
        ChangeEvent ev = new ChangeEvent(this);
        while (it.hasNext()) {
            it.next().stateChanged(ev);
        }
    }
    
    /**
     * Tries to fix/adjust broken artifacts' references.
     * <p>Acquires write access from {@link ProjectManager#mutex}.</p>
     */
    public void adjustReferences() {
        ProjectManager.mutex().writeAccess(new Runnable() {
            public void run() {
                for (VisualClassPathItem vcpi : project.getProjectProperties().getJarContentAdditional()) {
                    if (vcpi.getObject() instanceof AntArtifact) {
                        String raw = vcpi.getRaw();
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
    
    private void adjustReference(final VisualClassPathItem vcpi, final String referenceKey) {
        EditableProperties prjProps = project.getAntProjectHelper().getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        String referenceValue = prjProps.getProperty(referenceKey);
        if (referenceValue.matches("^\\$\\{project\\..*\\}.+")) { // NOI18N
            String prjReference = referenceValue.substring(0, referenceValue.indexOf('}', 2) + 1);
            String relPath = vcpi.getEvaluated();
            prjProps.setProperty(referenceKey, prjReference + '/' + relPath);
        }
        project.getAntProjectHelper().putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, prjProps);
        try {
            ProjectManager.getDefault().saveProject(project);
        } catch (IOException ioe) {
            ErrorManager.getDefault().notify(ErrorManager.WARNING, ioe);
        }
    }
    
    private class ArtifactListener extends FileChangeAdapter {
        
        public void fileDeleted(FileEvent fe) {
            fireChangeEvent();
        }
        
        public void fileRenamed(FileRenameEvent fe) {
            fireChangeEvent();
        }
        
    }
    
}
