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

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.text.Document;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.openide.filesystems.FileObject;

/**
 * Services to update or save bookmarks to persistent format.
 *
 * @author Miloslav Metelka
 */

public class BookmarkManager {
    
    private static final BookmarkManager INSTANCE = new BookmarkManager();
    
    public static BookmarkManager getLocked() {
        return INSTANCE.lock();
    }

    /**
     * Contains mapping of a project and its corresponding bookmarks (lazily read upon request).
     * <br/>
     * Once the bookmarks exist in the map they will be written to project's private.xml upon project close.
     */
    private final Map<Project,ProjectBookmarks> project2Bookmarks =
            new HashMap<Project,ProjectBookmarks> ();
    
    private List<BookmarkManagerListener> listenerList = new CopyOnWriteArrayList<BookmarkManagerListener>();
    
    private Thread locker;
    
    private int lockDepth;
    
    private List<BookmarkChange> transactionChanges;
    
    private BookmarkManager() {
    }
    
    synchronized BookmarkManager lock() {
        try {
            Thread currentThread = Thread.currentThread();
            while (locker != null) {
                if (currentThread == locker) {
                    lockDepth++;
                    return this;
                }
                wait();
            }
            locker = currentThread;
            lockDepth = 1;
            transactionChanges = new ArrayList<BookmarkChange>();
        } catch (InterruptedException e) {
            throw new Error("Interrupted lock attempt.");
        }
        return this;
    }
    
    public synchronized void unlock() {
        Thread currentThread = Thread.currentThread();
        if (currentThread != locker) {
            throw new IllegalStateException("currentThread=" + currentThread + " != locker=" + locker);
        }
        if (--lockDepth <= 0) {
            locker = null;
            lockDepth = 0;
            if (!transactionChanges.isEmpty()) {
                fireChange(transactionChanges);
            }
            transactionChanges = null;
            notifyAll();
        }
    }
    
    public List<ProjectBookmarks> allLoadedProjectBookmarks() {
        return new ArrayList<ProjectBookmarks>(project2Bookmarks.values());
    }
    
    /**
     * Load bookmarks for the given projects (if not loaded yet).
     */
    void ensureProjectBookmarksLoaded(List<Project> projects) {
        for (Project project : projects) {
            getProjectBookmarks(project, true, false); // Force project's bookmarks loading
        }
    }
    
    public BookmarkInfo findBookmarkByNameOrKey(String nameOrKey, boolean byKey) {
        for (ProjectBookmarks projectBookmarks : allLoadedProjectBookmarks()) {
            for (URL url : projectBookmarks.allURLs()) {
                FileBookmarks fileBookmarks = projectBookmarks.get(url);
                for (BookmarkInfo info : fileBookmarks.getBookmarks()) {
                    if (nameOrKey.equals(info.getName())) {
                        return info;
                    }
                }
            }
        }
        return null;
    }
    
    public void addBookmarkManagerListener(BookmarkManagerListener l) {
        listenerList.add(l);
    }
    
    public void removeBookmarkManagerListener(BookmarkManagerListener l) {
        listenerList.remove(l);
    }
    
    void fireChange(List<BookmarkChange> changes) {
        BookmarkManagerEvent evt = new BookmarkManagerEvent(this, changes);
        for (BookmarkManagerListener l : listenerList) {
            l.bookmarksChanged(evt);
        }
    }

    /**
     * Get all fileobjects that contain bookmarks located in the given project.
     * 
     * @param prj 
     */
    public FileObject[] getSortedFileObjects(ProjectBookmarks projectBookmarks) {
        List<FileObject> foList;
        if (projectBookmarks != null) {
            Collection<FileBookmarks> allFileBookmarks = projectBookmarks.allFileBookmarks();
            foList = new ArrayList<FileObject>(allFileBookmarks.size());
            for (FileBookmarks fileBookmarks : allFileBookmarks) {
                FileObject fo = fileBookmarks.getFileObject();
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
    public FileBookmarks getFileBookmarks(Document document) {
        FileBookmarks ret = null;
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

    public ProjectBookmarks getProjectBookmarks(Document document) {
        ProjectBookmarks ret = null;
        FileObject fo = NbEditorUtilities.getFileObject (document);
        if (fo != null) {
            Project project = FileOwnerQuery.getOwner(fo);
            if (project != null) {
                ret = getProjectBookmarks(project, true, true);
            }
        }
        return ret;
    }
    
    public ProjectBookmarks getProjectBookmarks(Project project, boolean load, boolean forceCreation) {
        ProjectBookmarks projectBookmarks;
        projectBookmarks = project2Bookmarks.get(project);
        if (projectBookmarks == null) {
            if (load) {
                projectBookmarks = BookmarksPersistence.get().loadProjectBookmarks(project);
                if (projectBookmarks != null) {
                    BookmarkChange change = new BookmarkChange(projectBookmarks.getProject(), null);
                    change.markAdded();
                }
            }
            if (projectBookmarks == null && forceCreation) {
                projectBookmarks = new ProjectBookmarks(project);
            }
            if (projectBookmarks != null) {
                project2Bookmarks.put(project, projectBookmarks);
                BookmarkChange change = new BookmarkChange(project);
                change.markAdded();
                transactionChanges.add(change);
            }
        }
        return projectBookmarks;
    }
    
    public void removeProjectBookmarks(ProjectBookmarks projectBookmarks) {
        project2Bookmarks.remove(projectBookmarks.getProject());
        projectBookmarks.markRemoved();
        BookmarkChange change = new BookmarkChange(projectBookmarks.getProject());
        change.markRemoved();
        transactionChanges.add(change);
    }
    
    public void addBookmarkNotify(BookmarkInfo bookmark) {
        BookmarkChange change = new BookmarkChange(bookmark);
        change.markAdded();
        transactionChanges.add(change);
    }
    
    public void removeBookmarks(List<BookmarkInfo> bookmarks) {
        for (BookmarkInfo bookmark : bookmarks) {
            BookmarkChange change = new BookmarkChange(bookmark);
            FileBookmarks fileBookmarks = bookmark.getFileBookmarks();
            if (fileBookmarks.remove(bookmark)) {
                change.markRemoved();
                transactionChanges.add(change);
            }
        }
    }
    
    public void updateLineIndex(BookmarkInfo bookmark, int lineIndex) {
        if (bookmark.getLineIndex() != lineIndex) {
            BookmarkChange change = new BookmarkChange(bookmark);
            bookmark.setLineIndex(lineIndex); // Also calls setCurrentLineIndex()
            change.markLineIndexChanged();
            transactionChanges.add(change);
        }
    }
    
    public void updateNameOrKey(BookmarkInfo bookmark, boolean nameChanged, boolean keyChanged) {
        BookmarkChange change = new BookmarkChange(bookmark);
        if (nameChanged) {
            change.markNameChanged();
        }
        if (keyChanged) {
            change.markKeyChanged();
        }
        transactionChanges.add(change);
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(200);
        for (ProjectBookmarks projectBookmarks : allLoadedProjectBookmarks()) {
            sb.append("Project ").append(projectBookmarks.getProject()).append('\n');
            for (URL url : projectBookmarks.allURLs()) {
                sb.append("    ").append(url).append("\n");
                for (BookmarkInfo info : projectBookmarks.get(url).getBookmarks()) {
                    sb.append("        ").append(info).append('\n');
                }
            }
        }
        return sb.toString();
    }

}
