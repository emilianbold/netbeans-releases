/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.refactoring.java;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.WildcardType;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Types;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.text.AttributeSet;
import javax.swing.text.StyleConstants;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;

/**
 *
 * @author Jan Becicka
 */
public class RetoucheUtils {
    
    private static final String JAVA_MIME_TYPE = "text/x-java";
    
    public static String htmlize(String input) {
        String temp = org.openide.util.Utilities.replaceString(input, "<", "&lt;"); // NOI18N
        temp = org.openide.util.Utilities.replaceString(temp, ">", "&gt;"); // NOI18N
        return temp;
    }
    
    public static Collection<ExecutableElement> getOverridenMethods(ExecutableElement e, CompilationInfo info) {
        return getOverridenMethods(e, SourceUtils.getEnclosingTypeElement(e), info);
    }

    private static Collection<ExecutableElement> getOverridenMethods(ExecutableElement e, TypeElement parent, CompilationInfo info) {
        ArrayList<ExecutableElement> result = new ArrayList<ExecutableElement>();
        
        TypeMirror sup = parent.getSuperclass();
        if (sup.getKind() == TypeKind.DECLARED) {
            TypeElement next = (TypeElement) ((DeclaredType)sup).asElement();
            ExecutableElement overriden = getMethod(e, next, info);
                result.addAll(getOverridenMethods(e,next, info));
            if (overriden!=null) {
                result.add(overriden);
            }
        }
        for (TypeMirror tm:parent.getInterfaces()) {
            TypeElement next = (TypeElement) ((DeclaredType)tm).asElement();
            ExecutableElement overriden2 = getMethod(e, next, info);
            result.addAll(getOverridenMethods(e,next, info));
            if (overriden2!=null) {
                result.add(overriden2);
            }
        }
        return result;
    }    
    
    private static ExecutableElement getMethod(ExecutableElement method, TypeElement type, CompilationInfo info) {
        for (ExecutableElement met: ElementFilter.methodsIn(type.getEnclosedElements())){
            if (info.getElements().overrides(method, met, type)) {
                return met;
            }
        }
        return null;
    }
    
    public static Set<ElementHandle<TypeElement>> getImplementorsAsHandles(ClassIndex idx, ClasspathInfo cpInfo, TypeElement el) {    
       LinkedList<ElementHandle<TypeElement>> elements = new LinkedList<ElementHandle<TypeElement>>(idx.getElements(ElementHandle.create(el),
                EnumSet.of(ClassIndex.SearchKind.IMPLEMENTORS),
                EnumSet.of(ClassIndex.SearchScope.SOURCE)));
        HashSet<ElementHandle<TypeElement>> result = new HashSet<ElementHandle<TypeElement>>();
        while(!elements.isEmpty()) {
            ElementHandle<TypeElement> next = elements.removeFirst();
            result.add(next);
            elements.addAll(idx.getElements(next,
                    EnumSet.of(ClassIndex.SearchKind.IMPLEMENTORS),
                    EnumSet.of(ClassIndex.SearchScope.SOURCE)));
        }
        return result;
    }

    public static Collection<ExecutableElement> getOverridingMethods(ExecutableElement e, CompilationInfo info) {
        Collection<ExecutableElement> result = new ArrayList();
        TypeElement parentType = (TypeElement) e.getEnclosingElement();
        //XXX: Fixme IMPLEMENTORS_RECURSIVE were removed
        Set<ElementHandle<TypeElement>> subTypes = getImplementorsAsHandles(info.getClasspathInfo().getClassIndex(), info.getClasspathInfo(), parentType);
        for (ElementHandle<TypeElement> subTypeHandle: subTypes){
            TypeElement type = subTypeHandle.resolve(info);
            for (ExecutableElement method: ElementFilter.methodsIn(type.getEnclosedElements())) {
                if (info.getElements().overrides(method, e, type)) {
                    result.add(method);
                }
            }
        }
        return result;
    }

    public static boolean isJavaFile(FileObject f) {
        return JAVA_MIME_TYPE.equals(f.getMIMEType()); //NOI18N
    }
    
    public static String getHtml(String text) {
        StringBuffer buf = new StringBuffer();
        TokenHierarchy tokenH = TokenHierarchy.create(text, JavaTokenId.language());
        Lookup lookup = MimeLookup.getLookup(MimePath.get(JAVA_MIME_TYPE));
        FontColorSettings settings = lookup.lookup(FontColorSettings.class);
        TokenSequence tok = tokenH.tokenSequence();
        while (tok.moveNext()) {
            Token<JavaTokenId> token = (Token) tok.token();
            String category = token.id().primaryCategory();
            if (category == null) {
                category = "whitespace"; //NOI18N
            }
            AttributeSet set = settings.getTokenFontColors(category);
            buf.append(color(htmlize(token.text().toString()), set));
        }
        return buf.toString();
    }

    private static String color(String string, AttributeSet set) {
        if (set==null)
            return string;
        if (string.trim().length() == 0) {
            return Utilities.replaceString(Utilities.replaceString(string, " ", "&nbsp;"), "\n", "<br>"); //NOI18N
        } 
        StringBuffer buf = new StringBuffer(string);
        if (StyleConstants.isBold(set)) {
            buf.insert(0,"<b>"); //NOI18N
            buf.append("</b>"); //NOI18N
        }
        if (StyleConstants.isItalic(set)) {
            buf.insert(0,"<i>"); //NOI18N
            buf.append("</i>"); //NOI18N
        }
        if (StyleConstants.isStrikeThrough(set)) {
            buf.insert(0,"<s>");
            buf.append("</s>");
        }
        buf.insert(0,"<font color=" + getHTMLColor(StyleConstants.getForeground(set)) + ">"); //NOI18N
        buf.append("</font>"); //NOI18N
        return buf.toString();
    }
    
    private static String getHTMLColor(Color c) {
        String colorR = "0" + Integer.toHexString(c.getRed()); //NOI18N
        colorR = colorR.substring(colorR.length() - 2); 
        String colorG = "0" + Integer.toHexString(c.getGreen()); //NOI18N
        colorG = colorG.substring(colorG.length() - 2);
        String colorB = "0" + Integer.toHexString(c.getBlue()); //NOI18N
        colorB = colorB.substring(colorB.length() - 2);
        String html_color = "#" + colorR + colorG + colorB; //NOI18N
        return html_color;
    }

    public static boolean isElementInOpenProject(FileObject f) {
        Project p = FileOwnerQuery.getOwner(f);
        Project[] opened = OpenProjects.getDefault().getOpenProjects();
        for (int i = 0; i<opened.length; i++) {
            if (p==opened[i])
                return true;
        }
        return false;
    }
    public static boolean isFromLibrary(Element element, ClasspathInfo info) {
        FileObject file = SourceUtils.getFile(element, info);
        if (file==null) {
            //no source for given element. Element is from library
            return true;
        }
        return FileUtil.getArchiveFile(file)!=null;
    }

    public static boolean isValidPackageName(String name) {
        if (name.endsWith(".")) //NOI18N
            return false;
        if (name.startsWith("."))  //NOI18N
            return  false;
        StringTokenizer tokenizer = new StringTokenizer(name, "."); // NOI18N
        while (tokenizer.hasMoreTokens()) {
            if (!Utilities.isJavaIdentifier(tokenizer.nextToken())) {
                return false;
            }
        }
        return true;
    }
    
    public static boolean isFileInOpenProject(FileObject file) {
        assert file != null;
        Project p = FileOwnerQuery.getOwner(file);
        Project[] opened = OpenProjects.getDefault().getOpenProjects();
        for (int i = 0; i<opened.length; i++) {
            if (p==opened[i])
                return true;
        }
        return false;
    }
    
    public static boolean isOnSourceClasspath(FileObject fo) {
        Project p = FileOwnerQuery.getOwner(fo);
        if (p==null) return false;
        Project[] opened = OpenProjects.getDefault().getOpenProjects();
        for (int i = 0; i<opened.length; i++) {
            if (p==opened[i]) {
                SourceGroup[] gr = ProjectUtils.getSources(p).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
                for (int j = 0; j < gr.length; j++) {
                    if (fo==gr[j].getRootFolder()) return true;
                    if (FileUtil.isParentOf(gr[j].getRootFolder(), fo))
                        return true;
                }
                return false;
            }
        }
        return false;
    }

    public static boolean isClasspathRoot(FileObject fo) {
        return fo.equals(ClassPath.getClassPath(fo, ClassPath.SOURCE).findOwnerRoot(fo));
    }
    
    public static boolean isRefactorable(FileObject file) {
        return isJavaFile(file) && isFileInOpenProject(file) && isOnSourceClasspath(file);
    }
    
    public static String getPackageName(FileObject folder) {
        assert folder.isFolder() : "argument must be folder";
        return ClassPath.getClassPath(
                folder, ClassPath.SOURCE)
                .getResourceName(folder, '.', false);
    }
    
    public static String getPackageName(URL url) {
        File f = null;
        try {
            String path = URLDecoder.decode(url.getPath(), "utf-8");
            f = FileUtil.normalizeFile(new File(path));
        } catch (UnsupportedEncodingException u) {
            throw new IllegalArgumentException("Cannot create package name for url " + url);
        }
        String suffix = "";
        
        do {
            FileObject fo = FileUtil.toFileObject(f);
            if (fo != null) {
                if ("".equals(suffix))
                    return getPackageName(fo);
                String prefix = getPackageName(fo);
                return prefix + ("".equals(prefix)?"":".") + suffix;
            }
            if (!"".equals(suffix)) {
                suffix = "." + suffix;
            }
            try {
                suffix = URLDecoder.decode(f.getPath().substring(f.getPath().lastIndexOf(File.separatorChar) + 1), "utf-8") + suffix;
            } catch (UnsupportedEncodingException u) {
                throw new IllegalArgumentException("Cannot create package name for url " + url);
            }
            f = f.getParentFile();
        } while (f!=null);
        throw new IllegalArgumentException("Cannot create package name for url " + url);
    }

    /**
     * creates or finds FileObject according to 
     * @param url
     * @return FileObject
     */
    public static FileObject getOrCreateFolder(URL url) throws IOException {
        try {
            FileObject result = URLMapper.findFileObject(url);
            if (result != null)
                return result;
            File f = new File(url.toURI());
            
            result = FileUtil.createFolder(f);
            return result;
        } catch (URISyntaxException ex) {
            throw (IOException) new IOException().initCause(ex);
        }
    }
    
    public static FileObject getClassPathRoot(URL url) throws IOException {
        FileObject result = URLMapper.findFileObject(url);
        File f = FileUtil.normalizeFile(new File(url.getPath()));
        while (result==null) {
            result = FileUtil.toFileObject(f);
            f = f.getParentFile();
        }
        return ClassPath.getClassPath(result, ClassPath.SOURCE).findOwnerRoot(result);
    }
    
    public static Collection<TypeElement> getSuperTypes(TypeElement type, CompilationInfo info) {
        Collection<TypeElement> result = new HashSet<TypeElement>();
        LinkedList<TypeElement> l = new LinkedList<TypeElement>();
        l.add(type);
        while (!l.isEmpty()) {
            TypeElement t = l.removeFirst();
            TypeElement superClass = typeToElement(t.getSuperclass(), info);
            if (superClass!=null) {
                result.add(superClass);
                l.addLast((TypeElement)superClass);
            }
            Collection<TypeElement> interfaces = typesToElements(t.getInterfaces(), info);
            result.addAll(interfaces);
            l.addAll(interfaces);
        }
        return result;
    }
    
    public static Collection<FileObject> getSuperTypesFiles(TreePathHandle handle) {
        try {
            SuperTypesTask ff;
            JavaSource source = JavaSource.forFileObject(handle.getFileObject());
            source.runUserActionTask(ff=new SuperTypesTask(handle), true);
            return ff.getFileObjects();
        } catch (IOException ex) {
            throw (RuntimeException) new RuntimeException().initCause(ex);
        }    
    }
    
    public static Collection<TypeElement> getSuperTypes(TypeElement type, CompilationInfo info, boolean sourceOnly) {
        if (!sourceOnly)
            return getSuperTypes(type, info);
        Collection<TypeElement> result = new HashSet<TypeElement>();
        for (TypeElement el: getSuperTypes(type, info)) {
            FileObject file = SourceUtils.getFile(el, info.getClasspathInfo());
            if (file!=null && isFileInOpenProject(file) && !isFromLibrary(el, info.getClasspathInfo())) {
                result.add(el);
            }
        }
        return result;
    }
    
    public static TypeElement typeToElement(TypeMirror type, CompilationInfo info) {
        return (TypeElement) info.getTypes().asElement(type);
    }
    
    private static Collection<TypeElement> typesToElements(Collection<? extends TypeMirror> types, CompilationInfo info) {
        Collection<TypeElement> result = new HashSet();
        for (TypeMirror tm : types) {
            result.add(typeToElement(tm, info));
        }
        return result;
    }
    
    public static Collection<FileObject> elementsToFile(Collection<? extends Element> elements, ClasspathInfo cpInfo ) {
        Collection <FileObject> result = new HashSet();
        for (Element handle:elements) {
            result.add(SourceUtils.getFile(handle, cpInfo));
        }
        return result;
    }
    
    public static boolean elementExistsIn(TypeElement target, Element member, CompilationInfo info) {
        for (Element currentMember: target.getEnclosedElements()) {
            if (info.getElements().hides(member, currentMember) || info.getElements().hides(currentMember, member))
                return true;
            if (member instanceof ExecutableElement 
                    && currentMember instanceof ExecutableElement 
                    && (info.getElements().overrides((ExecutableElement)member, (ExecutableElement)currentMember, target) ||
                       (info.getElements().overrides((ExecutableElement)currentMember, (ExecutableElement)member, target)))) {
                return true;
            }
        }
        return false;
    }
    
    public static ElementHandle getElementHandle(TreePathHandle tph) {
        try {
            CompilerTask ff;
            JavaSource source = JavaSource.forFileObject(tph.getFileObject());
            source.runUserActionTask(ff=new CompilerTask(tph), true);
            return ff.getElementHandle();
        } catch (IOException ex) {
            throw (RuntimeException) new RuntimeException().initCause(ex);
        }    
    }
    
    public static ElementKind getElementKind(TreePathHandle tph) {
        try {
            CompilerTask ff;
            JavaSource source = JavaSource.forFileObject(tph.getFileObject());
            source.runUserActionTask(ff=new CompilerTask(tph), true);
            return ff.getElementKind();
        } catch (IOException ex) {
            throw (RuntimeException) new RuntimeException().initCause(ex);
        }    
    }
    
    
    public static String getSimpleName(TreePathHandle tph) {
        try {
            CompilerTask ff;
            JavaSource source = JavaSource.forFileObject(tph.getFileObject());
            source.runUserActionTask(ff=new CompilerTask(tph), true);
            return ff.getSimpleName();
        } catch (IOException ex) {
            throw (RuntimeException) new RuntimeException().initCause(ex);
        }    
    }
    
    
    public static FileObject getFileObject(final TreePathHandle handle) {
        try {
            CompilerTask ff;
            JavaSource source = JavaSource.forFileObject(handle.getFileObject());
            source.runUserActionTask(ff=new CompilerTask(handle), true);
            return ff.getFileObject();
        } catch (IOException ex) {
            throw (RuntimeException) new RuntimeException().initCause(ex);
        }
    }
    
    public static String getQualifiedName(TreePathHandle tph) {
        try {
            CompilerTask ff;
            JavaSource source = JavaSource.forFileObject(tph.getFileObject());
            source.runUserActionTask(ff=new CompilerTask(tph), true);
            return ff.getQualifiedName();
        } catch (IOException ex) {
            throw (RuntimeException) new RuntimeException().initCause(ex);
        }    
    }
    
    public static boolean typeExist(TreePathHandle tph, String fqn) {
        try {
            CompilerTask ff;
            JavaSource source = JavaSource.forFileObject(tph.getFileObject());
            source.runUserActionTask(ff=new CompilerTask(tph, fqn), true);
            return ff.typeExist();
        } catch (IOException ex) {
            throw (RuntimeException) new RuntimeException().initCause(ex);
        }    
    }
    
    public static ClasspathInfo getClasspathInfoFor(FileObject ... files) {
        return getClasspathInfoFor(true, files);
    }
    
    public static ClasspathInfo getClasspathInfoFor(boolean dependencies, FileObject ... files) {
        assert files.length >0;
        Set<URL> dependentRoots = new HashSet();
        for (FileObject fo: files) {
            Project p = null;
            if (fo!=null)
                p=FileOwnerQuery.getOwner(fo);
            if (p!=null) {
                URL sourceRoot = URLMapper.findURL(ClassPath.getClassPath(fo, ClassPath.SOURCE).findOwnerRoot(fo), URLMapper.INTERNAL);
                if (dependencies) {
                    dependentRoots.addAll(SourceUtils.getDependentRoots(sourceRoot));
                } else {
                    dependentRoots.add(sourceRoot);
                }
                for (SourceGroup root:ProjectUtils.getSources(p).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA)) {
                    dependentRoots.add(URLMapper.findURL(root.getRootFolder(), URLMapper.INTERNAL));
                }
            } else {
                for(ClassPath cp: GlobalPathRegistry.getDefault().getPaths(ClassPath.SOURCE)) {
                    for (FileObject root:cp.getRoots()) {
                        dependentRoots.add(URLMapper.findURL(root, URLMapper.INTERNAL));
                    }
                }
            }
        }
        
        ClassPath rcp = ClassPathSupport.createClassPath(dependentRoots.toArray(new URL[dependentRoots.size()]));
        ClassPath nullPath = ClassPathSupport.createClassPath(new FileObject[0]);
        ClassPath boot = files[0]!=null?ClassPath.getClassPath(files[0], ClassPath.BOOT):nullPath;
        ClassPath compile = files[0]!=null?ClassPath.getClassPath(files[0], ClassPath.COMPILE):nullPath;
        ClasspathInfo cpInfo = ClasspathInfo.create(boot, compile, rcp);
        return cpInfo;
    }
    
    public static ClasspathInfo getClasspathInfoFor(TreePathHandle ... handles) {
        FileObject[] result = new FileObject[handles.length];
        int i=0;
        for (TreePathHandle handle:handles) {
            result[i++]=getFileObject(handle);
        }
        return getClasspathInfoFor(result);
    }
    
    /**
     * Finds type parameters from <code>typeArgs</code> list that are referenced
     * by <code>tm</code> type.
     * @param utils compilation type utils
     * @param typeArgs modifiable list of type parameters to search; found types will be removed (performance reasons).
     * @param result modifiable list that will contain referenced type parameters
     * @param tm parametrized type to analyze
     */
    public static void findUsedGenericTypes(Types utils, List<TypeMirror> typeArgs, List<TypeMirror> result, TypeMirror tm) {
        if (typeArgs.isEmpty()) {
            return;
        } else if (tm.getKind() == TypeKind.TYPEVAR) {
            TypeVariable type = (TypeVariable) tm;
            TypeMirror low = type.getLowerBound();
            if (low != null && low.getKind() != TypeKind.NULL) {
                findUsedGenericTypes(utils, typeArgs, result, low);
            }
            TypeMirror up = type.getUpperBound();
            if (up != null) {
                findUsedGenericTypes(utils, typeArgs, result, up);
            }
            int index = findTypeIndex(utils, typeArgs, type);
            if (index >= 0) {
                result.add(typeArgs.remove(index));
            }
        } else if (tm.getKind() == TypeKind.DECLARED) {
            DeclaredType type = (DeclaredType) tm;
            for (TypeMirror tp : type.getTypeArguments()) {
                findUsedGenericTypes(utils, typeArgs, result, tp);
            }
        } else if (tm.getKind() == TypeKind.WILDCARD) {
            WildcardType type = (WildcardType) tm;
            TypeMirror ex = type.getExtendsBound();
            if (ex != null) {
                findUsedGenericTypes(utils, typeArgs, result, ex);
            }
            TypeMirror su = type.getSuperBound();
            if (su != null) {
                findUsedGenericTypes(utils, typeArgs, result, su);
            }
        }
    }
    
    private static int findTypeIndex(Types utils, List<TypeMirror> typeArgs, TypeMirror type) {
        int i = -1;
        for (TypeMirror typeArg : typeArgs) {
            i++;
            if (utils.isSameType(type, typeArg)) {
                return i;
            }
        }
        return -1;
    }
    
    /**
     * translates list of elements to list of types
     * @param typeParams elements
     * @return types
     */
    public static List<TypeMirror> resolveTypeParamsAsTypes(List<? extends Element> typeParams) {
        if (typeParams.isEmpty()) {
            return Collections.<TypeMirror>emptyList();
        }
        List<TypeMirror> typeArgs = new ArrayList<TypeMirror>(typeParams.size());
        for (Element elm : typeParams) {
            typeArgs.add(elm.asType());
        }
        return typeArgs;
    }
    
    /**
     * finds the nearest enclosing ClassTree on <code>path</code> that
     * is class or interface or enum or annotation type and is or is not annonymous.
     * In case no ClassTree is found the first top level ClassTree is returned.
     * 
     * Especially useful for selecting proper tree to refactor.
     * 
     * @param javac javac
     * @param path path to search
     * @param isClass stop on class
     * @param isInterface  stop on interface
     * @param isEnum stop on enum
     * @param isAnnotation stop on annotation type
     * @param isAnonymous check if class or interface is annonymous
     * @return path to the enclosing ClassTree
     */
    public static TreePath findEnclosingClass(CompilationInfo javac, TreePath path, boolean isClass, boolean isInterface, boolean isEnum, boolean isAnnotation, boolean isAnonymous) {
        Tree selectedTree = path.getLeaf();
        TreeUtilities utils = javac.getTreeUtilities();
        while(true) {
            if (Tree.Kind.CLASS == selectedTree.getKind()) {
                ClassTree classTree = (ClassTree) selectedTree;
                if (isEnum && utils.isEnum(classTree)
                        || isInterface && utils.isInterface(classTree)
                        || isAnnotation && utils.isAnnotation(classTree)
                        || isClass && !(utils.isInterface(classTree) || utils.isEnum(classTree) || utils.isAnnotation(classTree))) {
                    
                    Tree.Kind parentKind = path.getParentPath().getLeaf().getKind();
                    if (isAnonymous || Tree.Kind.NEW_CLASS != parentKind) {
                        break;
                    }
                }
            }
            
            path = path.getParentPath();
            if (path == null) {
                selectedTree = javac.getCompilationUnit().getTypeDecls().get(0);
                path = javac.getTrees().getPath(javac.getCompilationUnit(), selectedTree);
                break;
            }
            selectedTree = path.getLeaf();
        }
        return path;
    }
    
    private static class CompilerTask implements CancellableTask<CompilationController> {
        
        private FileObject f;
        private ElementHandle eh;
        private String name;
        private String fqn;
        private String typeToCheck;
        private boolean typeExist;
        private ElementHandle<TypeElement> enclosingTypeHandle;
        private ElementKind kind;
        private IllegalArgumentException iae;
        
        TreePathHandle handle;
        
        CompilerTask(TreePathHandle handle) {
            this.handle = handle;
        }
        
        CompilerTask(TreePathHandle handle, String fqn) {
            this(handle);
            typeToCheck = fqn;
        }
        
        public void cancel() {
            
        }
        
        public void run(CompilationController cc) {
            try {
                cc.toPhase(JavaSource.Phase.RESOLVED);
            } catch (IOException ex) {
                throw (RuntimeException) new RuntimeException().initCause(ex);
            }
            Element el = handle.resolveElement(cc);
            f = SourceUtils.getFile(el, cc.getClasspathInfo());
            try {
                eh=ElementHandle.create(el);
            } catch (IllegalArgumentException iae)  {
                this.iae = iae;
            }
            name = el.getSimpleName().toString();
            if (el instanceof TypeElement) 
                fqn = ((TypeElement) el).getQualifiedName().toString();
            if (typeToCheck!=null) {
                typeExist = cc.getElements().getTypeElement(typeToCheck)!=null;
            }
                if (el instanceof TypeElement) {
                    enclosingTypeHandle = ElementHandle.create((TypeElement)el);
                } else {
                    enclosingTypeHandle = ElementHandle.create(SourceUtils.getEnclosingTypeElement(el));
                }
            
            kind = el.getKind();
        }
        
        public FileObject getFileObject() {
            return f;
        }
        
        public ElementHandle getElementHandle() {
            return eh;
        }
        
        public String getSimpleName() {
            return name;
        }
        
        public String getQualifiedName() {
            return fqn;
        }
        
        public boolean typeExist() {
            return typeExist;
        }
        
        public ElementHandle<TypeElement> getEnclosingTypeHandle() {
            if (iae!=null)
                throw iae;
            return enclosingTypeHandle;
        }
        
        public ElementKind getElementKind() {
            return kind;
        }
    }
    
    private static class SuperTypesTask implements CancellableTask<CompilationController> {
        
        private Collection<FileObject> files;
        
        TreePathHandle handle;
        
        SuperTypesTask(TreePathHandle handle) {
            this.handle = handle;
        }
        
        public void cancel() {
            
        }
        
        public void run(CompilationController cc) {
            try {
                cc.toPhase(JavaSource.Phase.RESOLVED);
            } catch (IOException ex) {
                throw (RuntimeException) new RuntimeException().initCause(ex);
            }
            Element el = handle.resolveElement(cc);
            files = elementsToFile(getSuperTypes((TypeElement) el, cc, true), cc.getClasspathInfo());
        }
        
        public Collection<FileObject> getFileObjects() {
            return files;
        }
    }
    
/**
     * This is a helper method to provide support for delaying invocations of actions
     * depending on java model. See <a href="http://java.netbeans.org/ui/waitscanfinished.html">UI Specification</a>.
     * <br>Behavior of this method is following:<br>
     * If classpath scanning is not in progress, runnable's run() is called. <br>
     * If classpath scanning is in progress, modal cancellable notification dialog with specified
     * tile is opened.
     * </ul>
     * As soon as classpath scanning finishes, this dialog is closed and runnable's run() is called.
     * This method must be called in AWT EventQueue. Runnable is performed in AWT thread.
     * 
     * @param runnable Runnable instance which will be called.
     * @param actionName Title of wait dialog.
     * @return true action was cancelled <br>
     *         false action was performed
     */
    public static boolean invokeAfterScanFinished(final Runnable runnable , final String actionName) {
        assert SwingUtilities.isEventDispatchThread();
        if (SourceUtils.isScanInProgress()) {
            final ActionPerformer ap = new ActionPerformer(runnable);
            ActionListener listener = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    ap.cancel();
                    waitTask.cancel();
                }
            };
            JLabel label = new JLabel(getString("MSG_WaitScan"), javax.swing.UIManager.getIcon("OptionPane.informationIcon"), SwingConstants.LEFT);
            label.setBorder(new EmptyBorder(12,12,11,11));
            DialogDescriptor dd = new DialogDescriptor(label, actionName, true, new Object[]{getString("LBL_CancelAction", new Object[]{actionName})}, null, 0, null, listener);
            waitDialog = DialogDisplayer.getDefault().createDialog(dd);
            waitDialog.pack();
            waitTask = RequestProcessor.getDefault().post(ap);
            waitDialog.setVisible(true);
            waitTask = null;
            waitDialog = null;
            return ap.hasBeenCancelled();
        } else {
            runnable.run();
            return false;
        }
    }
    
    private static Dialog waitDialog = null;
    private static RequestProcessor.Task waitTask = null;
    
    private static String getString(String key) {
        return NbBundle.getMessage(RetoucheUtils.class, key);
    }

    private static String getString(String key, Object values) {
        return new MessageFormat(getString(key)).format(values);
    }

    

    private static class ActionPerformer implements Runnable {
        private Runnable action;
        private boolean cancel = false;

        ActionPerformer(Runnable a) {
            this.action = a;
        }
        
        public boolean hasBeenCancelled() {
            return cancel;
        }
        
        public void run() {
            try {
                SourceUtils.waitScanFinished();
            } catch (InterruptedException ie) {
                Exceptions.printStackTrace(ie);
            }
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    if (!cancel) {
                        waitDialog.setVisible(false);
                        action.run();
                    }
                }
            });
        }
        
        public void cancel() {
            assert SwingUtilities.isEventDispatchThread();
            // check if the scanning did not finish during cancel
            // invocation - in such case do not set cancel to true
            // and do not try to hide waitDialog window
            if (waitDialog != null) {
                cancel = true;
                waitDialog.setVisible(false);
            }
        }
    }
    
}
