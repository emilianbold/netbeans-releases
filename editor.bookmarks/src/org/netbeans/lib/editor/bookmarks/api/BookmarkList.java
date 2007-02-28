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

package org.netbeans.lib.editor.bookmarks.api;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;
import javax.swing.text.Document;
import javax.swing.text.Element;
import org.netbeans.lib.editor.bookmarks.BookmarksApiPackageAccessor;
import org.netbeans.lib.editor.bookmarks.BookmarksSpiPackageAccessor;
import org.netbeans.lib.editor.bookmarks.spi.BookmarkImplementation;
import org.netbeans.lib.editor.bookmarks.spi.BookmarkManager;
import org.netbeans.lib.editor.bookmarks.spi.BookmarkManagerFactory;
import org.netbeans.lib.editor.bookmarks.spi.BookmarkManagerSupport;
import org.openide.util.Lookup;

/**
 * Services around document bookmarks.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public final class BookmarkList {
    
    private static BookmarkManagerFactory bookmarkManagerFactory;
    
    static {
        BookmarksApiPackageAccessor.register(new ApiAccessor());
        BookmarkManagerSupport.initPackageAccess();
    }
    
    public static BookmarkList get(Document doc) {
        synchronized (org.netbeans.modules.editor.bookmarks.PersistentBookmarks.class) {
            BookmarkList bookmarkList = (BookmarkList)doc.getProperty(BookmarkList.class);
            if (bookmarkList == null) {
                BookmarkManager mgr = getBookmarkManagerFactory().createBookmarkManager(doc);
                bookmarkList = new BookmarkList(doc, mgr);
                doc.putProperty(BookmarkList.class, bookmarkList);
            }
            return bookmarkList;
        }
    }
    
    private static BookmarkManagerFactory getBookmarkManagerFactory() {
        if (bookmarkManagerFactory == null) {
            bookmarkManagerFactory = (BookmarkManagerFactory)
                Lookup.getDefault().lookup(BookmarkManagerFactory.class);
            assert (bookmarkManagerFactory != null) : "No BookmarkManagerFactory available"; // NOI18N
        }
        return bookmarkManagerFactory;
    }

    private static final String PROP_BOOKMARKS = "bookmarks";
    
    /**
     * Document for which the bookmark list was created.
     */
    private Document doc;
    
    /**
     * Manager of the bookmarks.
     */
    private BookmarkManager manager;
    
    /**
     * Support for the manager.
     */
    private BookmarkManagerSupport managerSupport;
    
    /**
     * List of bookmark instances.
     */
    private List bookmarks;

    private final PropertyChangeSupport PCS = new PropertyChangeSupport(this);
    
    private BookmarkList(Document doc, BookmarkManager manager) {
        if (doc == null) {
            throw new NullPointerException("Document cannot be null"); // NOI18N
        }
        this.doc = doc;
        this.bookmarks = new ArrayList();
        this.manager = manager;
        this.managerSupport = BookmarksSpiPackageAccessor.get().createBookmarkManagerSupport(this);
        this.manager.init(managerSupport);
    }

    /**
     * Get document on which this bookmark list operates.
     *
     * @return non-null document.
     */
    public Document getDocument() {
        return doc;
    }
    
    /**
     * Total count of bookmarks managed by this bookmark list.
     *
     * @return &gt;=0 total count of bookmarks.
     */
    public int getBookmarkCount() {
        return bookmarks.size();
    }
    
    /**
     * Get bookmark at the specified index.
     * <br>
     * The bookmarks are ordered by increasing offset.
     *
     * @param index index of the bookmark in the list of bookmarks.
     * @return non-null bookmark instance.
     */
    public Bookmark getBookmark(int index) {
        return (Bookmark)bookmarks.get(index);
    }

    /**
     * Get the first bookmark
     * that has the offset greater than the specified offset.
     *
     * @param offset &gt;=-1 offset for searching of the next bookmark.
     *  The offset -1 searches for the first bookmark.
     * @param wrapSearch if true then continue searching from the begining of document
     *  in case a bookmark was not found.
     * @return valid bookmark or null if there is no bookmark satisfying the condition.
     */
    public Bookmark getNextBookmark(int offset, boolean wrapSearch) {
        offset++;
        checkOffsetNonNegative(offset);
        int index = getBookmarkIndex(offset);
        return (index < getBookmarkCount())
            ? getBookmark(index)
            : wrapSearch ? getNextBookmark(-1, false) : null;
    }
    
    /**
     * Get the first bookmark in backward direction
     * that has the line index lower than the specified line index.
     *
     * @param offset &gt;=0 offset for searching of the previous bookmark.
     *  The offset <code>Integer.MAX_VALUE</code> searches for the last bookmark.
     * @param wrapSearch if true then continue searching from the end of document
     *  in case a bookmark was not found.
     * @return valid bookmark or null if there is no bookmark satisfying the condition.
     */
    public Bookmark getPreviousBookmark(int offset, boolean wrapSearch) {
        checkOffsetNonNegative(offset);
        int bookmarkCount = getBookmarkCount();
        Bookmark bookmark; // result
        if (bookmarkCount > 0) {
            offset--; // search from previous offset
            int index = getBookmarkIndex(offset);
            if (index == bookmarkCount || (bookmark = getBookmark(index)).getOffset() != offset) {
                index--; // go below
                if (index >= 0) {
                    bookmark = getBookmark(index);
                } else { // prior first bookmark
                    if (wrapSearch) {
                        bookmark = getPreviousBookmark(Integer.MAX_VALUE, false);
                    } else { // no previous bookmark
                        bookmark = null;
                    }
                }
            } // else -> bookmark right at offset is assigned
        } else { // no bookmarks available
             bookmark = null;
        }
        return bookmark;
    }

    /**
     * Get index of first bookmark that has the line index greater or equal
     * to the requested offset.
     * <br>
     * Return <code>getBookmarkCount()</code> in case there is no such mark.
     * <br>
     * The algorithm uses binary search.
     *
     * @param offset offset by which the bookmarks will be searched.
     * @return &gt;=0 and &lt;={@link #getBookmarkCount()} index of the first bookmark
     *  with the offset greater or equal to the requested one.
     */
    public int getBookmarkIndex(int offset) {
        // Find next bookmark by binary search
        int low = 0;
        int high = getBookmarkCount() - 1;
        
        while (low <= high) {
            int mid = (low + high) / 2;
            int midOffset = getBookmark(mid).getOffset();
            
            if (midOffset < offset) {
                low = mid + 1;
            } else if (midOffset > offset) {
                high = mid - 1;
            } else { // bookmark right at the offset
                // Goto first bookmark of possible ones at the same line
                mid--;
                while (mid >= 0) {
                    if (getBookmark(mid).getOffset() != offset) {
                        break;
                    }
                    mid--;
                }
                mid++;
                return mid;
            }
        }
        
        return low;
    }
    
    /**
     * Create bookmark if it did not exist before at the line containing
     * the given offset.
     * <br>
     * Drop the existing bookmark if it was already present for the line
     * containing the given offset.
     *
     * @param offset offset on a line in the document for which the presence of bookmark
     *  should be checked. The bookmarks are checked in a line-wise way.
     * @return bookmark that was either created or removed by the operation.
     *  Calling {@link Bookmark#isValid()} determines whether the returned
     *  bookmark was added or removed by the operation.
     *  <br>
     *  <code>null</code> is returned if the offset is above the end of document.
     */
    public Bookmark toggleLineBookmark(int offset) {
        checkOffsetInDocument(offset);
        Element lineRoot = doc.getDefaultRootElement();
        int lineIndex = lineRoot.getElementIndex(offset);
        Bookmark bookmark = null;
        if (lineIndex < lineRoot.getElementCount()) {
            Element lineElem = lineRoot.getElement(lineIndex);
            int lineStartOffset = lineElem.getStartOffset();
            int index = getBookmarkIndex(lineStartOffset);
            if (index < getBookmarkCount() // valid bookmark
                && getBookmark(index).getOffset() < lineElem.getEndOffset() // inside line
            ) { // remove the existing bookmark
                bookmark = removeBookmarkAtIndex(index);
            } else { // add bookmark
                bookmark = addBookmark(manager.createBookmarkImplementation(lineStartOffset));
            }
            // Save the bookmarks
            manager.saveBookmarks();
        }
        return bookmark;
    }
    
    /**
     * Remove bookmark at the given index among the bookmarks.
     *
     * @param index index at which the bookmark should be removed.
     * @return removed (and invalidated) bookmark
     */
    public Bookmark removeBookmarkAtIndex(int index) {
        Bookmark bookmark = (Bookmark)bookmarks.remove(index);
        bookmark.release();
        PCS.firePropertyChange(PROP_BOOKMARKS, null, null);
        return bookmark;
    }
    
    /** Removes all bookmarks */
    public void removeAllBookmarks(){
        if (!bookmarks.isEmpty()) {
            for (int i = 0; i<bookmarks.size(); i++){
                Bookmark bookmark = (Bookmark)bookmarks.get(i);
                bookmark.release();
            }
            bookmarks.clear();
            PCS.firePropertyChange(PROP_BOOKMARKS, null, null);
        }
    }
    
    /**
     * Get manager of this bookmark list. Used by SPI accessor.
     */
    BookmarkManager getManager() {
        return manager;
    }
    
    /**
     * Add bookmark to this list.
     * <br>
     * Intended for SPI accessor only.
     */
    Bookmark addBookmark(BookmarkImplementation impl) {
        // Compute the index from increased offset to ensure to add the bookmark
        // after all the possible bookmarks with the same offset
        Bookmark bookmark = new Bookmark(this, impl);
        int index = getBookmarkIndex(impl.getOffset() + 1);
        bookmarks.add(index, bookmark);
        PCS.firePropertyChange(PROP_BOOKMARKS, null, null);
        return bookmark;
    }

    private void checkOffsetNonNegative(int offset) {
        if (offset < 0) {
            throw new IndexOutOfBoundsException("offset=" + offset + " < 0"); // NOI18N
        }
    }
    
    private void checkOffsetInDocument(int offset) {
        checkOffsetNonNegative(offset);
        int docLen = doc.getLength();
        if (offset > docLen) {
            throw new IndexOutOfBoundsException("offset=" + offset // NOI18N
                + " > doc.getLength()=" + docLen); // NOI18N
        }
    }
    
    public String toString() {
        return "Bookmarks: " + bookmarks; // NOI18N
    }

    void addPropertyChangeListener(PropertyChangeListener l) {
        PCS.addPropertyChangeListener(l);
    }
    
    void removePropertyChangeListener(PropertyChangeListener l) {
        PCS.removePropertyChangeListener(l);
    }
    
    /**
     * Implementation of the class accessing package-private methods
     * in the bookmarks API.
     */
    private static final class ApiAccessor extends BookmarksApiPackageAccessor {
        
        public BookmarkManager getBookmarkManager(BookmarkList bookmarkList) {
            return bookmarkList.getManager();
        }
        
        public BookmarkImplementation getBookmarkImplementation(Bookmark bookmark) {
            return bookmark.getImplementation();
        }

        public Bookmark addBookmark(BookmarkList list, BookmarkImplementation impl) {
            return list.addBookmark(impl);
        }
        
        public void addBookmarkListPcl(BookmarkList list, PropertyChangeListener l) {
            list.addPropertyChangeListener(l);
        }

        public void removeBookmarkListPcl(BookmarkList list, PropertyChangeListener l) {
            list.removePropertyChangeListener(l);
        }
    } // End of ApiAccessor class
}

