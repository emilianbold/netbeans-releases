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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.refactoring.java;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.ModifiersTree;
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
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
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
import org.netbeans.api.java.classpath.ClassPath.Entry;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.api.java.queries.SourceForBinaryQuery.Result;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.Comment;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.java.source.WorkingCopy;
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
    
    private static final String JAVA_MIME_TYPE = "text/x-java"; // NOI18N
    public static volatile boolean cancel = false;
    private static final Logger LOG = Logger.getLogger(RetoucheUtils.class.getName());
    private static final RequestProcessor RP = new RequestProcessor(RetoucheUtils.class.getName(), 1, false, false);
    
    public static String htmlize(String input) {
        String temp = input.replace("<", "&lt;"); // NOI18N
        temp = temp.replace(">", "&gt;"); // NOI18N
        return temp;
    }
    
    public static Collection<ExecutableElement> getOverridenMethods(ExecutableElement e, CompilationInfo info) {
        return getOverridenMethods(e, info.getElementUtilities().enclosingTypeElement(e), info);
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
        cancel = false;
        LinkedList<ElementHandle<TypeElement>> elements = new LinkedList<ElementHandle<TypeElement>>(
                implementorsQuery(idx, ElementHandle.create(el)));
        Set<ElementHandle<TypeElement>> result = new HashSet<ElementHandle<TypeElement>>();
        while (!elements.isEmpty()) {
            if (cancel) {
                cancel = false;
                return Collections.emptySet();
            }
            ElementHandle<TypeElement> next = elements.removeFirst();
            if (!result.add(next)) {
                // it is a duplicate; do not query again
                continue;
            }
            Set<ElementHandle<TypeElement>> foundElements = implementorsQuery(idx, next);
            elements.addAll(foundElements);
        }
        return result;
    }

    private static Set<ElementHandle<TypeElement>> implementorsQuery(ClassIndex idx, ElementHandle<TypeElement> next) {
        return idx.getElements(next,
                    EnumSet.of(ClassIndex.SearchKind.IMPLEMENTORS),
                    EnumSet.of(ClassIndex.SearchScope.SOURCE, ClassIndex.SearchScope.DEPENDENCIES));
    }

    public static Collection<ExecutableElement> getOverridingMethods(ExecutableElement e, CompilationInfo info) {
        Collection<ExecutableElement> result = new ArrayList();
        TypeElement parentType = (TypeElement) e.getEnclosingElement();
        //XXX: Fixme IMPLEMENTORS_RECURSIVE were removed
        Set<ElementHandle<TypeElement>> subTypes = getImplementorsAsHandles(info.getClasspathInfo().getClassIndex(), info.getClasspathInfo(), parentType);
        for (ElementHandle<TypeElement> subTypeHandle: subTypes){
            TypeElement type = subTypeHandle.resolve(info);
            if (type == null) {
                // #120577: log info to find out what is going wrong
                FileObject file = SourceUtils.getFile(subTypeHandle, info.getClasspathInfo());
                throw new NullPointerException("#120577: Cannot resolve " + subTypeHandle + "; file: " + file);
            }
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
            return string.replace(" ", "&nbsp;").replace("\n", "<br>"); //NOI18N
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
            buf.insert(0,"<s>"); // NOI18N
            buf.append("</s>"); // NOI18N
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
        if (f==null)
            return false;
        Project p = FileOwnerQuery.getOwner(f);
        return isOpenProject(p);
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
        if (p == null) {
            return false;
        }
        return isOpenProject(p);
    }
    
    public static boolean isOnSourceClasspath(FileObject fo) {
        Project p = FileOwnerQuery.getOwner(fo);
        if (p==null) 
            return false;

        //workaround for 143542
        Project[] opened = OpenProjects.getDefault().getOpenProjects();
        for (Project pr : opened) {
            for (SourceGroup sg : ProjectUtils.getSources(pr).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA)) {
                if (fo==sg.getRootFolder() || (FileUtil.isParentOf(sg.getRootFolder(), fo) && sg.contains(fo))) {
                    return ClassPath.getClassPath(fo, ClassPath.SOURCE) != null;
                }
            }
        }
        return false;
        //end of workaround
        //return ClassPath.getClassPath(fo, ClassPath.SOURCE)!=null;
    }

    public static boolean isClasspathRoot(FileObject fo) {
        ClassPath cp = ClassPath.getClassPath(fo, ClassPath.SOURCE);
        return cp != null ? fo.equals(cp.findOwnerRoot(fo)) : false;
    }
    
    public static boolean isRefactorable(FileObject file) {
        return isJavaFile(file) && isFileInOpenProject(file) && isOnSourceClasspath(file);
    }
    
    public static String getPackageName(FileObject folder) {
        assert folder.isFolder() : "argument must be folder";
        ClassPath cp = ClassPath.getClassPath(folder, ClassPath.SOURCE);
        if (cp == null) {
            // see http://www.netbeans.org/issues/show_bug.cgi?id=159228
            throw new IllegalStateException(String.format("No classpath for %s.", folder)); // NOI18N
        }
        return cp.getResourceName(folder, '.', false);
    }
    
    public static String getPackageName(CompilationUnitTree unit) {
        assert unit!=null;
        ExpressionTree name = unit.getPackageName();
        if (name==null) {
            //default package
            return "";
        }
        return name.toString();
    }
    
    public static String getPackageName(URL url) {
        File f = null;
        try {
            String path = URLDecoder.decode(url.getPath(), "utf-8"); // NOI18N
            f = FileUtil.normalizeFile(new File(path));
        } catch (UnsupportedEncodingException u) {
            throw new IllegalArgumentException("Cannot create package name for url " + url); // NOI18N
        }
        String suffix = "";
        
        do {
            FileObject fo = FileUtil.toFileObject(f);
            if (fo != null) {
                if ("".equals(suffix))
                    return getPackageName(fo);
                String prefix = getPackageName(fo);
                return prefix + ("".equals(prefix)?"":".") + suffix; // NOI18N
            }
            if (!"".equals(suffix)) {
                suffix = "." + suffix; // NOI18N
            }
            try {
                suffix = URLDecoder.decode(f.getPath().substring(f.getPath().lastIndexOf(File.separatorChar) + 1), "utf-8") + suffix; // NOI18N
            } catch (UnsupportedEncodingException u) {
                throw new IllegalArgumentException("Cannot create package name for url " + url); // NOI18N
            }
            f = f.getParentFile();
        } while (f!=null);
        throw new IllegalArgumentException("Cannot create package name for url " + url); // NOI18N
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
        File f = result != null ? null : FileUtil.normalizeFile(new File(URLDecoder.decode(url.getPath(), "UTF-8"))); //NOI18N
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

    private static boolean isOpenProject(Project p) {
        return OpenProjects.getDefault().isProjectOpen(p);
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
            assert source!=null:"JavaSource.forFileObject(" + tph.getFileObject().getPath() + ") \n returned null";
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
            assert source!=null:"JavaSource.forFileObject(" + tph.getFileObject().getPath() + ") \n returned null";
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
            assert source!=null:"JavaSource.forFileObject(" + tph.getFileObject().getPath() + ") \n returned null";
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
            assert source!=null:"JavaSource.forFileObject(" + handle.getFileObject().getPath() + ") \n returned null";
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
            assert source!=null:"JavaSource.forFileObject(" + tph.getFileObject().getPath() + ") \n returned null";
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
            assert source!=null:"JavaSource.forFileObject(" + tph.getFileObject().getPath() + ") \n returned null";
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
        return getClasspathInfoFor(dependencies, false, files);
    }
    
    public static ClasspathInfo getClasspathInfoFor(boolean dependencies, boolean backSource, FileObject ... files ) {
        assert files.length >0;
        Set<URL> dependentRoots = new HashSet();
        for (FileObject fo: files) {
            Project p = null;
            FileObject ownerRoot = null;
            if (fo != null) {
                p = FileOwnerQuery.getOwner(fo);
                ClassPath cp = ClassPath.getClassPath(fo, ClassPath.SOURCE);
                if (cp!=null) {
                    ownerRoot = cp.findOwnerRoot(fo);
                }
            }
            if (p != null && ownerRoot != null) {
                URL sourceRoot = URLMapper.findURL(ownerRoot, URLMapper.INTERNAL);
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
        
        if (backSource) {
            for (FileObject file : files) {
                if (file!=null) {
                    ClassPath source = ClassPath.getClassPath(file, ClassPath.COMPILE);
                    for (Entry root : source.entries()) {
                        Result r = SourceForBinaryQuery.findSourceRoots(root.getURL());
                        for (FileObject root2 : r.getRoots()) {
                            dependentRoots.add(URLMapper.findURL(root2, URLMapper.INTERNAL));
                        }
                    }
                }
            }
        }
        
        ClassPath rcp = ClassPathSupport.createClassPath(dependentRoots.toArray(new URL[dependentRoots.size()]));
        ClassPath nullPath = ClassPathSupport.createClassPath(new FileObject[0]);
        ClassPath boot = files[0]!=null?ClassPath.getClassPath(files[0], ClassPath.BOOT):nullPath;
        ClassPath compile = files[0]!=null?ClassPath.getClassPath(files[0], ClassPath.COMPILE):nullPath;
        //When file[0] is a class file, there is no compile cp but execute cp
        //try to get it
        if (compile == null) {
            compile = ClassPath.getClassPath(files[0], ClassPath.EXECUTE);
        }
        //If no cp found at all log the file and use nullPath since the ClasspathInfo.create
        //doesn't accept null compile or boot cp.
        if (compile == null) {
            LOG.warning ("No classpath for: " + FileUtil.getFileDisplayName(files[0]) + " " + FileOwnerQuery.getOwner(files[0]));
            compile = nullPath;
        }
        ClasspathInfo cpInfo = ClasspathInfo.create(boot, compile, rcp);
        return cpInfo;
    }
    
    public static ClasspathInfo getClasspathInfoFor(TreePathHandle ... handles) {
        FileObject[] result = new FileObject[handles.length];
        int i=0;
        for (TreePathHandle handle:handles) {
            FileObject fo = getFileObject(handle);
            if (i==0 && fo==null) {
                result = new FileObject[handles.length+1];
                result[i++] = handle.getFileObject();
            }
            result[i++] = fo;
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

    /**
     * Copies javadoc from <code>elm</code> to newly created <code>tree</code>. 
     * @param elm element containing some javadoc
     * @param tree newly created tree where the javadoc should be copied to
     * @param wc working copy where the tree belongs to
     */
    public static void copyJavadoc(Element elm, Tree tree, WorkingCopy wc) {
        TreeMaker make = wc.getTreeMaker();
        String jdtxt = wc.getElements().getDocComment(elm);
        if (jdtxt != null) {
            make.addComment(tree, Comment.create(Comment.Style.JAVADOC, -1, -1, -1, jdtxt), true);
        }
    }
    
    private static class CompilerTask implements CancellableTask<CompilationController> {
        
        private FileObject f;
        private ElementHandle eh;
        private String name;
        private String fqn;
        private String typeToCheck;
        private boolean typeExist;
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
            if (el == null) {
                return;
            }
            f = SourceUtils.getFile(el, cc.getClasspathInfo());
            try {
                eh=ElementHandle.create(el);
            } catch (IllegalArgumentException iae)  {
                this.iae = iae;
            }
            name = el.getSimpleName().toString();
            kind = el.getKind();
            if (kind.isClass() || kind.isInterface()) {
                fqn = ((TypeElement) el).getQualifiedName().toString();
            }

            if (typeToCheck!=null) {
                typeExist = cc.getElements().getTypeElement(typeToCheck)!=null;
            }
            
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
            //100ms is workaround for 127536
            waitTask = RP.post(ap, 100);
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
                        if (waitDialog != null) {
                            waitDialog.setVisible(false);
                            waitDialog.dispose();
                        }
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
                waitDialog.dispose();
            }
        }
    }

    //XXX: copied from SourceUtils.addImports. Ideally, should be on one place only:
    public static CompilationUnitTree addImports(CompilationUnitTree cut, List<String> toImport, TreeMaker make)
        throws IOException {
        // do not modify the list given by the caller (may be reused or immutable).
        toImport = new ArrayList<String>(toImport);
        Collections.sort(toImport);

        List<ImportTree> imports = new ArrayList<ImportTree>(cut.getImports());
        int currentToImport = toImport.size() - 1;
        int currentExisting = imports.size() - 1;

        while (currentToImport >= 0 && currentExisting >= 0) {
            String currentToImportText = toImport.get(currentToImport);

            while (currentExisting >= 0 && (imports.get(currentExisting).isStatic() || imports.get(currentExisting).getQualifiedIdentifier().toString().compareTo(currentToImportText) > 0))
                currentExisting--;

            if (currentExisting >= 0) {
                imports.add(currentExisting+1, make.Import(make.Identifier(currentToImportText), false));
                currentToImport--;
            }
        }
        // we are at the head of import section and we still have some imports
        // to add, put them to the very beginning
        while (currentToImport >= 0) {
            String importText = toImport.get(currentToImport);
            imports.add(0, make.Import(make.Identifier(importText), false));
            currentToImport--;
        }
        // return a copy of the unit with changed imports section
        return make.CompilationUnit(cut.getPackageName(), imports, cut.getTypeDecls(), cut.getSourceFile());
    }

    /**
     * transforms passed modifiers to abstract form
     * @param make a tree maker
     * @param oldMods modifiers of method or class
     * @return the abstract form of ModifiersTree
     */
    public static ModifiersTree makeAbstract(TreeMaker make, ModifiersTree oldMods) {
        if (oldMods.getFlags().contains(Modifier.ABSTRACT)) {
            return oldMods;
        }
        Set<Modifier> flags = new HashSet<Modifier>(oldMods.getFlags());
        flags.add(Modifier.ABSTRACT);
        flags.remove(Modifier.FINAL);
        return make.Modifiers(flags, oldMods.getAnnotations());
    }
}
