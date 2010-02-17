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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
package org.netbeans.modules.javacard.project.refactoring;

import com.sun.javacard.filemodels.DeploymentXmlAppletEntry;
import com.sun.javacard.filemodels.DeploymentXmlModel;
import com.sun.source.tree.Tree;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.net.URL;
import org.netbeans.api.fileinfo.NonRecursiveFolder;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.javacard.common.JCConstants;
import org.netbeans.modules.javacard.project.JCProject;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.netbeans.modules.refactoring.spi.SimpleRefactoringElementImplementation;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.openide.filesystems.FileAlreadyLockedException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.text.PositionBounds;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.javacard.spi.ProjectKind;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.MoveRefactoring;
import org.netbeans.modules.refactoring.api.RenameRefactoring;

public class ImportantFilesRenameRefactoring implements RefactoringPlugin {

    private final Transformer transformer;
    private final TransformerFactory tFactory = TransformerFactory.newInstance();
    private RefactoringPlugin refactoringPlugin;
    private final AbstractRefactoring renameRefactoring;
    private JCProject project;

    public ImportantFilesRenameRefactoring(AbstractRefactoring renameRefactoring) {
        try {
            transformer = tFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.STANDALONE, "no");
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION,
                    "yes");
        } catch (TransformerConfigurationException e) {
            throw new IllegalStateException("Internal error: failed to create" + " transformer", e);
        }
        this.renameRefactoring = renameRefactoring;
        Lookup lookup = renameRefactoring.getRefactoringSource();

        // 1. Check if this is the case of renaming a package
        NonRecursiveFolder folder = lookup.lookup(NonRecursiveFolder.class);
        if (folder != null) {
            project = JCProject.getOwnerProjectOf(folder.getFolder());
            if (project != null) {
                refactoringPlugin = new RenamePackageRefactoring(folder.getFolder());
            }
        }

        // 2. Check if this is the case of renaming a class 
        TreePathHandle tpHandle = lookup.lookup(TreePathHandle.class);
        if (tpHandle != null && tpHandle.getKind() == Tree.Kind.CLASS) {
            FileObject classSourceFO = tpHandle.getFileObject();
            if (classSourceFO != null) {
                project = JCProject.getOwnerProjectOf(classSourceFO);
                if (project != null) {
                    refactoringPlugin = new RenameClassRefactoring(classSourceFO);
                }
            }
        }

        // 3. Another case of renaming a class
        FileObject fo = lookup.lookup(FileObject.class);
        if (fo != null && fo.isData() && "text/x-java".equals(fo.getMIMEType())) { //NOI18N
            // check whether the source file belong to a Java Card project.
            project = JCProject.getOwnerProjectOf(fo);
            if (project != null) {
                refactoringPlugin = new RenameClassRefactoring(fo);
            }
        }
    }


    public Problem preCheck() {
        return refactoringPlugin != null ? refactoringPlugin.preCheck() : null;
    }


    public Problem checkParameters() {
        return refactoringPlugin != null
                ? refactoringPlugin.checkParameters()
                : null;
    }

    public Problem fastCheckParameters() {
        return refactoringPlugin != null
                ? refactoringPlugin.fastCheckParameters()
                : null;
    }

    public void cancelRequest() {
        //
    }

    public Problem prepare(RefactoringElementsBag elements) {
        Problem result = null;
        if (refactoringPlugin != null) {
            result = refactoringPlugin.prepare(elements);
        }
        return result;
    }

    private String getResourceName(FileObject fileOrFolder) {
        ClassPathProvider cpProvider =
                project.getLookup().lookup(ClassPathProvider.class);
        ClassPath srcPath =
                cpProvider.findClassPath(fileOrFolder, ClassPath.SOURCE);
        if (srcPath.contains(fileOrFolder)) {
            return srcPath.getResourceName(fileOrFolder, '.', false);
        }
        return null;
    }

    // Rename the class
    private Problem prepareClassRenaming(RefactoringElementsBag elements, String oldNamePrefix, String newNamePrefix) {
        Problem p = prepareClassRenamingForJavaCardXML(elements, oldNamePrefix, newNamePrefix);
        Problem pp = null;
        ProjectKind kind= project.kind();
        switch (kind) {
            case WEB:
                pp = prepareClassRenamingForWebXML(elements, oldNamePrefix, newNamePrefix);
                break;
            case EXTENDED_APPLET :
            case CLASSIC_APPLET:
                //XXX this note was in the original source;  also should apply to deployment.xml
                // JC-FIXME: for extended applet projects apply rename on applet.xml
                pp = prepareClassRenamingForAppletXML(elements, oldNamePrefix, newNamePrefix);
                break;
            case CLASSIC_LIBRARY:
            case EXTENSION_LIBRARY:
                break;
            default :
                throw new AssertionError();
        }
        if (p != null && pp != null) {
            p.setNext(pp);
        }
        return p == null ? pp : p;
    }

    private Problem prepareClassRenamingForAppletXML(RefactoringElementsBag elements,
            String oldNamePrefix, String newNamePrefix) {

        FileObject appletXMLFileObject = project.getProjectDirectory().getFileObject(JCConstants.APPLET_DESCRIPTOR_PATH);
        if (appletXMLFileObject == null) {
            return null;
        }
        File appletXMLFile = FileUtil.toFile(appletXMLFileObject);

        AppletXMLRefactoringSupport descriptor = AppletXMLRefactoringSupport.fromFile(appletXMLFile);

        if (descriptor != null) {
            List<Node> nodesToUpdate = new ArrayList<Node>();
            // Update servlet-class elements
            for (NodeList nodes : new NodeList[]{
                        descriptor.getAppletClassElements()
                    }) {

                for (int i = 0; i < nodes.getLength(); i++) {
                    Node node = nodes.item(i);
                    String text = node.getTextContent();
                    if (text.startsWith(oldNamePrefix)) {
                        nodesToUpdate.add(node);
                    }
                }
            }

            if (nodesToUpdate.size() > 0) {
                String message = NbBundle.getMessage(
                        JCRenameRefactoringPlugin.class,
                        "Update_applet_xml", oldNamePrefix);
                elements.add(renameRefactoring,
                        new UpdateXmlRefactoringElement(
                        appletXMLFileObject, oldNamePrefix, newNamePrefix,
                        descriptor.getDocument(), nodesToUpdate,
                        message));
            }
        }
        FileObject fo = project.getProjectDirectory().getFileObject(JCConstants.DEPLOYMENT_XML_PATH);
        if (fo != null) {
            try {
                DeploymentXmlModel mdl = new DeploymentXmlModel(new BufferedInputStream(fo.getInputStream()));
                boolean changed = false;
                for (DeploymentXmlAppletEntry e : mdl.getData()) {
                    String classHint = e.getClazzHint();
                    if (oldNamePrefix.equals(classHint)) {
                        e.setClazzHint(newNamePrefix);
                        changed = true;
                    }
                }
                if (changed) {
                    String msg = NbBundle.getMessage(JCRenameRefactoringPlugin.class,
                            "UPDATE_DEPLOYMENT_XML", oldNamePrefix); //NOI18N
                    elements.add(renameRefactoring,
                            new UpdateDeploymentXmlRefactoringElement(mdl,
                            msg, fo));
                }
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }
        }

        return null;
    }

    private Problem prepareClassRenamingForWebXML(RefactoringElementsBag elements,
            String oldNamePrefix, String newNamePrefix) {

        FileObject webXMLFileObject = project.getProjectDirectory().getFileObject(JCConstants.WEB_DESCRIPTOR_PATH);
        if (webXMLFileObject == null) {
            return null;
        }
        File webXMLFile = FileUtil.toFile(webXMLFileObject);

        WebXMLRefactoringSupport descriptor = WebXMLRefactoringSupport.fromFile(webXMLFile);

        if (descriptor != null) {
            List<Node> nodesToUpdate = new ArrayList<Node>();
            // Update servlet-class elements
            for (NodeList nodes : new NodeList[]{
                        descriptor.getServletClassElements(),
                        descriptor.getListenerClassElements(),
                        descriptor.getFilterClassElements()
                    }) {

                for (int i = 0; i < nodes.getLength(); i++) {
                    Node node = nodes.item(i);
                    String text = node.getTextContent();
                    if (text.startsWith(oldNamePrefix)) {
                        nodesToUpdate.add(node);
                    }
                }
            }

            if (nodesToUpdate.size() > 0) {
                String message = NbBundle.getMessage(
                        JCRenameRefactoringPlugin.class,
                        "Update_web_xml", oldNamePrefix);
                elements.add(renameRefactoring,
                        new UpdateXmlRefactoringElement(
                        webXMLFileObject, oldNamePrefix, newNamePrefix,
                        descriptor.getDocument(), nodesToUpdate,
                        message));
            }
        }
        return null;
    }

    private Problem prepareClassRenamingForJavaCardXML(
            RefactoringElementsBag elements,
            String oldNamePrefix, String newNamePrefix) {
        FileObject javaCardXMLFileObject = project.getProjectDirectory().getFileObject(
                JCConstants.JAVACARD_XML_PATH);
        if (javaCardXMLFileObject == null) {
            return null;
        }
        File javaCardXMLFile = FileUtil.toFile(javaCardXMLFileObject);
        JavaCardXMLRefactoringSupport descriptor = JavaCardXMLRefactoringSupport.fromFile(javaCardXMLFile);
        if (descriptor != null) {
            List<Node> nodesToUpdate = new ArrayList<Node>();
            // Update servlet-class elements
            for (NodeList nodes : new NodeList[]{
                        descriptor.getDynamicallyLoadedClassElements(),
                        descriptor.getShareableInterfaceClassElements()
                    }) {

                for (int i = 0; i < nodes.getLength(); i++) {
                    Node node = nodes.item(i);
                    String text = node.getTextContent();
                    if (text.startsWith(oldNamePrefix)) {
                        nodesToUpdate.add(node);
                    }
                }
            }

            if (nodesToUpdate.size() > 0) {
                String message = NbBundle.getMessage(
                        JCRenameRefactoringPlugin.class,
                        "Update_jc_specific_descriptor", oldNamePrefix);
                elements.add(renameRefactoring,
                        new UpdateXmlRefactoringElement(
                        javaCardXMLFileObject, oldNamePrefix, newNamePrefix,
                        descriptor.getDocument(), nodesToUpdate,
                        message));
            }
        }

        return null;
    }

    /**
     * Renaming the package
     */
    private class RenamePackageRefactoring implements RefactoringPlugin {

        private FileObject packageFolder;

        public RenamePackageRefactoring(FileObject packageFolder) {
            this.packageFolder = packageFolder;
        }


        public Problem preCheck() {
            return null;
        }


        public Problem checkParameters() {
            return null;
        }


        public Problem fastCheckParameters() {
            return null;
        }


        public void cancelRequest() {
        }


        public Problem prepare(RefactoringElementsBag elements) {
            String oldPackageName = getResourceName(packageFolder);
            if (renameRefactoring instanceof RenameRefactoring) {
                String newPackageName = ((RenameRefactoring) renameRefactoring).getNewName();
                return prepareClassRenaming(elements, oldPackageName, newPackageName);
            } else {
                //should never happen
                return null;
            }
        }
    }

    private final class RenameClassRefactoring implements RefactoringPlugin {

        FileObject sourceFile;

        public RenameClassRefactoring(FileObject sourceFile) {
            this.sourceFile = sourceFile;
        }


        public Problem preCheck() {
            return null;
        }


        public Problem checkParameters() {
            return null;
        }


        public Problem fastCheckParameters() {
            return null;
        }


        public void cancelRequest() {
        }

        public Problem prepare(RefactoringElementsBag elements) {
            if (renameRefactoring instanceof RenameRefactoring) {
                String oldClassName = getResourceName(sourceFile);
                String name = ((RenameRefactoring) renameRefactoring).getNewName();
                String newClassName =
                    oldClassName.substring(0, oldClassName.lastIndexOf('.') + 1) + name; //NOI18N
                return prepareClassRenaming(elements, oldClassName, newClassName);
            } else {
                Problem res = null;
                MoveRefactoring mv = (MoveRefactoring) renameRefactoring;
                URL url = mv.getTarget().lookup(URL.class);
                for (FileObject fo : mv.getRefactoringSource().lookupAll(FileObject.class)) {
                    String oldClassName = getResourceName(fo);
                    String newClassName = getRelativePath (fo, url);
                    Problem p = prepareClassRenaming(elements, oldClassName, newClassName);
                    if (res != null) {
                        res.setNext(p);
                    }
                    res = p;
                }
                return res;
            }
        }

        private String getRelativePath(FileObject fo, URL url) {
            if (url == null) {
                return null;
            }
            //XXX not pretty.  All of the safe ways of computing a path
            //involve FileObjects; we have a URL to a folder that may not
            //exist.
            //For a source, we get a FileObject and an Object[] containing a
            //TreePathHandle.  For a destination, just the URL.
            //PENDING:  This is probably broken for applets which are inner
            //classes, though that is an odd corner case
            if (fo != null) {
                File f = FileUtil.toFile(fo);
                try {
                    File x = new File(url.toURI());
                    Project p = FileOwnerQuery.getOwner(fo);
                    JCProject jp = p.getLookup().lookup(JCProject.class);
                    if (jp != null) {
                        ClassPath srcPath = jp.getSourceClassPath();
                        for (FileObject root : srcPath.getRoots()) {
                            if (root.equals(fo) || FileUtil.isParentOf(root, fo)) {
                                String newPackageName = srcPath.getResourceName(fo.getParent(), File.separatorChar, false);
                                int ix = f.getAbsolutePath().indexOf(newPackageName);
                                if (ix > 0 && ix < x.getAbsolutePath().length()) {
                                    String destAsPackage = x.getAbsolutePath().substring(ix).replace(File.separatorChar, '.'); //NOI18N
                                    return destAsPackage + '.' + fo.getName();
                                } else if (ix >= x.getAbsolutePath().length()) {
                                    //default package
                                    return fo.getName();
                                } else if (ix == 0) {
                                    //move *from* default package
                                    return x.getAbsolutePath().substring(f.getParentFile().getAbsolutePath().length() + 1).replace(File.separatorChar, '.') + '.' + fo.getName(); //NOI18N
                                }
                            }
                        }
                    }
                } catch (URISyntaxException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            return null;
        }
    }

    private static final class UpdateDeploymentXmlRefactoringElement extends SimpleRefactoringElementImplementation {

        private FileObject target;
        private DeploymentXmlModel mdl;
        private String msg;

        UpdateDeploymentXmlRefactoringElement(DeploymentXmlModel mdl, String msg, FileObject target) {
            this.mdl = mdl;
            this.msg = msg;
            this.target = target;
        }


        public String getText() {
            return msg;
        }


        public String getDisplayText() {
            return getText();
        }


        public void performChange() {
            try {
                OutputStream out = target.getOutputStream();
                String xml = mdl.toXml();
                PrintWriter pw = new PrintWriter(out);
                try {
                    pw.println(xml);
                } finally {
                    pw.close();
                    out.close();
                }
            } catch (FileAlreadyLockedException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }

        }


        public Lookup getLookup() {
            return Lookups.fixed(this, target);
        }


        public FileObject getParentFile() {
            return target;
        }


        public PositionBounds getPosition() {
            return null;
        }
    }

    /**
     * Element which describe a change in XML file.
     */
    private final class UpdateXmlRefactoringElement extends SimpleRefactoringElementImplementation {

        private String oldPrefix;
        private String newPrefix;
        private FileObject targetFile;
        private Document doc;
        private List<Node> nodesToUpdate;
        private String message;

        /**
         * 
         * @param targetFile    File which needs to be updated.
         * @param oldPrefix     Old prefix of a text element.
         * @param newPrefix     New prefix of a text element.
         * @param doc           Document view of the updated XML file.
         * @param nodesToUpdate Nodes which belongs to the specified document
         *              and has text value containing the specified prefix.
         */
        UpdateXmlRefactoringElement(FileObject targetFile,
                String oldPrefix, String newPrefix, Document doc,
                List<Node> nodesToUpdate, String message) {
            this.oldPrefix = oldPrefix;
            this.newPrefix = newPrefix;
            this.targetFile = targetFile;
            this.doc = doc;
            this.nodesToUpdate = nodesToUpdate;
            this.message = message;
        }

        /** Returns text describing the refactoring formatted for display (using HTML tags).
         * @return Formatted text.
         */

        public String getDisplayText() {
            return getText();
        }

        /** Performs the change represented by this refactoring element.
         */

        public void performChange() {
            for (Node node : nodesToUpdate) {
                String text = node.getTextContent();
                text = newPrefix + text.substring(oldPrefix.length());
                node.setTextContent(text);
            }
            try {
                transformer.transform(new DOMSource(doc),
                        new StreamResult(FileUtil.toFile(targetFile)));
            } catch (TransformerException ex) {
                Exceptions.printStackTrace(ex);
            }
        }


        public String getText() {
            return message;
        }


        public Lookup getLookup() {
            return Lookup.EMPTY;
        }


        public FileObject getParentFile() {
            return targetFile;
        }

        
        public PositionBounds getPosition() {
            return null;
        }
    }
}
