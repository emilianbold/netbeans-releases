/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
package org.netbeans.modules.vmd.palette;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.vmd.api.io.ProjectUtils;
import org.netbeans.modules.vmd.api.model.*;
import org.netbeans.modules.vmd.api.model.common.ActiveDocumentSupport;
import org.openide.filesystems.*;
import org.openide.util.Lookup;
import org.openide.util.WeakListeners;
import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import org.openide.util.RequestProcessor;

/**
 * 
 */
public final class PaletteMap implements ActiveDocumentSupport.Listener, DescriptorRegistryListener, PropertyChangeListener {

    private static final PaletteMap INSTANCE = new PaletteMap();
    
    private final WeakHashMap<String, WeakReference<PaletteKit>> kitMap = new WeakHashMap<String, WeakReference<PaletteKit>>();
    private String activeProjectID;
    private DescriptorRegistry registeredRegistry;
    private final AtomicBoolean requiresPaletteUpdate = new AtomicBoolean(false);
    private final Set<String> registeredProjects = new HashSet<String>();
    private static RequestProcessor updateRP = new RequestProcessor("Update paletteKit"); // NOI18N

    private PaletteMap() {
        ActiveDocumentSupport.getDefault().addActiveDocumentListener(this);
    }

    public static PaletteMap getInstance() {
        return INSTANCE;
    }

    public void activeDocumentChanged(DesignDocument deactivatedDocument, DesignDocument activatedDocument) {
        if (activatedDocument == null) {
            return;
        }

        DescriptorRegistry currentRegistry = activatedDocument.getDescriptorRegistry();
        if (registeredRegistry != currentRegistry) {
            if (registeredRegistry != null) {
                registeredRegistry.removeRegistryListener(this);
            }
            registeredRegistry = currentRegistry;
            if (registeredRegistry != null) {
                registeredRegistry.addRegistryListener(this);
            }
        }

        String oldProjectID;
        synchronized (this) {
            oldProjectID = activeProjectID;
            activeProjectID = activatedDocument.getDocumentInterface().getProjectID();
        }

        boolean isProjectIDChanged = !activeProjectID.equals(oldProjectID);
        if (isProjectIDChanged) {
            registerClassPathListener(activatedDocument);
        }

        updatePalette(activatedDocument, isProjectIDChanged);
    }

    public void activeComponentsChanged(Collection<DesignComponent> activeComponents) {
    }

    public void descriptorRegistryUpdated() {
        updatePalette(ActiveDocumentSupport.getDefault().getActiveDocument(), false);
    }

    private void updatePalette(DesignDocument document, boolean isProjectIDChanged) {
        if (isProjectIDChanged) {
            for (WeakReference<PaletteKit> kitReference : kitMap.values()) {
                PaletteKit kit = kitReference.get();
                if (kit != null) {
                    kit.clearNodesStateCache();
                }
            }
        }

        if (document == null) {
            return;
        }
        WeakReference<PaletteKit> kitReference = kitMap.get(document.getDocumentInterface().getProjectType());
        if (kitReference == null) {
            return;
        }
        PaletteKit kit = kitReference.get();
        if (kit == null) {
            return;
        }
        kit.setActiveDocument(document);
        if (isProjectIDChanged) {
            scheduleUpdateAfteCPScanned(document, kit);
        } else {
            kit.init();
        }
    }

    public synchronized PaletteKit getPaletteKitForProjectType(String projectType) {
        WeakReference<PaletteKit> reference = kitMap.get(projectType);
        PaletteKit kit = reference != null ? reference.get() : null;
        if (kit == null) {
            kit = new PaletteKit(projectType);
            kitMap.put(projectType, new WeakReference<PaletteKit>(kit));
        }
        return kit;
    }

    void checkValidity(String projectType, Lookup lookup) {
        WeakReference<PaletteKit> kitReference = kitMap.get(projectType);
        PaletteKit kit = kitReference != null ? kitReference.get() : null;
        if (kit == null) {
            PaletteItemDataNode node = lookup.lookup(PaletteItemDataNode.class);
            if (node != null) {
                node.setNeedCheck(false);
                node.setValid(true);
            }
        } else {
            kit.checkValidity(lookup);
        }
    }

    public void propertyChange(PropertyChangeEvent evt) {
        schedulePaletteUpdate();
    }

    private void scheduleUpdateAfteCPScanned(DesignDocument document, final PaletteKit kit) {
        final Project project = ProjectUtils.getProject(document);
        final ClasspathInfo info = getClasspathInfo(project);
        if (info == null) {
            return;
        }

        class UpdateTask implements Runnable, Task<CompilationController> {

            public void run() {
                try {
                    JavaSource.create(info).runWhenScanFinished(this, true);
                } catch (IOException ex) {
                    Debug.warning(ex);
                }
            }

            public void run(CompilationController controller) throws Exception {
                kit.init();
            }
        }
        updateRP.post(new UpdateTask());
    }

    private void registerClassPathListener(DesignDocument document) {
        final Project project = ProjectUtils.getProject(document);
        final ClasspathInfo info = getClasspathInfo(project);
        if (info == null) {
            return;
        }

        String projID = document.getDocumentInterface().getProjectID();
        if (!registeredProjects.contains(projID)) {
            Task<CompilationController> ct = new ListenerCancellableTask(info);
            try {
                JavaSource.create(info).runUserActionTask(ct, true);
                registeredProjects.add(projID);
            } catch (IOException ex) {
                Debug.warning(ex);
            }
        }
    }

    private ClasspathInfo getClasspathInfo(Project project) {
        if (project == null) {
            return null;
        }
        SourceGroup group = getSourceGroup(project);
        if (group == null) {
            return null;
        }
        FileObject fileObject = group.getRootFolder();
        return ClasspathInfo.create(fileObject);
    }

    private SourceGroup getSourceGroup(Project project) {
        SourceGroup[] sourceGroups = org.netbeans.api.project.ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        if (sourceGroups == null || sourceGroups.length < 1) {
            return null;
        }
        return sourceGroups[0];
    }

    private void schedulePaletteUpdate() {
        if (requiresPaletteUpdate.getAndSet(true)) {
            return;
        }

        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                while (requiresPaletteUpdate.getAndSet(false)) {
                    for (WeakReference<PaletteKit> kitReference : kitMap.values()) {
                        PaletteKit kit = kitReference.get();
                        if (kit == null) {
                            continue;
                        }
                        kit.clearNodesStateCache();
                        // HINT refresh only visible palette
                        kit.refreshPaletteController();
                    }
                }
            }
        });
    }

    private final class ListenerCancellableTask implements Task<CompilationController> {

        private ClasspathInfo info;

        public ListenerCancellableTask(ClasspathInfo info) {
            this.info = info;
        }

        public void run(CompilationController controller) throws Exception {
            ClassPath cp = info.getClassPath(ClasspathInfo.PathKind.BOOT);
            PropertyChangeListener wcl = WeakListeners.propertyChange(PaletteMap.this, cp);
            cp.addPropertyChangeListener(wcl);
        }
    }
}
