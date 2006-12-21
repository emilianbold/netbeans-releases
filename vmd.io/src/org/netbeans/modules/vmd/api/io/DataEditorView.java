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
package org.netbeans.modules.vmd.api.io;

import org.openide.awt.UndoRedo;
import org.openide.util.HelpCtx;

import javax.swing.*;
import java.io.Serializable;

/**
 * @author David Kaspar
 */
public interface DataEditorView extends Serializable {

    // TODO - expose lookup

    public enum Kind {
        NONE, CODE, MODEL
    }

    public DataObjectContext getContext ();
    public Kind getKind ();

    public String preferredID ();
    public String getDisplayName ();
    public HelpCtx getHelpCtx ();

    public JComponent getVisualRepresentation ();
    public JComponent getToolbarRepresentation ();

    // TODO - when a document is loaded/changed, the UndoRedo has to be reloaded
    // -> it needs a property-change event to be fired
    // for notifying the Undo and Redo action to update/reassign UndoRedo
    public UndoRedo getUndoRedo ();

    public void componentOpened ();
    public void componentClosed ();
    public void componentShowing ();
    public void componentHidden ();
    public void componentActivated ();
    public void componentDeactivated ();

    public int getOpenPriority ();
    public int getEditPriority ();
    public int getOrder ();

}
