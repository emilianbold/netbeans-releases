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

package org.netbeans.modules.ant.debugger.breakpoints;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.Set;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.text.Caret;
import javax.swing.text.StyledDocument;
import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.spi.debugger.ActionsProviderSupport;
import org.openide.ErrorManager;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.text.Line;
import org.openide.text.NbDocument;
import org.openide.util.WeakListeners;
import org.openide.windows.TopComponent;

/**
 *
 * @author  Honza
 */
public class AntBreakpointActionProvider extends ActionsProviderSupport
                                         implements PropertyChangeListener {
    
    private static final Set actions = Collections.singleton (
        ActionsManager.ACTION_TOGGLE_BREAKPOINT
    );
    
    
    public AntBreakpointActionProvider () {
        setEnabled (ActionsManager.ACTION_TOGGLE_BREAKPOINT, true);
        TopComponent.getRegistry().addPropertyChangeListener(
                WeakListeners.propertyChange(this, TopComponent.getRegistry()));
    }
    
    /**
     * Called when the action is called (action button is pressed).
     *
     * @param action an action which has been called
     */
    public void doAction (Object action) {
        Line line = getCurrentLine ();
        if (line == null) return ;
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
        //System.out.println("getCurrentLine()...");
        FileObject fo = (FileObject) n.getLookup ().lookup(FileObject.class);
        if (fo == null) {
            DataObject dobj = (DataObject) n.getLookup().lookup(DataObject.class);
            if (dobj != null) {
                fo = dobj.getPrimaryFile();
            }
        }
        //System.out.println("n = "+n+", FO = "+fo+" => is ANT = "+isAntFile(fo));
        if (!isAntFile(fo)) {
            return null;
        }
        LineCookie lineCookie = (LineCookie) n.getCookie (LineCookie.class);
        //System.out.println("line cookie = "+lineCookie);
        if (lineCookie == null) return null;
        EditorCookie editorCookie = (EditorCookie) n.getCookie (EditorCookie.class);
        if (editorCookie == null) return null;
        JEditorPane jEditorPane = getEditorPane(editorCookie);
        if (jEditorPane == null) return null;
        StyledDocument document = editorCookie.getDocument ();
        if (document == null) return null;
        Caret caret = jEditorPane.getCaret ();
        if (caret == null) return null;
        int lineNumber = NbDocument.findLineNumber (
            document,
            caret.getDot ()
        );
        try {
            Line.Set lineSet = lineCookie.getLineSet();
            assert lineSet != null : lineCookie;
            return lineSet.getCurrent(lineNumber);
        } catch (IndexOutOfBoundsException ex) {
            return null;
        }
    }
    
    private static JEditorPane getEditorPane_(EditorCookie editorCookie) {
        JEditorPane[] op = editorCookie.getOpenedPanes ();
        if ((op == null) || (op.length < 1)) return null;
        return op [0];
    }

    private static JEditorPane getEditorPane(final EditorCookie editorCookie) {
        if (SwingUtilities.isEventDispatchThread()) {
            return getEditorPane_(editorCookie);
        } else {
            final JEditorPane[] ce = new JEditorPane[1];
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    public void run() {
                        ce[0] = getEditorPane_(editorCookie);
                    }
                });
            } catch (InvocationTargetException ex) {
                ErrorManager.getDefault().notify(ex.getTargetException());
            } catch (InterruptedException ex) {
                ErrorManager.getDefault().notify(ex);
            }
            return ce[0];
        }
    }
    
    
    
    private static boolean isAntFile(FileObject fo) {
        if (fo == null) {
            return false;
        } else {
            return fo.getMIMEType().equals("text/x-ant+xml");
        }
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        // We need to push the state there :-(( instead of wait for someone to be interested in...
        boolean enabled = getCurrentLine() != null;
        setEnabled (ActionsManager.ACTION_TOGGLE_BREAKPOINT, enabled);
    }
    
}
