/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.cnd.cncppunit.editor.filecreation;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.toolchain.AbstractCompiler;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.api.toolchain.PredefinedToolKind;
import org.netbeans.modules.cnd.cncppunit.LibraryChecker;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;
import org.openide.util.RequestProcessor;

/**
 * @author Alexey Vladykin
 */
/*package*/ class TestLibChecker implements Runnable {

    private static final RequestProcessor RP =
            new RequestProcessor(TestLibChecker.class.getSimpleName(), 3);

    private static final Map<TestLibChecker, RequestProcessor.Task> CHECKERS =
            new HashMap<TestLibChecker, RequestProcessor.Task>();

    private final String lib;
    private final AbstractCompiler compiler;
    private final ChangeListener listener;

    private TestLibChecker(String lib, AbstractCompiler compiler, ChangeListener listener) {
        this.lib = lib;
        this.compiler = compiler;
        this.listener = listener;
    }

    @Override
    public void run() {
        ProgressHandle progressHandle = ProgressHandleFactory.createHandle(
                NbBundle.getMessage(TestLibChecker.class, "MSG_Checking_Library", lib, compiler.getExecutionEnvironment())); // NOI18N
        progressHandle.start();
        boolean result = false;
        try {
            Thread.sleep(10000);
            result = LibraryChecker.isLibraryAvailable(lib, compiler);
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
        } finally {
            progressHandle.finish();
            synchronized (CHECKERS) {
                CHECKERS.remove(this);
                if (listener != null) {
                    listener.stateChanged(new LibCheckerChangeEvent(this, result));
                }
            }
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TestLibChecker)) {
            return false;
        }
        final TestLibChecker that = (TestLibChecker) obj;
        return this.lib.equals(that.lib)
                && this.compiler == that.compiler
                && this.listener == that.listener;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + lib.hashCode();
        hash = 67 * hash + compiler.hashCode();
        hash = 67 * hash + (this.listener != null ? this.listener.hashCode() : 0);
        return hash;
    }

    private static CompilerSet getCompilerSet(Project project) {
        ConfigurationDescriptorProvider cdp = project.getLookup().lookup(ConfigurationDescriptorProvider.class);
        MakeConfiguration makeConfiguration = (MakeConfiguration) cdp.getConfigurationDescriptor().getConfs().getActive();
        return makeConfiguration == null? null : makeConfiguration.getCompilerSet().getCompilerSet();
    }

    /*package*/ static AbstractCompiler getCCompiler(Project project) {
        CompilerSet compilerSet = getCompilerSet(project);
        return compilerSet == null? null : (AbstractCompiler) compilerSet.getTool(PredefinedToolKind.CCompiler);
    }

    /*package*/ static AbstractCompiler getCppCompiler(Project project) {
        CompilerSet compilerSet = getCompilerSet(project);
        return compilerSet == null? null : (AbstractCompiler) compilerSet.getTool(PredefinedToolKind.CCCompiler);
    }

    /*package*/ static RequestProcessor.Task asyncCheck(String lib, AbstractCompiler compiler, ChangeListener listener) {
        Parameters.notWhitespace("lib", lib); // NOI18N
        Parameters.notNull("compiler", compiler); // NOI18N
        synchronized (CHECKERS) {
            TestLibChecker checker = new TestLibChecker(lib, compiler, listener);
            RequestProcessor.Task task = CHECKERS.get(checker);
            if (task == null) {
                task = RP.post(checker);
                CHECKERS.put(checker, task);
            }
            return task;
        }
    }

    public static final class LibCheckerChangeEvent extends ChangeEvent {

        private static final long serialVersionUID = -7102294055630832274L;

        private final boolean result;

        public LibCheckerChangeEvent(Object source, boolean result) {
            super(source);
            this.result = result;
        }

        public boolean getResult() {
            return result;
        }
    }
}
