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

import org.netbeans.lib.editor.bookmarks.BookmarksApiPackageAccessor;
import org.netbeans.lib.editor.bookmarks.BookmarksSpiPackageAccessor;
import org.netbeans.lib.editor.bookmarks.api.Bookmark;
import org.netbeans.lib.editor.bookmarks.api.BookmarkList;

/**
 * Supporting methods for a bookmark manager.
 *
 * @author Miloslav Metelka
 */

public final class BookmarkManagerSupport {
    
    private static boolean inited;
    
    public static void initPackageAccess() {
        if (!inited) {
            inited = true;
            BookmarksSpiPackageAccessor.register(new SpiAccessor());
        }
    }
    
    BookmarkList bookmarkList;
    
    /**
     * Construct manager support for the given bookmark list.
     * <br>
     * For SPI accessor only.
     */
    BookmarkManagerSupport(BookmarkList bookmarkList) {
        this.bookmarkList = bookmarkList;
    }
    
    public BookmarkList getBookmarkList() {
        return bookmarkList;
    }
    
    public Bookmark addBookmark(BookmarkImplementation impl) {
        return BookmarksApiPackageAccessor.get().addBookmark(bookmarkList, impl);
    }
    
    public BookmarkImplementation getBookmarkImplementation(Bookmark bookmark) {
        assert (bookmark.getList() == bookmarkList);
        return BookmarksApiPackageAccessor.get().getBookmarkImplementation(bookmark);
    }
    
    private static final class SpiAccessor extends BookmarksSpiPackageAccessor {
        
        public BookmarkManagerSupport createBookmarkManagerSupport(
        BookmarkList bookmarkList) {
            return new BookmarkManagerSupport(bookmarkList);
        }

    }
}

