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
package org.netbeans.modules.hibernate.refactoring;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.swing.text.BadLocationException;
import javax.swing.text.Position.Bias;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementUtilities;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.project.Project;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.TokenID;
import org.netbeans.editor.TokenItem;
import org.netbeans.modules.hibernate.mapping.model.HibernateMapping;
import org.netbeans.modules.hibernate.mapping.model.MyClass;
import org.netbeans.modules.hibernate.mapping.model.Property;
import org.netbeans.modules.hibernate.service.HibernateEnvironment;
import org.netbeans.modules.xml.text.api.XMLDefaultTokenContext;
import org.netbeans.modules.xml.text.syntax.SyntaxElement;
import org.netbeans.modules.xml.text.syntax.XMLSyntaxSupport;
import org.netbeans.modules.xml.text.syntax.dom.EmptyTag;
import org.netbeans.modules.xml.text.syntax.dom.StartTag;
import org.netbeans.modules.xml.text.syntax.dom.Tag;
import org.openide.ErrorManager;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.PositionBounds;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 *
 * @author Andrei Badea, Dongmei Cao
 */
public class HibernateRefactoringUtil {

    private static final Logger LOGGER = Logger.getLogger(HibernateRefactoringUtil.class.getName());
    private static final String JAVA_MIME_TYPE = "text/x-java"; // NOI18N

    public static boolean isJavaFile(FileObject fo) {
        return JAVA_MIME_TYPE.equals(fo.getMIMEType());
    }

    public static RenamedClassName getRenamedClassName(final TreePathHandle oldHandle, final JavaSource javaSource, final String newName) throws IOException {
        final RenamedClassName[] result = {null};
        javaSource.runUserActionTask(new Task<CompilationController>() {

            public void run(CompilationController cc) throws IOException {
                cc.toPhase(Phase.ELEMENTS_RESOLVED);
                Element element = oldHandle.resolveElement(cc);
                if (element == null || element.getKind() != ElementKind.CLASS) {
                    return;
                }
                String oldBinaryName = ElementUtilities.getBinaryName((TypeElement) element);
                String oldSimpleName = element.getSimpleName().toString();
                String newBinaryName = null;
                element = element.getEnclosingElement();
                if (element.getKind() == ElementKind.CLASS) {
                    newBinaryName = ElementUtilities.getBinaryName((TypeElement) element) + '$' + newName;
                } else if (element.getKind() == ElementKind.PACKAGE) {
                    String packageName = ((PackageElement) element).getQualifiedName().toString();
                    newBinaryName = createQualifiedName(packageName, newName);
                } else {
                    LOGGER.log(Level.WARNING, "Enclosing element of {0} was neither class nor package", oldHandle);
                }
                result[0] = new RenamedClassName(oldSimpleName, oldBinaryName, newBinaryName);
            }
        }, true);
        return result[0];
    }

    public static List<String> getTopLevelClassNames(FileObject fo) throws IOException {
        JavaSource javaSource = JavaSource.forFileObject(fo);
        if (javaSource == null) {
            return Collections.emptyList();
        }
        final List<String> result = new ArrayList<String>(1);
        javaSource.runUserActionTask(new Task<CompilationController>() {

            public void run(CompilationController cc) throws IOException {
                cc.toPhase(Phase.ELEMENTS_RESOLVED);
                for (TypeElement typeElement : cc.getTopLevelElements()) {
                    result.add(ElementUtilities.getBinaryName(typeElement));
                }
            }
        }, true);
        return result;
    }

    public static String getPackageName(FileObject folder) {
        ClassPath cp = ClassPath.getClassPath(folder, ClassPath.SOURCE);
        if (cp != null) {
            return cp.getResourceName(folder, '.', false);
        }
        return null;
    }

    public static String getRenamedPackageName(FileObject folder, String newName) {
        FileObject parent = folder.getParent();
        if (parent == null) {
            return null;
        }
        ClassPath cp = ClassPath.getClassPath(parent, ClassPath.SOURCE);
        if (cp == null) {
            return null;
        }
        String parentName = cp.getResourceName(parent, '.', false);
        if (parentName == null) {
            return null;
        }
        if (parentName.length() > 0) {
            return parentName + '.' + newName;
        } else {
            return newName;
        }
    }

    public static String getPackageName(URL url) {
        File f = null;
        try {
            String path = URLDecoder.decode(url.getPath(), "UTF-8"); // NOI18N
            f = FileUtil.normalizeFile(new File(path));
        } catch (UnsupportedEncodingException u) {
            throw new IllegalArgumentException("Cannot create package name for URL " + url); // NOI18N
        }
        String suffix = "";
        do {
            FileObject fo = FileUtil.toFileObject(f);
            if (fo != null) {
                if ("".equals(suffix)) {
                    return getPackageName(fo);
                }
                String prefix = getPackageName(fo);
                return prefix + ("".equals(prefix) ? "" : ".") + suffix; // NOI18N
            }
            if (!"".equals(suffix)) {
                suffix = "." + suffix; // NOI18N
            }
            try {
                suffix = URLDecoder.decode(f.getPath().substring(f.getPath().lastIndexOf(File.separatorChar) + 1), "UTF-8") + suffix; // NOI18N
            } catch (UnsupportedEncodingException u) {
                throw new IllegalArgumentException("Cannot create package name for URL " + url); // NOI18N
            }
            f = f.getParentFile();
        } while (f != null);
        throw new IllegalArgumentException("Cannot create package name for URL " + url); // NOI18N
    }

    public static String getSimpleElementName(String elementName) {
        for (;;) {
            if (elementName.length() == 0) {
                return elementName;
            }
            int lastDot = elementName.lastIndexOf('.');
            if (lastDot == -1) {
                return elementName;
            }
            if (lastDot == elementName.length() - 1) {
                elementName = elementName.substring(0, lastDot);
                continue;
            }
            return elementName.substring(lastDot + 1);
        }
    }

    public static String createQualifiedName(String packageName, String simpleName) {
        if (packageName.length() == 0) {
            return simpleName;
        } else {
            if (simpleName.length() == 0) {
                return packageName;
            } else {
                return packageName + '.' + simpleName;
            }
        }
    }

    public static final class RenamedClassName {

        private final String oldSimpleName;
        private final String oldBinaryName;
        private final String newBinaryName;

        public RenamedClassName(String oldSimpleName, String oldBinaryName, String newBinaryName) {
            this.oldSimpleName = oldSimpleName;
            this.oldBinaryName = oldBinaryName;
            this.newBinaryName = newBinaryName;
        }

        public String getOldSimpleName() {
            return oldSimpleName;
        }

        public String getOldBinaryName() {
            return oldBinaryName;
        }

        public String getNewBinaryName() {
            return newBinaryName;
        }
    }

    public static Map<FileObject, OccurrenceItem> getJavaClassOccurrences(List<FileObject> allMappingFiles, String oldBinaryName) {
        Map<FileObject, OccurrenceItem> occurrences = new HashMap<FileObject, OccurrenceItem>();
        for (FileObject mFileObj : allMappingFiles) {
            try {
                InputStream is = mFileObj.getInputStream();
                HibernateMapping hbMapping = HibernateMapping.createGraph(is);

                // Check the name attribute of the <class> elment
                MyClass[] myClazz = hbMapping.getMyClass();

                for (int ci = 0; ci < myClazz.length; ci++) {
                    String clsName = myClazz[ci].getAttributeValue("name"); // NO I18N
                    if (clsName != null && clsName.equals(oldBinaryName)) {
                        // Find the class. That means this file needs to be refactored
                        OccurrenceItem foundPlace = getJavaClassPositionBounds(mFileObj, oldBinaryName);
                        occurrences.put(mFileObj, foundPlace);

                        // It is safe to assume that this is only one <class> element
                        // with this particular Java class. 
                        // So, go on to the next file
                        break;
                    }
                }

            // TODO: class name can be in other elements/attributes. 
            // TODO: need to check all of them
            // TODO:

            } catch (FileNotFoundException ex) {
                ErrorManager.getDefault().notify(org.openide.ErrorManager.INFORMATIONAL, ex);
            }
        }
        return occurrences;
    }

    private static OccurrenceItem getJavaClassPositionBounds(FileObject mappingFile, String className) {
        try {
            // Get the document for this file
            DataObject dataObject = DataObject.find(mappingFile);
            EditorCookie result = dataObject.getCookie(EditorCookie.class);
            if (result == null) {
                throw new IllegalStateException("File " + mappingFile + " does not have an EditorCookie.");
            }

            CloneableEditorSupport editor = (CloneableEditorSupport) result;
            BaseDocument document = (BaseDocument) editor.openDocument();
            XMLSyntaxSupport syntaxSupport = (XMLSyntaxSupport) document.getSyntaxSupport();


            int start = document.getStartPosition().getOffset();
            TokenItem item = syntaxSupport.getTokenChain(start, Math.min(start + 1, document.getLength()));
            if (item == null) {
                return null;
            }

            String text = null;
            while (item != null) {
                TokenID tokenId = item.getTokenID();

                if (tokenId == XMLDefaultTokenContext.TAG) {
                    // Did we find the <class> element

                    SyntaxElement element = syntaxSupport.getElementChain(item.getOffset() + 1);
                    if (element instanceof StartTag || element instanceof EmptyTag) {
                        
                        Node theNode = (Node) element;

                        // Search for the <class> element
                        if (theNode.getNodeName().equalsIgnoreCase("class")) { // NOI18N
                            String nameAttribValue = getNameAttributeValue(theNode);
                            
                            if (nameAttribValue != null && nameAttribValue.equals(className)) {
                                
                                text = document.getText(item.getOffset(), element.getElementLength());
                                
                                int index = text.indexOf(className);
                                int startOffset = item.getOffset() + index;
                                int endOffset = startOffset + className.length();

                                PositionBounds loc = new PositionBounds(editor.createPositionRef(startOffset, Bias.Forward),
                                        editor.createPositionRef(endOffset, Bias.Forward));

                                return new OccurrenceItem(loc, text);
                            }
                        }
                    }
                } 
                
                item = item.getNext();
            }
            return null;
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(org.openide.ErrorManager.INFORMATIONAL, ex);
        } catch (BadLocationException ex) {
            ErrorManager.getDefault().notify(org.openide.ErrorManager.INFORMATIONAL, ex);
        }

        return null;
    }

    public static Map<FileObject, OccurrenceItem> getJavaFieldOccurrences(List<FileObject> allMappingFiles, String className, String fieldName) {
        Map<FileObject, OccurrenceItem> occurrences = new HashMap<FileObject, OccurrenceItem>();
        for (FileObject mFileObj : allMappingFiles) {
            try {
                InputStream is = mFileObj.getInputStream();
                HibernateMapping hbMapping = HibernateMapping.createGraph(is);

                // Check the name attribute of the <property> element in the <class> elements
                MyClass[] myClazz = hbMapping.getMyClass();
                for (int ci = 0; ci < myClazz.length; ci++) {
                    String clsName = myClazz[ci].getAttributeValue("name"); // NO I18N
                    if (clsName != null && clsName.equals(className)) {

                        Property[] clazzProps = myClazz[ci].getProperty2();
                        for (int pi = 0; pi < clazzProps.length; pi++) {
                            if (clazzProps[pi].getAttributeValue("name").equals(fieldName)) {

                                // Find the property to be refactored
                                OccurrenceItem foundPlace = getJavaFieldPositionBounds(mFileObj, className, fieldName);
                                occurrences.put(mFileObj, foundPlace);

                                // It is safe to assume that this is only one <class> element
                                // with this particular Java class. 
                                // So, go on to the next file
                                break;
                            }
                        }

                    // TODO: need to search other elements, such as, <id> etc.
                    }
                }

            } catch (FileNotFoundException ex) {
                ErrorManager.getDefault().notify(org.openide.ErrorManager.INFORMATIONAL, ex);
            }
        }
        return occurrences;
    }

    private static OccurrenceItem getJavaFieldPositionBounds(FileObject mappingFile, String className, String fieldName) {
        try {
            // Get the document for this file
            DataObject dataObject = DataObject.find(mappingFile);
            EditorCookie result = dataObject.getCookie(EditorCookie.class);
            if (result == null) {
                throw new IllegalStateException("File " + mappingFile + " does not have an EditorCookie.");
            }

            CloneableEditorSupport editor = (CloneableEditorSupport) result;
            BaseDocument document = (BaseDocument) editor.openDocument();
            XMLSyntaxSupport syntaxSupport = (XMLSyntaxSupport) document.getSyntaxSupport();


            int start = document.getStartPosition().getOffset();
            TokenItem item = syntaxSupport.getTokenChain(start, Math.min(start + 1, document.getLength()));
            if (item == null) {
                return null;
            }

            boolean thisIsTheClassElement = false;
            String text = null;
            while (item != null) {
                TokenID tokenId = item.getTokenID();

                if (tokenId == XMLDefaultTokenContext.TAG) {
                    // Did we find the <class> element

                    SyntaxElement element = syntaxSupport.getElementChain(item.getOffset() + 1);
                    if (element instanceof StartTag || element instanceof EmptyTag) {
                        
                        Node theNode = (Node)element;
                        
                        if (!thisIsTheClassElement) {
                            // Search for the <class> element
                            if (theNode.getNodeName().equalsIgnoreCase("class")) { // NOI18N
                                String nameAttribValue = getNameAttributeValue(theNode);
                                if(nameAttribValue != null && nameAttribValue.equals(className)) {
                                    thisIsTheClassElement = true;
                                }
                            }
                        } else {
                            // It is in the <class> element.
                            // Search for the <property> element
                            if (theNode.getNodeName().equalsIgnoreCase("property")) {
                               String nameAttribValue = getNameAttributeValue(theNode);
                                if( nameAttribValue != null && nameAttribValue.equals(fieldName)) {
                                    
                                    text = document.getText(item.getOffset(), element.getElementLength());
                                    
                                    // find the offset for the field name
                                    int index = text.indexOf(fieldName);
                                    int startOffset = item.getOffset() + index;
                                    int endOffset = startOffset + fieldName.length();
                                    PositionBounds loc = new PositionBounds(editor.createPositionRef(startOffset, Bias.Forward),
                                            editor.createPositionRef(endOffset, Bias.Forward));

                                    return new OccurrenceItem(loc, text);
                                }
                            }
                        }
                    }
                } 

                item = item.getNext();
            }
            return null;
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(org.openide.ErrorManager.INFORMATIONAL, ex);
        } catch (BadLocationException ex) {
            ErrorManager.getDefault().notify(org.openide.ErrorManager.INFORMATIONAL, ex);
        }

        return null;
    }

    public static Map<FileObject, List<OccurrenceItem>> getJavaPackageOccurrences(List<FileObject> allMappingFiles, String oldPkgName) {
        Map<FileObject, List<OccurrenceItem>> occurrences = new HashMap<FileObject, List<OccurrenceItem>>();

        for (FileObject mFileObj : allMappingFiles) {
            List<OccurrenceItem> foundPlaces = new ArrayList<OccurrenceItem>();
            int startOffset = -1;
            try {
                InputStream is = mFileObj.getInputStream();
                HibernateMapping hbMapping = HibernateMapping.createGraph(is);

                // Check package attribute in the <hibernate-mapping> tag
                String pkgName = hbMapping.getAttributeValue("package"); //NOI18N
                if (pkgName != null && pkgName.equals(oldPkgName)) {
                    // Find one
                    OccurrenceItem foundPlace = getJavaPackagePositionBounds(mFileObj, oldPkgName, startOffset);
                    foundPlaces.add(foundPlace);

                    startOffset = foundPlace.getLocation().getBegin().getOffset() + 1;
                }

                // Check the name attribute in the <class> tag
                MyClass[] myClazz = hbMapping.getMyClass();

                for (int ci = 0; ci < myClazz.length; ci++) {
                    String clsName = myClazz[ci].getAttributeValue("name"); // NO I18N
                    if (clsName != null && clsName.startsWith(oldPkgName)) {

                        OccurrenceItem foundPlace = getJavaPackagePositionBounds(mFileObj, oldPkgName, startOffset);
                        foundPlaces.add(foundPlace);

                        startOffset = foundPlace.getLocation().getBegin().getOffset() + 1;
                    }

                // TODO: class name can be in other elements/attributes. 
                // TODO: need to check all of them
                // TODO: 

                }
            } catch (FileNotFoundException ex) {
                ErrorManager.getDefault().notify(org.openide.ErrorManager.INFORMATIONAL, ex);
            }

            // Find everything in this file
            occurrences.put(mFileObj, foundPlaces);
        }

        return occurrences;
    }

    private static OccurrenceItem getJavaPackagePositionBounds(FileObject mappingFile, String pkgName, int start) {
        try {
            // Get the document for this file
            DataObject dataObject = DataObject.find(mappingFile);
            EditorCookie result = dataObject.getCookie(EditorCookie.class);
            if (result == null) {
                throw new IllegalStateException("File " + mappingFile + " does not have an EditorCookie.");
            }

            CloneableEditorSupport editor = (CloneableEditorSupport) result;
            BaseDocument document = (BaseDocument) editor.openDocument();
            XMLSyntaxSupport syntaxSupport = (XMLSyntaxSupport) document.getSyntaxSupport();

            if (start == -1) {
                start = document.getStartPosition().getOffset();
            }

            TokenItem item = syntaxSupport.getTokenChain(start, Math.min(start + 1, document.getLength()));
            if (item == null) {
                return null;
            }

            boolean inTheElement = false; //<hibernate-mapping> or <class>
            String text = null;
            while (item != null) {
                TokenID tokenId = item.getTokenID();

                if (tokenId == XMLDefaultTokenContext.TAG) {

                    SyntaxElement element = syntaxSupport.getElementChain(item.getOffset() + 1);
                    if (element instanceof StartTag || element instanceof EmptyTag) {
                        String tagName = ((Tag) element).getTagName();
                        if (tagName.equalsIgnoreCase("hibernate-mapping")) { // NOI18N
                            inTheElement = true;
                            text = document.getText(item.getOffset(), element.getElementLength());
                        } else if (tagName.equalsIgnoreCase("class")) { // NOI18N
                            text = document.getText(item.getOffset(), element.getElementLength());
                            inTheElement = true;
                        }
                    }

                } else if (tokenId == XMLDefaultTokenContext.VALUE && inTheElement) {

                    // Look for the class name to be refactored here

                    String image = item.getImage();
                    if (image.contains(pkgName)) {
                        // Found it
                        inTheElement = false;

                        int startOffset = item.getOffset() + 1;
                        int endOffset = startOffset + pkgName.length();
                        PositionBounds loc = new PositionBounds(editor.createPositionRef(startOffset, Bias.Forward),
                                editor.createPositionRef(endOffset, Bias.Forward));

                        return new OccurrenceItem(loc, text);
                    }
                }

                item = item.getNext();
            }
            return null;
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(org.openide.ErrorManager.INFORMATIONAL, ex);
        } catch (BadLocationException ex) {
            ErrorManager.getDefault().notify(org.openide.ErrorManager.INFORMATIONAL, ex);
        }

        return null;
    }

    public static final class OccurrenceItem {

        private String text;
        private PositionBounds location;

        public OccurrenceItem(PositionBounds location, String text) {
            this.location = location;
            this.text = text;
        }

        public String getText() {
            return this.text;
        }

        public PositionBounds getLocation() {
            return this.location;
        }
    }

    public static boolean anyHibernateMappingFiles(FileObject fo) {
        Project proj = org.netbeans.api.project.FileOwnerQuery.getOwner(fo);
        HibernateEnvironment env = new HibernateEnvironment(proj);
        List<FileObject> mFileObjs = env.getAllHibernateMappingFileObjects();
        if (mFileObjs == null || mFileObjs.size() == 0) {
            // OK, no mapping files at all. 
            return false;
        } else {
            return true;
        }
    }
    
    private static String getNameAttributeValue(Node node) {
        if(node == null )
            return null;
        
        NamedNodeMap attribs = node.getAttributes();
        if (attribs != null && attribs.getNamedItem("name") != null) { // NOI18N
            return attribs.getNamedItem("name").getNodeValue(); // NOI18N
        }
        
        return null;
    }
}
