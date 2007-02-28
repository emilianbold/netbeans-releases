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

package org.netbeans.modules.editor.bookmarks;

import java.awt.event.ActionEvent;
import javax.swing.Action;
import javax.swing.JEditorPane;
import org.netbeans.editor.BaseAction;
import org.netbeans.lib.editor.bookmarks.actions.ClearDocumentBookmarksAction;
import org.netbeans.lib.editor.bookmarks.actions.GotoBookmarkAction;
import org.netbeans.lib.editor.bookmarks.actions.ToggleBookmarkAction;
import org.openide.cookies.EditorCookie;
import org.openide.nodes.Node;
import org.openide.util.actions.NodeAction;


/**
 * Action wrapping the bookmark actions.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public class WrapperBookmarkAction extends NodeAction {
    
    static final long serialVersionUID = 0L;
    
    private Action originalAction;

    public WrapperBookmarkAction(Action originalAction) {
        this.originalAction = originalAction;
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
        // Re-add the property as SystemAction.putValue() is final
//        putValue(BaseAction.ICON_RESOURCE_PROPERTY, getValue(BaseAction.ICON_RESOURCE_PROPERTY));
    }
    
    public String getName() {
        String name = (String)originalAction.getValue(Action.SHORT_DESCRIPTION);
        assert (name != null);
        return name;
    }

    public void performAction(Node[] activatedNodes) {
        if (activatedNodes != null && activatedNodes.length == 1) {
            EditorCookie ec = (EditorCookie)activatedNodes[0].getCookie(EditorCookie.class);
            if (ec != null) {
                JEditorPane panes[] = ec.getOpenedPanes();
                if (panes != null && panes.length > 0) {
                    JEditorPane pane = panes[0];
                    ActionEvent paneEvt = new ActionEvent(pane, 0, "");
                    originalAction.actionPerformed(paneEvt);
                }
            }
        }
    }
    
    protected boolean enable(Node[] activatedNodes) {
        return true;
    }

    public org.openide.util.HelpCtx getHelpCtx() {
        return null;
    }

    protected boolean asynchronous() {
        return false;
    }

    protected String iconResource() {
        return (String)originalAction.getValue(BaseAction.ICON_RESOURCE_PROPERTY);
    }

    public static final class Next extends WrapperBookmarkAction {
        
        public Next() {
            super(GotoBookmarkAction.createNext());
        }

    }

    public static final class Previous extends WrapperBookmarkAction {
        
        public Previous() {
            super(GotoBookmarkAction.createPrevious());
        }

    }

    public static final class ClearDocumentBookmarks extends WrapperBookmarkAction {
        
        public ClearDocumentBookmarks() {
            super(new ClearDocumentBookmarksAction());
        }

    }

}

