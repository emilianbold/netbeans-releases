/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
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
        super("bookmarks-kit-install");
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


