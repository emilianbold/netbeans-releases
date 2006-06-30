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
