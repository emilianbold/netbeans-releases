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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Map;
import java.util.WeakHashMap;
import javax.swing.text.Document;
import org.netbeans.lib.editor.bookmarks.BookmarksApiPackageAccessor;
import org.netbeans.lib.editor.bookmarks.api.BookmarkList;
import org.netbeans.lib.editor.bookmarks.spi.BookmarkManager;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;

/**
 * Listening on when the document becomes unmodified
 * and notification to bookmark manager.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

class DocumentUnmodifiedListener implements PropertyChangeListener {
    
    private static final Map eco2listener = new WeakHashMap();
    
    private static final DocumentUnmodifiedListener INSTANCE = new DocumentUnmodifiedListener();
    
    /**
     * Initialize listening for document unmodification
     * for the given document.
     */
    public static void init(Document doc) {
        FileObject fo = NbEditorUtilities.getFileObject(doc);
        if (fo != null) {
            EditorCookie.Observable eco;
            try {
                DataObject dob = DataObject.find(fo);
                eco = (EditorCookie.Observable)dob.getCookie(EditorCookie.Observable.class);
            } catch (DataObjectNotFoundException e) {
                eco = null;
            }
            if (eco != null && eco2listener.get(eco) == null) { // not listening yet
                eco.addPropertyChangeListener(INSTANCE);
                eco2listener.put(eco, INSTANCE); // adding to weak hash map
            }
        }
    }
            
    private DocumentUnmodifiedListener() {
        // just a private singleton instance allowed
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        if ("modified".equals(evt.getPropertyName())) { // become unmodified
            if (!(Boolean.TRUE.equals(evt.getNewValue()))) {
                EditorCookie.Observable eco = (EditorCookie.Observable)evt.getSource();
                Document doc = eco.getDocument();
                if (doc != null) {
                    // Document is being saved
                    BookmarkList bookmarkList = BookmarkList.get(doc);
                    BookmarkManager manager = BookmarksApiPackageAccessor.get().getBookmarkManager(bookmarkList);
                    manager.saveBookmarks();
                }
            }
        }
    }
    
}

