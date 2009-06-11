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
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Delete;
import org.apache.tools.ant.taskdefs.Javac;
import org.apache.tools.ant.taskdefs.compilers.CompilerAdapter;
import org.apache.tools.ant.taskdefs.compilers.DefaultCompilerAdapter;
import org.apache.tools.ant.types.Commandline;
import org.apache.tools.ant.types.FileSet;
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

    private Path processorPath;
    public void addProcessorPath(Path cp) {
        processorPath = cp;
    }

    private boolean usingExplicitIncludes;
    @Override
    public void setIncludes(String includes) {
        super.setIncludes(includes);
        usingExplicitIncludes = true;
    }

    @Override
    public void execute() throws BuildException {
        if (!usingExplicitIncludes) {
            cleanUpStaleClasses();
        }
        super.execute();
    }

    @Override
    protected void compile() {
        if (processorPath != null && processorPath.size() > 0) {
            createCompilerArg().setValue("-processorpath");
            createCompilerArg().setPath(processorPath);
        }
        createCompilerArg().setValue("-implicit:class");
        String specifiedCompiler = getProject().getProperty("build.compiler");
        if (specifiedCompiler != null) {
            if (specifiedCompiler.equals("extJavac")) {
                log("JSR 269 not found, loading from " + javacClasspath);
                createCompilerArg().setValue("-J-Xbootclasspath/p:" + javacClasspath);
            } else {
                log("Warning: build.compiler=" + specifiedCompiler + " so JSR 269 annotation processing may not work", Project.MSG_WARN);
            }
            super.compile();
            return;
        }
        try {
            Class.forName("javax.tools.JavaCompiler");
            // Fine, have 269 in process, proceed...
            super.compile();
        } catch (ClassNotFoundException x) {
            log("JSR 269 not found, loading from " + javacClasspath);
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
    }

    /**
     * See issue #166888. If there are any existing class files with no matching
     * source file, assume this is an incremental build and the source file has
     * since been deleted. In that case, delete the whole classes dir. (Could
     * merely delete the stale class file, but if an annotation processor also
     * created associated resources, these may be stale too. Kill them all and
     * let JSR 269 sort it out.)
     */
    private void cleanUpStaleClasses() {
        File d = getDestdir();
        if (!d.isDirectory()) {
            return;
        }
        String[] _sources = getSrcdir().list();
        File[] sources = new File[_sources.length];
        for (int i = 0; i < _sources.length; i++) {
            sources[i] = new File(_sources[i]);
        }
        FileSet classes = new FileSet();
        classes.setDir(d);
        classes.setIncludes("**/*.class");
        classes.setExcludes("**/*$*.class");
        for (String clazz : classes.getDirectoryScanner(getProject()).getIncludedFiles()) {
            String java = clazz.substring(0, clazz.length() - ".class".length()) + ".java";
            boolean found = false;
            for (File source : sources) {
                if (new File(source, java).isFile()) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                // XXX might be a false negative in case this was a nonpublic outer class
                // (could check for "ClassName.java" in bytecode to see)
                log(new File(d, clazz) + " appears to be stale, rebuilding all module sources", Project.MSG_WARN);
                Delete delete = new Delete();
                delete.setProject(getProject());
                delete.setOwningTarget(getOwningTarget());
                delete.setLocation(getLocation());
                FileSet deletables = new FileSet();
                deletables.setDir(d);
                delete.addFileset(deletables);
                delete.init();
                delete.execute();
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

    @Override
    public void setFork(boolean f) {
        throw new UnsupportedOperationException();
    }

    private static class CustomAdapter extends DefaultCompilerAdapter {

        public boolean execute() throws BuildException {
            // adapted from Javac13
            Commandline cmd = setupModernJavacCommand();
            try {
                Path cp = ((CustomJavac) getJavac()).javacClasspath;
                AntClassLoader cl = new AntClassLoader(getJavac().getProject(), cp, false) {
                    public @Override Enumeration<URL> getResources(String name) throws IOException {
                        // #158934 - ACL.gR is unimplemented.
                        if (name.equals("META-INF/services/javax.annotation.processing.Processor")) {
                            return Collections.enumeration(Collections.<URL>emptySet());
                        } else {
                            return super.getResources(name);
                        }
                    }
                };
                cl.setIsolated(true); // otherwise RB.gB("c.s.t.j.r.compiler") -> tools.jar's compiler.class despite our compiler.properties
                Class<?> c = Class.forName("com.sun.tools.javac.Main", true, cl);
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
