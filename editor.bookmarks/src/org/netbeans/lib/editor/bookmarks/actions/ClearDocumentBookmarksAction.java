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

import javax.swing.text.JTextComponent;
import java.awt.event.ActionEvent;
import javax.swing.Action;

import org.netbeans.editor.BaseAction;
import org.openide.util.NbBundle;

import org.netbeans.lib.editor.bookmarks.api.BookmarkList;

/**
 *  Clear all bookmarks in the current file
 */
public class ClearDocumentBookmarksAction extends BaseAction {
    
    public static final String NAME = "clear-document-bookmarks";

    public ClearDocumentBookmarksAction() {
	super(NAME);
        putValue(BaseAction.ICON_RESOURCE_PROPERTY,
	    "org/netbeans/modules/editor/bookmarks/resources/clear_bookmark.png"); // NOI18N
    }

    public String getName() {
	return (String) getValue(NAME);
    }

    public void actionPerformed(ActionEvent e, JTextComponent target) {
	BookmarkList bookmarkList = BookmarkList.get(target.getDocument());

	for (int i = bookmarkList.getBookmarkCount() - 1; i >= 0; i--) {
	    bookmarkList.removeBookmarkAtIndex(i);
	}
    }

    protected Object getDefaultShortDescription() {
        return NbBundle.getBundle(ClearDocumentBookmarksAction.class).getString(
                (String)getValue(Action.NAME));
    }

}
