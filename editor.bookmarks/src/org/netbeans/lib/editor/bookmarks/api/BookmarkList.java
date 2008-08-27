/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.lib.editor.bookmarks.api;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import javax.swing.SwingUtilities;
import javax.swing.text.Document;
import javax.swing.text.Element;

import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.editor.bookmarks.BookmarksPersistence;
import org.openide.cookies.EditorCookie.Observable;
import org.openide.loaders.DataObject;
import org.openide.util.WeakSet;


/**
 * Services around document bookmarks.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public final class BookmarkList {
    
    public static BookmarkList get (Document doc) {
        BookmarkList bookmarkList = (BookmarkList) doc.getProperty (BookmarkList.class);
        if (bookmarkList == null) {
            bookmarkList = new BookmarkList (doc);
            doc.putProperty (BookmarkList.class, bookmarkList);
        }
        return bookmarkList;
    }

    private static final String
                            PROP_BOOKMARKS = "bookmarks";

    private static Set<Observable>
                            observedObservables = new WeakSet<Observable> ();
    /**
     * Document for which the bookmark list was created.
     */
    private Document        document;
    
    /**
     * List of bookmark instances.
     */
    private List            bookmarks;

    private final PropertyChangeSupport 
                            propertyChangeSupport = new PropertyChangeSupport (this);
    
    private BookmarkList (Document document) {
        if (document == null) {
            throw new NullPointerException ("Document cannot be null"); // NOI18N
        }
        this.document = document;
        this.bookmarks = new ArrayList ();
        BookmarksPersistence.loadBookmarks (this);
        
        DataObject dataObject = NbEditorUtilities.getDataObject (document);
        if (dataObject != null) {
            Observable observable = dataObject.getCookie (Observable.class);
            if (observable != null) {
                if (!observedObservables.contains (observable)) {
                    observable.addPropertyChangeListener (documentModifiedListener);
                    observedObservables.add (observable);
                }
            }
        }
    }

    /**
     * Get document on which this bookmark list operates.
     *
     * @return non-null document.
     */
    public Document getDocument () {
        return document;
    }
    
    /**
     * Returns list of all bookmarks.
     * 
     * @return list of all bookmarks
     */
    public synchronized  List<Bookmark> getBookmarks () {
        return Collections.<Bookmark> unmodifiableList (bookmarks);
    }
    
//    /**
//     * Total count of bookmarks managed by this bookmark list.
//     *
//     * @return &gt;=0 total count of bookmarks.
//     */
//    public int getBookmarkCount () {
//        return bookmarks.size ();
//    }
//    
//    /**
//     * Get bookmark at the specified index.
//     * <br>
//     * The bookmarks are ordered by increasing offset.
//     *
//     * @param index index of the bookmark in the list of bookmarks.
//     * @return non-null bookmark instance.
//     */
//    public Bookmark getBookmark (int index) {
//        return (Bookmark) bookmarks.get (index);
//    }

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
    public Bookmark getNextBookmark (int offset, boolean wrapSearch) {
        offset++;
        checkOffsetNonNegative (offset);
        Bookmark bookmark = getNextBookmark (offset);
        return bookmark != null || !wrapSearch ?
            bookmark :
            getNextBookmark (-1, false);
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
    public Bookmark getPreviousBookmark (int offset, boolean wrapSearch) {
        checkOffsetNonNegative (offset);
        List<Bookmark> bookmarks = new ArrayList<Bookmark> (getBookmarks ());
        Bookmark bookmark; // result
        if (!bookmarks.isEmpty ()) {
            offset--; // search from previous offset
            bookmark = getNextBookmark (offset);
            if (bookmark == null || bookmark.getOffset () != offset) {
                // go below
                int index = bookmark == null ?
                    bookmarks.size () :
                    bookmarks.indexOf (bookmark);
                index--;
                if (index >= 0) {
                    bookmark = bookmarks.get (index);
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
     * First bookmark that has the line index greater or equal
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
    private Bookmark getNextBookmark (int offset) {
        // Find next bookmark by binary search
        int low = 0;
        List<Bookmark> bookmarks = new ArrayList<Bookmark> (getBookmarks ());
        if (bookmarks.isEmpty ()) return null;
        int high = bookmarks.size () - 1;
        
        while (low <= high) {
            int mid = (low + high) / 2;
            int midOffset = bookmarks.get (mid).getOffset();
            
            if (midOffset < offset) {
                low = mid + 1;
            } else if (midOffset > offset) {
                high = mid - 1;
            } else { // bookmark right at the offset
                // Goto first bookmark of possible ones at the same line
                mid--;
                while (mid >= 0) {
                    if (bookmarks.get (mid).getOffset() != offset) {
                        break;
                    }
                    mid--;
                }
                mid++;
                return bookmarks.get (mid);
            }
        }
        if (low < bookmarks.size ())
            return bookmarks.get (low);
        return null;
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
    public Bookmark toggleLineBookmark (int offset) {
        checkOffsetInDocument (offset);
        Element lineRoot = document.getDefaultRootElement ();
        int lineIndex = lineRoot.getElementIndex (offset);
        Bookmark bookmark = null;
        if (lineIndex < lineRoot.getElementCount ()) {
            Element lineElem = lineRoot.getElement (lineIndex);
            int lineStartOffset = lineElem.getStartOffset ();
            bookmark = getNextBookmark (lineStartOffset);
            if (bookmark != null &&
                bookmark.getOffset () < lineElem.getEndOffset () // inside line
            ) { // remove the existing bookmark
                removeBookmark (bookmark);
            } else { // add bookmark
                bookmark = addBookmark  (lineStartOffset);
            }
            // Save the bookmarks
            BookmarksPersistence.saveBookmarks (this);
        }
        return bookmark;
    }
    
    /**
     * Remove bookmark at the given index among the bookmarks.
     *
     * @param index index at which the bookmark should be removed.
     * @return removed (and invalidated) bookmark
     */
    public synchronized boolean removeBookmark (Bookmark bookmark) {
        boolean removed = bookmarks.remove (bookmark);
        if (removed) {
            bookmark.release();
            SwingUtilities.invokeLater (new Runnable () {
                public void run () {
                    propertyChangeSupport.firePropertyChange (PROP_BOOKMARKS, null, null);
                }
            });
        }
        return removed;
    }
    
    /** Removes all bookmarks */
    public synchronized void removeAllBookmarks (){
        if (!bookmarks.isEmpty()) {
            for (int i = 0; i < bookmarks.size (); i++){
                Bookmark bookmark = (Bookmark) bookmarks.get (i);
                bookmark.release();
            }
            bookmarks.clear();
            SwingUtilities.invokeLater (new Runnable () {
                public void run () {
                    propertyChangeSupport.firePropertyChange (PROP_BOOKMARKS, null, null);
                }
            });
        }
    }
    
    /**
     * Add bookmark to this list.
     * <br>
     */
    public synchronized Bookmark addBookmark (int lineOffset) {
        // Compute the index from increased offset to ensure to add the bookmark
        // after all the possible bookmarks with the same offset
        Bookmark bookmark = new Bookmark (this, lineOffset);
        bookmarks.add (bookmark);
        Collections.sort (bookmarks, breakpointsComparator);
        SwingUtilities.invokeLater (new Runnable () {
            public void run() {
                propertyChangeSupport.firePropertyChange (PROP_BOOKMARKS, null, null);
            }
        });
        return bookmark;
    }

    private void checkOffsetNonNegative(int offset) {
        if (offset < 0) {
            throw new IndexOutOfBoundsException("offset=" + offset + " < 0"); // NOI18N
        }
    }
    
    private void checkOffsetInDocument(int offset) {
        checkOffsetNonNegative(offset);
        int docLen = document.getLength();
        if (offset > docLen) {
            throw new IndexOutOfBoundsException("offset=" + offset // NOI18N
                + " > doc.getLength()=" + docLen); // NOI18N
        }
    }
    
    @Override
    public synchronized String toString() {
        return "Bookmarks: " + bookmarks; // NOI18N
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        propertyChangeSupport.addPropertyChangeListener(l);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener l) {
        propertyChangeSupport.removePropertyChangeListener(l);
    }
    
    
    // innerclassses ...........................................................
    
    private static PropertyChangeListener 
                            documentModifiedListener = new PropertyChangeListener () {

        public void propertyChange (PropertyChangeEvent evt) {
            if ("modified".equals (evt.getPropertyName ()) &&
                Boolean.FALSE.equals (evt.getNewValue ())
            ) {
                Observable observable = (Observable) evt.getSource();
                Document document = observable.getDocument ();
                if (document != null) {
                    // Document is being saved
                    BookmarkList bookmarkList = BookmarkList.get (document);
                    BookmarksPersistence.saveBookmarks (bookmarkList);
                }
            }
        }
    };
    
    private static final Comparator<Bookmark> breakpointsComparator = new Comparator<Bookmark> () {

        public int compare (Bookmark bookmark1, Bookmark bookmark2) {
            return bookmark1.getOffset () - bookmark2.getOffset ();
        }
    };
}

