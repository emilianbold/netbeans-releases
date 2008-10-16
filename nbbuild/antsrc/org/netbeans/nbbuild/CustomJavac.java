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

package org.netbeans.nbbuild;

import java.io.File;
import java.lang.reflect.Method;
import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Javac;
import org.apache.tools.ant.taskdefs.compilers.CompilerAdapter;
import org.apache.tools.ant.taskdefs.compilers.DefaultCompilerAdapter;
import org.apache.tools.ant.types.Commandline;
import org.apache.tools.ant.types.Path;

/**
 * Runs javac in-process from a local implementation, e.g. so as to ensure JSR 269 is supported.
 * Works just like using the default <code>modern</code> compiler, except loaded from the specified place.
 */
public class CustomJavac extends Javac {

    public CustomJavac() {}

    private Path javacClasspath;
    public void addJavacClasspath(Path cp) {
        javacClasspath = cp;
    }

    @Override
    protected void compile() {
        String specifiedCompiler = getProject().getProperty("build.compiler");
        if (specifiedCompiler != null) {
            log("Warning: build.compiler=" + specifiedCompiler + " so disabling JSR 269 annotation processing", Project.MSG_WARN);
            super.compile();
            return;
        }
        if (compileList.length > 0) {
            log("Compiling " + compileList.length + " source file" +
                    (compileList.length == 1 ? "" : "s") +
                    (getDestdir() != null ? " to " + getDestdir() : ""));
            if (listFiles) {
                for (File f : compileList) {
                    log(f.getAbsolutePath());
                }
            }
            CompilerAdapter adapter = new CustomAdapter();
            adapter.setJavac(this);
            if (adapter.execute()) {
                // XXX updateDirList
            } else {
                // other modes not supported, see below
                throw new BuildException("Compile failed; see the compiler error output for details.", getLocation());
            }
        }
    }

    @Override
    public void setErrorProperty(String errorProperty) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setUpdatedProperty(String updatedProperty) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setFailonerror(boolean fail) {
        throw new UnsupportedOperationException();
    }


    private static class CustomAdapter extends DefaultCompilerAdapter {

        public boolean execute() throws BuildException {
            // adapted from Javac13
            Commandline cmd = setupModernJavacCommand();
            try {
                Path cp = ((CustomJavac) getJavac()).javacClasspath;
                ClassLoader cl = new AntClassLoader(getJavac().getProject(), cp);
                Class c = Class.forName("com.sun.tools.javac.Main", true, cl);
                getJavac().log("Running javac from " + c.getProtectionDomain().getCodeSource().getLocation(), Project.MSG_VERBOSE);
                Method compile = c.getMethod("compile", String[].class);
                int result = (Integer) compile.invoke(null, (Object) cmd.getArguments());
                return result == 0;
            } catch (Exception ex) {
                throw new BuildException("Error starting compiler: " + ex, ex, location);
            }
        }

    }

}
