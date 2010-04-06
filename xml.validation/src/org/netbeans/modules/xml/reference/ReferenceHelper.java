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
 * License. When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP. Sun designates this
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
package org.netbeans.modules.xml.reference;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.windows.WindowManager;

import org.netbeans.api.project.Project;
import org.netbeans.modules.xml.retriever.catalog.CatalogElement;
import org.netbeans.modules.xml.retriever.catalog.CatalogEntry;
import org.netbeans.modules.xml.retriever.catalog.CatalogWriteModel;
import org.netbeans.modules.xml.retriever.catalog.CatalogWriteModelFactory;
import org.netbeans.modules.xml.retriever.catalog.ProjectCatalogSupport;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.netbeans.modules.xml.catalogsupport.DefaultProjectCatalogSupport;
import static org.netbeans.modules.xml.util.UI.*;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2008.06.17
 */
final class ReferenceHelper {

    ReferenceHelper(FileObject file) {
        this(ReferenceUtil.getProject(file));
    }

    ReferenceHelper(Project project) {
        myProject = project;
//out();
//out("myProject: " + myProject);
        myCatalog = getCatalog(project);
//out("myCatalog: " + myCatalog);
        if (ourChooser == null) {
            ourChooser = new JFileChooser();
            ourChooser.setCurrentDirectory(FileUtil.toFile(myProject.getProjectDirectory().getParent()));
            ourChooser.setMultiSelectionEnabled(true);
        }
    }

    List<ReferenceChild> getReferencedResources(String extension) {
//out();
//out("GER REFERENCED RESOURCES: " + myProject + " " + extension);
        List<ReferenceChild> children = getReferencedResources();
//out("                     all: " + children.size());

        List<ReferenceChild> resources = new ArrayList<ReferenceChild>();

        for (ReferenceChild child : children) {
//out("  see: " + child);
            if (child.hasExtension(extension)) {
//out("  add.");
                resources.add(child);
            }
        }
        return resources;
    }

    List<ReferenceChild> getReferencedResources() {
        List<ReferenceChild> list = new ArrayList<ReferenceChild>();
        ReferenceNode ReferenceNode = new ReferenceNode(myProject);
        Children children = ReferenceNode.getChildren();

        if (children == null) {
            return list;
        }
        Node[] nodes = children.getNodes();

        if (nodes == null) {
            return list;
        }
        for (Node child : nodes) {
            if (child instanceof ReferenceChild) {
                list.add((ReferenceChild) child);
            }
        }
        return list;
    }

    ReferenceChild addURLAction(String value) {
//out("Add url");
        String url = (String) JOptionPane.showInputDialog(
            null, // parent
            i18n(ReferenceHelper.class, "LBL_Input_URL"), // NOI18N
            i18n(ReferenceHelper.class, "LBL_Input"), // NOI18N
            JOptionPane.PLAIN_MESSAGE,
            null, // icon
            null, // values
            value // init value
        );
        return addURL(url);
    }

    ReferenceChild addURL(String url) {
        if (url == null) {
            return null;
        }
//out();
//out();
//out("URL: " + url);
        url = url.trim();

        if (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }
        String source = url;

        if ( !source.contains("://")) { // NOI18N
            source = "http://" + source; // NOI18N
        }
//out("source: " + source);
        String target = ReferenceUtil.removeProtocol(url);
        // # 174640
        target = target.replace('?', '/');
//out();
//out("target: " + target);
        try {
            CatalogWriteModel nextCatalog = getNextCatalog(myCatalog);
//out();
//out("next: " + nextCatalog);

            if (nextCatalog == null) {
                nextCatalog = createNextCatalog(myCatalog);
            }
//out("next: " + nextCatalog);
            nextCatalog.addURI(new URI(source), new URI(target));
        }
        catch (IOException e) {
            printError(e);
        }
        catch (URISyntaxException e) {
            printError(e);
        }
        final String systemId = source;

        try {
            Thread.currentThread().sleep(SLEAP);
        }
        catch (InterruptedException e) {
            return null;
        }
        ReferenceChild child = findReferencedResources(systemId);
//out();
//out("child: " + child);
//out("   id: " + systemId);
//out();
        if (child != null) {
            child.getFromRemote(true);
        }
        return child;
    }

    private ReferenceChild findReferencedResources(String systemId) {
        List<ReferenceChild> children = getReferencedResources();

        for (ReferenceChild child : children) {
            if (child.getSystemId().equals(systemId)) {
                return child;
            }
        }
        return null;
    }

    URI addFileAction() {
        if (ourChooser.showOpenDialog(WindowManager.getDefault().getMainWindow()) != JFileChooser.APPROVE_OPTION) {
            return null;
        }
        File[] files = ourChooser.getSelectedFiles();

        if (files == null) {
            return null;
        }
        URI lastURI = null;
//out();
        for (File file : files) {
//out("add file: " + file.getName());
            URI uri = addFile(file);

            if (uri == null) {
                break;
            }
            lastURI = uri;
        }
        return lastURI;
    }
    
    URI addFile(File file) {
//out();
//out("Add FILE: " + file);
//out();
        FileObject source = getSource();

        if (source == null) {
//out("SOURCE IS EMPTY: ");
            return null;
        }
        FileObject target = FileUtil.toFileObject(file);
        ProjectCatalogSupport support = getProjectCatalogSupport();

        try {
//out();
//out(" class: " + support.getClass().getName());
//out("source: " + source);
//out("target: " + target);
//out("   URI: " + support.createCatalogEntry(source, target).toString());
            return support.createCatalogEntry(source, target);
        }
        catch (CatalogModelException e) {
            printError(e);
        }
        catch (IOException e) {
            printError(e);
        }
        return null;
    }

    CatalogWriteModel getCatalog() {
        return myCatalog;
    }

    CatalogWriteModel getCatalog(String name) {
        if (name == null) {
            return null;
        }
        try {
            FileObject catalog = myProject.getProjectDirectory().getFileObject(name);
//out();
//out();
//out("FILE: " + catalog);
//out();
//out();
            if (catalog == null) {
                return null;
            }
            return CatalogWriteModelFactory.getInstance().getCatalogWriteModelForCatalogFile(catalog);
        }
        catch (CatalogModelException e) {
            return null;
        }
    }

    private static CatalogWriteModel getCatalog(Project project) {
        if (project == null) {
            return null;
        }
        try {
            return CatalogWriteModelFactory.getInstance().getCatalogWriteModelForProject(project.getProjectDirectory());
        }
        catch (CatalogModelException e) {
            return null;
        }
    }

    CatalogWriteModel getNextCatalog(CatalogWriteModel catalog) {
        try {
            return getNextCatalog(catalog, true);
        }
        catch (IOException e) {
            printError(e);
        }
        catch (URISyntaxException e) {
            printError(e);
        }
        return null;
    }

    private CatalogWriteModel getNextCatalog(CatalogWriteModel catalog, boolean create) throws URISyntaxException, IOException {
        Collection<CatalogEntry> entries = catalog.getCatalogEntries();
//out();
//out("CATALOG: " + catalog.getClass());
        for (CatalogEntry entry : entries) {
//out("        type: " + entry.getEntryType());
            if (entry.getEntryType() == CatalogElement.nextCatalog) {
                return getCatalog(entry.getSource());
            }
        }
        if (create) {
            return createNextCatalog(catalog);
        }
        return catalog;
    }

    private CatalogWriteModel createNextCatalog(final CatalogWriteModel catalog) throws URISyntaxException, IOException {
        String name = "catalog.xml"; // NOI18N
        FileObject folder = createPrivateCacheRetriever(myProject.getProjectDirectory());
        FileObject next = folder.getFileObject(name);

        if (next == null) {
            folder.createData(name);
        }
        catalog.addNextCatalog(new URI("nbproject/private/cache/retriever/" + name), false); // NOI18N

        return getNextCatalog(catalog, false);
    }
/*
    private void saveFile(FileObject file) {
//out("== SAVE == : " + file);

        if (file == null) {
//out("file == null");
          return;
        }
        try {
            DataObject data = DataObject.find(file);
       
            if (data == null) {
//out("data == null");
              return;
            }
            SaveCookie cookie = data.getCookie(SaveCookie.class);

            if (cookie == null) {
//out("cookie == null");
              return;
            }
            cookie.save();
        }
        catch (DataObjectNotFoundException e) {
            // ignore
        }
        catch (IOException e) {
            // ignore
        }
    }
*/
    private FileObject createPrivateCacheRetriever(FileObject folder) throws IOException {
        folder = ReferenceUtil.createFolder(folder, "nbproject"); // NOI18N
        folder = ReferenceUtil.createFolder(folder, "private"); // NOI18N
        folder = ReferenceUtil.createFolder(folder, "cache"); // NOI18N
        folder = ReferenceUtil.createFolder(folder, "retriever"); // NOI18N
        return folder;
    }

    private FileObject getSource() {
//out("GET SOURCE: " + src);
        return ReferenceUtil.getSrcFolder(myProject);
    }

    String getSystemId(FileObject file) {
//out();
//out("GET SYSTEM ID: " + file);
        if (file == null) {
            return null;
        }
        return getSystemId(file, myCatalog);
    }

    private String getSystemId(FileObject file, CatalogWriteModel catalog) {
//out();
//out("get system ID: " + catalog);
        if (catalog == null) {
            return null;
        }
        Collection<CatalogEntry> entries = catalog.getCatalogEntries();

        for (CatalogEntry entry : entries) {
            if (entry.getEntryType() == CatalogElement.nextCatalog) {
                String systemId = getSystemId(file, getCatalog(entry.getSource()));

                if (systemId != null) {
                    return systemId;
                }
            }
            if (entry.getEntryType() != CatalogElement.system) {
                continue;
            }
            FileObject next = getFileObject(catalog, entry);
//out("          see: " + entry.getSource());
//out("             : " + entry.getTarget());
//out("             : " + next);
//out();
            if (next == null) {
                continue;
            }
            if (file.toString().equals(next.toString())) {
                return entry.getSource();
            }
        }
        return null;
    }

    private ProjectCatalogSupport getProjectCatalogSupport() {
        return new DefaultProjectCatalogSupport(myProject);
    }

    FileObject getFromProjectOrLocal(String source) {
        ProjectCatalogSupport support = getProjectCatalogSupport();

        try {
            URI uri = new URI(source);
//out();
//out("URI: " + uri);

            if (support.isProjectProtocol(uri)) {
//out(": " + support.resolveProjectProtocol(uri));
                return support.resolveProjectProtocol(uri);
            }
        }
        catch (URISyntaxException e) {
            return myProject.getProjectDirectory().getFileObject(source);
//out("EXCEPTION !!!: " + e.getMessage());
        }
//out();
//out("GET: " + source);
//out();
        return myProject.getProjectDirectory().getFileObject(source);
    }

    private FileObject getFileObject(CatalogWriteModel catalog, CatalogEntry entry) {
        FileObject file = FileUtil.toFileObject(FileUtil.normalizeFile(new File(entry.getSource())));
//out();
        if (file != null) {
            return file;
        }
        file = getFromProjectOrLocal(entry.getTarget());

        if (file != null) {
            return file;
        }
        return getRemote(catalog, entry, false);
    }

    FileObject getRemote(CatalogWriteModel catalog, CatalogEntry entry, boolean forceReload) {
//out();
//out("GET REMOTE");
//out("          " + entry.getSource());
//out("          " + entry.getTarget());
        FileObject folder = catalog.getCatalogFileObject().getParent();
//out("ctg: " + folder);

        if (entry.getTarget().startsWith("nb-uri:")) { // NOI18N
            return null;
        }
        try {
            String target = entry.getTarget();
            FileObject file = folder.getFileObject(target);

            if ( !forceReload && file != null) {
                return file;
            }
            if (file == null) {
                file = createData(folder, target);
            }
//out("file: " + file);
            if (file == null) {
                return null;
            }
            org.netbeans.modules.xml.retriever.catalog.Utilities.retrieveAndCache(new URI(entry.getSource()), file);

            return file;
        }
        catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }

    private FileObject createData(FileObject folder, String name) {
//out();
//out("CREATE: " + folder);
//out("      : " + name);
        name = ReferenceUtil.removeProtocol(name);

        try {
            while (true) {
                int k = name.indexOf("/"); // NOI18N

                if (k == -1) {
                    break;
                }
                FileObject subFolder = folder.getFileObject(name.substring(0, k));

                if (subFolder == null) {
                    folder = folder.createFolder(removeColon(name.substring(0, k)));
                }
                else {
                    folder = subFolder;
                }
                name = name.substring(k + 1);
            }
//out("      : " + name);
            return folder.createData(name);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String removeColon(String value) {
        if (value == null) {
            return null;
        }
        if (value.endsWith(":")) { // NOI18N
            value = value.substring(0, value.length() - 1);
        }
        return value;
    }

    private Project myProject;
    private CatalogWriteModel myCatalog;
    private static final int SLEAP = 777;
    private static JFileChooser ourChooser;
}                                                         
