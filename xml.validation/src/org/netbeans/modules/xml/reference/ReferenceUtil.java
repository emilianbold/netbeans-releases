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
package org.netbeans.modules.xml.reference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.net.URI;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.tools.ant.module.api.support.ActionUtils;
import org.xml.sax.SAXException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.modules.xml.catalogsupport.DefaultProjectCatalogSupport;
import static org.netbeans.modules.xml.util.UI.*;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2007.10.22
 */
public final class ReferenceUtil {

    private ReferenceUtil() {}

    public static List<FileObject> getXSDFilesRecursively(Project project, boolean inProjectOnly) {
        return getFilesRecursively(project, XSD, inProjectOnly);
    }

    public static List<FileObject> getWSDLFilesRecursively(Project project, boolean inProjectOnly) {
        return getFilesRecursively(project, WSDL, inProjectOnly);
    }

    private static List<FileObject> getFilesRecursively(Project project, final String extension, boolean inProjectOnly) {
        final List<FileObject> files = new ArrayList<FileObject>();
        ReferenceTraveller traveller = new ReferenceTraveller() {
            public void travel(Project project) {
//out();
//out("see: " + project);
//out();
                FileObject src = getSrcFolder(project);

                if (src == null) {
                    return;
                }
                Enumeration children = src.getChildren(true);

                while (children.hasMoreElements()) {
                    FileObject file = (FileObject) children.nextElement();

                    if (file.getExt().equalsIgnoreCase(extension)) {
                        files.add(file);
                    }
                }
            }
        };
        if (inProjectOnly) {
            traveller.travel(project);
        } else {
            travelRecursively(project, traveller);
        }
        return files;
    }

    public static List<ReferenceFile> getWSDLResources(Project project) {
        return getResources(project, WSDL);
    }

    public static List<ReferenceFile> getXSDResources(Project project) {
        return getResources(project, XSD);
    }

    public static List<File> getArchiveFiles(FileObject file) {
        return getArchiveFiles(getProject(file));
    }

    public static List<File> getArchiveFiles(Project project) {
        List<ReferenceFile> archives = getArchiveResources(project);
        List<File> files = new ArrayList<File>();
//out();
//out("archives: " + archives.size());

        for (ReferenceFile archive : archives) {
            files.add(FileUtil.toFile(archive.getFile()));
        }
//out("   files: " + files.size());
        return files;
    }

    public static List<ReferenceFile> getArchiveResources(Project project) {
        List<ReferenceFile> archives = new ArrayList<ReferenceFile>();
        archives.addAll(getResources(project, JAR));
        archives.addAll(getResources(project, ZIP));
        return archives;
    }

    private static List<ReferenceFile> getResources(Project project, final String extension) {
        final List<ReferenceFile> files = new ArrayList<ReferenceFile>();
        travelRecursively(project, new ReferenceTraveller() {

            public void travel(Project project) {
//out();
//out("see: " + project + " " + extension);
//out();
                FileObject src = getSrcFolder(project);

                if (src == null) {
                    return;
                }
                Enumeration children = src.getChildren(true);

                while (children.hasMoreElements()) {
                    FileObject file = (FileObject) children.nextElement();

                    if (file.getExt().equalsIgnoreCase(extension)) {
                        add(files, new ReferenceFile(file, project));
                    }
                }
            }
        });
        addReferencedResources(project, files, extension);

        return files;
    }

    public static List<ReferenceChild> getReferencedResources(Project project, String extension) {
        return new ReferenceHelper(project).getReferencedResources(extension);
    }

    public static List<ReferenceChild> getReferencedResources(Project project) {
        return new ReferenceHelper(project).getReferencedResources();
    }

    private static void addReferencedResources(Project project, List<ReferenceFile> files, String extension) {
        List<ReferenceChild> children = new ReferenceHelper(project).getReferencedResources(extension);
//out();
//out("addReferencedResources: " + children.size());

        for (ReferenceChild child : children) {
            FileObject file = child.getFileObject();
//out("fo: " + child.getDisplayName() + " " + child.getFileObject());

            if (file != null) {
                add(files, new ReferenceFile(file, project, child.getURL(), child.isRemoteResource()));
//out("added.");
            }
        }
    }

    private static void add(List<ReferenceFile> files, ReferenceFile file) {
        if ( !files.contains(file)) {
            files.add(file);
        }
    }

    public static void travelRecursively(Project project, ReferenceTraveller traveller) {
        travelRecursively(project, traveller, new ArrayList<Project>());
    }

    private static void travelRecursively(Project project, ReferenceTraveller traveller, List<Project> travelled) {
        if (project == null) {
            return;
        }
        if (travelled.contains(project)) {
            return;
        }
        travelled.add(project);
        traveller.travel(project);

        List<Project> referencedProjects = getReferencedProjects(project);

        if (referencedProjects == null) {
            return;
        }
        for (Project referencedProject : referencedProjects) {
            travelRecursively(referencedProject, traveller, travelled);
        }
    }

    public static List<Project> getReferencedProjects(Project project) {
        return getReferencedProjects(project.getProjectDirectory());
    }

    private static List<Project> getReferencedProjects(FileObject project) {
        DefaultProjectCatalogSupport instance = DefaultProjectCatalogSupport.getInstance(project);
        
        if (instance == null) {
            return null;
        }
        Iterator iterator = instance.getProjectReferences().iterator();
        ArrayList<Project> projects = new ArrayList<Project>();

        while (iterator.hasNext()) {
            projects.add((Project) iterator.next());
        }
        return projects;
    }

    public static FileObject getSrcFolder(Project project) {
        return project.getProjectDirectory().getFileObject(SRC_FOLDER);
    }

    public static String removeProtocol(String value) {
        if (value == null) {
            return null;
        }
        int k = value.indexOf("://"); // NOI18N

        if (k != -1) {
            value = value.substring(k + 2 + 1);
        }
        return value;
    }

    public static URI addFile(FileObject project, FileObject file) {
        return new ReferenceHelper(project).addFile(FileUtil.toFile(file));
    }

    public static URI addFile(FileObject project, File file) {
        return new ReferenceHelper(project).addFile(file);
    }

    public static boolean isSameProject(FileObject file1, FileObject file2) {
        return getProject(file1) == getProject(file2);
    }

    public static Project getProject(FileObject file) {
//out();
//out();
//out("================");
//out("get project for: " + file);

        while (file != null) {
//out("           file: " + file);
//out("         folder: " + file.isFolder());

            if (file.isFolder()) {
//out("         is prj: " + ProjectManager.getDefault().isProject(file));

                if (ProjectManager.getDefault().isProject(file)) {
                    try {
                        return ProjectManager.getDefault().findProject(file);
                    }
                    catch (IOException e) {
//out();
//out("!!! EXC");
//out();
                        return null;
                    }
                }
            }
            file = file.getParent();
        }
//out();
//out("===============");
//out();
//out("!!! NULL");
//out();
        return null;
    }

    public static FileObject generateWsdlFromEjbModule(FileObject wsdl) {
        if (wsdl == null) {
            return null;
        }
        return generateWsdl(getProject(wsdl), getWebServiceName(wsdl.getName()), getWebServiceName(wsdl.getName()));
    }

    public static FileObject generateWsdlFromJavaModule(FileObject java) {
        if (java == null) {
            return null;
        }
        return generateWsdl(getProject(java), getWebServiceName(java), java.getName());
    }

    private static FileObject generateWsdl(Project project, String webServiceName, String javaName) {
//out();
//out("GENERATE");
//out("       project: " + project);
//out("webServiceName: " + webServiceName);
//out("      javaName: " + javaName);
        if (project == null) {
            return null;
        }
        if (webServiceName == null) {
            printError(i18n(ReferenceUtil.class, "ERR_Cannot_find_Web_service", javaName)); // NOI18N
            return null;
        }
//out();
        ActionProvider provider = (ActionProvider) project.getLookup().lookup(ActionProvider.class);
//out("provider: " + provider);
        FileObject build = project.getProjectDirectory().getFileObject("build.xml"); // NOI18N
        ExecutorTask task = null;
        FileObject dir = project.getProjectDirectory();
//out();
//out("webServiceName: " + webServiceName);
//out("javaName: " + javaName);
//out();
        try {
            task = ActionUtils.runTarget(build, new String[] {"wsgen-" + webServiceName}, null); // NOI18N
        }
        catch (IOException e) {
            return null;
        }
        task.waitFinished();
//out();
//out("done");
//out();
        FileObject resources = dir.getFileObject("build/generated-sources/jax-ws/resources"); // NOI18N

//out("resources: " + resources);
//out("wsdl: " + resources.getFileObject(name + "Service.wsdl"));
        FileObject wsdls = copy(resources, dir.getFileObject("src"), "conf", "wsdl"); // NOI18N

        if (wsdls == null) {
            return null;
        }
        return wsdls.getFileObject(javaName + "Service.wsdl"); // NOI18N
    }

    private static FileObject copy(FileObject source, FileObject target, String name1, String name2) {
        if (source == null || target == null) {
            return null;
        }
        FileObject folder2 = null;

        try {
            FileObject folder1 = createFolder(target, name1);

            if (folder1 == null) {
                return null;
            }
            folder2 = createFolder(folder1, name2);

            if (folder2 == null) {
                return null;
            }
            FileObject[] artifacts = source.getChildren();

            for (FileObject artifact : artifacts) {
                overwrite(artifact, folder2);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return folder2;
    }

    private static void overwrite(FileObject source, FileObject target) throws IOException {
        FileObject file = target.getFileObject(source.getNameExt());

        if (file != null) {
            FileLock lock = null;

            try {
                lock = file.lock();
                file.delete(lock);
            } finally {
                if (lock != null) {
                    lock.releaseLock();
                }
            }
        }
        FileUtil.copyFile(source, target, source.getName());
    }

    static FileObject createFolder(FileObject folder, String name) throws IOException {
        FileObject subFolder = folder.getFileObject(name);

        if (subFolder == null) {
            subFolder = folder.createFolder(name);
        }
        return subFolder;
    }

    public static String getLocation(FileObject project, FileObject file) {
//out("ReferenceUtil.getLocation: " + project);
//out("                         : " + file);
        String location = new ReferenceHelper(project).getSystemId(file);

        if (location != null) {
            return location;
        }
        // # 178340
        Project targetProject = getProject(file);
//out("GET LOCATION");
//out();
//out("targetProject: " + targetProject);
        List<Project> referencedProjects = getReferencedProjects(project);

        if (referencedProjects == null) {
            return null;
        }
        for (Project referencedProject : referencedProjects) {
            if (referencedProject != targetProject) {
                continue;
            }
            return addFile(project, file).toString();
        }
        return null;
    }

    private static String getWebServiceName(String name) {
        if ( !name.endsWith("Service")) { // NOI18N
            return name;
        }
        return name.substring(0, name.length() - "Service".length()); // NOI18N
    }

    private static String getWebServiceName(FileObject java) {
        if (java == null) {
            return null;
        }
        FileObject project = getProject(java).getProjectDirectory();

        if (project == null) {
            return null;
        }
        String srcPath = project.getPath().replace("\\", "/") + "/src/java/"; // NOI18N
        String javaPath = java.getPath().replace("\\", "/"); // NOI18N
        String name = java.getName();
//out();
//out(" srcPath: " + srcPath);
//out("javaPath: " + javaPath);
        if ( !javaPath.startsWith(srcPath)) {
            return null;
        }
        name = javaPath.substring(srcPath.length());
        name = name.replace("/", "."); // NOI18N

        if (name.toLowerCase().endsWith(JAVA)) {
            name = name.substring(0, name.length() - JAVA.length());
        }
        return findWebServiceName(project, name);
    }

    private static void show(Node node, String indent) {
        out(indent + "Node: " + node.getNodeName() + ", value: '" + value(node) + "'" + ", attr: '" + attr(node) + "'"); // NOI18N
        NodeList children = node.getChildNodes();

        for (int i=0; i < children.getLength(); i++) {
            Node child = children.item(i);
            int type = child.getNodeType();

            if (type == Node.DOCUMENT_NODE || type == Node.ELEMENT_NODE) {
                show(child, indent + "    "); // NOI18N
            }
        }
    }

    private static String value(Node node) {
        if (node == null) {
            return "";
        }
        String value = node.getNodeValue();

        if (value == null) {
            return value(node.getFirstChild());
        }
        return value.trim();
    }

    private static String attr(Node node) {
        NamedNodeMap attributes = node.getAttributes();

        if (attributes == null) {
            return "";
        }
        StringBuilder value = new StringBuilder();

        for (int i=0; i < attributes.getLength(); i++) {
            Node attribute = attributes.item(i);
            value.append(" ATTR: " + attribute.getNodeName() + ", value: '" + attribute.getNodeValue() + "'"); // NOI18N
        }
        return value.toString();
    }

    private static String findWebServiceName(FileObject project, String java) {
        FileObject xml = project.getFileObject("nbproject/jax-ws.xml"); // NOI18N

        if (xml == null) {
            return null;
        }
        Document document = getDocument(FileUtil.toFile(xml));
//out("document: " + document);
//      show(document, "");

        if (document == null) {
            return null;
        }
        NodeList children = document.getElementsByTagName("implementation-class"); // NOI18N

        if (children == null) {
            return null;
        }
        for (int i=0; i < children.getLength(); i++) {
          Node implementation = children.item(i);
//show(implementation, "    ");

          if (implementation == null) {
//out("  1");
              continue;
          }
          if ( !java.equals(value(implementation))) {
//out("  2");
              continue;
          }
          Node service = implementation.getParentNode();

          if (service == null) {
              return null;
          }
          return getNameAttr(service);
        }
        return null;
    }

    private static Document getDocument(File file) {
        if (file == null) {
            return null;
        }
        try {
            return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);
        } 
        catch (SAXException e) {
            return null;
        }
        catch (IOException e) {
            return null;
        }
        catch (ParserConfigurationException e) {
            return null;
        }
    }

    private static String getNameAttr(Node node) {
        NamedNodeMap attributes = node.getAttributes();

        if (attributes == null) {
            return null;
        }
        if (attributes.getLength() != 1) {
            return null;
        }
        Node attribute = attributes.item(0);

        if (attribute == null) {
            return null;
        }
        if ( !"name".equals(attribute.getNodeName())) { // NOI18N
            return null;
        }
        return attribute.getNodeValue();
    }

    public static String getDisplayName(File file) {
        return getDisplayName(FileUtil.toFileObject(file), null);
    }

    public static String getDisplayName(FileObject file, String url) {
        Project project = getProject(file);

        if (project == null) {
            return FileUtil.toFile(file).getAbsolutePath();
        }
        String projectName = "[" + ProjectUtils.getInformation(project).getDisplayName() + "]"; // NOI18N
        String fileName = url != null ? url : calculateRelativeName(file, project);

        return projectName + " " + fileName; // NOI18N
    }

    private static String calculateRelativeName(FileObject file, Project project) {
        if (file == null) {
            return null;
        }
        String path = file.getPath();
        Sources sources = ProjectUtils.getSources(project);
        SourceGroup[] groups = sources.getSourceGroups(Sources.TYPE_GENERIC);

        for (SourceGroup group : groups) {
            String folder = group.getRootFolder().getPath();

            if (path.startsWith(folder)) {
                return removeSrcPrefix(path.substring(folder.length()));
            }
        }
        return removeSrcPrefix(path);
    }

    private static String removeSrcPrefix(String name) {
        if (name.startsWith(SLASHED_SRC)) {
            return name.substring(SLASHED_SRC.length());
        }
        return name;
    }

    public static final String XSD = "xsd"; // NOI18N
    public static final String WSDL = "wsdl"; // NOI18N
    private static final String JAR = "jar"; // NOI18N
    private static final String ZIP = "zip"; // NOI18N
    private static final String SRC_FOLDER = "src"; // NOI18N
    private static final String SLASHED_SRC = "/" + SRC_FOLDER + "/"; // NOI18N
    private static final String JAVA = ".java"; // NOI18N
}
