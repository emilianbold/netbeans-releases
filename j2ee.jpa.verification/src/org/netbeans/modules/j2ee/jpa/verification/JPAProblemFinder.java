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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.jpa.verification;

import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.TypeElement;
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
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.EntityMappingsMetadata;
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
    
    public JPAProblemFinder(FileObject file){
        this.file = file;
    }
    
    public void run(final CompilationInfo info) throws Exception{
        if (runningInstance != null){
            runningInstance.cancel();
        }
        
        synchronized(singleInstanceLock){
            runningInstance = this;
            // the 'cancelled' flag must be reset as the instance of JPAProblemFinder is reused
            cancelled = false;
            problemsFound.clear();
            createPersistenceScopesListener(file, info.getDocument());
            Project project = FileOwnerQuery.getOwner(file);
            
            if (project == null){
                return; // the source file doesn't belong to any project, skip all checks
            }
            
            PersistenceScopes scopes = PersistenceScopes.getPersistenceScopes(project);
            
            if (scopes == null){
                return; // project of this type doesn't provide a list of persistence scopes
            }
            
            //TODO: a workaround for 102643, remove it when the issue is fixed
            if (scopes.getPersistenceScopes().length == 0){
                return;
            }
            
            PersistenceScope scope = scopes.getPersistenceScopes()[0];
            // end of workround for 102643
            
            MetadataModel<EntityMappingsMetadata> emModel = scope.getEntityMappingsModel(null);
            emModel.runReadAction(new MetadataModelAction<EntityMappingsMetadata, Void>() {
                public Void run(EntityMappingsMetadata metadata) {
                    for (Tree tree : info.getCompilationUnit().getTypeDecls()){
                        if (isCancelled()){
                            break;
                        }
                        
                        if (tree.getKind() == Tree.Kind.CLASS){
                            TreePath path = info.getTrees().getPath(info.getCompilationUnit(), tree);
                            TypeElement javaClass = (TypeElement) info.getTrees().getElement(path);
                            LOG.fine("processing class " + javaClass.getSimpleName());
                            context = findProblemContext(info, javaClass, metadata, false);
                            JPARulesEngine rulesEngine = new JPARulesEngine();
                            javaClass.accept(rulesEngine, context);
                            problemsFound.addAll(rulesEngine.getProblemsFound());
                            
                            problemsFound.addAll(processIdClassAnnotation(info, javaClass, metadata));
                            
                            synchronized(cancellationLock){
                                context = null;
                            }
                        }
                    }
                    
                    return null;
                }
            });
            
            //TODO: should we really reset the errors if the task is cancelled?
            LOG.log(Level.FINE, "resetting errors, current number of errors in file: {0}", problemsFound.size());
            HintsController.setErrors(file, "JPA Verification", problemsFound); //NOI18N
            runningInstance = null;
        }
    }
    
    /**
     * If there is IdClassAnotation present run the rules on the pointed class and show
     * found errors locally
     */
    private List<ErrorDescription> processIdClassAnnotation(CompilationInfo info, TypeElement javaClass,
            EntityMappingsMetadata metadata){
        
        //TODO: use model
        AnnotationMirror annIdClass = Utilities.findAnnotation(javaClass, JPAAnnotations.ID_CLASS);
        
        if (annIdClass != null){
            AnnotationValue annValue = Utilities.getAnnotationAttrValue(annIdClass, JPAAnnotations.VALUE_ATTR);
            
            if (annValue != null){
                Object rawIdClass = annValue.getValue();
                TypeElement idClass = info.getElements().getTypeElement(rawIdClass.toString());
                
                if (idClass != null){
                    JPAProblemContext context = findProblemContext(info, idClass, metadata, true);
                    context.setElementToAnnotate(info.getTrees().getTree(javaClass, annIdClass, annValue));
                    JPARulesEngine rulesEngine = new JPARulesEngine();
                    idClass.accept(rulesEngine, context);
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
                //TODO: uncomment when #103058 is fixed
                //modelElement = ModelUtils.getEmbeddable(metadata, javaClass);
                
                if (modelElement != null){
                    context.setEmbeddable(true);
                } else{
                    //TODO: uncomment when #103059 is fixed
                    //modelElement = ModelUtils.getMappedSuperclass(metadata, javaClass);
                    
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
        }
        
        return context;
    }
    
    public void cancel(){
        LOG.info("Cancelling JPAProblemFinder task");
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
                    LOG.log(Level.WARNING, e.getMessage(), e);
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
