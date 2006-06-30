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


package org.netbeans.modules.javadoc.search;

import javax.swing.JEditorPane;
import org.openide.cookies.EditorCookie;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;
import org.openide.windows.TopComponent;

import java.awt.*;

/**
 * Try to find generated and mounted documentation for selected node.
 * //!!! It has mixed semantics with the find doc action because
 * it tries to inspect opened editor too
 *
 * @author  Petr Suchomel
 * @version 1.0
 */
public final class ShowDocAction extends CookieAction {

    static final long serialVersionUID =3578357584245478L;

    public ShowDocAction() {
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
    }    
    /** Human presentable name of the action. This should be
    * presented as an item in a menu.
    * @return the name of the action
    */
    public String getName () {
        return NbBundle.getBundle( ShowDocAction.class ).getString ("CTL_SHOWDOC_MenuItem");   //NOI18N
    }

    /** Cookie classes contains one class returned by cookie () method.
    */
    protected final Class[] cookieClasses () {
        return new Class[] { EditorCookie.class };
    }

    /** All must be DataFolders or JavaDataObjects
    */
    protected int mode () {
        return MODE_EXACTLY_ONE;
    }

    /** Help context where to find more about the action.
    * @return the help context for this action
    */
    public HelpCtx getHelpCtx () {
        return new HelpCtx (ShowDocAction.class);
    }

    /** This method is called by one of the "invokers" as a result of
    * some user's action that should lead to actual "performing" of the action.
    * This default implementation calls the assigned actionPerformer if it
    * is not null otherwise the action is ignored.
    */
    public void performAction ( Node[] nodes ) {
        IndexSearch indexSearch = IndexSearch.getDefault();
                
        if( nodes.length == 1 && nodes[0] != null ) {
            String toFind = findTextFromNode(nodes[0]);
            if (toFind != null)
                indexSearch.setTextToFind( toFind );
        }
        indexSearch.open ();
        indexSearch.requestActive();
    }
    
    /**
     * Attempts to find a suitable text from the node. 
     */
    private String findTextFromNode(Node n) {
        EditorCookie ec = (EditorCookie)n.getCookie(EditorCookie.class);
        // no editor underneath the node --> node's name is the only searchable text.
        if (ec != null) {
            JEditorPane[] panes = ec.getOpenedPanes();
            if (panes != null) {
                TopComponent activetc = TopComponent.getRegistry().getActivated();
                for (int i = 0; i < panes.length; i++) {
                    if (activetc.isAncestorOf(panes[i])) {
                        // we have found the correct JEditorPane
                        String s = GetJavaWord.forPane(panes[i]);
                        if (s != null)
                            return s;
                        else
                            break;
                    }
                }
            }
        }
        return n.getName();
    }

    protected boolean asynchronous() {
        return false;
    }
}
