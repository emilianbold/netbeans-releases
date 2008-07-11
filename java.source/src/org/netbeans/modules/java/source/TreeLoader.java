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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.java.source;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import com.sun.tools.javac.api.JavacTaskImpl;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.comp.AttrContext;
import com.sun.tools.javac.comp.Enter;
import com.sun.tools.javac.comp.Env;
import com.sun.tools.javac.model.LazyTreeLoader;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCBlock;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.tree.TreeScanner;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.CouplingAbort;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Log;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.modules.java.source.parsing.FileObjects;
import org.netbeans.modules.java.source.usages.ClasspathInfoAccessor;
import org.netbeans.modules.java.source.usages.Index;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Dusan Balek
 */
public class TreeLoader extends LazyTreeLoader {

    public static void preRegister(final Context context, final ClasspathInfo cpInfo) {
        context.put(lazyTreeLoaderKey, new TreeLoader(context, cpInfo));
    }
    
    public static TreeLoader instance (final Context ctx) {
        final LazyTreeLoader tl = LazyTreeLoader.instance(ctx);
        return (tl instanceof TreeLoader) ? (TreeLoader)tl : null;
    }
    
    private static final Logger LOGGER = Logger.getLogger(TreeLoader.class.getName());
    public  static boolean DISABLE_CONFINEMENT_TEST = false; //Only for tests!

    private Context context;
    private ClasspathInfo cpInfo;
    private Map<ClassSymbol, StringBuilder> couplingErrors;
    private boolean partialReparse;

    private TreeLoader(Context context, ClasspathInfo cpInfo) {
        this.context = context;
        this.cpInfo = cpInfo;
    }
    
    @Override
    public boolean loadTreeFor(final ClassSymbol clazz, boolean persist) {
        assert DISABLE_CONFINEMENT_TEST || JavaSourceAccessor.getINSTANCE().isJavaCompilerLocked();
        
        if (clazz != null) {
            try {
                FileObject fo = SourceUtils.getFile(clazz, cpInfo);                
                JavacTaskImpl jti = context.get(JavacTaskImpl.class);
                if (fo != null && jti != null) {
                    Log.instance(context).nerrors = 0;
                    JavaFileObject jfo = FileObjects.nbFileObject(fo, null);
                    Map<ClassSymbol, StringBuilder> oldCouplingErrors = couplingErrors;
                    try {
                        couplingErrors = new HashMap<ClassSymbol, StringBuilder>();
                        jti.analyze(jti.enter(jti.parse(jfo)));
                        if (persist)
                            dumpSymFile(jti, clazz);
                        return true;
                    } finally {
                        for (Map.Entry<ClassSymbol, StringBuilder> e : couplingErrors.entrySet()) {
                            logCouplingError(e.getKey(), e.getValue().toString());
                        }
                        couplingErrors = oldCouplingErrors;
                    }
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return false;
    }
    
    public final void startPartialReparse () {
        this.partialReparse = true;
    }
    
    public final void endPartialReparse () {
        this.partialReparse = false;
    }

    @Override
    public void couplingError(ClassSymbol clazz, Tree t) {
        if (this.partialReparse) {
            throw new CouplingAbort(clazz.classfile, t);
        }
        StringBuilder info = new StringBuilder("\n"); //NOI18N
        switch (t.getKind()) {
            case CLASS:
                info.append("CLASS: ").append(((ClassTree)t).getSimpleName().toString()); //NOI18N
                break;
            case VARIABLE:
                info.append("VARIABLE: ").append(((VariableTree)t).getName().toString()); //NOI18N
                break;
            case METHOD:
                info.append("METHOD: ").append(((MethodTree)t).getName().toString()); //NOI18N
                break;
            case TYPE_PARAMETER:
                info.append("TYPE_PARAMETER: ").append(((TypeParameterTree)t).getName().toString()); //NOI18N
                break;
            default:
                info.append("TREE: <unknown>"); //NOI18N
                break;
        }
        if (clazz != null && couplingErrors != null) {
            StringBuilder sb = couplingErrors.get(clazz);            
            if (sb != null)
                sb.append(info);
            else
                couplingErrors.put(clazz, info);
        } else {
            logCouplingError(clazz, info.toString());
        }
    }
    
    private void logCouplingError(ClassSymbol clazz, String info) {
        JavaFileObject classFile = clazz != null ? clazz.classfile : null;
        String cfURI = classFile != null ? classFile.toUri().toASCIIString() : "<unknown>"; //NOI18N
        JavaFileObject sourceFile = clazz != null ? clazz.sourcefile : null;
        String sfURI = classFile != null ? sourceFile.toUri().toASCIIString() : "<unknown>"; //NOI18N
        LOGGER.log(Level.WARNING, "Coupling error:\nclass file: {0}\nsource file: {1}{2}\n", new Object[] {cfURI, sfURI, info});
    }

    private void dumpSymFile(JavacTaskImpl jti, ClassSymbol clazz) throws IOException {
        Env<AttrContext> env = Enter.instance(context).getEnv(clazz);
        if (env == null)
            return;
        new TreeScanner() {
            @Override
            public void visitMethodDef(JCMethodDecl tree) {
                super.visitMethodDef(tree);
                tree.body = null;
            }
            @Override
            public void visitVarDef(JCVariableDecl tree) {
                super.visitVarDef(tree);
                tree.init = null;
            }
            @Override
            public void visitClassDef(JCClassDecl tree) {
                scan(tree.mods);
                scan(tree.typarams);
                scan(tree.extending);
                scan(tree.implementing);
                if (tree.defs != null) {
                    List<JCTree> prev = null;
                    for (List<JCTree> l = tree.defs; l.nonEmpty(); l = l.tail) {
                        scan(l.head);
                        if (l.head.getTag() == JCTree.BLOCK && ((JCBlock)l.head).isStatic()) {
                            if (prev != null)
                                prev.tail = l.tail;
                            else
                                tree.defs = l.tail;
                        }
                        prev = l;
                    }
                }
            }
        }.scan(env.toplevel);
        JavaFileManager fm = ClasspathInfoAccessor.getINSTANCE().getFileManager(cpInfo);
        try {
            String binaryName = null;
            if (clazz.classfile != null) {
                binaryName = fm.inferBinaryName(StandardLocation.PLATFORM_CLASS_PATH, clazz.classfile);
                if (binaryName == null)
                    binaryName = fm.inferBinaryName(StandardLocation.CLASS_PATH, clazz.classfile);                
            }
            else if (clazz.sourcefile != null) {
                binaryName = fm.inferBinaryName(StandardLocation.SOURCE_PATH, clazz.sourcefile);
            }
            if (binaryName == null) {
                return;
            }
            String surl = clazz.classfile.toUri().toURL().toExternalForm();
            int index = surl.lastIndexOf(FileObjects.convertPackage2Folder(binaryName));
            assert index > 0;
            File classes = Index.getClassFolder(new URL(surl.substring(0, index)));
            fm.handleOption("output-root", Collections.singletonList(classes.getPath()).iterator()); //NOI18N
            jti.generate(Collections.singletonList(clazz));
        } finally {
            fm.handleOption("output-root", Collections.singletonList("").iterator()); //NOI18N
        }
    }
}
