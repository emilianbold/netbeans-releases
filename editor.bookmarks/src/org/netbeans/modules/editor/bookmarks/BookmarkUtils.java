/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

import javax.swing.JEditorPane;
import javax.swing.text.Document;
import org.netbeans.lib.editor.bookmarks.api.Bookmark;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.Line;

/**
 * Services to update or save bookmarks to persistent format.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public final class BookmarkUtils {
    
    private BookmarkUtils() {
        // no instances
    }
    
    public static int offset2LineIndex(Document doc, int offset) {
        javax.swing.text.Element lineRoot = doc.getDefaultRootElement();
        int lineIndex = lineRoot.getElementIndex(offset);
        return lineIndex;
        
    }
    
    public static int lineIndex2Offset(Document doc, int lineIndex) {
        javax.swing.text.Element lineRoot = doc.getDefaultRootElement();
        int offset = (lineIndex < lineRoot.getElementCount())
                ? lineRoot.getElement(lineIndex).getStartOffset()
                : doc.getLength();
        return offset;
        
    }
    
    public static void openEditor(EditorCookie ec, int lineIndex) {
        Line.Set lineSet = ec.getLineSet();
        if (lineSet != null) {
            Line line = lineSet.getCurrent(lineIndex);
            if (line != null) {
                line.show(Line.ShowOpenType.OPEN, Line.ShowVisibilityType.FOCUS);
                JEditorPane[] panes = ec.getOpenedPanes();
                if (panes.length > 0) {
                    panes[0].requestFocusInWindow();
                }
            }
        }
    }
    
    public static void updateCurrentLineIndex(BookmarkInfo info, Document doc) {
        Bookmark b = BookmarkAPIAccessor.INSTANCE.getBookmark(doc, info);
        if (b != null) {
            info.setCurrentLineIndex(b.getLineNumber());
        }
    }
    
    public static EditorCookie findEditorCookie(BookmarkInfo info) {
        EditorCookie ec = null;
        FileBookmarks fileBookmarks = info.getFileBookmarks();
        if (fileBookmarks != null) {
            FileObject fo = fileBookmarks.getFileObject();
            if (fo != null) {
                try {
                    DataObject dob = DataObject.find(fo);
                    ec = dob.getCookie(EditorCookie.class);
                } catch (DataObjectNotFoundException ex) {
                    // Leave ec == null
                }
            }
        }
        return ec;
    }
    
}

