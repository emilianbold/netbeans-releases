/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.lib.editor.bookmarks;

import javax.swing.event.DocumentEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.lib.editor.bookmarks.api.Bookmark;
import org.netbeans.lib.editor.bookmarks.api.BookmarkList;
import org.netbeans.lib.editor.bookmarks.spi.BookmarkImplementation;
import org.netbeans.lib.editor.bookmarks.spi.BookmarkManager;

/**
 * Accessor for the package-private functionality of bookmarks API.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public abstract class BookmarksApiPackageAccessor {
    
    private static BookmarksApiPackageAccessor INSTANCE;
    
    public static BookmarksApiPackageAccessor get() {
        return INSTANCE;
    }

    /**
     * Register the accessor. The method can only be called once
     * - othewise it throws IllegalStateException.
     * 
     * @param accessor instance.
     */
    public static void register(BookmarksApiPackageAccessor accessor) {
        if (INSTANCE != null) {
            throw new IllegalStateException("Already registered"); // NOI18N
        }
        INSTANCE = accessor;
    }
    
    public abstract BookmarkManager getBookmarkManager(BookmarkList bookmarkList);
    
    public abstract BookmarkImplementation getBookmarkImplementation(Bookmark bookmark);
    
    public abstract Bookmark addBookmark(BookmarkList list, BookmarkImplementation impl);

}
