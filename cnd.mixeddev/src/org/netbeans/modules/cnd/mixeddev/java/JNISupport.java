/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.mixeddev.java;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.PrimitiveTypeTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.swing.text.Document;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import static org.netbeans.modules.cnd.mixeddev.MixedDevUtils.*;
import org.netbeans.modules.cnd.mixeddev.MixedDevUtils.Converter;
import static org.netbeans.modules.cnd.mixeddev.java.JavaContextSupport.createMethodInfo;
import org.netbeans.modules.cnd.mixeddev.java.model.JavaMethodInfo;
import org.netbeans.modules.cnd.mixeddev.java.model.JavaTypeInfo;
import org.netbeans.modules.cnd.mixeddev.java.model.QualifiedNamePart;
import org.openide.filesystems.FileObject;
import org.openide.util.Pair;

/**
 *
 * @author Petr Kudryavtsev <petrk@netbeans.org>
 */
public final class JNISupport {

    public static List<String> getJNIClasses(FileObject fObj) {
        return JavaContextSupport.resolveContext(fObj, new GetJavaJNIClassesTask());
    }    
    
    public static List<String> getJNIClasses(Document doc) {
        return JavaContextSupport.resolveContext(doc, new GetJavaJNIClassesTask());
    }    
    
    public static JavaMethodInfo getJNIMethod(FileObject fObj, int offset) {
        return JavaContextSupport.resolveContext(fObj, new ResolveJNIMethodTask(offset));
    }    
    
    public static JavaMethodInfo getJNIMethod(Document doc, int offset) {
        return JavaContextSupport.resolveContext(doc, new ResolveJNIMethodTask(offset));
    }
    
    public static String getCppMethodSignature(JavaMethodInfo methodInfo) {
        if (methodInfo == null) {
            return null;
        }
        StringBuilder method = new StringBuilder();
        
        // Add method name
        method.append(QN_PREFIX + "_").append(stringize(transform(methodInfo.getFullQualifiedName(), new QualNameToCppStringConverter()), "_")); // NOI18N
        
        if (methodInfo.isOverloaded()) {
            // Ambiguity!
            method.append(PARAMS_SIGNATURE_PREFIX);
            for (JavaTypeInfo param : methodInfo.getParameters()) {
                method.append(getTypeSignature(param));
            }
        }
        
        // Add parameters
        List<String> cppTypes = new ArrayList<String>();
        cppTypes.addAll(IMPLICIT_PARAMS);
        cppTypes.addAll(transform(methodInfo.getParameters(), JavaCppTypeConverter.INSTANCE));        
        method.append(LPAREN).append(stringize(cppTypes, COMMA)).append(RPAREN);
        
        return method.toString();
    }
    
//<editor-fold defaultstate="collapsed" desc="Implementation">    
    private static final String QN_PREFIX = "Java"; // NOI18N
    
    private static final String PARAMS_SIGNATURE_PREFIX = "__"; // NOI18N
    
    private static final String JNIENV = "JNIEnv";  // NOI18N
    
    private static final String JOBJECT = "jobject";    // NOI18N
    
    // 0x00024 is '$' sign
    private static final String NETSED_PREFIX = "00024"; // NOI18N 
    
    private static final List<String> IMPLICIT_PARAMS = Arrays.asList(JNIENV + POINTER, JOBJECT);
    
    private static final Map<String, String> javaToCppTypes = createMapping(
        Pair.of("boolean", "jboolean"), // NOI18N 
        Pair.of("byte", "jbyte"), // NOI18N
        Pair.of("char", "jchar"), // NOI18N
        Pair.of("short", "jshort"), // NOI18N
        Pair.of("int", "jint"), // NOI18N
        Pair.of("long", "jlong"), // NOI18N
        Pair.of("float", "jfloat"), // NOI18N
        Pair.of("double", "jdouble"), // NOI18N
        Pair.of("void", "void"), // NOI18N
        Pair.of("String", "jstring"), // NOI18N
        Pair.of("boolean[]", "jbooleanArray"), // NOI18N
        Pair.of("byte[]", "jbyteArray"), // NOI18N
        Pair.of("char[]", "jcharArray"), // NOI18N
        Pair.of("short[]", "jshortArray"), // NOI18N
        Pair.of("int[]", "jintArray"), // NOI18N
        Pair.of("long[]", "jlongArray"), // NOI18N
        Pair.of("float[]", "jfloatArray"), // NOI18N
        Pair.of("double[]", "jdoubleArray") // NOI18N
    );
    
    private static final Map<String, String> javaToSignatures = createMapping(
        Pair.of("boolean", "Z"), // NOI18N
        Pair.of("byte", "B"), // NOI18N
        Pair.of("char", "C"), // NOI18N
        Pair.of("short", "S"), // NOI18N
        Pair.of("int", "I"), // NOI18N
        Pair.of("long", "J"), // NOI18N
        Pair.of("float", "F"), // NOI18N
        Pair.of("double", "D"), // NOI18N
        Pair.of("void", "V") // NOI18N
    );
    
    private static String getCppType(JavaTypeInfo javaType) {
        String typeName = javaType.getText().toString();
        if (javaToCppTypes.containsKey(typeName)) {
            return javaToCppTypes.get(typeName);
        }
        return javaType.getArrayDepth() > 0 ? "jobjectArray" : "jobject"; // NOI18N
    }
    
    private static String getTypeSignature(JavaTypeInfo javaType) {
        String typeName = javaType.getText().toString();
        if (javaToSignatures.containsKey(typeName)) {
            return javaToSignatures.get(typeName);
        }
        return javaType.getName().toString();
    }
    
    private final static class JavaCppTypeConverter implements Converter<JavaTypeInfo, String> {
        
        public static final JavaCppTypeConverter INSTANCE = new JavaCppTypeConverter();
        
        @Override
        public String convert(JavaTypeInfo from) {
            return getCppType(from);
        }
    }
    
    private static final class ResolveJNIMethodTask implements ResolveJavaContextTask<JavaMethodInfo> {
        
        private final int offset;
        
        private JavaMethodInfo result;
        
        public ResolveJNIMethodTask(int offset) {
            this.offset = offset;
        }
        
        @Override
        public boolean hasResult() {
            return result != null;
        }
        
        @Override
        public JavaMethodInfo getResult() {
            return result;
        }
        
        @Override
        public void cancel() {
            // Do nothing
        }
        
        @Override
        public void run(CompilationController controller) throws Exception {
            if (controller == null || controller.toPhase(JavaSource.Phase.RESOLVED).compareTo(JavaSource.Phase.RESOLVED) < 0) {
                return;
            }
            // Looking for current element
            TreePath tp = controller.getTreeUtilities().pathFor(offset);
            if (tp != null && tp.getLeaf() != null && tp.getLeaf().getKind() == Tree.Kind.METHOD) {
                if (((MethodTree) tp.getLeaf()).getModifiers().getFlags().contains(Modifier.NATIVE)) {
                    result = validateMethodInfo(createMethodInfo(controller, tp));
                }
            }
        }
        
        private JavaMethodInfo validateMethodInfo(JavaMethodInfo mtdInfo) {
            for (JavaTypeInfo type : mtdInfo.getParameters()) {
                if (type == null || type.getName() == null) {
                    return null;
                }
            }
            for (QualifiedNamePart namePart : mtdInfo.getFullQualifiedName()) {
                if (namePart.getText() == null || namePart.getText().length() == 0) {
                    return null;
                }
            }
            if (mtdInfo.getReturnType() == null || mtdInfo.getReturnType().getName() == null) {
                return null;
            }
            return mtdInfo;
        }
    }
    
    private static final class GetJavaJNIClassesTask implements ResolveJavaContextTask<List<String>> {
        
        private final List<String> result = new ArrayList<String>();
        
        @Override
        public boolean hasResult() {
            return true;
        }
        
        @Override
        public List<String> getResult() {
            return Collections.unmodifiableList(result);
        }
        
        @Override
        public void cancel() {
            // Do nothing
        }
        
        @Override
        public void run(CompilationController controller) throws Exception {
            if (controller == null || controller.toPhase(JavaSource.Phase.RESOLVED).compareTo(JavaSource.Phase.RESOLVED) < 0) {
                return;
            }
            
            List<? extends Tree> topLevelDecls = controller.getCompilationUnit().getTypeDecls();
            for (Tree topLevelDecl : topLevelDecls) {
                if (topLevelDecl.getKind() == Tree.Kind.CLASS) {
                    if (hasJniMethods((ClassTree) topLevelDecl)) {
                        result.add(stringize(transform(JavaContextSupport.getQualifiedName(controller, topLevelDecl), new QualNameToJavaStringConverter()), DOT));
                    }
                }
            }
        }
        
        private boolean hasJniMethods(ClassTree clsTree) {
            for (Tree memberTree : clsTree.getMembers()) {
                if (memberTree.getKind() == Tree.Kind.METHOD) {
                    if (((MethodTree) memberTree).getModifiers().getFlags().contains(Modifier.NATIVE)) {
                        return true;
                    }
                }
            }
            return false;
        }
    }
    
    private static class QualNameToCppStringConverter implements Converter<QualifiedNamePart, String> {
        
        @Override
        public String convert(QualifiedNamePart from) {
            switch (from.getKind()) {
                case NESTED_CLASS:
                    return NETSED_PREFIX + from.getText();
                    
                default:
                    return from.getText().toString();
            }
        }
    }
    
    private static class QualNameToJavaStringConverter implements Converter<QualifiedNamePart, String> {
        
        @Override
        public String convert(QualifiedNamePart from) {
            return from.getText().toString();
        }
    }
    
    private JNISupport() {
        throw new AssertionError("Not instantiable!"); // NOI18N
    }
//</editor-fold>
}
