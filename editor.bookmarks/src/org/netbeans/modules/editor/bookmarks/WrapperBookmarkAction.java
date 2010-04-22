/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.Set;
import javax.swing.Action;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorRegistry;

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
    
    @Override
    public String getName() {
        String name = (String)originalAction.getValue(Action.SHORT_DESCRIPTION);
        assert (name != null);
        return name;
    }

    @Override
    public void performAction(Node[] activatedNodes) {
        JTextComponent editorPane = getEditorPane (activatedNodes);
        if (editorPane != null) {
            ActionEvent paneEvt = new ActionEvent (editorPane, 0, "");
            originalAction.actionPerformed (paneEvt);
        }
    }
    
    @Override
    protected boolean enable (Node[] activatedNodes) {
        if (activatedNodes == null || activatedNodes.length != 1) {
            return false;
        }

        if (EditorRegistry.componentList().isEmpty()) {
            return false;
        }

        return activatedNodes[0].getLookup().lookup(EditorCookie.class) != null;
    }
    
    private static JTextComponent getEditorPane (Node[] activatedNodes) {
        if (activatedNodes != null && activatedNodes.length > 0) {
            Set<JTextComponent> editors = new HashSet<JTextComponent> ();
            for (Node node : activatedNodes) {
                JTextComponent pane = ToggleBookmarkAction.findComponent(node.getLookup());
                if (pane != null) {
                    editors.add(pane);
                }
            }
            if (editors.size () == 1) {
                return editors.iterator ().next ();
            }
        }
        return null;
    }

    @Override
    public org.openide.util.HelpCtx getHelpCtx() {
        return null;
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }

    @Override
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

