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
 *
 */

package org.netbeans.modules.vmd.io.editor;

import org.netbeans.modules.vmd.api.io.DataObjectContext;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.Lookup;
import org.openide.windows.CloneableTopComponent;

import javax.swing.*;

/**
 * @author David Kaspar
 */
public class CodeEditorTopComponent extends EditorTopComponent implements CloneableEditorSupport.Pane {

    private transient JEditorPane pane;

    public CodeEditorTopComponent (DataObjectContext context, Lookup lookup, JComponent view) {
        super (context, lookup, view);
    }

    public JEditorPane getEditorPane () {
        if (pane == null) {
            JComponent view = getView ();
            pane = view instanceof CloneableEditorSupport.Pane ? ((CloneableEditorSupport.Pane) view).getEditorPane () : null;
            getActionMap ().setParent (pane.getActionMap ());
            pane.getActionMap ().remove ("cloneWindow"); // NOI18N
        }
        return pane;
    }

    public CloneableTopComponent getComponent () {
        JComponent view = getView ();
        return view instanceof CloneableEditorSupport.Pane ? ((CloneableEditorSupport.Pane) view).getComponent () : null;
    }

    public void updateName () {
        JComponent view = getView ();
        if (view instanceof CloneableEditorSupport.Pane)
            ((CloneableEditorSupport.Pane) view).updateName ();
    }

    public void ensureVisible () {
        JComponent view = getView ();
        if (view instanceof CloneableEditorSupport.Pane)
            ((CloneableEditorSupport.Pane) view).ensureVisible ();
    }

}
