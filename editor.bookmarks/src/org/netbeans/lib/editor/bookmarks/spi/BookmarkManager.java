/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
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

