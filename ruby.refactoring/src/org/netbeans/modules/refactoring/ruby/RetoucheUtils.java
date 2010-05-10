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

package org.netbeans.modules.refactoring.ruby;

import java.awt.Color;
import java.io.CharConversionException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.swing.text.AttributeSet;
import javax.swing.text.StyleConstants;
import org.jrubyparser.ast.AliasNode;
import org.jrubyparser.ast.Colon2Node;
import org.jrubyparser.ast.INameNode;
import org.jrubyparser.ast.IScopingNode;
import org.jrubyparser.ast.Node;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
import org.netbeans.modules.ruby.AstUtilities;
import org.netbeans.modules.ruby.RubyIndex;
import org.netbeans.modules.ruby.RubyUtils;
import org.netbeans.modules.ruby.elements.IndexedMethod;
import org.netbeans.modules.ruby.lexer.RubyTokenId;
import org.netbeans.modules.ruby.rubyproject.RubyBaseProject;
import org.netbeans.modules.ruby.rubyproject.RubyProject;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.xml.XMLUtil;

/**
 * Various utilities related to Ruby refactoring; the generic ones are based
 * on the ones from the Java refactoring module.
 * 
 * @author Jan Becicka
 * @author Tor Norbye
 */
public class RetoucheUtils {
    
    private RetoucheUtils() {
    }

    public static BaseDocument getDocument(ParserResult parserResult, FileObject fo) {
        BaseDocument doc = null;

        if (parserResult != null) {
            doc = RubyUtils.getDocument(parserResult);
        }

        if (doc == null) {
            try {
                // Gotta open it first
                DataObject od = DataObject.find(fo);
                EditorCookie ec = od.getCookie(EditorCookie.class);

                if (ec != null) {
                    doc = (BaseDocument)ec.openDocument();
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        return doc;
    }

    public static BaseDocument getDocument(ParserResult info) {
        BaseDocument doc = null;

        if (info != null) {
            doc = RubyUtils.getDocument(info, true);
        }

        return doc;
    }

    /** Compute the names (full and simple, e.g. Foo::Bar and Bar) for the given node, if any, and return as 
     * a String[2] = {name,simpleName} */
    public static String[] getNodeNames(Node node) {
        String name = null;
        String simpleName = null;
       
        if (node instanceof Colon2Node) {
            Colon2Node c2n = (Colon2Node)node;
            simpleName = c2n.getName();
            name = AstUtilities.getFqn(c2n);
        } else if (node instanceof AliasNode) {
            name = AstUtilities.getNameOrValue(((AliasNode)node).getNewName());
        }
        
        if (name == null && node instanceof INameNode) {
            name = ((INameNode)node).getName();
        }
        if (name == null && node instanceof IScopingNode) {
            if (((IScopingNode)node).getCPath() instanceof Colon2Node) {
                Colon2Node c2n = (Colon2Node)((IScopingNode)node).getCPath();
                simpleName = c2n.getName();
                name = AstUtilities.getFqn(c2n);
            } else {
                name = AstUtilities.getClassOrModuleName((IScopingNode)node);
            }
        }
        if (simpleName == null) {
            simpleName = name;
        }
        
        return new String[] { name, simpleName };
    }
    
    public static CloneableEditorSupport findCloneableEditorSupport(ParserResult info) {
        DataObject dob = null;
        try {
            dob = DataObject.find(RubyUtils.getFileObject(info));
        } catch (DataObjectNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }
        return RetoucheUtils.findCloneableEditorSupport(dob);
    }
    
    public static CloneableEditorSupport findCloneableEditorSupport(DataObject dob) {
        Object obj = dob.getCookie(org.openide.cookies.OpenCookie.class);
        if (obj instanceof CloneableEditorSupport) {
            return (CloneableEditorSupport)obj;
        }
        obj = dob.getCookie(org.openide.cookies.EditorCookie.class);
        if (obj instanceof CloneableEditorSupport) {
            return (CloneableEditorSupport)obj;
        }
        return null;
    }

    public static String htmlize(String input) {
        try {
            return XMLUtil.toElementContent(input);
        } catch (CharConversionException cce) {
            Exceptions.printStackTrace(cce);
            return input;
        }
    }

    /** Return the most distant method in the hierarchy that is overriding the given method, or null */
    public static IndexedMethod getOverridingMethod(RubyElementCtx element, ParserResult info) {
        return getOverridingMethod(element, RubyIndex.get(info));
    }

    /** Return the most distant method in the hierarchy that is overriding the given method, or null */
    public static IndexedMethod getOverridingMethod(RubyElementCtx element, FileObject fo) {
        return getOverridingMethod(element, RubyIndex.get(fo));
    }

    private static IndexedMethod getOverridingMethod(RubyElementCtx element, RubyIndex index) {
        if (index == null) {
            return null;
        }
        String fqn = AstUtilities.getFqnName(element.getPath());
        return index.getSuperMethod(fqn, element.getName(), false);
    }


    public static String getHtml(String text) {
        StringBuffer buf = new StringBuffer();
        // TODO - check whether we need ruby highlighting or rhtml highlighting
        TokenHierarchy tokenH = TokenHierarchy.create(text, RubyTokenId.language());
        Lookup lookup = MimeLookup.getLookup(MimePath.get(RubyUtils.RUBY_MIME_TYPE));
        FontColorSettings settings = lookup.lookup(FontColorSettings.class);
        @SuppressWarnings("unchecked")
        TokenSequence<? extends TokenId> tok = tokenH.tokenSequence();
        while (tok.moveNext()) {
            Token<? extends TokenId> token = tok.token();
            String category = token.id().name();
            AttributeSet set = settings.getTokenFontColors(category);
            if (set == null) {
                category = token.id().primaryCategory();
                if (category == null) {
                    category = "whitespace"; //NOI18N
                }
                set = settings.getTokenFontColors(category);                
            }
            String tokenText = htmlize(token.text().toString());
            buf.append(color(tokenText, set));
        }
        return buf.toString();
    }

    private static String color(String string, AttributeSet set) {
        if (set==null) {
            return string;
        }
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

    public static boolean isFileInOpenProject(FileObject file) {
        assert file != null;
        Project p = FileOwnerQuery.getOwner(file);
        if (OpenProjects.getDefault().isProjectOpen(p)) {
            return true;
        }
        return false;
    }
    
    public static boolean isOnSourceClasspath(FileObject fo) {
        Project p = FileOwnerQuery.getOwner(fo);
        if (p==null) {
            return false;
        }
        Project[] opened = OpenProjects.getDefault().getOpenProjects();
        for (int i = 0; i<opened.length; i++) {
            if (p.equals(opened[i]) || opened[i].equals(p)) {
                SourceGroup[] gr = ProjectUtils.getSources(p).getSourceGroups(RubyProject.SOURCES_TYPE_RUBY);
                for (int j = 0; j < gr.length; j++) {
                    if (fo==gr[j].getRootFolder()) {
                        return true;
                    }
                    if (FileUtil.isParentOf(gr[j].getRootFolder(), fo)) {
                        return true;
                    }
                }
                return false;
            }
        }
        return false;
    }

    // XXX Parsing API
//    public static boolean isClasspathRoot(FileObject fo) {
//        ClassPath cp = ClassPath.getClassPath(fo, ClassPath.SOURCE);
//        if (cp != null) {
//            FileObject f = cp.findOwnerRoot(fo);
//            if (f != null) {
//                return fo.equals(f);
//            }
//        }
//
//        return false;
//    }
    
    public static boolean isRefactorable(FileObject file) {
        return RubyUtils.canContainRuby(file) && isFileInOpenProject(file) && isOnSourceClasspath(file);
    }

    public static String getPackageName(FileObject folder) {
        assert folder.isFolder() : "argument must be folder";
        return ClassPath.getClassPath(
                folder, ClassPath.SOURCE)
                .getResourceName(folder, '.', false);
    }

//    public static FileObject getClassPathRoot(URL url) throws IOException {
//        FileObject result = URLMapper.findFileObject(url);
//        File f = FileUtil.normalizeFile(new File(url.getPath()));
//        while (result==null) {
//            result = FileUtil.toFileObject(f);
//            f = f.getParentFile();
//        }
//        return ClassPath.getClassPath(result, ClassPath.SOURCE).findOwnerRoot(result);
//    }
//
//    public static ElementKind getElementKind(RubyElementCtx tph) {
//        return tph.getKind();
//    }
//
//    public static ClasspathInfo getClasspathInfoFor(FileObject ... files) {
//        assert files.length >0;
//        Set<URL> dependentRoots = new HashSet<URL>();
//        for (FileObject fo: files) {
//            Project p = null;
//            if (fo!=null) {
//                p = FileOwnerQuery.getOwner(fo);
//            }
//            if (p!=null) {
//                ClassPath classPath = ClassPath.getClassPath(fo, ClassPath.SOURCE);
//                if (classPath == null) {
//                    return null;
//                }
//                FileObject ownerRoot = classPath.findOwnerRoot(fo);
//                if (ownerRoot != null) {
//                    URL sourceRoot = URLMapper.findURL(ownerRoot, URLMapper.INTERNAL);
//                    dependentRoots.addAll(SourceUtils.getDependentRoots(sourceRoot));
//                    for (SourceGroup root:ProjectUtils.getSources(p).getSourceGroups(RubyProject.SOURCES_TYPE_RUBY)) {
//                        dependentRoots.add(URLMapper.findURL(root.getRootFolder(), URLMapper.INTERNAL));
//                    }
//                } else {
//                    dependentRoots.add(URLMapper.findURL(fo.getParent(), URLMapper.INTERNAL));
//                }
//            } else {
//                for(ClassPath cp: GlobalPathRegistry.getDefault().getPaths(ClassPath.SOURCE)) {
//                    for (FileObject root:cp.getRoots()) {
//                        dependentRoots.add(URLMapper.findURL(root, URLMapper.INTERNAL));
//                    }
//                }
//            }
//        }
//
//        ClassPath rcp = ClassPathSupport.createClassPath(dependentRoots.toArray(new URL[dependentRoots.size()]));
//        ClassPath nullPath = ClassPathSupport.createClassPath(new FileObject[0]);
//        ClassPath boot = files[0]!=null?ClassPath.getClassPath(files[0], ClassPath.BOOT):nullPath;
//        ClassPath compile = files[0]!=null?ClassPath.getClassPath(files[0], ClassPath.COMPILE):nullPath;
//
//        if (boot == null || compile == null) { // 146499
//            return null;
//        }
//
//        ClasspathInfo cpInfo = ClasspathInfo.create(boot, compile, rcp);
//        return cpInfo;
//    }
//
//    public static ClasspathInfo getClasspathInfoFor(RubyElementCtx ctx) {
//        return getClasspathInfoFor(ctx.getFileObject());
//    }
//

    public static Collection<FileObject> getProjectRoots(FileObject fileInProject) {
        Project owner = FileOwnerQuery.getOwner(fileInProject);
        // XXX won't owner always be a RubyBaseProject?
        if (owner instanceof RubyBaseProject) {
            Collection<FileObject> result =  new HashSet<FileObject>();
            result.addAll(Arrays.asList(((RubyBaseProject) owner).getSourceRootFiles()));
            result.addAll(Arrays.asList(((RubyBaseProject) owner).getTestSourceRootFiles()));
            return result;
        } else {
            // fallback
            return QuerySupport.findRoots(fileInProject,
                    null, Collections.<String>emptySet(), Collections.<String>emptySet());
        }
    }

    public static Set<FileObject> getRubyFilesInProject(FileObject fileInProject) {
        Set<FileObject> files = new HashSet<FileObject>(100);
        Project owner = FileOwnerQuery.getOwner(fileInProject);
        Collection<FileObject> sourceRoots = getProjectRoots(fileInProject);
        for (FileObject root : sourceRoots) {
            String name = root.getName();
            // Skip non-refactorable parts in renaming
            if (name.equals("vendor") || name.equals("script")) { // NOI18N
                continue;
            }
            addRubyFiles(files, root);
        }

        return files;
    }

    
    private static void addRubyFiles(Set<FileObject> files, FileObject f) {
        if (f.isFolder()) {
            for (FileObject child : f.getChildren()) {
                addRubyFiles(files, child);
            }
        } else if (RubyUtils.canContainRuby(f)) {
            files.add(f);
        }
    }
}
