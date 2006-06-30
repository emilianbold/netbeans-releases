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

