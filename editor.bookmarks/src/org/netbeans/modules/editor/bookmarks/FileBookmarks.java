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

package org.netbeans.modules.editor.bookmarks;

import java.net.URL;

/**
 * Information about a persisted bookmark.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public class FileBookmarks {
    
    private URL url;
    
    private int[] bookmarkLineIndexes;
    
    FileBookmarks(URL url, int[] lineIndexes) {
        this.url = url;
        this.bookmarkLineIndexes = lineIndexes;
    }

    public final URL getURL() {
        return url;
    }
    
    public final int getBookmarkCount() {
        return bookmarkLineIndexes.length;
    }
    
    public final int getBookmarkLineIndex(int bookmarkIndex) {
        return bookmarkLineIndexes[bookmarkIndex];
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("url="); // NOI18N
        sb.append(url);
        sb.append(" { "); // NOI18N
        for (int i = 0; i < bookmarkLineIndexes.length; i++) {
            sb.append(bookmarkLineIndexes[i]);
        }
        sb.append("}"); // NOI18N
        return sb.toString();
    }

}

