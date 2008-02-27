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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.hibernate.editor;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.SimpleElementVisitor6;
import javax.swing.text.Document;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.ui.ElementOpen;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 *
 * @author Dongmei Cao
 */
public class HibernateEditorUtil {

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

    public static Node getClassNode(Node tag) {
        if (tag == null) {
            return null;
        }

        if (tag.getNodeName().equalsIgnoreCase("class")) { // NOI18N
            return tag;
        }

        Node current = tag;
        while (true) {
            Node parent = current.getParentNode();
            if (parent.getNodeName().equalsIgnoreCase("class")) {
                // Found it
                return parent;
            } else if (parent.getNodeName().equalsIgnoreCase("hibernate-mapping")) {
                // Hit the root element
                return null;
            } else {
                // Keep going
                current = parent;
            }
        }
    }

    public static String getClassName(Node tag) {
        Node classNode = getClassNode(tag);
        if (classNode != null) {
            NamedNodeMap attribs = classNode.getAttributes();
            if (attribs != null && attribs.getNamedItem("name") != null) { // NOI18N
                return attribs.getNamedItem("name").getNodeValue(); // NOI18N
            }
        }

        return null;
    }

    public static String getTableName(Node tag) {
        Node classNode = getClassNode(tag);
        if (classNode != null) {
            NamedNodeMap attribs = classNode.getAttributes();
            if (attribs != null && attribs.getNamedItem("table") != null) { // NOI18N
                return attribs.getNamedItem("table").getNodeValue(); // NOI18N
            }
        }

        return null;
    }

    public static String getHbPropertyName(Node tag) {
        if (!tag.getNodeName().equalsIgnoreCase("property")) {
            return null;
        } else {
            NamedNodeMap attribs = tag.getAttributes();
            if (attribs != null && attribs.getNamedItem("name") != null) { // NOI18N
                return attribs.getNamedItem("name").getNodeValue();
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
            for (Element e : packElem.getEnclosedElements()) {
                if (e.getKind().isClass()) {
                    TypeElement ret = e.accept(this, binaryName);
                    if (ret != null) {
                        return ret;
                    }
                }
            }

            return null;
        }
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
                            // TODO: I18N
                            //String msg = NbBundle.getMessage(HibernateCompletionEditorUtil.class, "LBL_SourceNotFound", classBinaryName);
                            String msg = "Source not found (TODO: I18N)";
                            StatusDisplayer.getDefault().setStatusText(msg);
                        }
                    }
                    }, false);
            } catch (IOException ex) {
                Logger.getLogger("global").log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
    }
    }

