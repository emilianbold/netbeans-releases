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

package org.netbeans.modules.editor.bookmarks;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import javax.swing.text.Document;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.lib.editor.bookmarks.api.Bookmark;
import org.netbeans.lib.editor.bookmarks.api.BookmarkList;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.w3c.dom.Element;

/**
 * Services to update or save bookmarks to persistent format.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public class PersistentBookmarks {
    
    private static final int[] EMPTY_INT_ARRAY = new int[0];
    
    private static final Map project2bookmarksMap = new WeakHashMap();
    
    static synchronized void loadBookmarks(NbBookmarkManager manager) {
        Document doc = manager.getDocument();
        FileObject fo = NbEditorUtilities.getFileObject(doc);
        if (fo != null) {
            // Add the bookmarks if any
            FileBookmarksMap map = findOwningBookmarksMap(fo);
            if (map != null) {
                URL url;
                try {
                    url = fo.getURL();
                } catch (FileStateInvalidException e) {
                    // Ignore this file (could be deleted etc.)
                    url = null;
                }

                if (url != null) {
                    FileBookmarks fileBookmarks = map.get(url);
                    if (fileBookmarks != null) {
                        int bookmarkCount = fileBookmarks.getBookmarkCount();
                        for (int i = 0; i < bookmarkCount; i++) {
                            int lineIndex = fileBookmarks.getBookmarkLineIndex(i);
                            manager.addLoadedBookmark(lineIndex);
                        }
                    }
                }
            }
        }
    }
        
    /**
     * Save the bookmarks for the given FileObject.
     * <br>
     * This is usually done upon bookmark toggling or when a file is being closed.
     *
     * <p>
     * This method may also save bookmarks for other files for the same project
     * as the bookmarks for all sources in the project are serialized into
     * project's private.xml file.
     */
    static synchronized void saveBookmarks(NbBookmarkManager manager) {
        Document doc = manager.getDocument();
        FileObject fo = NbEditorUtilities.getFileObject(doc);
        if (fo != null) {
            int[] bookmarkLineIndexes = getLineIndexes(doc);
            URL url;
            try {
                url = fo.getURL();
            } catch (FileStateInvalidException e) {
                // Ignore this file - could be deleted etc.
                url = null;
            }

            if (url != null) {
                FileBookmarks bookmarks = new FileBookmarks(
                        url, bookmarkLineIndexes);
                Project owner = FileOwnerQuery.getOwner(fo);
                FileBookmarksMap fileBookmarksMap = findBookmarksMap(owner);
                if (fileBookmarksMap != null) {
                    fileBookmarksMap.put(bookmarks);
                }
            }
        }
    }
    
    static synchronized void saveAllProjectBookmarks() {
        List allPrjs = new ArrayList(project2bookmarksMap.keySet());
        for (Iterator it = allPrjs.iterator(); it.hasNext();) {
            saveProjectBookmarks((Project)it.next());
        }
    }
    
    static void saveProjectBookmarks(Project prj) {
        if (!ProjectManager.getDefault().isValid(prj)) {
            return; // cannot modify it now anyway
        }
        FileBookmarksMap fileBookmarksMap = findBookmarksMap(prj);
        if (fileBookmarksMap != null && fileBookmarksMap.isModified()) {
            saveBookmarksMap(prj, fileBookmarksMap);
        }
    }
    
    private static int[] getLineIndexes(Document doc) {
        BookmarkList bookmarkList = BookmarkList.get(doc);
        int bookmarkCount = bookmarkList.getBookmarkCount();
        int[] lineIndexesArray = new int[bookmarkCount];
        for (int i = 0; i < bookmarkCount; i++) {
            Bookmark bookmark = bookmarkList.getBookmark(i);
            lineIndexesArray[i] = bookmark.getLineIndex();
        }
        return lineIndexesArray;
    }

    private static FileBookmarksMap findOwningBookmarksMap(FileObject fo) {
        return findBookmarksMap(FileOwnerQuery.getOwner(fo));
    }

    private static FileBookmarksMap findBookmarksMap(Project project) {
        FileBookmarksMap map = null;
        if (project != null) {
            map = (FileBookmarksMap)project2bookmarksMap.get(project);
            if (map == null) {
                map = loadBookmarksMap(project);
                if (map != null) {
                    project2bookmarksMap.put(project, map);
                }
            }
        }
        return map;
    }
    
    /**
     * Get a map of urls to line-numbers for the given project.
     */
    private static FileBookmarksMap loadBookmarksMap(Project project) {
        AuxiliaryConfiguration ac = ProjectUtils.getAuxiliaryConfiguration(project);
        FileBookmarksMap projectBookmarksMap = new FileBookmarksMap();
        Element bookmarksElem = ac.getConfigurationFragment(
                BookmarksXMLHandler.EDITOR_BOOKMARKS_ELEM,
                BookmarksXMLHandler.EDITOR_BOOKMARKS_NAMESPACE_URI,
                false
        );
        URL projectFolderURL;
        try {
            projectFolderURL = project.getProjectDirectory().getURL();
        } catch (FileStateInvalidException e) {
            // Use global naming in such case
            projectFolderURL = null;
        }
        // If bookmarks element exists load the data from it
        if (bookmarksElem != null) {
            BookmarksXMLHandler.loadFileBookmarksMap(
                    projectBookmarksMap, bookmarksElem,
                    projectFolderURL
            );
        }
        return projectBookmarksMap;
    }

    /**
     * Save the map of bookmarks into project's private xml file.
     */
    private static void saveBookmarksMap(Project project,
    FileBookmarksMap fileBookmarksMap) {
        AuxiliaryConfiguration ac = (AuxiliaryConfiguration)project.getLookup().lookup(
                AuxiliaryConfiguration.class);
        if (ac != null) {
            URI baseURI;
            try {
                baseURI = new URI(project.getProjectDirectory().getURL().toExternalForm());
            } catch (FileStateInvalidException e) {
                // Use global urls in such case
                baseURI = null;
            } catch (URISyntaxException e) {
                // Use global urls in such case
                baseURI = null;
            }
            Element bookmarksElem = BookmarksXMLHandler.saveFileBookmarksMap(
                    fileBookmarksMap, baseURI);
            ac.putConfigurationFragment(
                    bookmarksElem, false);
        }
    }

}

