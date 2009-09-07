/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.api.execution;

import org.netbeans.modules.cnd.spi.compilers.NativeExecutionProvider;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.logging.Logger;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.util.Lookup;

/**
 * This class provides the getNativeExecution factory. It shouldn't be directy instantiated outside
 * of the getNativeExecution() method.
 * 
 * @author gordonp
 * @deprecated  Use {@link @org-netbeans-modules-nativexecution@} instead
 */
public abstract class NativeExecution {

    protected static final Logger log = Logger.getLogger("cnd.execution.logger"); // NOI18N

    /** Gets a NativeExecution instance for the given host */
    public static NativeExecution getDefault(ExecutionEnvironment execEnv) {
        if (execEnv != null) {
            for(NativeExecutionProvider provider : Lookup.getDefault().lookupAll(NativeExecutionProvider.class)) {
                if (provider.isApplicable(execEnv)) {
                    final NativeExecution nativeExecution = provider.getNativeExecution(execEnv);
                    assert nativeExecution != null;
                    return nativeExecution;
                }
            }
        }
        throw new IllegalStateException("No NativeExecution found for " + execEnv); //NOI18N
    }


    /**
     * Execute an executable, a makefile, or a script
     * @param runDir absolute path to directory from where the command should be executed
     * @param executable absolute or relative path to executable, makefile, or script
     * @param arguments space separated list of arguments
     * @param envp environment variables (name-value pairs of the form ABC=123)
     * @param out Output
     * @param io Input
     * @param unbuffer - true if stdout unbuffering is needed
     * @return completion code
     */
    public abstract int executeCommand(
            File runDirFile,
            String executable,
            String arguments,
            String[] envp,
            PrintWriter out,
            Reader in,
            boolean unbuffer) throws IOException, InterruptedException;

    public abstract void stop();
}
