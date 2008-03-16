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
import com.sun.tools.javac.code.Kinds;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.comp.TransTypes;
import com.sun.tools.javac.model.LazyTreeLoader;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Log;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
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
import org.netbeans.modules.java.source.usages.SymbolDumper;
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
    
    private static final Logger LOGGER = Logger.getLogger(TreeLoader.class.getName());

    private Context context;
    private ClasspathInfo cpInfo;
    private Map<ClassSymbol, StringBuilder> couplingErrors;

    private TreeLoader(Context context, ClasspathInfo cpInfo) {
        this.context = context;
        this.cpInfo = cpInfo;
    }
    
    @Override
    public boolean loadTreeFor(final ClassSymbol clazz) {
        assert JavaSourceAccessor.getINSTANCE().isJavaCompilerLocked();
        
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
                        dumpSymFile(clazz);
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

    @Override
    public void couplingError(ClassSymbol clazz, Tree t) {
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

    private void dumpSymFile(ClassSymbol clazz) throws IOException {
        PrintWriter writer = null;
        File outputFile = null;
        boolean deleteResult = false;
        try {
            JavaFileManager fm = ClasspathInfoAccessor.getINSTANCE().getFileManager(cpInfo);
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
            String pkg, name;
            index = binaryName.lastIndexOf('.');
            if (index < 0) {
                pkg = null;
                name = binaryName;
            } else {
                pkg = binaryName.substring(0, index);
                assert binaryName.length() > index;
                name = binaryName.substring(index + 1);
            }
            if (pkg != null) {
                classes = new File(classes, pkg.replace('.', File.separatorChar));
                if (!classes.exists())
                    classes.mkdirs();
            }
            outputFile = new File(classes, name + '.' + FileObjects.SIG);
            if (outputFile.exists())
                return ;//no point in dumping again already existing sig file
            deleteResult = true;
            writer = new PrintWriter(outputFile, "UTF-8");
            Symbol owner;
            if (clazz.owner.kind == Kinds.PCK) {
                owner = null;
            }
            else if (clazz.owner.kind == Kinds.VAR) {
                owner = clazz.owner.owner;
            }
            else {
                owner = clazz.owner;
            }
            deleteResult = SymbolDumper.dump(writer, Types.instance(context), TransTypes.instance(context), clazz, owner);
        } finally {
            if (writer != null)
                writer.close();
            if (deleteResult) {
                assert outputFile != null;
                
                outputFile.delete();
            }
        }
    }
}
