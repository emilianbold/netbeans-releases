/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.editor.bookmarks;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Bookmarks for a project consist of bookmarks for all URLs (where the bookmarks exist)
 * within the project.
 * 
 * @author Miloslav Metelka
 */
public final class ProjectBookmarks {
    
    private final URI projectURI;

    private volatile int lastBookmarkId;

    private final Map<URL,FileBookmarks> url2FileBookmarks;
    
    private boolean released;

    private final Map<Object,Boolean> activeClients = new WeakHashMap<Object, Boolean>();
    
    public ProjectBookmarks(URI projectURI) {
        this(projectURI, 0);
    }
    
    public ProjectBookmarks(URI projectURI, int lastBookmarkId) {
        this.projectURI = projectURI;
        this.lastBookmarkId = lastBookmarkId;
        url2FileBookmarks = new HashMap<URL, FileBookmarks>();
    }

    public URI getProjectURI() {
        return projectURI;
    }
    
    public int getLastBookmarkId() {
        return lastBookmarkId;
    }

    public int generateBookmarkId() {
        return ++lastBookmarkId;
    }
    
    public void ensureBookmarkIdSkip(int bookmarkId) {
        lastBookmarkId = Math.max(lastBookmarkId, bookmarkId);
    }

    public FileBookmarks get(URL url) {
        return url2FileBookmarks.get(url);
    }
    
    public void remove(URL url) {
        url2FileBookmarks.remove(url);
    }
    
    public void add(FileBookmarks fileBookmarks) {
        url2FileBookmarks.put(fileBookmarks.getUrl(), fileBookmarks);
    }
    
    public Collection<URL> allURLs() {
        return url2FileBookmarks.keySet();
    }
    
    public boolean containsAnyBookmarks() {
        for (FileBookmarks fileBookmarks : url2FileBookmarks.values()) {
            if (fileBookmarks.containsAnyBookmarks()) {
                return true;
            }
        }
        return false;
    }

    public Collection<FileBookmarks> allFileBookmarks() {
        return url2FileBookmarks.values();
    }
    
    public List<BookmarkInfo> allBookmarks() {
       List<BookmarkInfo> allBookmarks = new ArrayList<BookmarkInfo>();
       for (FileBookmarks fileBookmarks : url2FileBookmarks.values()) {
           allBookmarks.addAll(fileBookmarks.getBookmarks());
       }
       return allBookmarks;
    }
    
    public void release() {
        released = true;
    }

    public boolean isReleased() {
        return released;
    }

    /**
     * Clients notify itself into this method so that the bookmarks are aware
     * of them and create a weak reference to them. Once all the clients get released
     * the projects bookmarks may be released as well.
     *
     * @param activeClient
     */
    public void activeClientNotify(Object activeClient) {
        activeClients.put(activeClient, Boolean.TRUE);
    }

    public boolean hasActiveClients() {
        return (activeClients.size() > 0);
    }

    @Override
    public String toString() {
        return "project=" + projectURI + ", lastBId=" + lastBookmarkId + ", removed=" + released; // NOI18N
    }

}
