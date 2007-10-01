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

