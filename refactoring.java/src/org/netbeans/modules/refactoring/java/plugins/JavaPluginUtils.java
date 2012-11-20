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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.refactoring.java.plugins;

import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.*;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.*;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.ClasspathInfo.PathKind;
import org.netbeans.api.java.source.*;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.java.RefactoringUtils;
import org.netbeans.modules.refactoring.java.api.JavaRefactoringUtils;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * Utility class for java plugins.
 */
public final class JavaPluginUtils {
    private static final Logger LOG = Logger.getLogger(JavaPluginUtils.class.getName());

    public static Problem isSourceElement(Element el, CompilationInfo info) {
        Problem preCheckProblem;
        Element typeElement;
        if(el.getKind() != ElementKind.PACKAGE) {
            typeElement = info.getElementUtilities().enclosingTypeElement(el);
            if(typeElement == null) {
                typeElement = el;
            }
        } else {
            typeElement = el;
        }
        ElementHandle<Element> handle = null;
        try {
            handle = ElementHandle.create(typeElement);
        } catch (IllegalArgumentException ex) {
            LOG.log(Level.WARNING, "Cannot create handle for source element", ex);
        }
        if (handle == null || JavaRefactoringUtils.isFromLibrary(handle, info.getClasspathInfo())) { //NOI18N
            preCheckProblem = new Problem(true, NbBundle.getMessage(
                    JavaPluginUtils.class, "ERR_CannotRefactorLibraryClass",
                    el.getKind()==ElementKind.PACKAGE?el:el.getEnclosingElement()
                    ));
            return preCheckProblem;
        }
        FileObject file = SourceUtils.getFile(handle, info.getClasspathInfo());
        // RefactoringUtils.isFromLibrary already checked file for null
        if (!RefactoringUtils.isFileInOpenProject(file)) {
            preCheckProblem =new Problem(true, NbBundle.getMessage(
                    JavaPluginUtils.class,
                    "ERR_ProjectNotOpened",
                    FileUtil.getFileDisplayName(file)));
            return preCheckProblem;
        }
        return null;
    }
    
    public static TreePath findMethod(TreePath path) {
        while (path != null) {
            if (path.getLeaf().getKind() == Kind.METHOD) {
                return path;
            }

            if (path.getLeaf().getKind() == Kind.BLOCK
                    && path.getParentPath() != null
                    && TreeUtilities.CLASS_TREE_KINDS.contains(path.getParentPath().getLeaf().getKind())) {
                //initializer:
                return path;
            }

            path = path.getParentPath();
        }

        return null;
    }
    
    public static TreePath findStatement(TreePath statementPath) {
        while (statementPath != null
                && (!StatementTree.class.isAssignableFrom(statementPath.getLeaf().getKind().asInterface())
                || (statementPath.getParentPath() != null
                && statementPath.getParentPath().getLeaf().getKind() != Kind.BLOCK))) {
            if (TreeUtilities.CLASS_TREE_KINDS.contains(statementPath.getLeaf().getKind())) {
                return null;
            }

            statementPath = statementPath.getParentPath();
        }

        return statementPath;
    }
    
    public static boolean isParentOf(TreePath parent, TreePath path) {
        Tree parentLeaf = parent.getLeaf();

        while (path != null && path.getLeaf() != parentLeaf) {
            path = path.getParentPath();
        }

        return path != null;
    }

    public static boolean isParentOf(TreePath parent, List<? extends TreePath> candidates) {
        for (TreePath tp : candidates) {
            if (!isParentOf(parent, tp)) {
                return false;
            }
        }

        return true;
    }
    
    public static Problem chainProblems(Problem result, Problem problem) {
        if (result == null) {
            return problem;
        }
        if (problem == null) {
            return result;
        }
        Problem value;
        if(problem.isFatal()) {
            problem.setNext(result);
            value = problem;
        } else {
            Problem next = value = result;
            while (next.getNext() != null) {
                next = next.getNext();
            }
            next.setNext(problem);
        }
        return value;
    }
    
    /**
     * Convert typemirror of an anonymous class to supertype/iface
     * 
     * @return typemirror of supertype/iface, initial tm if not anonymous
     */
    public static TypeMirror convertIfAnonymous(TypeMirror tm) {
        //anonymous class?
        Set<ElementKind> fm = EnumSet.of(ElementKind.METHOD, ElementKind.FIELD);
        if (tm instanceof DeclaredType) {
            Element el = ((DeclaredType) tm).asElement();
            if (el.getSimpleName().length() == 0 || fm.contains(el.getEnclosingElement().getKind())) {
                List<? extends TypeMirror> interfaces = ((TypeElement) el).getInterfaces();
                if (interfaces.isEmpty()) {
                    tm = ((TypeElement) el).getSuperclass();
                } else {
                    tm = interfaces.get(0);
                }
            }
        }
        return tm;
    }
    
    public static TypeMirror resolveCapturedType(CompilationInfo info, TypeMirror tm) {
        TypeMirror type = resolveCapturedTypeInt(info, tm);
        
        if (type.getKind() == TypeKind.WILDCARD) {
            TypeMirror tmirr = ((WildcardType) type).getExtendsBound();
            if (tmirr != null) {
                return tmirr;
            }
            else { //no extends, just '?'
                return info.getElements().getTypeElement("java.lang.Object").asType(); // NOI18N
            }
                
        }
        
        return type;
    }
    
    private static TypeMirror resolveCapturedTypeInt(CompilationInfo info, TypeMirror tm) {
        TypeMirror orig = SourceUtils.resolveCapturedType(tm);

        if (orig != null) {
            if (orig.getKind() == TypeKind.WILDCARD) {
                TypeMirror extendsBound = ((WildcardType) orig).getExtendsBound();
                TypeMirror rct = SourceUtils.resolveCapturedType(extendsBound != null ? extendsBound : ((WildcardType) orig).getSuperBound());
                if (rct != null) {
                    return rct;
                }
            }
            return orig;
        }
        
        if (tm.getKind() == TypeKind.DECLARED) {
            DeclaredType dt = (DeclaredType) tm;
            List<TypeMirror> typeArguments = new LinkedList<TypeMirror>();
            
            for (TypeMirror t : dt.getTypeArguments()) {
                typeArguments.add(resolveCapturedTypeInt(info, t));
            }
            
            final TypeMirror enclosingType = dt.getEnclosingType();
            if (enclosingType.getKind() == TypeKind.DECLARED) {
                return info.getTypes().getDeclaredType((DeclaredType) enclosingType, (TypeElement) dt.asElement(), typeArguments.toArray(new TypeMirror[0]));
            } else {
                return info.getTypes().getDeclaredType((TypeElement) dt.asElement(), typeArguments.toArray(new TypeMirror[0]));
            }
        }

        if (tm.getKind() == TypeKind.ARRAY) {
            ArrayType at = (ArrayType) tm;

            return info.getTypes().getArrayType(resolveCapturedTypeInt(info, at.getComponentType()));
        }
        
        return tm;
    }

    public static JavaSource createSource(final FileObject file, final ClasspathInfo cpInfo, final TreePathHandle tph) throws IllegalArgumentException {
        JavaSource source;
        if (file != null) {
            final ClassPath mergedPlatformPath = merge(cpInfo.getClassPath(PathKind.BOOT), ClassPath.getClassPath(file, ClassPath.BOOT));
            final ClassPath mergedCompilePath = merge(cpInfo.getClassPath(PathKind.COMPILE), ClassPath.getClassPath(file, ClassPath.COMPILE));
            final ClassPath mergedSourcePath = merge(cpInfo.getClassPath(PathKind.SOURCE), ClassPath.getClassPath(file, ClassPath.SOURCE));
            final ClasspathInfo mergedInfo = ClasspathInfo.create(mergedPlatformPath, mergedCompilePath, mergedSourcePath);
            source = JavaSource.create(mergedInfo, new FileObject[]{tph.getFileObject()});
        } else {
            source = JavaSource.create(cpInfo);
        }
        return source;
    }

    @SuppressWarnings("CollectionContainsUrl")
    public static ClassPath merge(final ClassPath... cps) {
        final Set<URL> roots = new LinkedHashSet<URL>(cps.length);
        for (final ClassPath cp : cps) {
            if (cp != null) {
                for (final ClassPath.Entry entry : cp.entries()) {
                    final URL root = entry.getURL();
                    if (!roots.contains(root)) {
                        roots.add(root);
                    }
                }
            }
        }
        return ClassPathSupport.createClassPath(roots.toArray(new URL[roots.size()]));
    }
    
    //<editor-fold defaultstate="collapsed" desc="TODO: Copy from org.netbeans.modules.java.hints.errors.Utilities">
    public static final String DEFAULT_NAME = "par"; // NOI18N
    public static String makeNameUnique(CompilationInfo info, Scope s, String name, MethodTree method) {
        int counter = 0;
        boolean cont = true;
        String proposedName = name;
        
        while (cont) {
            proposedName = name + (counter != 0 ? String.valueOf(counter) : "");
            
            cont = false;
            
            if (s != null) {
                for (Element e : info.getElementUtilities().getLocalMembersAndVars(s, new VariablesFilter())) {
                    if (proposedName.equals(e.getSimpleName().toString())) {
                        counter++;
                        cont = true;
                        break;
                    }
                }
            }
        }
        
        return proposedName;
    }
    
    public static String getName(Tree et) {
        return adjustName(getNameRaw(et));
    }
    
    public static String getName(TypeMirror tm) {
        if (tm.getKind().isPrimitive()) {
            return "" + Character.toLowerCase(tm.getKind().name().charAt(0));
        }

        switch (tm.getKind()) {
            case DECLARED:
                DeclaredType dt = (DeclaredType) tm;
                return firstToLower(dt.asElement().getSimpleName().toString());
            case ARRAY:
                return getName(((ArrayType) tm).getComponentType());
            default:
                return DEFAULT_NAME;
        }
    }
    
    private static String getNameRaw(Tree et) {
        if (et == null) {
            return null;
        }
        
        switch (et.getKind()) {
            case IDENTIFIER:
                return ((IdentifierTree) et).getName().toString();
            case METHOD_INVOCATION:
                return getName(((MethodInvocationTree) et).getMethodSelect());
            case MEMBER_SELECT:
                return ((MemberSelectTree) et).getIdentifier().toString();
            case NEW_CLASS:
                return firstToLower(getName(((NewClassTree) et).getIdentifier()));
            case PARAMETERIZED_TYPE:
                return firstToLower(getName(((ParameterizedTypeTree) et).getType()));
            case STRING_LITERAL:
                String name = guessLiteralName((String) ((LiteralTree) et).getValue());
                if(name == null) {
                    return firstToLower(String.class.getSimpleName());
                } else {
                    return firstToLower(name);
                }
            case VARIABLE:
                return ((VariableTree) et).getName().toString();
            default:
                return null;
        }
    }
    
    static String adjustName(String name) {
        if (name == null) {
            return null;
        }
        
        String shortName = null;
        
        if (name.startsWith("get") && name.length() > 3) {
            shortName = name.substring(3);
        }
        
        if (name.startsWith("is") && name.length() > 2) {
            shortName = name.substring(2);
        }
        
        if (shortName != null) {
            return firstToLower(shortName);
        }
        
        if (SourceVersion.isKeyword(name)) {
            return "a" + Character.toUpperCase(name.charAt(0)) + name.substring(1);
        } else {
            return name;
        }
    }
    
    private static String firstToLower(String name) {
        if (name.length() == 0) {
            return null;
        }
        
        StringBuilder result = new StringBuilder();
        boolean toLower = true;
        char last = Character.toLowerCase(name.charAt(0));
        
        for (int i = 1; i < name.length(); i++) {
            if (toLower && Character.isUpperCase(name.charAt(i))) {
                result.append(Character.toLowerCase(last));
            } else {
                result.append(last);
                toLower = false;
            }
            last = name.charAt(i);
            
        }
        
        result.append(last);
        
        if (SourceVersion.isKeyword(result)) {
            return "a" + name;
        } else {
            return result.toString();
        }
    }
    
    private static String guessLiteralName(String str) {
        StringBuilder sb = new StringBuilder();
        if(str.length() == 0) {
            return null;
        }
        char first = str.charAt(0);
        if(Character.isJavaIdentifierStart(str.charAt(0))) {
            sb.append(first);
        }
        
        for (int i = 1; i < str.length(); i++) {
            char ch = str.charAt(i);
            if(ch == ' ') {
                sb.append('_');
                continue;
            }
            if (Character.isJavaIdentifierPart(ch)) {
                sb.append(ch);
            }
            if (i > 40) {
                break;
            }
        }
        if (sb.length() == 0) {
            return null;
        }
        else {
            return sb.toString();
        }
    }

    public static CompilationUnitTree createCompilationUnit(FileObject sourceRoot, String relativePath, Tree typeDecl, WorkingCopy workingCopy, TreeMaker make) {
        GeneratorUtilities genUtils = GeneratorUtilities.get(workingCopy);
        CompilationUnitTree newCompilation;
        try {
            newCompilation = genUtils.createFromTemplate(sourceRoot, relativePath, ElementKind.CLASS);
            List<? extends Tree> typeDecls = newCompilation.getTypeDecls();
            if (typeDecls.isEmpty()) {
                newCompilation = make.addCompUnitTypeDecl(newCompilation, typeDecl);
            } else {
                List<Tree> typeDeclarations = new LinkedList<Tree>(newCompilation.getTypeDecls());
                Tree templateClazz = typeDeclarations.remove(0); // TODO: Check for class with correct name, template could start with another type.
                if (workingCopy.getTreeUtilities().getComments(typeDecl, true).isEmpty()) {
                    genUtils.copyComments(templateClazz, typeDecl, true);
                } else if (workingCopy.getTreeUtilities().getComments(typeDecl, false).isEmpty()) {
                    genUtils.copyComments(templateClazz, typeDecl, false);
                }
                typeDeclarations.add(0, typeDecl);
                newCompilation = make.CompilationUnit(newCompilation.getPackageAnnotations(), sourceRoot, relativePath, newCompilation.getImports(), typeDeclarations);
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            newCompilation = make.CompilationUnit(sourceRoot, relativePath, null, Collections.singletonList(typeDecl));
        }
        return newCompilation;
    }
    
    public static final class VariablesFilter implements ElementUtilities.ElementAcceptor {
        
        private static final Set<ElementKind> ACCEPTABLE_KINDS = EnumSet.of(ElementKind.ENUM_CONSTANT, ElementKind.EXCEPTION_PARAMETER, ElementKind.FIELD, ElementKind.LOCAL_VARIABLE, ElementKind.PARAMETER);
        
        public boolean accept(Element e, TypeMirror type) {
            return ACCEPTABLE_KINDS.contains(e.getKind());
        }
        
    }
    //</editor-fold>
}
