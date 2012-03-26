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
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.JEditorPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.Document;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.lib.editor.bookmarks.api.Bookmark;
import org.netbeans.lib.editor.bookmarks.api.BookmarkList;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.openide.ErrorManager;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.Line;
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
    
    public static int offset2LineIndex(Document doc, int offset) {
        javax.swing.text.Element lineRoot = doc.getDefaultRootElement();
        int lineIndex = lineRoot.getElementIndex(offset);
        return lineIndex;
        
    }
    
    public static int lineIndex2Offset(Document doc, int lineIndex) {
        javax.swing.text.Element lineRoot = doc.getDefaultRootElement();
        int offset = (lineIndex < lineRoot.getElementCount())
                ? lineRoot.getElement(lineIndex).getStartOffset()
                : doc.getLength();
        return offset;
        
    }
    
    private static final String EDITOR_BOOKMARKS_1_NAMESPACE_URI = "http://www.netbeans.org/ns/editor-bookmarks/1"; // NOI18N
    
    private static final String EDITOR_BOOKMARKS_2_NAMESPACE_URI = "http://www.netbeans.org/ns/editor-bookmarks/2"; // NOI18N

    private static RequestProcessor RP = new RequestProcessor("Bookmarks saver"); // NOI18N
    
    private static final BookmarksPersistence INSTANCE = new BookmarksPersistence();
    
    public static BookmarksPersistence get() {
        return INSTANCE;
    }

    /**
     * Contains mapping of a project and its corresponding bookmarks (lazily read upon request).
     * <br/>
     * Once the bookmarks exist in the map they will be written to project's private.xml upon project close.
     */
    private final Map<Project,ProjectBookmarks> project2Bookmarks =
            new HashMap<Project,ProjectBookmarks> ();
    
    /**
     * Once a project listener will be fired this list will be used for finding out
     * which projects were just closed and so need their bookmarks to be written to their private.xml.
     */
    private final List<Project> lastOpenProjects;
    
    private List<ChangeListener> listenerList = new CopyOnWriteArrayList<ChangeListener>();
    
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
        List<Project> projects = allProjectsWithBookmarks();
        saveProjectsBookmarks(projects);
        synchronized (lastOpenProjects) {
            lastOpenProjects.clear();
        }
    }
    
    public List<Project> allProjectsWithBookmarks() {
        synchronized (project2Bookmarks) {
            return new ArrayList<Project>(project2Bookmarks.keySet());
        }
    }
    
    public void openEditor(FileObject fo, BookmarkInfo info) {
        if (fo == null) {
            URL url = info.getURL();
            if (url != null) {
                fo = URLMapper.findFileObject(url);
            }
        }
        if (fo != null) {
            DataObject dob;
            try {
                dob = DataObject.find(fo);
            } catch (DataObjectNotFoundException ex) {
                dob = null;
            }
            if (dob != null) {
                EditorCookie ec = dob.getCookie(EditorCookie.class);
                if (ec != null) {
                    Line.Set lineSet = ec.getLineSet();
                    if (lineSet != null) {
                        Line line = lineSet.getCurrent(info.getLineIndex());
                        if (line != null) {
                            line.show(Line.ShowOpenType.OPEN, Line.ShowVisibilityType.FOCUS);
                            JEditorPane[] panes = ec.getOpenedPanes();
                            if (panes.length > 0) {
                                panes[0].requestFocusInWindow();
                            }
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Load bookmarks for all projects currently opened.
     * <br/>
     * This is used by BookmarksView's initialization to display all currently known bookmarks.
     */
    public void ensureAllProjectsBookmarksLoaded() {
        List<Project> projects;
        synchronized (lastOpenProjects) {
            projects = new ArrayList<Project>(lastOpenProjects);
        }
        for (Project project : projects) {
            getProjectBookmarks(project, false);
        }
    }
    
    public BookmarkInfo findBookmarkByNameOrKey(String name, boolean byKey) {
        for (Project p : allProjectsWithBookmarks()) {
            ProjectBookmarks projectBookmarks = getProjectBookmarks(p, false);
            if (projectBookmarks != null) {
                for (URL url : projectBookmarks.allURLs()) {
                    URLBookmarks urlBookmarks = projectBookmarks.get(url);
                    for (BookmarkInfo info : urlBookmarks.getBookmarkInfos()) {
                        if (name.equals(info.getName())) {
                            info = getBookmarkInfoUpdated(info, url);
                            return info;
                        }
                    }
                }
            }
        }
        return null;
    }
    
    private Document getDocument(BookmarkInfo info, URL url) {
        Document doc = null;
        try {
            if (url == null) {
                url = info.getURL();
            }
            assert (url != null) : "Null URL"; // NOI18N
            FileObject fo = URLMapper.findFileObject(url);
            DataObject dob = DataObject.find(fo);
            EditorCookie ec = dob.getCookie(EditorCookie.class);
            if (ec != null) {
                doc = ec.getDocument();
            }
        } catch (DataObjectNotFoundException ex) {
            // doc remains == null;
        }
        return doc;
    }
    
    private BookmarkInfo getBookmarkInfoUpdated(BookmarkInfo info, URL url) {
        Document doc = getDocument(info, url);
        if (doc != null) {
            List<Bookmark> bookmarks = BookmarkList.get(doc).getBookmarks();
            for (Bookmark bookmark : bookmarks) {
                if (BookmarkAPIAccessor.INSTANCE.getInfo(bookmark) == info) {
                    return BookmarkInfo.create(info, url, bookmark.getLineNumber());
                }
            }
        }
        info = BookmarkInfo.create(info, url);
        return info;
    }
    
    public void addChangeListener(ChangeListener l) {
        listenerList.add(l);
    }
    
    public void removeChangeListener(ChangeListener l) {
        listenerList.remove(l);
    }
    
    void fireChange() {
        ChangeEvent evt = new ChangeEvent(this);
        for (ChangeListener l : listenerList) {
            l.stateChanged(evt);
        }
    }

    /**
     * Get all fileobjects that contain bookmarks located in the given project.
     * 
     * @param prj 
     */
    public FileObject[] getSortedFileObjects(ProjectBookmarks prjBookmarks) {
        List<FileObject> foList;
        if (prjBookmarks != null) {
            Collection<URL> allURLs = prjBookmarks.allURLs();
            foList = new ArrayList<FileObject>(allURLs.size());
            for (URL url : allURLs) {
                FileObject fo = URLMapper.findFileObject(url);
                if (fo != null) {
                    foList.add(fo);
                } // else: could be obsolete URL of a removed file
            }
            Collections.sort(foList, new Comparator<FileObject>() {
                @Override
                public int compare(FileObject fo1, FileObject fo2) {
                    return fo1.getNameExt().compareTo(fo2.getNameExt());
                }
            });
        } else {
            foList = Collections.emptyList();
        }
        return foList.toArray(new FileObject[foList.size()]);
    }
        
    /**
     * Loads bookmarks for a given document.
     * 
     * @param document non-null document for which the bookmarks should be loaded.
     */
    public synchronized URLBookmarks getURLBookmarks(Document document) {
        URLBookmarks ret = null;
        ProjectBookmarks projectBookmarks = getProjectBookmarks(document);
        if (projectBookmarks != null) {
            if (projectBookmarks != null) {
                FileObject fo = NbEditorUtilities.getFileObject (document); // fo should be non-null
                URL url = fo.toURL();
                ret = projectBookmarks.get(url);
            }
        }
        return ret;
    }

    public synchronized ProjectBookmarks getProjectBookmarks(Document document) {
        ProjectBookmarks ret = null;
        FileObject fo = NbEditorUtilities.getFileObject (document);
        if (fo != null) {
            Project project = FileOwnerQuery.getOwner(fo);
            if (project != null) {
                ret = getProjectBookmarks(project, true);
            }
        }
        return ret;
    }
    
    public ProjectBookmarks getProjectBookmarks(Project project, boolean forceCreation) {
        ProjectBookmarks projectBookmarks;
        synchronized (project2Bookmarks) {
            projectBookmarks = project2Bookmarks.get(project);
        }
        if (projectBookmarks == null) {
            projectBookmarks = loadProjectBookmarks (project);
            if (projectBookmarks == null && forceCreation) {
                projectBookmarks = new ProjectBookmarks();
            }
            if (projectBookmarks != null) {
                synchronized (project2Bookmarks) {
                    project2Bookmarks.put(project, projectBookmarks);
                }
                fireChange();
            }
        }
        return projectBookmarks;
    }
    
    private ProjectBookmarks loadProjectBookmarks(final Project project) {
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
                if (bookmarksElement == null) { // Attempt older version
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

                ProjectBookmarks projectBookmarks = new ProjectBookmarks();
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
                                Node nameElem = skipNonElementNode(lineOrBookmarkElem.getFirstChild());
                                assert "name".equals(nameElem.getNodeName());
                                Node nameTextNode = nameElem.getFirstChild();
                                String name = (nameTextNode != null) ? nameTextNode.getNodeValue() : "";
                                Node lineElem = skipNonElementNode(nameElem.getNextSibling());
                                int lineIndex = parseLineIndex(lineElem);
                                Node keyElem = skipNonElementNode(lineElem.getNextSibling());
                                Node keyTextNode = keyElem.getFirstChild();
                                String key = (keyTextNode != null) ? keyTextNode.getNodeValue() : "";
                                bookmarkInfo = BookmarkInfo.create(name, lineIndex, key);
                            } else {
                                int lineIndex = parseLineIndex(lineOrBookmarkElem);
                                bookmarkInfo = BookmarkInfo.create("", lineIndex, "");
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
                            projectBookmarks.put(url, new URLBookmarks(
                                    projectBookmarks, url, bookmarkInfos));
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
    
    /**
     * Save bookmarks in the given bookmark list.
     *
     * @param bookmarkList bookmark list to save.
     * @param origProject original project in which the document resided.
     * @param origURL original URL in case the document was moved 
     */
    public synchronized URLBookmarks updateBookmarkInfos (Document document,
            List<BookmarkInfo> bookmarkInfos, URLBookmarks origURLBookmarks)
    {
        // First ensure that the original infos get removed which will handle
        // possible source file's movement to another package or even project.
        if (origURLBookmarks != null) {
            origURLBookmarks.getProjectBookmarks().remove(origURLBookmarks.getUrl());
        }

        FileObject fo = NbEditorUtilities.getFileObject (document);
        if (fo != null) {
            URL url = fo.toURL ();
            Project project = FileOwnerQuery.getOwner (fo);
            ProjectBookmarks projectBookmarks;
            synchronized (project2Bookmarks) {
                projectBookmarks = project2Bookmarks.get (project);
            }
            if (projectBookmarks == null) {
                projectBookmarks = new ProjectBookmarks ();
                synchronized (project2Bookmarks) {
                    project2Bookmarks.put (project, projectBookmarks);
                }
            }
            URLBookmarks newURLBookmarks = new URLBookmarks(projectBookmarks, url, bookmarkInfos);
            projectBookmarks.put (url, newURLBookmarks);
            return newURLBookmarks;
        }
        return null;
    }
    
    private void saveProjectsBookmarks(Collection<Project> projects) {
        for (Project prj : projects) {
            saveProjectBookmarks(prj);
        }
    }
    
    private void saveProjectBookmarks(Project project) {
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
        ProjectBookmarks projectBookmarks = getProjectBookmarks(project, false);
        if (projectBookmarks == null) return;
        boolean legacy = false;
        
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            org.w3c.dom.Document document = builder.newDocument();
            String namespaceURI = legacy
                    ? EDITOR_BOOKMARKS_1_NAMESPACE_URI
                    : EDITOR_BOOKMARKS_2_NAMESPACE_URI;
            Element bookmarksElem = document.createElementNS(namespaceURI, "editor-bookmarks");
            for (URL url : projectBookmarks.allURLs()) {
                List<BookmarkInfo> bookmarkInfos = projectBookmarks.get(url).getBookmarkInfos();
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
        ProjectManager.mutex().writeAccess(new Runnable() {
            public void run() {
                List<Project> openProjects = Arrays.asList (OpenProjects.getDefault ().getOpenProjects ());
                // lastOpenProjects will contain the just closed projects
                List<Project> projectsToSave;
                synchronized (lastOpenProjects) {
                    lastOpenProjects.removeAll(openProjects);
                    projectsToSave = new ArrayList<Project>(lastOpenProjects);
                    lastOpenProjects.clear();
                    lastOpenProjects.addAll(openProjects);
                }
                for (Project p : projectsToSave) {
                    saveProjectBookmarks(p);
                }
            }
        });
    }

    @Override
    public void propertyChange (PropertyChangeEvent evt) {
        RP.post(this);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(200);
        for (Project p : allProjectsWithBookmarks()) {
            sb.append("Project ").append(p).append('\n');
            ProjectBookmarks projectBookmarks = getProjectBookmarks(p, false);
            if (p != null) {
                for (URL url : projectBookmarks.allURLs()) {
                    sb.append("    ").append(url).append("\n");
                    for (BookmarkInfo info : projectBookmarks.get(url).getBookmarkInfos()) {
                        sb.append("        ").append(info).append('\n');
                    }
                }
            }
        }
        return sb.toString();
    }

}

