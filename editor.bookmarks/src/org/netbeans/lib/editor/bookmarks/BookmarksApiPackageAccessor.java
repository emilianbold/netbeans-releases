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

package org.netbeans.lib.editor.bookmarks;

import java.beans.PropertyChangeListener;
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
    
    public abstract void addBookmarkListPcl(BookmarkList list, PropertyChangeListener l);

    public abstract void removeBookmarkListPcl(BookmarkList list, PropertyChangeListener l);
}
