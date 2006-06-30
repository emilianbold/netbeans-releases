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

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.Document;
import javax.swing.text.Element;
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
        EditorBookmarksModule.addChangeListener(WeakListeners.change(bookmarksModuleListener, EditorBookmarksModule.class));
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
