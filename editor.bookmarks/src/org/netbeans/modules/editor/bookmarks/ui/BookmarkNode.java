/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.editor.bookmarks.ui;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.lib.editor.bookmarks.api.Bookmark;
import org.netbeans.modules.editor.bookmarks.BookmarkAPIAccessor;
import org.netbeans.modules.editor.bookmarks.BookmarkInfo;
import org.netbeans.modules.editor.bookmarks.BookmarksPersistence;
import org.openide.filesystems.FileObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.ImageUtilities;

/**
 * Node for either real Bookmark or a BookmarkInfo.
 *
 * @author Miloslav Metelka
 */
public class BookmarkNode extends AbstractNode {
    
    private static final Action DEFAULT_ACTION = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            Object source = e.getSource();
            if (source instanceof BookmarkNode) {
                BookmarkNode node = (BookmarkNode) source;
                node.openInEditor();
            }
        }
    };
    
    private final FileObject fo;
    
    private final BookmarkInfo bookmarkInfo;
    
    private Reference<Bookmark> bookmarkRef;
    
    private static Image bookmarkIcon;

    public BookmarkNode(FileObject fo, Bookmark b) {
        this(fo, BookmarkAPIAccessor.INSTANCE.getInfo(b));
        this.bookmarkRef = new WeakReference<Bookmark>(b);
    }
    
    public BookmarkNode(FileObject fo, BookmarkInfo bookmarkInfo) {
        super(Children.LEAF);
        this.fo = fo;
        this.bookmarkInfo = bookmarkInfo;
    }
    
    @Override
    public String getName() {
        return bookmarkInfo.getName();
    }

    @Override
    public String getDisplayName() {
        String name = bookmarkInfo.getName();
        String lineInfo = "Line " + (bookmarkInfo.getLineIndex() + 1);
        return (name.length() > 0) ? (name + " at " + lineInfo) : lineInfo;
    }

    @Override
    public Action getPreferredAction() {
        return DEFAULT_ACTION;
    }

    @Override
    public Image getIcon(int type) {
        if (bookmarkIcon == null) {
            bookmarkIcon = ImageUtilities.loadImage(
                    "org/netbeans/modules/editor/bookmarks/resources/bookmark_16.png", false);
        }
        return bookmarkIcon;

    }
    
    private int lineIndex() {
        Bookmark b = bookmark();
        return (b != null) ? b.getLineNumber() : bookmarkInfo.getLineIndex();
    }
    
    void openInEditor() {
        BookmarksPersistence.get().openEditor(fo, getUpdatedInfo());
    }
    
    Bookmark bookmark() {
        return (bookmarkRef != null) ? bookmarkRef.get() : null;
    }
    
    public String getBookmarkName() {
        return bookmarkInfo.getName();
    }
    
    public void setBookmarkName(String bookmarkName) {
        bookmarkInfo.setName(bookmarkName);
    }
    
    public String getBookmarkLocation() {
        int lineIndex = lineIndex();
        return fo.getNameExt() + ":" + (lineIndex + 1);
    }

    public String getBookmarkFullLocation() {
        int lineIndex = lineIndex();
        return fo.getPath() + " at Line " + (lineIndex + 1);
    }

    public String getBookmarkKey() {
        return bookmarkInfo.getKey();
    }
    
    public void setBookmarkKey(String bookmarkKey) {
        bookmarkInfo.setKey(bookmarkKey);
    }
    
    public FileObject getFileObject() {
        return fo;
    }

    public BookmarkInfo getUpdatedInfo() {
        Bookmark b = bookmark();
        if (b != null) {
            return BookmarkInfo.create(bookmarkInfo, fo.toURL(), b.getLineNumber());
        }
        return bookmarkInfo;
    }
    
}
