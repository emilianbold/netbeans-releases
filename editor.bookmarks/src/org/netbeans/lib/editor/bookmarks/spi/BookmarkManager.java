/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.lib.editor.bookmarks.spi;

import javax.swing.text.JTextComponent;
import org.netbeans.lib.editor.bookmarks.api.Bookmark;

/**
 * Manager of the bookmarks for a document.
 * <br>
 * If the manager wishes to persist the bookmarks
 * its implementation must do so at the appropriate times.
 *
 * @author Miloslav Metelka
 */

public interface BookmarkManager {

    /**
     * Initialize the bookmarks.
     * <br>
     * If the manager persists the bookmarks it should restore
     * them now and use
     * {@link BookmarkManagerSupport#addBookmark(BookmarkImplementation)}
     * to add them to the bookmark list.
     */
    void init(BookmarkManagerSupport support);
    
    /**
     * Bookmark list calls this method once it's necessary
     * to create the bookmark implementation (for example when 
     * {@link BookmarkList#toggleBookmark(int)} gets called.
     *
     * @param offset offset at which the bookmark should be created.
     * @return non-null bookmark implementation for the given offset.
     */
    BookmarkImplementation createBookmarkImplementation(int offset);

    /**
     * This method is called by the infrastructure to save
     * the present bookmarks into a persistent storage.
     * <br>
     * This happens whenever a document gets uninstalled
     * from a text component or when a document's content
     * gets saved.
     */
    void saveBookmarks();

}

