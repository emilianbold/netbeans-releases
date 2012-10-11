/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.editor.bookmarks;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.openide.ErrorManager;
import org.openide.util.Mutex.Action;
import org.openide.util.RequestProcessor;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Services to update or save bookmarks to persistent format.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public class BookmarksPersistence implements PropertyChangeListener, Runnable {
    
    private static final String EDITOR_BOOKMARKS_1_NAMESPACE_URI = "http://www.netbeans.org/ns/editor-bookmarks/1"; // NOI18N
    
    private static final String EDITOR_BOOKMARKS_2_NAMESPACE_URI = "http://www.netbeans.org/ns/editor-bookmarks/2"; // NOI18N

    private static RequestProcessor RP = new RequestProcessor("Bookmarks saver"); // NOI18N
    
    private static final BookmarksPersistence INSTANCE = new BookmarksPersistence();
    
    public static BookmarksPersistence get() {
        return INSTANCE;
    }

    /**
     * Once a project listener will be fired this list will be used for finding out
     * which projects were just closed and so need their bookmarks to be written to their private.xml.
     */
    private final List<Project> lastOpenProjects;
    
    private boolean ensureProjectsBookmarksLoadedUponProjectsLoad;
    
    private BookmarksPersistence() {
        lastOpenProjects = new ArrayList<Project>();
    }
    
    public void initProjectsListening() {
        OpenProjects openProjects = OpenProjects.getDefault();
        List<Project> projects = Arrays.asList(openProjects.getOpenProjects());
        synchronized (lastOpenProjects) {
            lastOpenProjects.addAll(projects);
        }
        openProjects.addPropertyChangeListener(this);
    }
    
    public void endProjectsListening() {
        OpenProjects.getDefault ().removePropertyChangeListener (this);
        BookmarkManager lockedBookmarkManager = BookmarkManager.getLocked();
        try {
            List<Project> projects;
            synchronized (lastOpenProjects) {
                projects = new ArrayList<Project>(lastOpenProjects);
            }
            for (Project project : projects) {
                URI projectURI = BookmarkUtils.toURI(project);
                ProjectBookmarks projectBookmarks;
                if (projectURI != null &&
                        (projectBookmarks = lockedBookmarkManager.
                            getProjectBookmarks(project, projectURI, false, false)) != null)
                {
                    saveProjectBookmarks(project, projectBookmarks);
                    lockedBookmarkManager.removeProjectBookmarks(projectBookmarks);
                }
            }
            for (ProjectBookmarks projectBookmarks : lockedBookmarkManager.allLoadedProjectBookmarks()) {
                URI prjURI = projectBookmarks.getProjectURI();
                Project prj = BookmarkUtils.findProject(prjURI);
                if (prj != null) {
                    saveProjectBookmarks(prj, projectBookmarks);
                    lockedBookmarkManager.removeProjectBookmarks(projectBookmarks);
                }
            }
        } finally {
            lockedBookmarkManager.unlock();
        }
        synchronized (lastOpenProjects) {
            lastOpenProjects.clear();
        }
    }
    
    /**
     * Load bookmarks for all projects currently opened.
     * <br/>
     * This is used by BookmarksView's initialization to display all currently known bookmarks.
     */
    public void ensureAllOpenedProjectsBookmarksLoaded() {
        ensureProjectsBookmarksLoadedUponProjectsLoad = true;
        List<Project> projects;
        synchronized (lastOpenProjects) {
            projects = new ArrayList<Project>(lastOpenProjects);
        }
        BookmarkManager lockedBookmarkManager = BookmarkManager.getLocked();
        try {
            lockedBookmarkManager.ensureProjectBookmarksLoaded(projects);
        } finally {
            lockedBookmarkManager.unlock();
        }
        
    }
    
    ProjectBookmarks loadProjectBookmarks(final Project project) {
        return
        ProjectManager.mutex().readAccess(new Action<ProjectBookmarks>() {
            @Override
            public ProjectBookmarks run() {
                int version = 2;
                AuxiliaryConfiguration ac = ProjectUtils.getAuxiliaryConfiguration (project);
                Element bookmarksElement = ac.getConfigurationFragment(
                    "editor-bookmarks",
                    EDITOR_BOOKMARKS_2_NAMESPACE_URI,
                    false
                );
                int lastBookmarkId = 0;
                if (bookmarksElement != null) {
                    String lastBookmarkIdText = bookmarksElement.getAttribute("lastBookmarkId");
                    try {
                        lastBookmarkId = Integer.parseInt(lastBookmarkIdText);
                    } catch (NumberFormatException ex) {
                        // Leave lastBookmarkId == 0
                    }

                } else { // Attempt older version
                    version = 1;
                    bookmarksElement = ac.getConfigurationFragment(
                        "editor-bookmarks",
                        EDITOR_BOOKMARKS_1_NAMESPACE_URI,
                        false
                    );
                    if (bookmarksElement == null) {
                        return null;
                    }
                }

                URI projectURI = BookmarkUtils.toURI(project);
                ProjectBookmarks projectBookmarks = new ProjectBookmarks(projectURI, lastBookmarkId);
                URL projectFolderURL = project.getProjectDirectory().toURL();
                Node fileElem = skipNonElementNode (bookmarksElement.getFirstChild ());
                while (fileElem != null) {
                    assert "file".equals (fileElem.getNodeName ());
                    Node urlElem = skipNonElementNode (fileElem.getFirstChild ());
                    assert "url".equals (urlElem.getNodeName ());
                    Node lineOrBookmarkElem = skipNonElementNode (urlElem.getNextSibling ());
                    ArrayList<BookmarkInfo> bookmarkInfos = new ArrayList<BookmarkInfo>();
                    while (lineOrBookmarkElem != null) {
                        String nodeName = lineOrBookmarkElem.getNodeName();
                        try {
                            BookmarkInfo bookmarkInfo;
                            if (version == 2) {
                                assert "bookmark".equals(nodeName);
                                assert (lineOrBookmarkElem.getNodeType() == Node.ELEMENT_NODE);
                                Element bookmarkElem = (Element) lineOrBookmarkElem;
                                int id = -1;
                                if (bookmarkElem.hasAttributes()) {
                                    String idText = bookmarkElem.getAttribute("id");
                                    try {
                                        id = Integer.parseInt(idText);
                                        projectBookmarks.ensureBookmarkIdSkip(id);
                                    } catch (NumberFormatException ex) {
                                        // Leave id == -1
                                    }
                                }
                                if (id == -1) {
                                    id = projectBookmarks.generateBookmarkId();
                                }
                                Node nameElem = skipNonElementNode(lineOrBookmarkElem.getFirstChild());
                                assert "name".equals(nameElem.getNodeName());
                                Node nameTextNode = nameElem.getFirstChild();
                                String name = (nameTextNode != null) ? nameTextNode.getNodeValue() : "";
                                Node lineElem = skipNonElementNode(nameElem.getNextSibling());
                                int lineIndex = parseLineIndex(lineElem);
                                Node keyElem = skipNonElementNode(lineElem.getNextSibling());
                                Node keyTextNode = keyElem.getFirstChild();
                                String key = (keyTextNode != null) ? keyTextNode.getNodeValue() : "";
                                bookmarkInfo = BookmarkInfo.create(id, name, lineIndex, key);
                            } else {
                                int lineIndex = parseLineIndex(lineOrBookmarkElem);
                                int id = projectBookmarks.getLastBookmarkId();
                                bookmarkInfo = BookmarkInfo.create(id, "", lineIndex, "");
                            }
                            bookmarkInfos.add(bookmarkInfo);
                            
                        } catch (DOMException e) {
                            ErrorManager.getDefault().notify(e);
                        } catch (NumberFormatException e) {
                            ErrorManager.getDefault().notify(e);
                        }
                        lineOrBookmarkElem = skipNonElementNode(lineOrBookmarkElem.getNextSibling());
                    }
                    bookmarkInfos.trimToSize();

                    try {
                        try {
                            Node urlElemText = urlElem.getFirstChild();
                            String relOrAbsURLString = urlElemText.getNodeValue();
                            URI uri = new URI(relOrAbsURLString);
                            URL url;
                            if (!uri.isAbsolute() && projectFolderURL != null) { // relative URI
                                url = new URL(projectFolderURL, relOrAbsURLString);
                            } else { // absolute URL or don't have base URL
                                url = new URL(relOrAbsURLString);
                            }
                            projectBookmarks.add(new FileBookmarks(
                                    projectBookmarks, url, null, bookmarkInfos));
                        } catch (URISyntaxException e) {
                            ErrorManager.getDefault().notify(e);
                        } catch (MalformedURLException e) {
                            ErrorManager.getDefault().notify(e);
                        }
                    } catch (DOMException e) {
                        ErrorManager.getDefault ().notify (e);
                    }

                    fileElem = skipNonElementNode (fileElem.getNextSibling ());
                } // while element
                return projectBookmarks;
            }

        });
    }
    
    static int parseLineIndex(Node lineElem) {
        assert "line".equals(lineElem.getNodeName());
        // Fetch the line number from the node
        Node lineElemText = lineElem.getFirstChild();
        String lineIndexString = lineElemText.getNodeValue();
        return Integer.parseInt(lineIndexString);
    }
    
    private static Node skipNonElementNode (Node node) {
        while (node != null && node.getNodeType () != Node.ELEMENT_NODE) {
            node = node.getNextSibling ();
        }
        return node;
    }
    
    private void saveProjectBookmarks(Project project, ProjectBookmarks projectBookmarks) {
        if (project == null) { // Bookmarks that do not belong to any project
            return;
        }
        if (!ProjectManager.getDefault ().isValid (project)) {
            return; // cannot modify it now anyway
        }
        AuxiliaryConfiguration auxiliaryConfiguration = ProjectUtils.getAuxiliaryConfiguration(project);
        URI baseURI;
        try {
            baseURI = new URI (project.getProjectDirectory ().toURL ().toExternalForm ());
        } catch (URISyntaxException e) {
            // Use global urls in such case
            baseURI = null;
        }
        boolean legacy = false;
        
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            org.w3c.dom.Document document = builder.newDocument();
            String namespaceURI = legacy
                    ? EDITOR_BOOKMARKS_1_NAMESPACE_URI
                    : EDITOR_BOOKMARKS_2_NAMESPACE_URI;
            Element bookmarksElem = document.createElementNS(namespaceURI, "editor-bookmarks");
            bookmarksElem.setAttribute("lastBookmarkId", String.valueOf(projectBookmarks.getLastBookmarkId()));
            for (URL url : projectBookmarks.allURLs()) {
                List<BookmarkInfo> bookmarkInfos = projectBookmarks.get(url).getBookmarks();
                if (bookmarkInfos.isEmpty()) {
                    continue;
                }
                Element fileElem = document.createElementNS(namespaceURI, "file");
                Element urlElem = document.createElementNS(namespaceURI, "url");
                String url2 = url.toExternalForm();
                // Possibly relativize the URL
                if (baseURI != null) {
                    try {
                        URI absoluteURI = new URI (url2);
                        URI relativeURI = baseURI.relativize (absoluteURI);
                        url2 = relativeURI.toString();
                    } catch (URISyntaxException e) {
                        ErrorManager.getDefault().notify(e);
                        // leave the original full URL
                    }
                }
                urlElem.appendChild (document.createTextNode (url2));
                fileElem.appendChild (urlElem);
                for (BookmarkInfo bookmarkInfo : bookmarkInfos) {
                    if (legacy) { // Use legacy mode
                        Element lineElem = document.createElementNS(namespaceURI, "line");
                        lineElem.appendChild(document.createTextNode(Integer.toString(bookmarkInfo.getLineIndex())));
                        fileElem.appendChild(lineElem);
                    } else { // New mode
                        Element nameElem = document.createElementNS(namespaceURI, "name");
                        nameElem.appendChild(document.createTextNode(bookmarkInfo.getName()));
                        Element lineElem = document.createElementNS(namespaceURI, "line");
                        lineElem.appendChild(document.createTextNode (Integer.toString(bookmarkInfo.getLineIndex())));
                        Element keyElem = document.createElementNS(namespaceURI, "key");
                        keyElem.appendChild(document.createTextNode(String.valueOf(bookmarkInfo.getKey())));
                        Element bookmarkElem = document.createElementNS(namespaceURI, "bookmark");
                        bookmarkElem.setAttribute("id", String.valueOf(bookmarkInfo.getId()));
                        bookmarkElem.appendChild(nameElem);
                        bookmarkElem.appendChild(lineElem);
                        bookmarkElem.appendChild(keyElem);
                        fileElem.appendChild(bookmarkElem);
                    }
                }
                bookmarksElem.appendChild(fileElem);
            } // for URL
            auxiliaryConfiguration.putConfigurationFragment (
                bookmarksElem, false
            );
        } catch (ParserConfigurationException e) {
            ErrorManager.getDefault().notify(e);
        }
    }
    
    @Override
    public void run() {
        final BookmarkManager lockedBookmarkManager = BookmarkManager.getLocked();
        try {
            ProjectManager.mutex().writeAccess(new Runnable() {
                @Override
                public void run() {
                    List<Project> openProjects = Arrays.asList(OpenProjects.getDefault().getOpenProjects());
                    // lastOpenProjects will contain the just closed projects
                    List<Project> projectsToSave;
                    synchronized (lastOpenProjects) {
                        lastOpenProjects.removeAll(openProjects);
                        projectsToSave = new ArrayList<Project>(lastOpenProjects);
                        lastOpenProjects.clear();
                        lastOpenProjects.addAll(openProjects);

                        for (Project p : projectsToSave) {
                            ProjectBookmarks projectBookmarks = lockedBookmarkManager.getProjectBookmarks(p, false, false);
                            if (projectBookmarks != null) {
                                saveProjectBookmarks(p, projectBookmarks); // Write into private.xml under project's mutex acquired
                                lockedBookmarkManager.removeProjectBookmarks(projectBookmarks);
                            }
                        }
                        // If ensureAllOpenedProjectsBookmarksLoaded requested previously do it now
                        if (ensureProjectsBookmarksLoadedUponProjectsLoad) {
                            ensureProjectsBookmarksLoadedUponProjectsLoad = false;
                            ensureAllOpenedProjectsBookmarksLoaded();
                        }
                    }
                }
            });
        } finally {
            lockedBookmarkManager.unlock();
        }
    }

    @Override
    public void propertyChange (PropertyChangeEvent evt) {
        RP.post(this);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(200);
        sb.append("Opened Projects:\n"); // NOI18N
        synchronized (lastOpenProjects) {
            for (Project p : lastOpenProjects) {
                sb.append("Project ").append(p).append('\n'); // NOI18N
            }
        }
        sb.append("------------------------"); // NOI18N
        return sb.toString();
    }

}

