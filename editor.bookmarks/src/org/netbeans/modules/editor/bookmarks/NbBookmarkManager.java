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

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.Document;
import javax.swing.text.Element;
import org.netbeans.lib.editor.bookmarks.api.Bookmark;
import org.netbeans.lib.editor.bookmarks.api.BookmarkList;
import org.netbeans.lib.editor.bookmarks.spi.BookmarkImplementation;
import org.netbeans.lib.editor.bookmarks.spi.BookmarkManager;
import org.netbeans.lib.editor.bookmarks.spi.BookmarkManagerFactory;
import org.netbeans.lib.editor.bookmarks.spi.BookmarkManagerSupport;
import org.openide.util.WeakListeners;


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

    private transient ChangeListener bookmarksModuleListener = new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
            BookmarkList bl = support.getBookmarkList();
            bl.getDocument().putProperty(BookmarkList.class, null);
            bl.removeAllBookmarks();
        }
    };
    
    public void init(BookmarkManagerSupport support) {
        this.support = support;
        EditorBookmarksModule.getListenerSupport().addChangeListener(WeakListeners.change(bookmarksModuleListener, EditorBookmarksModule.getListenerSupport()));
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

