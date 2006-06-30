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

package org.netbeans.lib.editor.bookmarks.actions;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.BaseAction;
import org.netbeans.editor.BaseKit;
import org.netbeans.lib.editor.bookmarks.BookmarksApiPackageAccessor;
import org.netbeans.lib.editor.bookmarks.api.Bookmark;
import org.netbeans.lib.editor.bookmarks.api.BookmarkList;
import org.netbeans.lib.editor.bookmarks.spi.BookmarkManager;


/**
 * Action that jumps to next/previous bookmark.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public class BookmarksKitInstallAction extends BaseAction {
    
    static final long serialVersionUID = -0L;
    
    public static final BookmarksKitInstallAction INSTANCE = new BookmarksKitInstallAction();
    
    BookmarksKitInstallAction() {
        super("bookmarks-kit-install"); // NOI18N
        putValue(BaseAction.NO_KEYBINDING, Boolean.TRUE);        
    }

    public void actionPerformed(ActionEvent evt, JTextComponent target) {
        assert (target != null);
        Document doc = target.getDocument();
        BookmarkList.get(doc); // Initialize the bookmark list
        target.addPropertyChangeListener(BookmarksRefreshListener.INSTANCE);
    }

    private static final class BookmarksRefreshListener implements PropertyChangeListener {
        
        static final BookmarksRefreshListener INSTANCE = new BookmarksRefreshListener();
        
        public void propertyChange(PropertyChangeEvent evt) {
            if ("document".equals(evt.getPropertyName())) { // NOI18N
                Document newDoc = (Document)evt.getNewValue();
                if (newDoc != null) {
                    BookmarkList bml = BookmarkList.get(newDoc); // ask for the list to initialize it
                }
            }
                
        }
        
    }
}


