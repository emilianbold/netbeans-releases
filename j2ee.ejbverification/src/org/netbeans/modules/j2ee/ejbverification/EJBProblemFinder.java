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

package org.netbeans.modules.j2ee.ejbverification;

import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.HintsController;
import org.openide.filesystems.FileObject;
import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
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
            // the 'cancelled' flag must be reset as the instance of EJBProblemFinder is reused
            cancelled = false;
            problemsFound.clear();

            boolean isEjb = false;
            Project prj = FileOwnerQuery.getOwner(file);
            //#156889: Add check for null.
            if (prj == null) {
                return;
            }
            J2eeModuleProvider provider = (J2eeModuleProvider) prj.getLookup().lookup(J2eeModuleProvider.class);
            if (provider != null) {
                J2eeModule module = provider.getJ2eeModule();
                if (module != null) {
                    if (J2eeModule.Type.EJB.equals(module.getType())) {
                        isEjb = true;
                    }
                }
            }
            
            if (!isEjb) {
                return; // File doesn't belong to EJB project
            }

            EjbJar ejbModule = EjbJar.getEjbJar(file);
            if (ejbModule == null
                    || !Profile.JAVA_EE_5.equals(ejbModule.getJ2eeProfile())) {
                return; // EJB version is not supported
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
                            
                            problemsFound.addAll(EJBRulesRegistry.check(ctx));
                            
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
