/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.ant.debugger.breakpoints;

import java.util.Collections;
import java.util.Set;
import javax.swing.JEditorPane;
import javax.swing.text.Caret;
import javax.swing.text.StyledDocument;
import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.spi.debugger.ActionsProviderSupport;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.text.Line;
import org.openide.text.NbDocument;
import org.openide.windows.TopComponent;


/**
 *
 * @author  Honza
 */
public class AntBreakpointActionProvider extends ActionsProviderSupport {
    
    private static final Set actions = Collections.singleton (
        ActionsManager.ACTION_TOGGLE_BREAKPOINT
    );
    
    
    public AntBreakpointActionProvider () {
        setEnabled (ActionsManager.ACTION_TOGGLE_BREAKPOINT, true);
    }
    
    /**
     * Called when the action is called (action button is pressed).
     *
     * @param action an action which has been called
     */
    public void doAction (Object action) {
        Line line = getCurrentLine ();
        FileObject fileObject = (FileObject) line.getLookup ().lookup 
            (FileObject.class);
        if (!fileObject.getExt ().equals ("xml")) return;
        Breakpoint[] breakpoints = DebuggerManager.getDebuggerManager ().
            getBreakpoints ();
        int i, k = breakpoints.length;
        for (i = 0; i < k; i++)
            if ( breakpoints [i] instanceof AntBreakpoint &&
                 ((AntBreakpoint) breakpoints [i]).getLine ().equals (line)
            ) {
                DebuggerManager.getDebuggerManager ().removeBreakpoint
                    (breakpoints [i]);
                break;
            }
        if (i == k)
            DebuggerManager.getDebuggerManager ().addBreakpoint (
                new AntBreakpoint (line)
            );
        //S ystem.out.println("toggle");
    }
    
    /**
     * Returns set of actions supported by this ActionsProvider.
     *
     * @return set of actions supported by this ActionsProvider
     */
    public Set getActions () {
        return actions;
    }
    
    
    static Line getCurrentLine () {
        Node[] nodes = TopComponent.getRegistry ().getCurrentNodes ();
        if (nodes == null) return null;
        if (nodes.length != 1) return null;
        Node n = nodes [0];
        LineCookie lineCookie = (LineCookie) n.getCookie (LineCookie.class);
        if (lineCookie == null) return null;
        EditorCookie editorCookie = (EditorCookie) n.getCookie (EditorCookie.class);
        if (editorCookie == null) return null;
        JEditorPane[] jEditorPane = editorCookie.getOpenedPanes ();
        if ((jEditorPane == null) || (jEditorPane.length < 1)) return null;
        StyledDocument document = editorCookie.getDocument ();
        if (document == null) return null;
        Caret caret = jEditorPane [0].getCaret ();
        if (caret == null) return null;
        int lineNumber = NbDocument.findLineNumber (
            document,
            caret.getDot ()
        );
        try {
            return lineCookie.getLineSet ().getCurrent (lineNumber);
        } catch (IndexOutOfBoundsException ex) {
            return null;
        }
    }
}
