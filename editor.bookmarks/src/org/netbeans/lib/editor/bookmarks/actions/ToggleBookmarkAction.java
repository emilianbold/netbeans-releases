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
import javax.swing.Action;
import javax.swing.text.Caret;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.BaseAction;
import org.netbeans.lib.editor.bookmarks.api.BookmarkList;
import org.openide.util.NbBundle;


/**
 * Information about a persisted bookmark.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public final class ToggleBookmarkAction extends BaseAction {
    
    public static final String NAME = "bookmark-toggle";
    
    static final long serialVersionUID = -8438899482709646741L;

    public ToggleBookmarkAction() {
        super(NAME);
        putValue(BaseAction.ICON_RESOURCE_PROPERTY,
            "org/netbeans/modules/editor/bookmarks/resources/toggle_bookmark.png"); // NOI18N
    }

    public void actionPerformed(ActionEvent evt, JTextComponent target) {
        if (target != null) {
            if (org.netbeans.editor.Utilities.getEditorUI(target).isGlyphGutterVisible()) {
                Caret caret = target.getCaret();
                BookmarkList bookmarkList = BookmarkList.get(target.getDocument());
                bookmarkList.toggleLineBookmark(caret.getDot());

            } else { // Glyph gutter not visible -> just beep
                target.getToolkit().beep();
            }
        }
    }

    protected Object getDefaultShortDescription() {
        return NbBundle.getBundle(GotoBookmarkAction.class).getString(
                (String)getValue(Action.NAME));
    }

}

