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
import org.netbeans.modules.j2ee.jpa.verification.common.Utilities;
import org.netbeans.modules.j2ee.persistence.api.PersistenceScope;
import org.netbeans.modules.j2ee.persistence.api.PersistenceScopes;
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
    public final static Logger LOG = Logger.getLogger(JPAProblemFinder.class.getName());
    private final static String PERSISTENCE_SCOPES_LISTENER = "jpa.verification.scopes_listener";
    
    public JPAProblemFinder(FileObject file){
        this.file = file;
    }
    
    public void run(CompilationInfo info) throws Exception{
        // the 'cancelled' flag must be reset as the instance of JPAProblemFinder is reused
        cancelled = false;
        List<ErrorDescription> problemsFound = new ArrayList<ErrorDescription>();
        createPersistenceScopesListener(file, info.getDocument());
        
        for (Tree tree : info.getCompilationUnit().getTypeDecls()){
            if (isCancelled()){
                break;
            }
            
            if (tree.getKind() == Tree.Kind.CLASS){
                TreePath path = info.getTrees().getPath(info.getCompilationUnit(), tree);
                TypeElement javaClass = (TypeElement) info.getTrees().getElement(path);
                LOG.fine("processing class " + javaClass.getSimpleName());
                context = findProblemContext(info, javaClass);
                JPARulesEngine rulesEngine = new JPARulesEngine();
                javaClass.accept(rulesEngine, context);
                problemsFound.addAll(rulesEngine.getProblemsFound());
                
                problemsFound.addAll(processIdClassAnnotation(info, javaClass));
                
                synchronized(cancellationLock){
                    context = null;
                }
            }
        }
        
        //TODO: should we really reset the errors if the task is cancelled?
        LOG.info("resetting errors, current number of errors in file:" + problemsFound.size());
        HintsController.setErrors(file, "JPA Verification", problemsFound); //NOI18N
    }
    
    /**
     * If there is IdClassAnotation present run the rules on the pointed class and show
     * found errors locally
     */
    private List<ErrorDescription> processIdClassAnnotation(CompilationInfo info, TypeElement javaClass){
        AnnotationMirror annIdClass = Utilities.findAnnotation(javaClass, JPAAnnotations.ID_CLASS);
        
        if (annIdClass != null){
            AnnotationValue annValue = Utilities.getAnnotationAttrValue(annIdClass, JPAAnnotations.VALUE_ATTR);
            
            if (annValue != null){
                Object rawIdClass = annValue.getValue();
                TypeElement idClass = info.getElements().getTypeElement(rawIdClass.toString());
                
                if (idClass != null){
                    JPAProblemContext context = findProblemContext(info, idClass);
                    context.setIdClass(true);
                    
                    // just to avoid troubles...
                    context.setEntity(false);
                    context.setEmbeddable(false);
                    context.setMappedSuperClass(false);
                    context.setElementToAnnotate(info.getTrees().getTree(javaClass, annIdClass, annValue));
                    
                    JPARulesEngine rulesEngine = new JPARulesEngine();
                    idClass.accept(rulesEngine, context);
                    return rulesEngine.getProblemsFound();
                }
            }
        }
        
        return Collections.EMPTY_LIST;
    }
    
    private JPAProblemContext findProblemContext(CompilationInfo info, TypeElement javaClass){
        JPAProblemContext context = new JPAProblemContext();
        
        AnnotationMirror annEntity = Utilities.findAnnotation(javaClass, JPAAnnotations.ENTITY);
        
        if (annEntity != null){
            context.setEntity(true);
            context.setElementToAnnotate(info.getTrees().getTree(javaClass, annEntity));
        }
        
        AnnotationMirror annEmbeddable = Utilities.findAnnotation(javaClass, JPAAnnotations.EMBEDDABLE);
        
        if (annEmbeddable != null){
            context.setEmbeddable(true);
            context.setElementToAnnotateOrNullIfExists(info.getTrees().getTree(javaClass, annEmbeddable));
        }
        
        AnnotationMirror annMappedSuperClass = Utilities.findAnnotation(javaClass, JPAAnnotations.MAPPED_SUPERCLASS);
        
        if (annMappedSuperClass != null){
            context.setMappedSuperClass(true);
            context.setElementToAnnotateOrNullIfExists(info.getTrees().getTree(javaClass, annMappedSuperClass));
        }
        
        context.setFileObject(file);
        context.setCompilationInfo(info);
        return context;
    }
    
    public void cancel(){
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
