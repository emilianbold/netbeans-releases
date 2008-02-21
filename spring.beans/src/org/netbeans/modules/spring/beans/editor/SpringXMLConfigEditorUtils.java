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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.SimpleElementVisitor6;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.ElementUtilities;
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
import org.netbeans.modules.spring.api.Action;
import org.netbeans.modules.spring.api.beans.model.Location;
import org.netbeans.modules.spring.api.beans.model.SpringBean;
import org.netbeans.modules.spring.api.beans.model.SpringBeans;
import org.netbeans.modules.spring.api.beans.model.SpringConfigModel;
import org.netbeans.modules.spring.beans.utils.StringUtils;
import org.netbeans.modules.xml.text.syntax.SyntaxElement;
import org.netbeans.modules.xml.text.syntax.XMLSyntaxSupport;
import org.netbeans.modules.xml.text.syntax.dom.EmptyTag;
import org.netbeans.modules.xml.text.syntax.dom.StartTag;
import org.netbeans.modules.xml.text.syntax.dom.Tag;
import org.openide.awt.StatusDisplayer;
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
    
    public static TypeElement findClassElementByBinaryName(final String binaryName, CompilationController cc) {
        if (!binaryName.contains("$")) { // NOI18N
            // fast search based on fqn
            return cc.getElements().getTypeElement(binaryName);
        } else {
            // get containing package
            String packageName = ""; // NOI18N
            int dotIndex = binaryName.lastIndexOf("."); // NOI18N
            if (dotIndex != -1) {
                packageName = binaryName.substring(0, dotIndex);
            }
            PackageElement packElem = cc.getElements().getPackageElement(packageName);
            if (packElem == null) {
                return null;
            }

            // scan for element matching the binaryName
            return new BinaryNameTypeScanner().visit(packElem, binaryName);
        }
    }
    
    private static class BinaryNameTypeScanner extends SimpleElementVisitor6<TypeElement, String> {

        @Override
        public TypeElement visitPackage(PackageElement packElem, String binaryName) {
            for(Element e : packElem.getEnclosedElements()) {
                if(e.getKind().isClass()) {
                    TypeElement ret = e.accept(this, binaryName);
                    if(ret != null) {
                        return ret;
                    }
                }
            }
            
            return null;
        }

        @Override
        public TypeElement visitType(TypeElement typeElement, String binaryName) {
            String bName = ElementUtilities.getBinaryName(typeElement);
            if(binaryName.equals(bName)) {
                return typeElement;
            } else if(binaryName.startsWith(bName)) {
                for(Element child : typeElement.getEnclosedElements()) {
                    if(!child.getKind().isClass()) {
                        continue;
                    }
                    
                    TypeElement retVal = child.accept(this, binaryName);
                    if(retVal != null) {
                        return retVal;
                    }
                }
            }
            
            return null;
        }
        
    }

    public static Node getBean(Node tag) {
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

    public static String getBeanClassName(Node tag) {
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

    public static void findAndOpenJavaClass(final String classBinaryName, Document doc) {
        final JavaSource js = getJavaSource(doc);
        if (js != null) {
            try {
                js.runUserActionTask(new Task<CompilationController>() {
                    public void run(CompilationController cc) throws Exception {
                        boolean opened = false;
                        TypeElement element = findClassElementByBinaryName(classBinaryName, cc);
                        if (element != null) {
                            opened = ElementOpen.open(js.getClasspathInfo(), element);
                        }
                        if (!opened) {
                            String msg = NbBundle.getMessage(SpringXMLConfigEditorUtils.class, "LBL_SourceNotFound", classBinaryName);
                            StatusDisplayer.getDefault().setStatusText(msg);
                        }
                    }
                }, false);
            } catch (IOException ex) {
                Logger.getLogger("global").log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
    }

    public static ElementHandle<ExecutableElement> findMethod(Document doc, final String classBinName,
            final String methodName, int argCount, Public publicFlag, Static staticFlag) {
        JavaSource js = getJavaSource(doc);
        if (js != null) {
            try {
                MethodFinder methodFinder = new MethodFinder(classBinName, methodName, argCount, publicFlag, staticFlag);
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
     * @param classBinName binary name of the class whose method is to be opened in the editor
     * @param methodName name of the method
     * @param argCount number of arguments that the method has (-1 if caller doesn't care)
     * @param publicFlag YES if the method is public, NO if not, DONT_CARE if caller doesn't care
     * @param staticFlag YES if the method is static, NO if not, DONT_CARE if caller doesn't care
     */
    public static void openMethodInEditor(Document doc, final String classBinName,
            final String methodName, int argCount, Public publicFlag, Static staticFlag) {
        if (classBinName == null || methodName == null || doc == null) {
            return;
        }

        final JavaSource js = getJavaSource(doc);
        if (js == null) {
            return;
        }

        final ElementHandle<ExecutableElement> eh = findMethod(doc, classBinName, methodName, argCount, publicFlag, staticFlag);
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
    
    public static String getPropertyNameFromMethodName(String methodName) {
        if(methodName.length() < 4) {
            return null;
        }
        char[] propertyName = methodName.substring(3).toCharArray();
        propertyName[0] = Character.toLowerCase(propertyName[0]);
        return String.valueOf(propertyName);
    }
    
    private static final class MethodFinder implements Task<CompilationController> {

        private String classBinName;
        private String methodName;
        private int argCount;
        private Public publicFlag;
        private Static staticFlag;
        private ElementHandle<ExecutableElement> methodHandle;

        public MethodFinder(String classBinName, String methodName, int argCount, Public publicFlag, Static staticFlag) {
            this.classBinName = classBinName;
            this.methodName = methodName;
            this.argCount = argCount;
            this.publicFlag = publicFlag;
            this.staticFlag = staticFlag;
        }

        public void run(CompilationController cc) throws Exception {
            cc.toPhase(Phase.ELEMENTS_RESOLVED);
            TypeElement element = findClassElementByBinaryName(classBinName, cc);
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
    
    public static SpringBean getMergedBean(SpringBean origBean, Document doc) {
        if(origBean == null) {
            return null;
        }
        
        if(origBean.getParent() == null) {
            return origBean;
        }
        
        ModelBasedSpringBean logicalBean = new ModelBasedSpringBean(origBean, doc);
        return getMergedBean(logicalBean, doc);
    }
    
    public static SpringBean getMergedBean(Node beanNode, Document doc) {

        NodeBasedSpringBean logicalBean = new NodeBasedSpringBean(beanNode, doc);
        if (!StringUtils.hasText(logicalBean.getParent())) {
            return logicalBean;
        }

        return getMergedBean(logicalBean, doc);
    }
    
    private static SpringBean getMergedBean(MutableSpringBean startBean, Document doc) {
        final MutableSpringBean[] logicalBean = { startBean };
        SpringConfigModel model = SpringConfigModel.forFileObject(NbEditorUtilities.getFileObject(doc));
        if (model == null) {
            return null;
        }

        try {
            model.runReadAction(new Action<SpringBeans>() {

                public void run(SpringBeans springBeans) {
                    String currParent = logicalBean[0].getParent();
                    Set<SpringBean> walkedBeans = new HashSet<SpringBean>();
                    while (currParent != null && (logicalBean[0].getClassName() == null 
                            || logicalBean[0].getFactoryBean() == null || logicalBean[0].getFactoryMethod() == null)) {
                        SpringBean currBean = springBeans.findBean(currParent);
                        if (walkedBeans.contains(currBean)) {
                            // circular dep. nullify everything
                            logicalBean[0] = null;
                            break;
                        }

                        if (logicalBean[0].getClassName() == null) {
                            logicalBean[0].setClassName(currBean.getClassName());
                        }
                        if (logicalBean[0].getFactoryBean() == null) {
                            logicalBean[0].setFactoryBean(currBean.getFactoryBean());
                        }
                        if (logicalBean[0].getFactoryMethod() == null) {
                            logicalBean[0].setFactoryMethod(currBean.getFactoryMethod());
                        }

                        walkedBeans.add(currBean);
                        currParent = currBean.getParent();
                    }
                }
            });
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
            logicalBean[0] = null;
        }
        
        return logicalBean[0];
    }
    
    private static interface MutableSpringBean extends SpringBean {
        void setClassName(String className);
        void setFactoryBean(String factoryBean);
        void setFactoryMethod(String factoryMethod);
    }
    
    private static class ModelBasedSpringBean implements MutableSpringBean {
        private String className;
        private String factoryBean;
        private String factoryMethod;
        private String parent;
        private String id;
        private List<String> names;
        private Location location;

        public ModelBasedSpringBean(SpringBean springBean, Document doc) {
            this.className = springBean.getClassName();
            this.factoryBean = springBean.getFactoryBean();
            this.factoryMethod = springBean.getFactoryMethod();
            this.parent = springBean.getParent();
            this.id = springBean.getId();
            this.location = springBean.getLocation();
            this.names = springBean.getNames();
        }

        public String getId() {
            return id;
        }

        public List<String> getNames() {
            return names;
        }

        public String getClassName() {
            return className;
        }

        public void setClassName(String className) {
            this.className = className;
        }
        
        public String getParent() {
            return parent;
        }

        public String getFactoryBean() {
            return factoryBean;
        }

        public void setFactoryBean(String factoryBean) {
            this.factoryBean = factoryBean;
        }
        
        public String getFactoryMethod() {
            return factoryMethod;
        }

        public void setFactoryMethod(String factoryMethod) {
            this.factoryMethod = factoryMethod;
        }

        public Location getLocation() {
            return location;
        }
        
    }
    
    private static class NodeBasedSpringBean implements MutableSpringBean {

        private String className;
        private String factoryBean;
        private String factoryMethod;
        private String parent;
        private String id;
        private List<String> names;
        private int offset;
        private File file;

        public NodeBasedSpringBean(Node node, Document doc) {
            this.className = getAttribute(node, "class"); // NOI18N
            this.factoryBean = getAttribute(node, "factory-bean"); // NOI18N
            this.factoryMethod = getAttribute(node, "factory-method"); // NOI18N
            this.parent = getAttribute(node, "parent"); // NOI18N
            this.id = getAttribute(node, "id"); // NOI18N
            this.offset = ((Tag) node).getElementOffset();
            this.file = FileUtil.toFile(NbEditorUtilities.getFileObject(doc));
            
            if(!hasAttribute(node, "name")) { // NOI18N
                this.names = Collections.<String>emptyList();
            }
            this.names = StringUtils.tokenize(getAttribute(node, "name"), BEAN_NAME_DELIMITERS); // NOI18N
        }
        
        public String getId() {
            return this.id;
        }

        public List<String> getNames() {
            return names;
        }

        public String getClassName() {
            return className;
        }
        
        public void setClassName(String className) {
            this.className = className;
        }

        public String getParent() {
            return this.parent;
        }

        public String getFactoryBean() {
            return this.factoryBean;
        }

        public void setFactoryBean(String factoryBean) {
            this.factoryBean = factoryBean;
        }
        
        public String getFactoryMethod() {
            return this.factoryMethod;
        }

        public void setFactoryMethod(String factoryMethod) {
            this.factoryMethod = factoryMethod;
        }
        
        public Location getLocation() {
            return new Location() {

                public File getFile() {
                    return file;
                }

                public int getOffset() {
                    return offset;
                }
            };
        }
        
    }
    
    public static List<ExecutableElement> findPropertiesOnType(ElementUtilities eu, TypeMirror type,
            String propertyName, boolean searchGetters, boolean searchSetters) {
        PropertyAcceptor propertyAcceptor = new PropertyAcceptor(propertyName, searchGetters, searchSetters);
        Iterable<? extends Element> matchingProp = eu.getMembers(type, propertyAcceptor);
        Iterator<? extends Element> it = matchingProp.iterator();
        // no matching element found
        if (!it.hasNext()) {
            return Collections.emptyList();
        }
        
        List<ExecutableElement> retList = new ArrayList<ExecutableElement>();
        for(Element e : matchingProp) {
            retList.add((ExecutableElement) e);
        }
        
        return retList;
    }
       
    private static class PropertyAcceptor implements ElementUtilities.ElementAcceptor {
        private boolean searchSetters;
        private boolean searchGetters;
        private String propPrefix;

        public PropertyAcceptor(String propPrefix, boolean searchGetters, boolean searchSetters) {
            // captialize first character of the property prefix - for matching
            if(propPrefix.length() > 0) {
                char[] prop = propPrefix.toCharArray();
                prop[0] = Character.toUpperCase(prop[0]);
                this.propPrefix = String.valueOf(prop);
            } else {
                this.propPrefix = propPrefix;
            }
            this.searchGetters = searchGetters;
            this.searchSetters = searchSetters;
        }
        
        public boolean accept(Element e, TypeMirror type) {
            if (e.getKind() != ElementKind.METHOD) {
                return false;
            }

            ExecutableElement ee = (ExecutableElement) e;
            String methodName = ee.getSimpleName().toString();
            if(methodName.length() < 4) {
                return false;
            }
            
            if(ee.getModifiers().contains(Modifier.PRIVATE) || ee.getModifiers().contains(Modifier.STATIC)) {
                return false;
            }
            
            if(!methodName.startsWith(propPrefix, 3)) {
                return false;
            }
            
            if (searchSetters && methodName.startsWith("set") && ee.getParameters().size() == 1 
                    && ee.getReturnType().getKind() == TypeKind.VOID) { // NOI18N
                return true;
            }
            if(searchGetters && methodName.startsWith("get") && ee.getParameters().size() == 0
                    && ee.getReturnType().getKind() != TypeKind.VOID) { // NOI18N
                return true;
            }

            return false;
        }
    }
}
