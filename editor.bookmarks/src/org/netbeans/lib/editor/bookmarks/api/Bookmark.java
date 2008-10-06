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
import java.lang.ref.WeakReference;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.swing.text.StyledDocument;

import org.netbeans.modules.editor.NbEditorUtilities;
import org.openide.loaders.DataObject;
import org.openide.text.Annotation;
import org.openide.text.Line;
import org.openide.text.NbDocument;
import org.openide.util.NbBundle;


/**
 * Interface to a bookmark.
 *
 * @author Miloslav Metelka
 */

public final class Bookmark {

    public static final String BOOKMARK_ANNOTATION_TYPE = "editor-bookmark"; // NOI18N

    // cary mary fuk!
    private static Map<Line,AAnnotation> lineToAnnotation = new WeakHashMap<Line,AAnnotation> ();

    /**
     * Bookmark list to which this bookmark belongs.
     */
    private BookmarkList    bookmarkList;

    /**
     * Whether this mark was released or not.
     */
    private boolean         released;
    
    private Line            line;
    private AAnnotation     annotation;
    private Map<BookmarkList,LineListener>
                            bookmarkListToLineListener = new WeakHashMap<BookmarkList,LineListener> ();
    
    /**
     * Construct new instance of bookmark.
     *
     * <p>
     * The constructor is not public intentionally.
     * Please see <code>BookmarksApiPackageAccessor</code> for details.
     */
    Bookmark (BookmarkList bookmarkList, int offset) {
        this.bookmarkList = bookmarkList;
        StyledDocument document = (StyledDocument) bookmarkList.getDocument ();
        int lineNumber = NbDocument.findLineNumber (document, offset);
        DataObject dataObject = NbEditorUtilities.getDataObject (document);
        for (Line line : lineToAnnotation.keySet ()) {
            if (line.getLineNumber () == lineNumber &&
                line.getLookup().lookup (DataObject.class).equals (dataObject)
            ) {
                this.line = line;
                this.annotation = lineToAnnotation.get (line);
                return;
            }
        }
        annotation = new AAnnotation ();
        line = NbEditorUtilities.getLine (bookmarkList.getDocument (), offset, false);
        lineToAnnotation.put (line, annotation);
        annotation.attach (line);
        LineListener lineListener = bookmarkListToLineListener.get (bookmarkList);
        if (lineListener == null) {
            lineListener = new LineListener (bookmarkList);
            bookmarkListToLineListener.put (bookmarkList, lineListener);
        }
        line.addPropertyChangeListener (lineListener);
    }

    /**
     * Get offset of this bookmark.
     * <br>
     * Offsets behave like {@link javax.swing.text.Position}s (they track
     * inserts/removals).
     */
    public int getOffset () {
        return NbDocument.findLineOffset (
            (StyledDocument) bookmarkList.getDocument (), 
            line.getLineNumber ()
        );
    }

    /**
     * Get the index of the line at which this bookmark resides.
     */
    public int getLineNumber () {
        return line.getLineNumber ();
    }
    
    /**
     * Get the bookmark list for which this bookmark was created.
     */
    public BookmarkList getList() {
        return bookmarkList;
    }
    
    /**
     * Return true if this mark was released (removed from its bookmark list)
     * and is no longer actively used.
     */
    public boolean isReleased() {
        return released;
    }
    
    /**
     * Mark the current bookmark as invalid.
     */
    void release () {
        assert (!released);
        released = true;
        annotation.detach ();
        lineToAnnotation.remove (line);
    }
    
    
    // innerclasses ............................................................
    
    public final class AAnnotation extends Annotation {

        public String getAnnotationType () {
            return BOOKMARK_ANNOTATION_TYPE;
        }

        public String getShortDescription () {
            String fmt = NbBundle.getBundle (Bookmark.class).getString ("Bookmark_Tooltip"); // NOI18N
            int lineIndex = getLineNumber ();
            return MessageFormat.format (fmt, new Object[] {new Integer (lineIndex + 1)});
        }

        public String toString() {
            return getShortDescription();
        }
    }

    private static class LineListener implements PropertyChangeListener {

        private WeakReference<BookmarkList> bookmarkListReference;

        LineListener (BookmarkList bookmarkList) {
            bookmarkListReference = new WeakReference (bookmarkList);
        }

        public void propertyChange(PropertyChangeEvent evt) {
            BookmarkList bookmarkList = bookmarkListReference.get ();
            if (bookmarkList == null)
                return;
            List<Bookmark> bookmarks = new ArrayList<Bookmark> (bookmarkList.getBookmarks ());
            int lineNumber = -1;
            for (Bookmark bookmark : bookmarks) {
                if (bookmark.getLineNumber () == lineNumber) {
                    bookmarkList.removeBookmark (bookmark);
                }
                lineNumber = bookmark.getLineNumber ();
            }
        }
    };
}

