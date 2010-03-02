/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
import com.sun.tools.javac.code.Scope;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.code.Symbol.MethodSymbol;
import com.sun.tools.javac.code.Symbol.VarSymbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.comp.AttrContext;
import com.sun.tools.javac.comp.Enter;
import com.sun.tools.javac.comp.Env;
import com.sun.tools.javac.main.JavaCompiler;
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.ChangedCharSetException;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTML.Tag;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.HTMLEditorKit.ParserCallback;
import javax.swing.text.html.parser.ParserDelegator;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.modules.java.source.indexing.JavaIndex;
import org.netbeans.modules.java.source.parsing.FileObjects;
import org.netbeans.modules.java.source.parsing.OutputFileManager.InvalidSourcePath;
import org.netbeans.modules.java.source.usages.ClasspathInfoAccessor;
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
    public  static boolean DISABLE_ARTIFICAL_PARAMETER_NAMES = false; //Only for tests!

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
                JavaCompiler jc = JavaCompiler.instance(context);
                if (fo != null && jti != null) {
                    final Log log = Log.instance(context);
                    log.nerrors = 0;
                    JavaFileObject jfo = FileObjects.nbFileObject(fo, null);
                    Map<ClassSymbol, StringBuilder> oldCouplingErrors = couplingErrors;
                    boolean oldSkipAPT = jc.skipAnnotationProcessing;
                    try {
                        couplingErrors = new HashMap<ClassSymbol, StringBuilder>();
                        jc.skipAnnotationProcessing = true;
                        jti.analyze(jti.enter(jti.parse(jfo)));
                        if (persist)
                            dumpSymFile(ClasspathInfoAccessor.getINSTANCE().getFileManager(cpInfo), jti, clazz);
                        return true;
                    } finally {
                        jc.skipAnnotationProcessing = oldSkipAPT;
                        log.nerrors = 0;
                        for (Map.Entry<ClassSymbol, StringBuilder> e : couplingErrors.entrySet()) {
                            dumpCouplingAbort(new CouplingAbort(e.getKey(), null), e.getValue().toString());
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
    public boolean loadParamNames(ClassSymbol clazz) {
        assert DISABLE_CONFINEMENT_TEST || JavaSourceAccessor.getINSTANCE().isJavaCompilerLocked();
        if (clazz != null) {
            URL url = SourceUtils.getJavadoc(clazz, cpInfo);
            if (url != null) {
                if (getParamNamesFromJavadocText(url, clazz)) {
                    return true;
                }
            }
            if (!DISABLE_ARTIFICAL_PARAMETER_NAMES) {
                fillArtificalParamNames(clazz);
                return true;
            }
        }
        
        return false;
    }

    @Override
    public void couplingError(ClassSymbol clazz, Tree t) {
        if (this.partialReparse) {
            super.couplingError(clazz, t);
        }
        if (clazz != null && couplingErrors != null) {
            StringBuilder sb = couplingErrors.get(clazz);            
            if (sb != null)
                sb.append(getTreeInfo(t));
            else
                couplingErrors.put(clazz, getTreeInfo(t));
        } else {
            dumpCouplingAbort(new CouplingAbort(clazz, t), null);
        }
    }

    @Override
    public void updateContext(Context context) {
        this.context = context;
    }

    public final void startPartialReparse () {
        this.partialReparse = true;
    }

    public final void endPartialReparse () {
        this.partialReparse = false;
    }

    public static void dumpSymFile(JavaFileManager jfm, JavacTaskImpl jti, ClassSymbol clazz) throws IOException {
        Env<AttrContext> env = Enter.instance(jti.getContext()).getEnv(clazz);
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
        try {
            String binaryName = null;
            String surl = null;
            if (clazz.classfile != null) {
                binaryName = jfm.inferBinaryName(StandardLocation.PLATFORM_CLASS_PATH, clazz.classfile);
                if (binaryName == null)
                    binaryName = jfm.inferBinaryName(StandardLocation.CLASS_PATH, clazz.classfile);
                surl = clazz.classfile.toUri().toURL().toExternalForm();
            }
            else if (clazz.sourcefile != null) {
                binaryName = jfm.inferBinaryName(StandardLocation.SOURCE_PATH, clazz.sourcefile);
                surl = clazz.sourcefile.toUri().toURL().toExternalForm();
            }
            if (binaryName == null || surl == null) {
                return;
            }
            int index = surl.lastIndexOf(FileObjects.convertPackage2Folder(binaryName));
            assert index > 0;
            File classes = JavaIndex.getClassFolder(new URL(surl.substring(0, index)));
            jfm.handleOption("output-root", Collections.singletonList(classes.getPath()).iterator()); //NOI18N
            jti.generate(Collections.singletonList(clazz));
        } catch (InvalidSourcePath isp) {
            LOGGER.log(Level.INFO, "InvalidSourcePath reported when writing sym file for class: {0}", clazz.flatname); // NOI18N
        } finally {
            jfm.handleOption("output-root", Collections.singletonList("").iterator()); //NOI18N
        }
    }

    private static final int MAX_DUMPS = Integer.getInteger("org.netbeans.modules.java.source.parsing.JavacParser.maxDumps", 255);
    
    public static void dumpCouplingAbort(CouplingAbort couplingAbort, String treeInfo) {
        if (treeInfo == null)
            treeInfo = getTreeInfo(couplingAbort.getTree()).toString();
        String dumpDir = System.getProperty("netbeans.user") + "/var/log/"; //NOI18N
        JavaFileObject classFile = couplingAbort.getClassFile();
        String cfURI = classFile != null ? classFile.toUri().toASCIIString() : "<unknown>"; //NOI18N
        JavaFileObject sourceFile = couplingAbort.getSourceFile();
        String sfURI = sourceFile != null ? sourceFile.toUri().toASCIIString() : "<unknown>"; //NOI18N
        String origName = classFile != null ? classFile.getName() : "unknown"; //NOI18N
        File f = new File(dumpDir + origName + ".dump"); // NOI18N
        boolean dumpSucceeded = false;
        int i = 1;
        while (i < MAX_DUMPS) {
            if (!f.exists())
                break;
            f = new File(dumpDir + origName + '_' + i + ".dump"); // NOI18N
            i++;
        }
        if (!f.exists()) {
            try {
                f.getParentFile().mkdirs();
                OutputStream os = new FileOutputStream(f);
                PrintWriter writer = new PrintWriter(new OutputStreamWriter(os, "UTF-8")); // NOI18N
                try {
                    writer.println("Coupling error:"); //NOI18N
                    writer.println(String.format("class file %s", cfURI)); //NOI18N
                    writer.println(String.format("source file %s", sfURI)); //NOI18N
                    writer.println("----- Source file content: ----------------------------------------"); // NOI18N
                    if (sourceFile != null)
                        writer.println(sourceFile.getCharContent(true));
                    else
                        writer.println("<unknown>");
                    writer.print("----- Trees: -------------------------------------------------------"); // NOI18N
                    writer.println(treeInfo);
                    writer.println("----- Stack trace: ---------------------------------------------"); // NOI18N
                    couplingAbort.printStackTrace(writer);
                } finally {
                    writer.close();
                    dumpSucceeded = true;
                }
            } catch (IOException ioe) {
                LOGGER.log(Level.INFO, "Error when writing coupling error dump file!", ioe); // NOI18N
            }
        }
        LOGGER.log(Level.WARNING, "Coupling error:\nclass file: {0}\nsource file: {1}{2}\n", new Object[] {cfURI, sfURI, treeInfo});
        if (!dumpSucceeded) {
            LOGGER.log(Level.WARNING,
                    "Dump could not be written. Either dump file could not " + // NOI18N
                    "be created or all dump files were already used. Please " + // NOI18N
                    "check that you have write permission to '" + dumpDir + "' and " + // NOI18N
                    "clean all *.dump files in that directory."); // NOI18N
        }
    }

    private static StringBuilder getTreeInfo(Tree t) {
        StringBuilder info = new StringBuilder("\n"); //NOI18N
        if (t != null) {
            switch (t.getKind()) {
                case CLASS:
                    info.append("CLASS: ").append(((ClassTree) t).getSimpleName().toString()); //NOI18N
                    break;
                case VARIABLE:
                    info.append("VARIABLE: ").append(((VariableTree) t).getName().toString()); //NOI18N
                    break;
                case METHOD:
                    info.append("METHOD: ").append(((MethodTree) t).getName().toString()); //NOI18N
                    break;
                case TYPE_PARAMETER:
                    info.append("TYPE_PARAMETER: ").append(((TypeParameterTree) t).getName().toString()); //NOI18N
                    break;
                default:
                    info.append("TREE: <unknown>"); //NOI18N
                    break;
            }
        }
        return info;
    }

    private boolean getParamNamesFromJavadocText(final URL url, final ClassSymbol clazz) {
        HTMLEditorKit.Parser parser;
        InputStream is = null;        
        String charset = null;
        for (;;) {
            try{
                is = url.openStream();
                Reader reader = charset == null ? new InputStreamReader(is): new InputStreamReader(is, charset);
                parser = new ParserDelegator();
                parser.parse(reader, new ParserCallback() {

                    private static final String ctor_summary_name = "constructor_summary"; //NOI18N
                    private static final String method_summary_name = "method_summary"; //NOI18N
                    private static final String ctor_detail_name = "constructor_detail"; //NOI18N
                    private static final String method_detail_name = "method_detail"; //NOI18N

                    private int state = 0; //init
                    private String signature = null;
                    private StringBuilder sb = null;

                    @Override
                    public void handleStartTag(HTML.Tag t, MutableAttributeSet a, int pos) {
                        if (t == HTML.Tag.A) {
                            String attrName = (String)a.getAttribute(HTML.Attribute.NAME);
                            if (ctor_summary_name.equals(attrName)) {
                                // we have found desired javadoc constructor info anchor
                                state = 10; //ctos open
                            } else if (method_summary_name.equals(attrName)) {
                                // we have found desired javadoc method info anchor
                                state = 20; //methods open
                            } else if (ctor_detail_name.equals(attrName)) {
                                state = 30; //end
                            } else if (method_detail_name.equals(attrName)) {
                                state = 30; //end
                            } else if (state == 12 || state == 22) {
                                String attrHref = (String)a.getAttribute(HTML.Attribute.HREF);
                                if (attrHref != null) {
                                    int idx = attrHref.indexOf('#');
                                    if (idx >= 0) {
                                        signature = attrHref.substring(idx + 1);
                                        sb = new StringBuilder();
                                    }
                                }
                            }
                        } else if (t == HTML.Tag.TR) {
                            if (state == 10 || state == 20)
                                state++;
                        } else if (t == HTML.Tag.CODE) {
                            if (state == 11 || state == 21)
                                state++;
                        }
                    }

                    @Override
                    public void handleEndTag(Tag t, int pos) {
                        if (t == HTML.Tag.CODE && (state == 12 || state == 22))
                            state--;
                    }

                    @Override
                    public void handleText(char[] data, int pos) {
                        if (signature != null && (state == 12 || state == 22))
                            sb.append(data);
                    }

                    @Override
                    public void handleSimpleTag(Tag t, MutableAttributeSet a, int pos) {
                        if (t == HTML.Tag.BR) {
                            if (state == 11) {
                                state--;
                                setParamNames(signature, sb.toString().trim(), true);
                                sb = new StringBuilder();
                            } else if (state == 21) {
                                state--;
                                setParamNames(signature, sb.toString().trim(), false);
                                sb = new StringBuilder();
                            }
                        }
                    }

                    private void setParamNames(String signature, String names, boolean isCtor) {
                        ArrayList<String> paramTypes = new ArrayList<String>();
                        int idx = -1;
                        for(int i = 0; i < signature.length(); i++) {
                            switch(signature.charAt(i)) {
                                case '(':
                                    idx = i;
                                    break;
                                case ')':
                                case ',':
                                    if (idx > -1 && idx < i - 1) {
                                        String typeName = signature.substring(idx + 1, i).trim();
                                        if (typeName.endsWith("...")) //NOI18N
                                            typeName = typeName.substring(0, typeName.length() - 3) + "[]"; //NOI18N
                                        paramTypes.add(typeName);
                                    }
                                    idx = i;
                                    break;
                            }
                        }
                        String methodName = null;
                        ArrayList<String> paramNames = new ArrayList<String>();
                        idx = -1;
                        for(int i = 0; i < names.length(); i++) {
                            switch(names.charAt(i)) {
                                case '(':
                                    methodName = names.substring(0, i);
                                    break;
                                case ')':
                                case ',':
                                    if (idx > -1) {
                                        paramNames.add(names.substring(idx + 1, i));
                                        idx = -1;
                                    }
                                    break;
                                case 160: //&nbsp;
                                    idx = i;
                                    break;
                            }
                        }
                        assert methodName != null : "Null methodName. Signature: [" + signature + "], Names: [" + names + "]";
                        assert paramTypes.size() == paramNames.size() : "Inconsistent param types/names. Signature: [" + signature + "], Names: [" + names + "]";
                        if (paramNames.size() > 0) {
                            for (Scope.Entry e = clazz.members().lookup(isCtor
                                    ? clazz.name.table.names.init
                                    : clazz.name.table.fromString(methodName)); e.scope != null; e = e.next()) {
                                if (e.sym.kind == Kinds.MTH && e.sym.owner == clazz) {
                                    MethodSymbol sym = (MethodSymbol)e.sym;
                                    List<VarSymbol> params = sym.params;
                                    if (checkParamTypes(params, paramTypes)) {
                                        for (String name : paramNames) {
                                            params.head.setName(clazz.name.table.fromString(name));
                                            params = params.tail;
                                        }
                                    }
                                }
                            }
                        }
                    }

                    private boolean checkParamTypes(List<VarSymbol> params, ArrayList<String> paramTypes) {
                        Types types = Types.instance(context);
                        for (String typeName : paramTypes) {
                            if (params.isEmpty())
                                return false;
                            Type type = params.head.type;
                            if (type.isParameterized())
                                type = types.erasure(type);
                            if (!typeName.equals(type.toString()))
                                return false;
                            params = params.tail;
                        }
                        return params.isEmpty();
                    }
                }, charset != null);
                return true;
            } catch (ChangedCharSetException e) {
                if (charset == null) {
                    charset = getCharSet(e);
                    //restart with valid charset
                } else {
                    e.printStackTrace();
                    break;
                }
            } catch(IOException ioe){
                ioe.printStackTrace();
                break;
            }finally{
                parser = null;
                if (is!=null) {
                    try{
                        is.close();
                    }catch(IOException ioe){
                        ioe.printStackTrace();
                    }
                }
            }
        }
        return false;
    }
    
    private void fillArtificalParamNames(final ClassSymbol clazz) {
        for (Symbol s : clazz.getEnclosedElements()) {
            if (s instanceof MethodSymbol) {
                MethodSymbol ms = (MethodSymbol) s;

                if (ms.getParameters().isEmpty()) {
                    continue;
                }
                
                Set<String> usedNames = new HashSet<String>();
                
                for (VarSymbol vs : ms.getParameters()) {
                    String name = JavaSourceAccessor.getINSTANCE().generateReadableParameterName(vs.asType().toString(), usedNames);

                    vs.setName(clazz.name.table.fromString(name));
                }
            }
        }
    }

    private String getCharSet(ChangedCharSetException e) {
        String spec = e.getCharSetSpec();
        if (e.keyEqualsCharSet()) {
            //charsetspec contains only charset
            return spec;
        }
        
        //charsetspec is in form "text/html; charset=UTF-8"
                
        int index = spec.indexOf(";"); // NOI18N
        if (index != -1) {
            spec = spec.substring(index + 1);
        }
        
        spec = spec.toLowerCase();
        
        StringTokenizer st = new StringTokenizer(spec, " \t=", true); //NOI18N
        boolean foundCharSet = false;
        boolean foundEquals = false;
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            if (token.equals(" ") || token.equals("\t")) { //NOI18N
                continue;
            }
            if (foundCharSet == false && foundEquals == false
                    && token.equals("charset")) { //NOI18N
                foundCharSet = true;
                continue;
            } else if (foundEquals == false && token.equals("=")) {//NOI18N
                foundEquals = true;
                continue;
            } else if (foundEquals == true && foundCharSet == true) {
                return token;
            }
            
            foundCharSet = false;
            foundEquals = false;
        }
        
        return null;
    }
}
