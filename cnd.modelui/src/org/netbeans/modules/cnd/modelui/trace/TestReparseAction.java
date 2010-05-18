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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.modelui.trace;

import java.io.IOException;
import java.util.Collection;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmInclude;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.ProjectBase;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputEvent;
import org.openide.windows.OutputListener;
import org.openide.windows.OutputWriter;
        
/**
 * A test action that reparses the given project 
 * and redirects error output to the output pane
 * 
 * @author Vladimir Kvashin
 */
public class TestReparseAction extends TestProjectActionBase {

    private static boolean running = false;
    
    @Override
    public String getName() {
        return NbBundle.getMessage(getClass(), "CTL_TestProjectReparse"); //NOI18N
    }

    
    @Override
    protected void performAction(Collection<CsmProject> csmProjects) {
        if (csmProjects != null && !csmProjects.isEmpty()) {
            testReparse(csmProjects);
        }
    }

    private void testReparse(Collection<CsmProject> projects) {
        for (CsmProject p : projects) {
            testReparse((ProjectBase) p);
        }
    }
    
    
    private static class ErrorInfo {
        public final int line;
        public final int column;
        public final String text;
        public ErrorInfo(int line, int column, String text) {
            this.line = line;
            this.column = column;
            this.text = text;
        }
    }
    
    private void testReparse(ProjectBase project) {
        
        String task = "Parser Errors " + project.getName(); // NOI18N
        
        final ProgressHandle handle = ProgressHandleFactory.createHandle(task);
        handle.start();
        handle.switchToDeterminate(project.getAllFiles().size());
        int handled = 0;
        
        InputOutput io = IOProvider.getDefault().getIO(task, false);
        io.select();
        
        for( CsmFile file : project.getSourceFiles() ) {
            handle.progress("Parsing " + file.getName(), handled++); // NOI18N
            testReparse((FileImpl) file, io.getOut());
        }
        for( CsmFile file : project.getHeaderFiles() ) {
            handle.progress("Parsing " + file.getName(), handled++); // NOI18N
            testReparse((FileImpl) file, io.getOut());
        }
        
        handle.finish();
    }
    
    private void testReparse(final FileImpl fileImpl, final OutputWriter out) {
        for (CsmInclude include : fileImpl.getIncludes()) {
            if (include.getIncludeFile() == null) {
                int line = include.getStartPosition().getLine();
                int column = include.getStartPosition().getColumn();
                char lBracket = include.isSystem() ? '<' : '"'; //NOI18N
                char rBracket = include.isSystem() ? '>' : '"'; //NOI18N
                printError(out, fileImpl, line, column, "Unresolved include: " + lBracket + include.getIncludeName() + rBracket); //NOI18N
            }
        }
        fileImpl.getErrors(new FileImpl.ErrorListener() {
            public void error(String text, int line, int column) {
                printError(out, fileImpl, line, column, text);
            }
        });
    }

    private void printError(OutputWriter out, CsmFile fileImpl, int line, int column, String text) {
        ErrorInfo info = new ErrorInfo(line, column, text);
        text = fileImpl.getAbsolutePath().toString() + ':' + info.line + ':' + info.column + ": " + info.text; //NOI18N
        try {
            out.println(text, new MyOutputListener(fileImpl, info));
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    private static class MyOutputListener implements OutputListener {
        
        private CsmFile file;
        private ErrorInfo info;

        public MyOutputListener(CsmFile file, ErrorInfo info) {
            this.file = file;
            this.info = info;
        }
        
        public void outputLineAction(OutputEvent ev) {
            CsmUtilities.openSource(file, info.line, info.column);
        }
        
        public void outputLineSelected(OutputEvent ev) {}
        public void outputLineCleared(OutputEvent ev) {}
    }
    
    
}
