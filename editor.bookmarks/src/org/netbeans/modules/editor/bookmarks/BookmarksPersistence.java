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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;
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
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.text.NbDocument;
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

public class BookmarksPersistence {
    
    private static final String     EDITOR_BOOKMARKS_NAMESPACE_URI = "http://www.netbeans.org/ns/editor-bookmarks/1"; // NOI18N
    private static final Map<Project,URLToBookmarks> 
                                    projectToBookmarks = new WeakHashMap<Project,URLToBookmarks> ();
    private static ProjectsListener projectsListener;
    
    public static void init () {
        projectsListener = new ProjectsListener ();
    }
    
    public static void destroy () {
        if (projectsListener != null) { // #160292
            projectsListener.destroy ();
        }
    
        List<Project> projects = new ArrayList (projectToBookmarks.keySet ());
        for (Project project : projects)
            saveBookmarks (project);
    }
    
    /**
     * Loads {@link BookmarkList} from cache or from project settings.
     * 
     * @param bookmarkList
     */
    public static synchronized void loadBookmarks (BookmarkList bookmarkList) {
        Document document = bookmarkList.getDocument();
        FileObject fo = NbEditorUtilities.getFileObject (document);
        if (fo == null) return;
        Project project = FileOwnerQuery.getOwner (fo);
        if (project == null) return;
        URLToBookmarks urlToBookmarks = projectToBookmarks.get (project);
        if (urlToBookmarks == null) {
            urlToBookmarks = loadBookmarks (project);
            if (urlToBookmarks != null)
                projectToBookmarks.put (project, urlToBookmarks);
        }
        if (urlToBookmarks == null) return;
        try {
            URL url = fo.getURL ();
            int[] lines = urlToBookmarks.get (url);
            if (lines != null)
                for (int lineNumber : lines) {
                    try {
                        int offset = NbDocument.findLineOffset ((StyledDocument) document, lineNumber);
                        bookmarkList.addBookmark (offset);
                    } catch (IndexOutOfBoundsException ex) {
                        // line does not exists now (some external changes)
                    }
                }
        } catch (FileStateInvalidException e) {
            // Ignore this file (could be deleted etc.)
        }
    }
    
    private static URLToBookmarks loadBookmarks (final Project project) {
        return
        ProjectManager.mutex().readAccess(new Action<URLToBookmarks>() {
            @Override
            public URLToBookmarks run() {
                AuxiliaryConfiguration ac = ProjectUtils.getAuxiliaryConfiguration (project);
                Element bookmarksElement = ac.getConfigurationFragment(
                    "editor-bookmarks",
                    EDITOR_BOOKMARKS_NAMESPACE_URI,
                    false
                );
                if (bookmarksElement == null) return null;
                try {
                    URLToBookmarks urlToBookmarks = new URLToBookmarks ();
                    URL projectFolderURL = project.getProjectDirectory ().getURL ();
                    Node fileElem = skipNonElementNode (bookmarksElement.getFirstChild ());
                    while (fileElem != null) {
                        assert "file".equals (fileElem.getNodeName ());
                        Node urlElem = skipNonElementNode (fileElem.getFirstChild ());
                        assert "url".equals (urlElem.getNodeName ());
                        Node lineElem = skipNonElementNode (urlElem.getNextSibling ());
                        int[] lineIndexesArray = new int[1];
                        int lineCount = 0;
                        while (lineElem != null) {
                            assert "line".equals (lineElem.getNodeName ());
                            // Check whether there is enough space in the line number array
                            if (lineCount == lineIndexesArray.length) {
                                lineIndexesArray = reallocateIntArray (lineIndexesArray, lineCount, lineCount << 1);
                            }
                            // Fetch the line number from the node
                            try {
                                Node lineElemText = lineElem.getFirstChild();
                                String lineNumberString = lineElemText.getNodeValue ();
                                int lineNumber = Integer.parseInt (lineNumberString);
                                lineIndexesArray[lineCount++] = lineNumber;
                            } catch (DOMException e) {
                                ErrorManager.getDefault().notify(e);
                            } catch (NumberFormatException e) {
                                ErrorManager.getDefault().notify(e);
                            }
                            lineElem = skipNonElementNode(lineElem.getNextSibling());
                        }

                        try {
                            URL url;
                            try {
                                Node urlElemText = urlElem.getFirstChild();
                                String relOrAbsURLString = urlElemText.getNodeValue();
                                URI uri = new URI(relOrAbsURLString);
                                if (!uri.isAbsolute() && projectFolderURL != null) { // relative URI
                                    url = new URL(projectFolderURL, relOrAbsURLString);
                                } else { // absolute URL or don't have base URL
                                    url = new URL(relOrAbsURLString);
                                }
                            } catch (URISyntaxException e) {
                                ErrorManager.getDefault().notify(e);
                                url = null;
                            } catch (MalformedURLException e) {
                                ErrorManager.getDefault().notify(e);
                                url = null;
                            }

                            if (url != null) {
                                if (lineCount != lineIndexesArray.length) {
                                    lineIndexesArray = reallocateIntArray(lineIndexesArray, lineCount, lineCount);
                                }
                                urlToBookmarks.put (url, lineIndexesArray);
                            }
                        } catch (DOMException e) {
                            ErrorManager.getDefault ().notify (e);
                        }

                        fileElem = skipNonElementNode (fileElem.getNextSibling ());
                    } // while element
                    return urlToBookmarks;
                } catch (FileStateInvalidException e) {
                    return null;
                }
            }

        });
    }
    
    private static Node skipNonElementNode (Node node) {
        while (node != null && node.getNodeType () != Node.ELEMENT_NODE) {
            node = node.getNextSibling ();
        }
        return node;
    }
    
   private static int[] reallocateIntArray (int[] intArray, int count, int newLength) {
        int[] newIntArray = new int [newLength];
        System.arraycopy (intArray, 0, newIntArray, 0, count);
        return newIntArray;
    }
   
    public static synchronized void saveBookmarks (BookmarkList bookmarkList) {
        Document document = bookmarkList.getDocument ();
        FileObject fileObject = NbEditorUtilities.getFileObject (document);
        if (fileObject == null) return;
        List<Bookmark> bookmarks = new ArrayList<Bookmark> (bookmarkList.getBookmarks ());
        int[] lineNumbers = new int [bookmarks.size ()];
        for (int i = 0; i < bookmarks.size (); i++) {
            Bookmark bookmark = bookmarks.get (i);
            lineNumbers [i] = bookmark.getLineNumber ();
        }
        try {
            URL url = fileObject.getURL ();
            Project project = FileOwnerQuery.getOwner (fileObject);
            URLToBookmarks urlToBookmarks = projectToBookmarks.get (project);
            if (urlToBookmarks == null) {
                urlToBookmarks = new URLToBookmarks ();
                projectToBookmarks.put (project, urlToBookmarks);
            }
            urlToBookmarks.put (url, lineNumbers);
        } catch (FileStateInvalidException e) {
            // Ignore this file - could be deleted etc.
        }
    }
    
    private static void saveBookmarks (Project project) {
        if (!ProjectManager.getDefault ().isValid (project)) {
            return; // cannot modify it now anyway
        }
        AuxiliaryConfiguration auxiliaryConfiguration = ProjectUtils.
            getAuxiliaryConfiguration (project);
        URI baseURI;
        try {
            baseURI = new URI (project.getProjectDirectory ().getURL ().toExternalForm ());
        } catch (FileStateInvalidException e) {
            // Use global urls in such case
            baseURI = null;
        } catch (URISyntaxException e) {
            // Use global urls in such case
            baseURI = null;
        }
        URLToBookmarks urlToBookmarks = projectToBookmarks.get (project);
        if (urlToBookmarks == null) return;
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            org.w3c.dom.Document document = builder.newDocument();
            Element bookmarksElement = document.createElementNS (
                EDITOR_BOOKMARKS_NAMESPACE_URI, 
                "editor-bookmarks"
            );
            for (URL url : urlToBookmarks.keySet ()) {
                if (urlToBookmarks.get(url).length == 0) {
                    continue;
                }
                
                Element fileElement = document.createElementNS (
                    EDITOR_BOOKMARKS_NAMESPACE_URI, 
                    "file"
                );
                Element urlElement = document.createElementNS (
                    EDITOR_BOOKMARKS_NAMESPACE_URI,
                    "url");
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
                urlElement.appendChild (document.createTextNode (url2));
                fileElement.appendChild (urlElement);
                int[] lineNumbers = urlToBookmarks.get (url);
                for (int lineNumber : lineNumbers) {
                    Element lineElem = document.createElementNS (
                        EDITOR_BOOKMARKS_NAMESPACE_URI, 
                        "line"
                    );
                    lineElem.appendChild (document.createTextNode (Integer.toString (lineNumber)));
                    fileElement.appendChild(lineElem);
                }
                bookmarksElement.appendChild(fileElement);
            } // for URL
            auxiliaryConfiguration.putConfigurationFragment (
                bookmarksElement, false
            );
        } catch (ParserConfigurationException e) {
            ErrorManager.getDefault().notify(e);
        }
    }
    
    
    // innerclasses ............................................................
    
    private static class URLToBookmarks extends HashMap<URL,int[]> {}
    
    private static class ProjectsListener implements PropertyChangeListener, Runnable {
        
        private static List<Project>    lastOpenProjects;
        private static RequestProcessor RP = new RequestProcessor("Bookmarks saver"); // NOI18N

        @SuppressWarnings("LeakingThisInConstructor")
        public ProjectsListener () {
            OpenProjects openProjects = OpenProjects.getDefault ();
            lastOpenProjects = new ArrayList (Arrays.asList (openProjects.getOpenProjects ()));
            openProjects.addPropertyChangeListener (this);
        }

        @Override
        public void run() {
            ProjectManager.mutex().writeAccess(new Runnable() {
                public void run() {
                    List<Project> openProjects = Arrays.asList (OpenProjects.getDefault ().getOpenProjects ());
                    // lastOpenProjects will contain the just closed projects
                    lastOpenProjects.removeAll (openProjects);
                    for (Iterator<Project> it = lastOpenProjects.iterator (); it.hasNext ();) {
                        saveBookmarks (it.next ());
                    }
                    lastOpenProjects = new ArrayList (openProjects);
                }
            });
        }

        @Override
        public void propertyChange (PropertyChangeEvent evt) {
            RP.post(this);
        }
        
        void destroy () {
            OpenProjects.getDefault ().removePropertyChangeListener (this);
        }
    }
}

