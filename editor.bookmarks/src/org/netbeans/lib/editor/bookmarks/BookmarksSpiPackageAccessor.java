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

import org.netbeans.lib.editor.bookmarks.api.BookmarkList;
import org.netbeans.lib.editor.bookmarks.spi.BookmarkManagerSupport;


/**
 * Accessor for the package-private functionality of bookmarks API.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public abstract class BookmarksSpiPackageAccessor {
    
    private static BookmarksSpiPackageAccessor INSTANCE;
    
    public static BookmarksSpiPackageAccessor get() {
        return INSTANCE;
    }

    /**
     * Register the accessor. The method can only be called once
     * - othewise it throws IllegalStateException.
     * 
     * @param accessor instance.
     */
    public static void register(BookmarksSpiPackageAccessor accessor) {
        if (INSTANCE != null) {
            throw new IllegalStateException("Already registered"); // NOI18N
        }
        INSTANCE = accessor;
    }
    
    /**
     * Create bookmark manager support for the given bookmark list.
     */
    public abstract BookmarkManagerSupport createBookmarkManagerSupport(BookmarkList bookmarkList);

}
