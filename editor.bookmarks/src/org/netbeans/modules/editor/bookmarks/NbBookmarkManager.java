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

package org.netbeans.modules.editor.bookmarks;

import javax.swing.text.Document;
import javax.swing.text.Element;
import org.netbeans.lib.editor.bookmarks.spi.BookmarkImplementation;
import org.netbeans.lib.editor.bookmarks.spi.BookmarkManager;
import org.netbeans.lib.editor.bookmarks.spi.BookmarkManagerFactory;
import org.netbeans.lib.editor.bookmarks.spi.BookmarkManagerSupport;


/**
 * Interface to a bookmark.
 *
 * @author Miloslav Metelka
 */

public final class NbBookmarkManager implements BookmarkManager {

    private static final String BOOKMARK_ANNOTATION_TYPE = "editor-bookmark"; // NOI18N

    private BookmarkManagerSupport support;
    
    public NbBookmarkManager() {
    }

    public void init(BookmarkManagerSupport support) {
        this.support = support;
        
        PersistentBookmarks.loadBookmarks(this);
    }
    
    void addLoadedBookmark(int lineIndex) {
        // First obtain offset for the line number
        Document doc = getDocument();
        Element lineRoot = doc.getDefaultRootElement();
        int lineCount = lineRoot.getElementCount();
        if (lineIndex < lineCount) {
            Element lineElem = lineRoot.getElement(lineIndex);
            int offset = lineElem.getStartOffset();
            support.addBookmark(createBookmarkImplementation(offset));
        } // bookmarks past end of document are not restored
    }
    
    public Document getDocument() {
        return support.getBookmarkList().getDocument();
    }
    
    public BookmarkImplementation createBookmarkImplementation(int offset) {
        return new NbBookmarkImplementation(this, offset);
    }

    public void saveBookmarks() {
        PersistentBookmarks.saveBookmarks(this);
    }
    
    public static final class Factory implements BookmarkManagerFactory {
        
        public BookmarkManager createBookmarkManager(Document doc) {
            DocumentUnmodifiedListener.init(doc); // start listening on notifyUnmodified()
            return new NbBookmarkManager();
        }
    }
    
}

