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

