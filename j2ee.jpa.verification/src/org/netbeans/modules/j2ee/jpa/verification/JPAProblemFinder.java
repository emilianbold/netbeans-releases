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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.j2ee.jpa.verification;

import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import javax.swing.text.Document;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.jpa.model.JPAAnnotations;
import org.netbeans.modules.j2ee.jpa.model.JPAHelper;
import org.netbeans.modules.j2ee.jpa.model.ModelUtils;
import org.netbeans.modules.j2ee.jpa.verification.common.Utilities;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.persistence.api.PersistenceScope;
import org.netbeans.modules.j2ee.persistence.api.PersistenceScopes;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Entity;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.EntityMappingsMetadata;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.IdClass;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.MappedSuperclass;
import org.netbeans.modules.j2ee.persistence.dd.PersistenceUtils;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.HintsController;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.util.WeakListeners;

/**
 *
 * @author Tomasz.Slota@Sun.COM
 */
public abstract class JPAProblemFinder {
    private boolean cancelled = false;
    private FileObject file = null;
    private Object cancellationLock = new Object();
    private JPAProblemContext context = null;
    private List<ErrorDescription> problemsFound = new ArrayList<ErrorDescription>();
    
    public final static Logger LOG = Logger.getLogger(JPAProblemFinder.class.getName());
    private final static String PERSISTENCE_SCOPES_LISTENER = "jpa.verification.scopes_listener"; //NOI18N
    private final static Object singleInstanceLock = new Object();
    private static JPAProblemFinder runningInstance = null;
    private static boolean usgLogged;
    
    public JPAProblemFinder(FileObject file){
        assert file != null;
        this.file = file;
    }
    
    public void run(final CompilationInfo info) throws Exception{
        if (!"text/x-java".equals(file.getMIMEType())){ //NOI18N
            return;
        }
        
        if (runningInstance != null){
            runningInstance.cancel();
        }
        
        synchronized(singleInstanceLock){
            runningInstance = this;
            // the 'cancelled' flag must be reset as the instance of JPAProblemFinder is reused
            cancelled = false;
            problemsFound.clear();
            createPersistenceScopesListener(file, info.getDocument());
            MetadataModel<EntityMappingsMetadata> emModel = ModelUtils.getModel(file);
            
            if (emModel == null){
                return; // File doesn't belong to any project or project doesn't support JPA
            }
            
            emModel.runReadActionWhenReady(new MetadataModelAction<EntityMappingsMetadata, Void>() {
                public Void run(EntityMappingsMetadata metadata) {
                    for (Tree tree : info.getCompilationUnit().getTypeDecls()){
                        if (isCancelled()){
                            break;
                        }
                        
                        if (tree.getKind() == Tree.Kind.CLASS){
                            
                            TreePath path = info.getTrees().getPath(info.getCompilationUnit(), tree);
                            TypeElement javaClass = (TypeElement) info.getTrees().getElement(path);
                            
                            processClass(info, metadata, javaClass);
                            
                            for (TypeElement innerClass : ElementFilter.typesIn(javaClass.getEnclosedElements())){
                                processClass(info, metadata, innerClass);
                            }
                            
                            synchronized(cancellationLock){
                                context = null;
                            }
                        }
                    }
                    
                    return null;
                }
            });
            
            //TODO: should we really reset the errors if the task is cancelled?
            HintsController.setErrors(file, "JPA Verification", problemsFound); //NOI18N
            runningInstance = null;
        }
    }
    
    private void processClass(CompilationInfo info,
            EntityMappingsMetadata metadata,
            TypeElement javaClass) {
        long startTime = Calendar.getInstance().getTimeInMillis();
        context = findProblemContext(info, javaClass, metadata, false);
        JPARulesEngine rulesEngine = new JPARulesEngine();
        rulesEngine.visitTypeAsClass(javaClass, context);
        problemsFound.addAll(rulesEngine.getProblemsFound());

        // signal locally errors found in the IdClass
        problemsFound.addAll(processIdClass(info, javaClass, metadata, context.getModelElement()));

        if (LOG.isLoggable(Level.FINE)) {
            long timeElapsed = Calendar.getInstance().getTimeInMillis() - startTime;

            LOG.log(Level.FINE, "processed class {0} in {1} ms", new Object[]{javaClass.getSimpleName(), timeElapsed});
        }
    }
    
    /**
     * If there is IdClass annotation present run the rules on the referenced class
     * and show problems found in the referencing class
     */
    private List<ErrorDescription> processIdClass(CompilationInfo info, TypeElement javaClass,
            EntityMappingsMetadata metadata, Object modelElement){
        
        IdClass idClassElem = null;
        
        if (modelElement instanceof Entity){
            idClassElem = ((Entity)modelElement).getIdClass();
        }
        else if (modelElement instanceof MappedSuperclass){
            idClassElem = ((MappedSuperclass)modelElement).getIdClass();
        }
        
        if (idClassElem != null){
            String idClassName = idClassElem.getClass2();
            
            if (idClassName != null){
                TypeElement idClass = info.getElements().getTypeElement(idClassName);
                
                if (idClass != null){
                    JPAProblemContext context = findProblemContext(info, idClass, metadata, true);
                    AnnotationMirror annIdClass = Utilities.findAnnotation(javaClass, JPAAnnotations.ID_CLASS);
                    
                    // By default underline the @IdClass annotation. 
                    // If IdClass is defined in orm.xml underline the class name 
                    // (of the entity using IdClass)
                    Tree treeToAnnotate = annIdClass != null ? 
                        info.getTrees().getTree(javaClass, annIdClass) : info.getTrees().getTree(javaClass);
                    
                    context.setElementToAnnotate(treeToAnnotate);
                    JPARulesEngine rulesEngine = new JPARulesEngine();
                    rulesEngine.visitTypeAsClass(idClass, context);
                    return rulesEngine.getProblemsFound();
                }
            }
        }
        
        return Collections.<ErrorDescription>emptyList();
    }
    
    private JPAProblemContext findProblemContext(CompilationInfo info,
            TypeElement javaClass, EntityMappingsMetadata metadata, boolean idClass){
        
        JPAProblemContext context = new JPAProblemContext();
        context.setMetaData(metadata);
        context.setJavaClass(javaClass);
        
        if (!idClass){
            Object modelElement = ModelUtils.getEntity(metadata, javaClass);
            
            if (modelElement != null){
                context.setEntity(true);
            } else{
                modelElement = ModelUtils.getEmbeddable(metadata, javaClass);
                
                if (modelElement != null){
                    context.setEmbeddable(true);
                } else{
                    modelElement = ModelUtils.getMappedSuperclass(metadata, javaClass);
                    
                    if (modelElement != null){
                        context.setMappedSuperClass(true);
                    }
                }
            }
            
            context.setModelElement(modelElement);
        }
        context.setIdClass(idClass);
        context.setFileObject(file);
        context.setCompilationInfo(info);
        
        if (context.isJPAClass()){
            context.setAccessType(JPAHelper.findAccessType(javaClass, context.getModelElement()));
            if(!usgLogged) {
                usgLogged = true;
                PersistenceUtils.logUsage(JPAProblemFinder.class, "USG_PERSISTENCE_DETECTED", new String[]{"CLASS"});//NOI18N
            }
        }
        
        return context;
    }
    
    public void cancel(){
        LOG.fine("Cancelling JPAProblemFinder task");
        cancelled = true;
        
        synchronized(cancellationLock){
            if (context != null){
                context.setCancelled(true);
            }
        }
    }
    
    public boolean isCancelled(){
        return cancelled;
    }
    
    public List<? extends ErrorDescription> getProblemsFound(){
        return problemsFound;
    }
    
    private void createPersistenceScopesListener(FileObject file, Document doc){
        if (doc == null){
            return;
        }
        
        LOG.fine("Creating PersistenceScopesListener on " + file.getName());
        Project project = FileOwnerQuery.getOwner(file);
        
        if (project != null){
            PersistenceScopes scopes = PersistenceScopes.getPersistenceScopes(project);
            
            if (scopes != null){
                PersistenceScopesListener listener = (PersistenceScopesListener) doc.getProperty(PERSISTENCE_SCOPES_LISTENER);
                
                if (listener == null){
                    listener = new PersistenceScopesListener(file);
                    PropertyChangeListener weakListener = WeakListeners.create(PropertyChangeListener.class, listener, null);
                    scopes.addPropertyChangeListener(weakListener);
                    
                    // scopes listener should live as long as the document
                    doc.putProperty(PERSISTENCE_SCOPES_LISTENER, listener);
                }
                
                ArrayList<PersistenceXMLListener> pxmlListeners = new ArrayList<PersistenceXMLListener>();
                
                for (PersistenceScope scope : scopes.getPersistenceScopes()){
                    FileObject persistenceXML = scope.getPersistenceXml();
                    PersistenceXMLListener pxmlListener = new PersistenceXMLListener(file);
                    FileChangeListener weakPXMLListener = WeakListeners.create(FileChangeListener.class, pxmlListener, null);
                    persistenceXML.addFileChangeListener(weakPXMLListener);
                    pxmlListeners.add(pxmlListener);
                    LOG.fine("Added PersistenceXMLListener to " + persistenceXML.getName());
                }
                
                // persistence.xml listeners should live as long as the scopes listener
                listener.setPXMLListeners(pxmlListeners);
            }
        }
    }
    
    private abstract class RescanTrigger{
        private FileObject file;
        
        RescanTrigger(FileObject file){
            this.file = file;
        }
        
        void rescan(){
            JavaSource javaSrc = JavaSource.forFileObject(file);
            
            if (javaSrc != null){
                try{
                    javaSrc.runUserActionTask(new ProblemFinderCompControl(file), true);
                } catch (IOException e){
                    // IOE can happen legitimatelly, see #103453
                    LOG.log(Level.FINE, e.getMessage(), e);
                }
            }
        }
    }
    
    private class PersistenceScopesListener extends RescanTrigger implements PropertyChangeListener{
        List<PersistenceXMLListener> pxmlListeners;
        
        PersistenceScopesListener(FileObject file){
            super(file);
        }
        
        public void propertyChange(PropertyChangeEvent evt){
            LOG.fine("Received a change event from PersistenceScopes");
            rescan();
        }
        
        void setPXMLListeners(List<PersistenceXMLListener> pxmlListeners){
            this.pxmlListeners = pxmlListeners;
        }
    }
    
    private class PersistenceXMLListener extends RescanTrigger implements FileChangeListener{
        PersistenceXMLListener(FileObject file){
            super(file);
        }
        
        public void fileChanged(FileEvent fe){
            LOG.fine("Received a change event from persistence.xml");
            rescan();
        }
        
        public void fileFolderCreated(FileEvent fe){}
        public void fileDataCreated(FileEvent fe){}
        public void fileDeleted(FileEvent fe){}
        public void fileRenamed(FileRenameEvent fe){}
        public void fileAttributeChanged(FileAttributeEvent fe){}
    }
    
    public static class ProblemFinderCompInfo extends JPAProblemFinder implements CancellableTask<CompilationInfo>{
        public ProblemFinderCompInfo(FileObject file){
            super(file);
        }
    }
    
    public static class ProblemFinderCompControl extends JPAProblemFinder implements CancellableTask<CompilationController>{
        public ProblemFinderCompControl(FileObject file){
            super(file);
        }
        
        public void run(CompilationController controller) throws Exception {
            controller.toPhase(JavaSource.Phase.RESOLVED);
            super.run(controller);
        }
    }
}
