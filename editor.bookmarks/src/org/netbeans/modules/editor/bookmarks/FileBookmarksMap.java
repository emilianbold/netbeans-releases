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
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Mapping of file's url to its bookmarks.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

class FileBookmarksMap {

    private Map/*<URL, FileBookmarks>*/ url2FileBookmarks = new HashMap();

    private boolean modified;

    FileBookmarksMap() {
    }

    public FileBookmarks get(URL url) {
        return (FileBookmarks)url2FileBookmarks.get(url);
    }

    public void put(FileBookmarks fileBookmarks) {
        url2FileBookmarks.put(fileBookmarks.getURL(), fileBookmarks);
        markModified();
    }
    
    public Collection all() {
        return url2FileBookmarks.values();
    }
    
    public boolean isModified() {
        return modified;
    }

    private void markModified() {
        modified = true;
    }

    public void markUnmodified() {
        modified = false;
    }

}

