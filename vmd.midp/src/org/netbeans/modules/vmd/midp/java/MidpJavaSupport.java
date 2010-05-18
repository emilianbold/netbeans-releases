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
package org.netbeans.modules.vmd.midp.java;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.modules.vmd.midp.components.*;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.vmd.api.io.DataObjectContext;
import org.netbeans.modules.vmd.api.io.ProjectUtils;
import org.netbeans.modules.vmd.api.model.ComponentProducer;
import org.netbeans.modules.vmd.api.model.Debug;
import org.netbeans.modules.vmd.api.model.DescriptorRegistry;
import org.netbeans.modules.vmd.api.model.DesignDocument;
import org.netbeans.modules.vmd.api.model.TypeID;
import org.openide.filesystems.FileObject;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;

/**
 * Utility class for checking whether given typeID is in classpath of current project
 *
 * @author Anton Chechel
 */
public final class MidpJavaSupport implements Runnable, PropertyChangeListener {

    private static HashMap<String, MidpJavaSupport> instanceMap;
    private static RequestProcessor validationRP;
    
    private final ConcurrentLinkedQueue<String> validationQueue;
    private final ConcurrentHashMap<String, Boolean> validationCache;
    private final AtomicBoolean isValidationRunning;
    private WeakReference<DesignDocument> document;

    private MidpJavaSupport(DesignDocument document) {
        this.document = new WeakReference<DesignDocument>(document);

        validationQueue = new ConcurrentLinkedQueue<String>();
        validationCache = new ConcurrentHashMap<String, Boolean>();
        isValidationRunning = new AtomicBoolean();

        registerClassPathListener();
    }

    public Boolean checkValidityCached(TypeID typeID) {
        return checkValidityCached(MidpTypes.getFQNClassName(typeID));
    }

    public Boolean checkValidityCached(String fqnName) {
        if (validationCache.containsKey(fqnName)) {
            return validationCache.get(fqnName);
        } else {
            scheduleValidation(fqnName);
            return null;
        }
    }

    private void scheduleValidation(String fqnName) {
        validationQueue.add(fqnName);
        if (!isValidationRunning.getAndSet(true)) {
            validationRP.post(this);
        }
    }

    public void run() {
        final DesignDocument doc = document.get();
        if (doc == null) {
            return;
        }
        
        while (true) {
            if (validationQueue.isEmpty()) {
                isValidationRunning.set(false);
                break;
            }
            String fqnName = validationQueue.remove();
            boolean isValid = checkValidity(doc, fqnName);
            validationCache.put(fqnName, isValid);
        }
    }

    private void registerClassPathListener() {
        final DesignDocument doc = document.get();
        if (doc == null) {
            return;
        }
        
        final ClasspathInfo info = getClasspathInfo(ProjectUtils.getProject(doc));
        if (info == null) {
            Debug.warning("Can't get ClasspathInfo for project"); // NOI18N
            return;
        }

        try {
            Task<CompilationController> ct = new Task<CompilationController>() {

                public void run(CompilationController cc) throws Exception {
                    ClassPath cp = info.getClassPath(ClasspathInfo.PathKind.BOOT);
                    PropertyChangeListener wcl = WeakListeners.propertyChange(MidpJavaSupport.this, cp);
                    cp.addPropertyChangeListener(wcl);
                }
            };

            JavaSource.create(info).runUserActionTask(ct, true);
        } catch (IOException ex) {
            Debug.warning(ex);
        }
    }

    private ClasspathInfo getClasspathInfo(Project project) {
        if (project == null) {
            return null;
        }

        SourceGroup group = getSourceGroup(project);
        return group != null ? ClasspathInfo.create(group.getRootFolder()) : null;
    }

    private SourceGroup getSourceGroup(Project project) {
        SourceGroup[] sourceGroups = org.netbeans.api.project.ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        return sourceGroups != null && sourceGroups.length > 0 ? sourceGroups[0] : null;
    }

    // for classpath listener
    public void propertyChange(PropertyChangeEvent evt) {
        updateCacheInternally();
    }
    
    void updateCacheInternally() {
        final DesignDocument doc = document.get();
        if (doc == null) {
            return;
        }
        
        validationCache.clear();

        final String projectID = doc.getDocumentInterface().getProjectID();
        final String projectType = doc.getDocumentInterface().getProjectType();
        final List<ComponentProducer> producers = new ArrayList<ComponentProducer>();
        
        final DescriptorRegistry registry = DescriptorRegistry.getDescriptorRegistry(projectType, projectID);
        registry.readAccess(new Runnable() {

            public void run() {
                producers.addAll(registry.getComponentProducers());
            }
        });
        
        final ClasspathInfo info = getClasspathInfo(ProjectUtils.getProject(doc));
        if (info != null) {
            try {
                JavaSource.create(info).runWhenScanFinished(new Task<CompilationController>() {
                    
                    public void run(CompilationController cc) throws Exception {
                        for (ComponentProducer componentProducer : producers) {
                            componentProducer.checkValidity(doc, true);
                        }
                    }
                }, true);
            } catch (IOException ex) {
                Debug.warning(ex);
            }
        }
        
    }

    /**
     * Checks whether given typeID is in classpath of current project
     * 
     * @param document container of gived typeID
     * @param typeID to be checked
     * @return isValid
     */
    public static boolean checkValidity(DesignDocument document, TypeID typeID) {
        return checkValidity(document, MidpTypes.getFQNClassName(typeID));
    }

    /**
     * Checks whether given fqName is in classpath of current project
     * 
     * @param document container with given classpath context
     * @param fqName to be checked
     * @return isValid
     */
    public static boolean checkValidity(DesignDocument document, String fqName) {
        DataObjectContext context = ProjectUtils.getDataObjectContextForDocument(document);
        if (context == null) { // document is loading
            return true;
        }

        List<SourceGroup> sg = ProjectUtils.getSourceGroups(context);
        boolean isValid = false;
        CheckingTask ct = new CheckingTask();
        Collection<FileObject> collection = Collections.emptySet();
        for (SourceGroup sourceGroup : sg) {
            ct.setFQName(fqName);

            ClasspathInfo cpi = ClasspathInfo.create(sourceGroup.getRootFolder());
            try {
                JavaSource.create(cpi, collection).runUserActionTask(ct, true);
            } catch (Exception ex) {
                Debug.warning("Can't create javasource for", fqName); // NOI18N
            }
            isValid = ct.getResult();
            if (!isValid) {
                break;
            }
        }

        return isValid;
    }

    /**
     * @return instance of JavaClassNameResolver for given DesignDocument, creates it
     * if doesn't exist
     */
    public synchronized static MidpJavaSupport getCache(DesignDocument document) {
        if (instanceMap == null) {
            instanceMap = new HashMap<String, MidpJavaSupport>(1);
            validationRP = new RequestProcessor("VMD MIDP ClassPath validation"); // NOI18N
        }

        String projectID = document.getDocumentInterface().getProjectID();
        MidpJavaSupport instance = instanceMap.get(projectID);
        if (instance == null) {
            instance = new MidpJavaSupport(document);
            instanceMap.put(projectID, instance);
        }

        return instance;
    }

    private static final class CheckingTask implements Task<CompilationController> {

        private boolean result;
        private String fqName;

        public void run(CompilationController controller) throws Exception {
            Elements elements = controller.getElements();
            TypeElement te = elements.getTypeElement(fqName);
            result = (te != null);
        }

        public boolean getResult() {
            return result;
        }

        public void setFQName(String fqName) {
            this.fqName = fqName;
        }
    }
}
