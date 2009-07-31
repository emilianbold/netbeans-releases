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
package org.netbeans.modules.refactoring.php.findusages;

import java.awt.Color;
import java.io.CharConversionException;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import javax.swing.text.AttributeSet;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyleConstants;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.php.editor.lexer.LexUtilities;
import org.netbeans.modules.php.editor.lexer.PHPTokenId;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.Expression;
import org.netbeans.modules.php.editor.parser.astnodes.Include;
import org.netbeans.modules.php.editor.parser.astnodes.ParenthesisExpression;
import org.netbeans.modules.php.editor.parser.astnodes.Program;
import org.netbeans.modules.php.editor.parser.astnodes.Scalar;
import org.netbeans.modules.php.editor.parser.astnodes.Scalar.Type;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultVisitor;
import org.netbeans.modules.php.project.api.PhpSourcePath;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.Exceptions;
import org.openide.windows.TopComponent;
import org.openide.xml.XMLUtil;

/**
 * Various utilies related to Php refactoring; the generic ones are based
 * on the ones from the Java refactoring module.
 * 
 * @author Jan Becicka, Tor Norbye, Jan Lahoda, Radek Matous
 */
public class RefactoringUtils {

    public static boolean isPhpFile(FileObject fo) {
        return PhpSourcePath.MIME_TYPE.equals(fo.getMIMEType());
    }

    public static Program getRoot(ParserResult info) {
        return (info instanceof PHPParseResult) ? ((PHPParseResult)info).getProgram() : null;
    }

    public static Source getSource(Document doc) {
        Source source = Source.create(doc);
        return source;
    }

    public static CloneableEditorSupport findCloneableEditorSupport(FileObject fo) {
        DataObject dob = null;
        try {
            dob = DataObject.find(fo);
        } catch (DataObjectNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }
        return RefactoringUtils.findCloneableEditorSupport(dob);
    }

    public static CloneableEditorSupport findCloneableEditorSupport(DataObject dob) {
        Object obj = dob.getCookie(org.openide.cookies.OpenCookie.class);
        if (obj instanceof CloneableEditorSupport) {
            return (CloneableEditorSupport) obj;
        }
        obj = dob.getCookie(org.openide.cookies.EditorCookie.class);
        if (obj instanceof CloneableEditorSupport) {
            return (CloneableEditorSupport) obj;
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

    public static String getHtml(String text) {
        StringBuffer buf = new StringBuffer();
        // TODO - check whether we need Js highlighting or rhtml highlighting
        TokenHierarchy tokenH = TokenHierarchy.create(text, PHPTokenId.language());
        Lookup lookup = MimeLookup.getLookup(MimePath.get(PhpSourcePath.MIME_TYPE));
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
        if (set == null) {
            return string;
        }
        if (string.trim().length() == 0) {
            return string.replace(" ", "&nbsp;").replace("\n", "<br>"); //NOI18N

        }
        StringBuffer buf = new StringBuffer(string);
        if (StyleConstants.isBold(set)) {
            buf.insert(0, "<b>"); //NOI18N

            buf.append("</b>"); //NOI18N

        }
        if (StyleConstants.isItalic(set)) {
            buf.insert(0, "<i>"); //NOI18N

            buf.append("</i>"); //NOI18N

        }
        if (StyleConstants.isStrikeThrough(set)) {
            buf.insert(0, "<s>"); // NOI18N

            buf.append("</s>"); // NOI18N

        }
        buf.insert(0, "<font color=" + getHTMLColor(StyleConstants.getForeground(set)) + ">"); //NOI18N

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
        return OpenProjects.getDefault().isProjectOpen(p);
    }

    public static boolean isOnSourceClasspath(FileObject fo) {
        Project p = FileOwnerQuery.getOwner(fo);
        if (p == null) {
            return false;
        }
        Project[] opened = OpenProjects.getDefault().getOpenProjects();
        for (int i = 0; i < opened.length; i++) {
            if (p.equals(opened[i]) || opened[i].equals(p)) {
                //SourceGroup[] gr = ProjectUtils.getSources(p).getSourceGroups(JsProject.SOURCES_TYPE_Js);
                SourceGroup[] gr = ProjectUtils.getSources(p).getSourceGroups(Sources.TYPE_GENERIC);
                for (int j = 0; j < gr.length; j++) {
                    if (fo == gr[j].getRootFolder()) {
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

    public static boolean isClasspathRoot(FileObject fo) {
        ClassPath cp = ClassPath.getClassPath(fo, ClassPath.SOURCE);
        if (cp != null) {
            FileObject f = cp.findOwnerRoot(fo);
            if (f != null) {
                return fo.equals(f);
            }
        }

        return false;
    }

    public static boolean isRefactorable(FileObject file) {
        return isPhpFile(file) && isFileInOpenProject(file) && isOnSourceClasspath(file);
    }

    public static String getPackageName(FileObject folder) {
        assert folder.isFolder() : "argument must be folder";
        return ClassPath.getClassPath(
                folder, ClassPath.SOURCE).getResourceName(folder, '.', false);
    }

    public static String getPackageName(URL url) {
        File f = null;
        try {
            f = FileUtil.normalizeFile(new File(url.toURI()));
        } catch (URISyntaxException uRISyntaxException) {
            throw new IllegalArgumentException("Cannot create package name for url " + url);
        }
        String suffix = "";

        do {
            FileObject fo = FileUtil.toFileObject(f);
            if (fo != null) {
                if ("".equals(suffix)) {
                    return getPackageName(fo);
                }
                String prefix = getPackageName(fo);
                return prefix + ("".equals(prefix) ? "" : ".") + suffix;
            }
            if (!"".equals(suffix)) {
                suffix = "." + suffix;
            }
            suffix = URLDecoder.decode(f.getPath().substring(f.getPath().lastIndexOf(File.separatorChar) + 1)) + suffix;
            f = f.getParentFile();
        } while (f != null);
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
            if (result != null) {
                return result;
            }
            File f = new File(url.toURI());

            result = FileUtil.createFolder(f);
            return result;
        } catch (URISyntaxException ex) {
            throw (IOException) new IOException().initCause(ex);
        }
    }

//    public static ClasspathInfo getClasspathInfoFor(FileObject... files) {
//        assert files.length > 0;
//        Set<URL> dependentRoots = new HashSet<URL>();
//        for (FileObject fo : files) {
//            Project p = null;
//            if (fo != null) {
//                p = FileOwnerQuery.getOwner(fo);
//            }
//            if (p != null) {
//                assert fo != null;
//                ClassPath classPath = ClassPath.getClassPath(fo, ClassPath.SOURCE);
//                if (classPath == null) {
//                    Logger.getLogger(RefactoringUtils.class.getName()).log(
//                            Level.WARNING, "ClassPath.getClassPath(fo, ClassPath.SOURCE) == null for fo: " + fo.getPath());//NOI18N
//                    continue;
//                }
//                URL sourceRoot = URLMapper.findURL(classPath.findOwnerRoot(fo), URLMapper.INTERNAL);
//                dependentRoots.addAll(SourceUtils.getDependentRoots(sourceRoot));
//                for (SourceGroup root : ProjectUtils.getSources(p).getSourceGroups(Sources.TYPE_GENERIC)) {
//                    dependentRoots.add(URLMapper.findURL(root.getRootFolder(), URLMapper.INTERNAL));
//                }
//            } else {
//                for (ClassPath cp : GlobalPathRegistry.getDefault().getPaths(PhpProject.SOURCE_CP)) {
//                    for (FileObject root : cp.getRoots()) {
//                        dependentRoots.add(URLMapper.findURL(root, URLMapper.INTERNAL));
//                    }
//                }
//            }
//        }
//
//        ClassPath rcp = ClassPathSupport.createClassPath(dependentRoots.toArray(new URL[dependentRoots.size()]));
//        ClassPath nullPath = ClassPathSupport.createClassPath(new FileObject[0]);
//        ClassPath boot = files[0] != null ? ClassPath.getClassPath(files[0], ClassPath.BOOT) : nullPath;
//        ClassPath compile = files[0] != null ? ClassPath.getClassPath(files[0], ClassPath.COMPILE) : nullPath;
//        if (boot == null || compile == null) { // 146499
//            return null;
//        }
//
//        ClasspathInfo cpInfo = ClasspathInfo.create(boot, compile, rcp);
//        return cpInfo;
//    }

    public static boolean isOutsidePhp(Lookup lookup, FileObject fo) {
        if (RefactoringUtils.isPhpFile(fo)) {
            EditorCookie ec = lookup.lookup(EditorCookie.class);
            if (isFromEditor(ec)) {
                JTextComponent textC = ec.getOpenedPanes()[0];
                Document d = textC.getDocument();
                if (!(d instanceof BaseDocument)) {
                    return true;
                }
                int caret = textC.getCaretPosition();
                if (LexUtilities.getToken((BaseDocument) d, caret) == null) {
                    // Not in PHP code!
                    return true;
                }

            }
        }

        return false;
    }

    static boolean isFromEditor(EditorCookie ec) {
        if (ec != null && ec.getOpenedPanes() != null) {
            TopComponent activetc = TopComponent.getRegistry().getActivated();
            if (activetc instanceof CloneableEditorSupport.Pane) {
                return true;
            }
        }
        return false;
    }
    
    public static List<ASTNode> underCaret(ParserResult info, final int offset) {
        class Result extends Error {

            private Stack<ASTNode> result;

            public Result(Stack<ASTNode> result) {
                this.result = result;
            }

            @Override
            public Throwable fillInStackTrace() {
                return this;
            }
        }
        try {
            new DefaultVisitor() {

                private Stack<ASTNode> s = new Stack<ASTNode>();

                @Override
                public void scan(ASTNode node) {
                    if (node == null) {
                        return;
                    }

                    if (node.getStartOffset() <= offset && offset <= node.getEndOffset()) {
                        s.push(node);
                        super.scan(node);
                        throw new Result(s);
                    }
                }
            }.scan(RefactoringUtils.getRoot(info));
        } catch (Result r) {
            return new LinkedList<ASTNode>(r.result);
        }

        return Collections.emptyList();
    }

    public static boolean isQuoted(String value) {
        return value.length() >= 2 &&
                (value.startsWith("\"") || value.startsWith("'")) &&
                (value.endsWith("\"") || value.endsWith("'"));
    }

    public static String dequote(String value) {
        assert isQuoted(value);

        return value.substring(1, value.length() - 1);
    }

    public static FileObject resolveInclude(ParserResult info, Include include) {
        Expression e = include.getExpression();

        if (e instanceof ParenthesisExpression) {
            e = ((ParenthesisExpression) e).getExpression();
        }

        if (e instanceof Scalar) {
            Scalar s = (Scalar) e;

            if (Type.STRING == s.getScalarType()) {
                String fileName = s.getStringValue();
                fileName = fileName.length() >= 2 ? fileName.substring(1, fileName.length() - 1) : fileName;//TODO: not nice

                return resolveRelativeFile(info, fileName);
            }
        }

        return null;
    }

    private static FileObject resolveRelativeFile(ParserResult info, String name) {
        PhpSourcePath psp = null;
        Project p = FileOwnerQuery.getOwner(info.getSnapshot().getSource().getFileObject());

        if (p != null) {
            psp = p.getLookup().lookup(PhpSourcePath.class);
        }

        while (true) {
            FileObject result;

            if (psp != null) {
                result = psp.resolveFile(info.getSnapshot().getSource().getFileObject().getParent(), name);
            } else {
                result = info.getSnapshot().getSource().getFileObject().getParent().getFileObject(name);
            }

            if (result != null) {
                return result;
            }

            //try to strip a directory from the "name":
            int slash = name.indexOf('/');

            if (slash != (-1)) {
                name = name.substring(slash + 1);
            } else {
                return null;
            }
        }
    }

    public static FileObject getFile(Document doc) {
        Object o = doc.getProperty(Document.StreamDescriptionProperty);

        if (o instanceof DataObject) {
            DataObject od = (DataObject) o;

            return od.getPrimaryFile();
        }

        return null;
    }
}
