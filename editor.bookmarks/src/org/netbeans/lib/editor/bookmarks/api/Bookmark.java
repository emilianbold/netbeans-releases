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

package org.netbeans.lib.editor.bookmarks.api;

import javax.swing.text.Document;
import org.netbeans.lib.editor.bookmarks.spi.BookmarkImplementation;

/**
 * Interface to a bookmark.
 *
 * @author Miloslav Metelka
 */

public final class Bookmark {
    
    /**
     * Bookmark list to which this bookmark belongs.
     */
    private BookmarkList bookmarkList;
    
    /**
     * Implementation to which this bookmark delegates.
     */
    private BookmarkImplementation impl;
    
    /**
     * Whether this mark was released or not.
     */
    private boolean released;
    
    /**
     * Construct new instance of bookmark.
     *
     * <p>
     * The constructor is not public intentionally.
     * Please see <code>BookmarksApiPackageAccessor</code> for details.
     */
    Bookmark(BookmarkList bookmarkList, BookmarkImplementation impl) {
        this.bookmarkList = bookmarkList;
        this.impl = impl;
    }

    /**
     * Get offset of this bookmark.
     * <br>
     * Offsets behave like {@link javax.swing.text.Position}s (they track
     * inserts/removals).
     */
    public int getOffset() {
        return impl.getOffset();
    }

    /**
     * Get the index of the line at which this bookmark resides.
     */
    public int getLineIndex() {
        return getDocument().getDefaultRootElement().getElementIndex(impl.getOffset());
    }
    
    /**
     * Get the bookmark list for which this bookmark was created.
     */
    public BookmarkList getList() {
        return bookmarkList;
    }
    
    /**
     * Return true if this mark was released (removed from its bookmark list)
     * and is no longer actively used.
     */
    public boolean isReleased() {
        return released;
    }
    
    /**
     * Mark the current bookmark as invalid.
     */
    void release() {
        assert (!released);
        released = true;
        impl.release();
    }

    /**
     * Get bookmark's implementation. For API accessor only.
     */
    BookmarkImplementation getImplementation() {
        return impl;
    }

    /**
     * Get document to which this bookmark belongs.
     */
    private Document getDocument() {
        return bookmarkList.getDocument();
    }
    
}

