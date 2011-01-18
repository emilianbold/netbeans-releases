/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.modelimpl.trace;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.services.CsmStandaloneFileProvider;
import org.netbeans.modules.cnd.api.model.util.CsmTracer;
import org.netbeans.modules.cnd.debug.CndDiagnosticProvider;
import org.netbeans.modules.cnd.modelimpl.csm.core.CsmStandaloneFileProviderImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileSnapshot;
import org.netbeans.modules.cnd.modelimpl.csm.core.LibraryManager;
import org.netbeans.modules.cnd.modelimpl.csm.core.ModelImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.ProjectBase;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author vv159170
 */

public final class CodeModelDiagnostic {

    @ServiceProvider(service = CndDiagnosticProvider.class, position = 1000)
    public final static class StandAloneProviderTrace implements CndDiagnosticProvider {

        @Override
        public String getDisplayName() {
            return "Standalone Files Information";// NOI18N 
        }

        @Override
        public void dumpInfo(Lookup context, PrintWriter printOut) {
            printOut.printf("====CsmStandaloneFileProviders info:\n");// NOI18N
            for (CsmStandaloneFileProvider sap : Lookup.getDefault().lookupAll(CsmStandaloneFileProvider.class)) {
                if (sap instanceof CsmStandaloneFileProviderImpl) {
                    ((CsmStandaloneFileProviderImpl) sap).dumpInfo(printOut);
                } else {
                    printOut.printf("UKNOWN FOR ME [%s] %s\n", sap.getClass().getName(), sap.toString());// NOI18N 
                }
            }
        }
    }
        
    @ServiceProvider(service = CndDiagnosticProvider.class, position = 1100)
    public final static class FileTrace implements CndDiagnosticProvider {

        @Override
        public String getDisplayName() {
            return "General File Information";// NOI18N 
        }

        @Override
        public void dumpInfo(Lookup context, PrintWriter printOut) {
            printOut.printf("====Files info:\nGlobal ParseCount=%d\n", FileImpl.getParseCount());// NOI18N 
            Collection<? extends CsmFile> allFiles = context.lookupAll(CsmFile.class);
            for (CsmFile csmFile : allFiles) {
                if (csmFile instanceof FileImpl) {
                    ((FileImpl) csmFile).dumpInfo(printOut);
                } else if (csmFile instanceof FileSnapshot) {
                    ((FileSnapshot) csmFile).dumpInfo(printOut);
                } else {
                    printOut.printf("UKNOWN FOR ME [%s] %s\n", csmFile.getClass().getName(), csmFile.toString());// NOI18N 
                }
            }
        }
    }
    
    @ServiceProvider(service = CndDiagnosticProvider.class, position = 1200)    
    public final static class PPStatesTrace implements CndDiagnosticProvider {

        @Override
        public String getDisplayName() {
            return "Preprocessor States";// NOI18N 
        }

        @Override
        public void dumpInfo(Lookup context, PrintWriter printOut) {
            printOut.printf("====Files info:\nGlobal ParseCount=%d\n", FileImpl.getParseCount());// NOI18N 
            Collection<? extends CsmFile> allFiles = context.lookupAll(CsmFile.class);
            for (CsmFile csmFile : allFiles) {
                if (csmFile instanceof FileImpl) {
                    ((FileImpl) csmFile).dumpPPStates(printOut);
                } else {
                    printOut.printf("UKNOWN FOR ME [%s] %s\n", csmFile.getClass().getName(), csmFile.toString());// NOI18N 
                }
            }
        }
    }
      
    @ServiceProvider(service = CndDiagnosticProvider.class, position = 1300)
    public final static class ModelProjectsTrace implements CndDiagnosticProvider {

        @Override
        public String getDisplayName() {
            return "Model Projects";// NOI18N 
        }

        @Override
        public void dumpInfo(Lookup context, PrintWriter printOut) {
            printOut.printf("====ModelImpl:\n");// NOI18N
            ModelImpl.instance().dumpInfo(printOut, false);
            printOut.printf("====Libraries:\n"); //NOI18N
            LibraryManager.getInstance().dumpInfo(printOut, false);
        }
    }
    
    @ServiceProvider(service = CndDiagnosticProvider.class, position = 1350)
    public final static class ModelProjectsContainers implements CndDiagnosticProvider {

        @Override
        public String getDisplayName() {
            return "Model Projects File Containers";// NOI18N 
        }

        @Override
        public void dumpInfo(Lookup context, PrintWriter printOut) {
            printOut.printf("====ModelImpl:\n");// NOI18N
            ModelImpl.instance().dumpInfo(printOut, true);
            printOut.printf("====Libraries:\n"); //NOI18N
            LibraryManager.getInstance().dumpInfo(printOut, true);
        }
    }
    
    @ServiceProvider(service = CndDiagnosticProvider.class, position = 1400)
    public final static class FileImplModelTrace implements CndDiagnosticProvider {

        @Override
        public String getDisplayName() {
            return "File Code Model";// NOI18N 
        }

        @Override
        public void dumpInfo(Lookup context, PrintWriter printOut) {
            Collection<? extends CsmFile> allFiles = context.lookupAll(CsmFile.class);
            for (CsmFile csmFile : allFiles) {
                new CsmTracer(printOut).dumpModel(csmFile);
            }            
        }
    }    

    @ServiceProvider(service = CndDiagnosticProvider.class, position = 1400)
    public final static class FileImplASTTrace implements CndDiagnosticProvider {

        @Override
        public String getDisplayName() {
            return "File AST";// NOI18N
        }

        @Override
        public void dumpInfo(Lookup context, PrintWriter printOut) {
            Collection<? extends CsmFile> allFiles = context.lookupAll(CsmFile.class);
            for (CsmFile csmFile : allFiles) {
                if(csmFile instanceof FileImpl) {
                    ASTFrameEx frame = new ASTFrameEx(csmFile.getName().toString(), ((FileImpl) csmFile).debugParse());
                    frame.setVisible(true);
                }
            }
        }
    }
    
    @ServiceProvider(service = CndDiagnosticProvider.class, position = 1500)
    public final static class ProjectDeclarationsTrace implements CndDiagnosticProvider {

        @Override
        public String getDisplayName() {
            return "Project Declaration Containers (Huge size)";// NOI18N 
        }

        @Override
        public void dumpInfo(Lookup context, PrintWriter printOut) {
            Collection<CsmProject> projects = new ArrayList<CsmProject>(context.lookupAll(CsmProject.class));
            if (projects.isEmpty()) {
                CsmFile file = context.lookup(CsmFile.class);
                if (file != null) {
                    CsmProject project = file.getProject();
                    if (project instanceof ProjectBase) {
                        projects.add(project);
                    }
                }
            }
            PrintStream ps = CsmTracer.toPrintStream(printOut);
            for (CsmProject prj : projects) {
                if (prj instanceof ProjectBase) {
                    ((ProjectBase)prj).traceProjectContainers(ps);
                }
            }
        }
    }
    
    @ServiceProvider(service = CndDiagnosticProvider.class, position = 1600)
    public final static class ModelTrace implements CndDiagnosticProvider {

        @Override
        public String getDisplayName() {
            return "Project Code Model (Huge size)";// NOI18N 
        }

        @Override
        public void dumpInfo(Lookup context, PrintWriter printOut) {
            Collection<CsmProject> projects = new ArrayList<CsmProject>(context.lookupAll(CsmProject.class));
            if (projects.isEmpty()) {
                CsmFile file = context.lookup(CsmFile.class);
                if (file != null) {
                    CsmProject project = file.getProject();
                    if (project != null) {
                        projects.add(project);
                    }
                }
            }
            for (CsmProject prj : projects) {
                new CsmTracer(printOut).dumpModel(prj);
            }
        }
    }    
    
    @ServiceProvider(service = CndDiagnosticProvider.class, position = 1600)
    public final static class ProjectReferencesTrace implements CndDiagnosticProvider {

        @Override
        public String getDisplayName() {
            return "Project References";// NOI18N
        }

        @Override
        public void dumpInfo(Lookup context, PrintWriter printOut) {
            Collection<CsmProject> projects = new ArrayList<CsmProject>(context.lookupAll(CsmProject.class));
            if (projects.isEmpty()) {
                CsmFile file = context.lookup(CsmFile.class);
                if (file != null) {
                    CsmProject project = file.getProject();
                    if (project != null) {
                        projects.add(project);
                    }
                }
            }
            printOut.println("References:"); // NOI18N
            for (CsmProject prj : projects) {
                printOut.print(prj.getName() + " : "); // NOI18N
                int refsNumber = 0;
                for (CsmFile file : prj.getAllFiles()) {
                    refsNumber += ((FileImpl)file).getReferences().size();
                }
                printOut.println(refsNumber);
            }
        }
    }    
}
