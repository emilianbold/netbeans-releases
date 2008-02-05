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
package org.netbeans.modules.spring.beans.editor;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.ui.ElementOpen;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.TokenItem;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.xml.text.syntax.SyntaxElement;
import org.netbeans.modules.xml.text.syntax.XMLSyntaxSupport;
import org.netbeans.modules.xml.text.syntax.dom.EmptyTag;
import org.netbeans.modules.xml.text.syntax.dom.StartTag;
import org.netbeans.modules.xml.text.syntax.dom.Tag;
import org.openide.awt.StatusDisplayer;
import org.openide.cookies.EditCookie;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.LineCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.text.Line;
import org.openide.text.NbDocument;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Utility methods for Spring XML configuration file editor
 * 
 * Inspired by BeansEditorUtils class from SpringIDE
 * 
 * @author Rohan Ranade (Rohan.Ranade@Sun.COM)
 */
public final class SpringXMLConfigEditorUtils {

    public static final String BEAN_NAME_DELIMITERS = ",; "; // NOI18N

    public enum Public {
        YES,
        NO,
        DONT_CARE
    };
        
    public enum Static {
        YES,
        NO,
        DONT_CARE
    };
    
    private SpringXMLConfigEditorUtils() {
    }
    
    public static String getBeanPropertySetterName(String property) {
        char[] buffer = property.toCharArray();
        buffer[0] = Character.toUpperCase(buffer[0]);

        return "set" + String.valueOf(buffer); // NOI18N
    }

    public static String getBeanFactoryMethod(Tag tag) {
        Node bean = getBean(tag);
        if (bean != null) {
            NamedNodeMap attribs = bean.getAttributes();
            if (attribs != null && attribs.getNamedItem("factory-method") != null) { // NOI18N
                return attribs.getNamedItem("factory-method").getNodeValue(); // NOI18N
            }
        }

        return null;
    }

    public static Node getBean(Tag tag) {
        if (tag == null) {
            return null;
        }

        if (tag.getNodeName().equals("bean")) { // NOI18N
            return tag;
        }

        if (tag.getNodeName().equals("lookup-method") || tag.getNodeName().equals("replaced-method") || tag.getNodeName().equals("property")) { // NOI18N
            Node parent = tag.getParentNode();
            if (parent.getNodeName().equals("bean")) { // NOI18N
                return parent;
            } else {
                return null;
            }
        }

        return null;

    }

    public static String getBeanClassName(Tag tag) {
        Node bean = getBean(tag);
        if (bean != null) {
            NamedNodeMap attribs = bean.getAttributes();
            if (attribs != null && attribs.getNamedItem("class") != null) { // NOI18N
                return attribs.getNamedItem("class").getNodeValue(); // NOI18N
            }
        }

        return null;
    }

    public static JavaSource getJavaSource(Document doc) {
        FileObject fileObject = NbEditorUtilities.getFileObject(doc);
        if (fileObject == null) {
            return null;
        }
        Project project = FileOwnerQuery.getOwner(fileObject);
        if (project == null) {
            return null;
        }
        // XXX this only works correctly with projects with a single sourcepath,
        // but we don't plan to support another kind of projects anyway (what about Maven?).
        SourceGroup[] sourceGroups = ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        for (SourceGroup sourceGroup : sourceGroups) {
            return JavaSource.create(ClasspathInfo.create(sourceGroup.getRootFolder()));
        }
        return null;
    }

    public static void findAndOpenJavaClass(final String fqn, Document doc) {
        final JavaSource js = getJavaSource(doc);
        if (js != null) {
            try {
                js.runUserActionTask(new Task<CompilationController>() {
                    public void run(CompilationController cc) throws Exception {
                        boolean opened = false;
                        TypeElement element = cc.getElements().getTypeElement(fqn.trim());
                        if (element != null) {
                            opened = !ElementOpen.open(js.getClasspathInfo(), element);
                        }
                        if (!opened) {
                            String msg = NbBundle.getMessage(SpringXMLConfigEditorUtils.class, "LBL_SourceNotFound", fqn);
                            StatusDisplayer.getDefault().setStatusText(msg);
                        }
                    }
                }, false);
            } catch (IOException ex) {
                Logger.getLogger("global").log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
    }

    public static ElementHandle<ExecutableElement> findMethod(Document doc, final String classFqn,
            final String methodName, int argCount, Public publicFlag, Static staticFlag) {
        JavaSource js = getJavaSource(doc);
        if (js != null) {
            try {
                MethodFinder methodFinder = new MethodFinder(classFqn, methodName, argCount, publicFlag, staticFlag);
                js.runUserActionTask(methodFinder, false);
                return methodFinder.getMethodHandle();
            } catch (IOException ex) {
                Logger.getLogger("global").log(Level.SEVERE, ex.getMessage(), ex);
            }
        }

        return null;
    }

    /**
     * Open the specified method of the specified class in the editor
     * 
     * @param doc The document on from which the java model context is to be created
     * @param classFqn fully qualified name of the class whose method is to be opened in the editor
     * @param methodName name of the method
     * @param argCount number of arguments that the method has (-1 if caller doesn't care)
     * @param publicFlag YES if the method is public, NO if not, DONT_CARE if caller doesn't care
     * @param staticFlag YES if the method is static, NO if not, DONT_CARE if caller doesn't care
     */
    public static void openMethodInEditor(Document doc, final String classFqn,
            final String methodName, int argCount, Public publicFlag, Static staticFlag) {
        if (classFqn == null || methodName == null || doc == null) {
            return;
        }

        final JavaSource js = getJavaSource(doc);
        if (js == null) {
            return;
        }

        final ElementHandle<ExecutableElement> eh = findMethod(doc, classFqn, methodName, argCount, publicFlag, staticFlag);
        if (eh != null) {
            try {
                js.runUserActionTask(new Task<CompilationController>() {

                    public void run(CompilationController cc) throws Exception {
                        ExecutableElement ee = eh.resolve(cc);
                        ElementOpen.open(js.getClasspathInfo(), ee);
                    }
                }, false);
            } catch (IOException ex) {
                Logger.getLogger("global").log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
    }
    
    public static final Tag getDocumentRoot(Document doc) {
        Tag retTag = null;
        try {
            XMLSyntaxSupport syntaxSupport =
                    (XMLSyntaxSupport) ((BaseDocument) doc).getSyntaxSupport();
            TokenItem tok = syntaxSupport.getTokenChain(0,1);
            if(tok != null) {
                while(!ContextUtilities.isTagToken(tok)) {
                    tok = tok.getNext();
                }
                SyntaxElement element = syntaxSupport.getElementChain(tok.getOffset()+tok.getImage().length());
                if(element instanceof StartTag || element instanceof EmptyTag) {
                    Tag tag = (Tag) element;
                    if(tag.getParentNode() instanceof org.w3c.dom.Document) {
                        return tag;
                    }
                }
            }
        } catch (BadLocationException ex) {
            // No context support available in this case
        }
        
        return retTag;
    }
    
    public static final boolean hasAttribute(Node node, String attributeName) {
        return (node != null && node.getAttributes() != null && node.getAttributes().getNamedItem(attributeName) != null);
    }

    public static final String getAttribute(Node node, String attributeName) {
        if (hasAttribute(node, attributeName)) {
            return node.getAttributes().getNamedItem(attributeName).getNodeValue();
        }
        return null;
    }
    
    public static boolean openFile(File file, int offset) {
        FileObject fo = FileUtil.toFileObject(file);
        if (fo != null) {
            return openFile(fo, offset);
        }
        return false;
    }
    
    public static boolean openFile(FileObject fo, int offset) {
        DataObject dataObject;
        boolean opened = false;
        try {
            dataObject = DataObject.find(fo);
            if (offset > 0) {
                opened = openFileAtOffset(dataObject, offset);
            }
        } catch (IOException e) {
            Exceptions.printStackTrace(e);
            return false;
        }
        if (opened) {
            return true;
        } else {
            OpenCookie oc = dataObject.getCookie(org.openide.cookies.OpenCookie.class);
            if (oc != null) {
                oc.open();
                return true;
            }
        }
        return false;
    }
    
    private static boolean openFileAtOffset(DataObject dataObject, int offset) throws IOException {
        EditorCookie ec = dataObject.getCookie(EditorCookie.class);
        LineCookie lc = dataObject.getCookie(LineCookie.class);
        if (ec != null && lc != null) {
            StyledDocument doc = ec.openDocument();
            if (doc != null) {
                int lineNumber = NbDocument.findLineNumber(doc, offset);
                if (lineNumber != -1) {
                    Line line = lc.getLineSet().getCurrent(lineNumber);
                    if (line != null) {
                        int lineOffset = NbDocument.findLineOffset(doc, lineNumber);
                        int column = offset - lineOffset;
                        line.show(Line.SHOW_GOTO, column);
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    private static final class MethodFinder implements Task<CompilationController> {

        private String classFqn;
        private String methodName;
        private int argCount;
        private Public publicFlag;
        private Static staticFlag;
        private ElementHandle<ExecutableElement> methodHandle;

        public MethodFinder(String classFqn, String methodName, int argCount, Public publicFlag, Static staticFlag) {
            this.classFqn = classFqn;
            this.methodName = methodName;
            this.argCount = argCount;
            this.publicFlag = publicFlag;
            this.staticFlag = staticFlag;
        }

        public void run(CompilationController cc) throws Exception {
            cc.toPhase(Phase.ELEMENTS_RESOLVED);
            Elements elements = cc.getElements();
            TypeElement element = elements.getTypeElement(classFqn.trim());
            while (element != null) {
                List<ExecutableElement> methods = ElementFilter.methodsIn(element.getEnclosedElements());
                for (ExecutableElement method : methods) {
                    // name match
                    String mName = method.getSimpleName().toString();
                    if (!mName.equals(methodName)) {
                        continue;
                    }

                    // argument match
                    if (this.argCount != -1) {
                        int actualArgCount = method.getParameters().size();
                        if (actualArgCount != argCount) {
                            continue;
                        }
                    }

                    // static match
                    if (staticFlag != Static.DONT_CARE) {
                        boolean isStatic = method.getModifiers().contains(Modifier.STATIC);
                        if ((isStatic && staticFlag == Static.NO) || (!isStatic && staticFlag == Static.YES)) {
                            continue;
                        }
                    }

                    // public match
                    if (publicFlag != Public.DONT_CARE) {
                        boolean isPublic = method.getModifiers().contains(Modifier.PUBLIC);
                        if ((isPublic && publicFlag == Public.NO) || (!isPublic && publicFlag == Public.YES)) {
                            continue;
                        }
                    }

                    // found!
                    this.methodHandle = ElementHandle.create(method);
                    return;
                }

                TypeMirror superClassMirror = element.getSuperclass();
                if (superClassMirror instanceof DeclaredType) {
                    DeclaredType declaredType = (DeclaredType) superClassMirror;
                    Element elem = declaredType.asElement();
                    if (elem.getKind() == ElementKind.CLASS) {
                        element = (TypeElement) elem;
                    }
                } else {
                    element = null;
                }
            }
        }

        public ElementHandle<ExecutableElement> getMethodHandle() {
            return this.methodHandle;
        }
    }
}
