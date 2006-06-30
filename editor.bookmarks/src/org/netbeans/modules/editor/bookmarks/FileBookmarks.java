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

