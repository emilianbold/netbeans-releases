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

package org.netbeans.modules.j2ee.ejbverification;

import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.HintsController;
import org.openide.filesystems.FileObject;
import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
import org.netbeans.modules.j2ee.api.ejbjar.EjbProjectConstants;
import org.netbeans.modules.j2ee.dd.api.ejb.Ejb;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJarMetadata;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;

/**
 *
 * @author Tomasz.Slota@Sun.COM
 */
public abstract class EJBProblemFinder {
    private boolean cancelled = false;
    private FileObject file = null;
    private List<ErrorDescription> problemsFound = new ArrayList<ErrorDescription>();
    
    public final static Logger LOG = Logger.getLogger(EJBProblemFinder.class.getName());
    private final static Object singleInstanceLock = new Object();
    private static EJBProblemFinder runningInstance = null;
    
    public EJBProblemFinder(FileObject file){
        this.file = file;
    }
    
    public void run(final CompilationInfo info) throws Exception{
        if (runningInstance != null){
            runningInstance.cancel();
        }
        
        synchronized(singleInstanceLock){
            runningInstance = this;
            // the 'cancelled' flag must be reset as the instance of EJBProblemFinder is reused
            cancelled = false;
            problemsFound.clear();
            EjbJar ejbModule = EjbJar.getEjbJar(file);
            
            if (ejbModule == null 
                    || !ejbModule.getJ2eePlatformVersion().equals(EjbProjectConstants.JAVA_EE_5_LEVEL)){
                return; // File doesn't belong to EJB project or the EJB version is not supported
            }
            
            ejbModule.getMetadataModel().runReadAction(new MetadataModelAction<EjbJarMetadata, Void>() {
                public Void run(EjbJarMetadata metadata) {
                    for (Tree tree : info.getCompilationUnit().getTypeDecls()){
                        if (isCancelled()){
                            break;
                        }
                        
                        if (tree.getKind() == Tree.Kind.CLASS){
                            long startTime = Calendar.getInstance().getTimeInMillis();                            
                            TreePath path = info.getTrees().getPath(info.getCompilationUnit(), tree);
                            TypeElement javaClass = (TypeElement) info.getTrees().getElement(path);
                          
                            Ejb ejb = metadata.findByEjbClass(javaClass.getQualifiedName().toString());
                            
                            EJBProblemContext ctx = new EJBProblemContext(
                                    info, file, javaClass, ejb, metadata);
                            
                            if (ejb != null){
                                problemsFound.addAll(EJBRulesRegistry.check(ctx));
                            }
                            
                            if (LOG.isLoggable(Level.FINE)){
                                long timeElapsed = Calendar.getInstance().getTimeInMillis() - startTime;
                                
                                LOG.log(Level.FINE, "processed class {0} in {1} ms",
                                        new Object[]{javaClass.getSimpleName(), timeElapsed});
                            }
                        }
                    }
                    
                    return null;
                }
            });
            
            //TODO: should we really reset the errors if the task is cancelled?
            HintsController.setErrors(file, "EJB Verification", problemsFound); //NOI18N
            runningInstance = null;
        }
    }
     
    public void cancel(){
        LOG.fine("Cancelling EJBProblemFinder task");
        cancelled = true;
    }
    
    public boolean isCancelled(){
        return cancelled;
    }
    
    public List<? extends ErrorDescription> getProblemsFound(){
        return problemsFound;
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
    
    public static class ProblemFinderCompInfo extends EJBProblemFinder implements CancellableTask<CompilationInfo>{
        public ProblemFinderCompInfo(FileObject file){
            super(file);
        }
    }
    
    public static class ProblemFinderCompControl extends EJBProblemFinder implements CancellableTask<CompilationController>{
        public ProblemFinderCompControl(FileObject file){
            super(file);
        }
        
        public void run(CompilationController controller) throws Exception {
            controller.toPhase(JavaSource.Phase.RESOLVED);
            super.run(controller);
        }
    }
}
