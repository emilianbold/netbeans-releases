/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.hints.jackpot.spi;

import com.sun.source.tree.BlockTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Scope;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePathScanner;
import com.sun.tools.javac.api.JavacTaskImpl;
import com.sun.tools.javac.main.JavaCompiler;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCErroneous;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Log;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileManager.Location;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.ClassPath.Entry;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.source.JavaSourceAccessor;
import org.netbeans.modules.java.source.parsing.FileObjects;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 *
 * @author lahvac
 */
public class Hacks {

    //XXX: copied from Utilities, for declarative hints, different import management:
    private static long inc;

    public static Scope constructScope(CompilationInfo info, String... importedClasses) {
        StringBuilder clazz = new StringBuilder();

        clazz.append("package $$;\n");

        for (String i : importedClasses) {
            clazz.append("import ").append(i).append(";\n");
        }

        clazz.append("public class $").append(inc++).append("{");

        clazz.append("private void test() {\n");
        clazz.append("}\n");
        clazz.append("}\n");

        JavacTaskImpl jti = JavaSourceAccessor.getINSTANCE().getJavacTask(info);
        Context context = jti.getContext();

        JavaCompiler jc = JavaCompiler.instance(context);
        Log.instance(context).nerrors = 0;

        JavaFileObject jfo = FileObjects.memoryFileObject("$$", "$", new File("/tmp/t.java").toURI(), System.currentTimeMillis(), clazz.toString());
        boolean oldSkipAPs = jc.skipAnnotationProcessing;

        try {
            jc.skipAnnotationProcessing = true;

            Iterable<? extends CompilationUnitTree> parsed = jti.parse(jfo);
            CompilationUnitTree cut = parsed.iterator().next();

            jti.analyze(jti.enter(parsed));

            return new ScannerImpl().scan(cut, info);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        } finally {
            jc.skipAnnotationProcessing = oldSkipAPs;
        }
    }

    private static final class ScannerImpl extends TreePathScanner<Scope, CompilationInfo> {

        @Override
        public Scope visitBlock(BlockTree node, CompilationInfo p) {
            return p.getTrees().getScope(getCurrentPath());
        }

        @Override
        public Scope visitMethod(MethodTree node, CompilationInfo p) {
            if (node.getReturnType() == null) {
                return null;
            }
            return super.visitMethod(node, p);
        }

        @Override
        public Scope reduce(Scope r1, Scope r2) {
            return r1 != null ? r1 : r2;
        }

    }

    private static final String SOURCE_LEVEL = "1.5"; //TODO: could be possibly inferred from the current Java platform

    public static Map<String, byte[]> compile(ClassPath boot, ClassPath compile, final String code) throws IOException {
        StandardJavaFileManager sjfm = ToolProvider.getSystemJavaCompiler().getStandardFileManager(null, null, null);

        sjfm.setLocation(StandardLocation.PLATFORM_CLASS_PATH, toFiles(boot));
        sjfm.setLocation(StandardLocation.CLASS_PATH, toFiles(compile));

        final Map<String, ByteArrayOutputStream> class2BAOS = new HashMap<String, ByteArrayOutputStream>();

        JavaFileManager jfm = new ForwardingJavaFileManager<JavaFileManager>(sjfm) {
            @Override
            public JavaFileObject getJavaFileForOutput(Location location, String className, Kind kind, FileObject sibling) throws IOException {
                final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                
                class2BAOS.put(className, buffer);
                return new SimpleJavaFileObject(sibling.toUri(), kind) {
                    @Override
                    public OutputStream openOutputStream() throws IOException {
                        return buffer;
                    }
                };
            }
        };

        JavaFileObject file = new SimpleJavaFileObject(URI.create("mem://mem"), Kind.SOURCE) {
            @Override
            public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
                return code;
            }
        };
        ToolProvider.getSystemJavaCompiler().getTask(null, jfm, null, /*XXX:*/Arrays.asList("-source", SOURCE_LEVEL, "-target", SOURCE_LEVEL), null, Arrays.asList(file)).call();

        Map<String, byte[]> result = new HashMap<String, byte[]>();

        for (Map.Entry<String, ByteArrayOutputStream> e : class2BAOS.entrySet()) {
            result.put(e.getKey(), e.getValue().toByteArray());
        }

        return result;
    }

    private static Iterable<? extends File> toFiles(ClassPath cp) {
        List<File> result = new LinkedList<File>();

        for (Entry e : cp.entries()) {
            File f = FileUtil.archiveOrDirForURL(e.getURL());

            if (f == null) {
                Logger.getLogger(Hacks.class.getName()).log(Level.INFO, "file == null, url={0}", e.getURL());
                continue;
            }

            result.add(f);
        }

        return result;
    }


    public static Tree createRenameTree(@NonNull Tree originalTree, @NonNull String newName) {
        return new RenameTree(originalTree, newName);
    }

    static final class RenameTree extends JCErroneous {

        final Tree originalTree;
        final String newName;

        public RenameTree(@NonNull Tree originalTree, @NonNull String newName) {
            super(com.sun.tools.javac.util.List.<JCTree>nil());
            this.originalTree = originalTree;
            this.newName = newName;
        }

    }

    public static @CheckForNull TypeMirror parseFQNType(@NonNull CompilationInfo info, @NonNull String spec) {
        TypeElement jlObject = info.getElements().getTypeElement("java.lang.Object");
        
        //XXX:
        TypeElement scope;

        if (info.getTopLevelElements().isEmpty()) {
            scope = jlObject;
        } else {
            scope = info.getTopLevelElements().iterator().next();
        }
        //XXX end
        
        return info.getTreeUtilities().parseType(spec, /*XXX: jlObject*/scope);
    }
}
