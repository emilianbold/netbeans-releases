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
package org.netbeans.modules.vmd.io;

import org.netbeans.modules.vmd.api.io.DataObjectContext;
import org.netbeans.modules.vmd.api.model.DocumentInterface;
import org.openide.awt.UndoRedo;

import javax.swing.undo.UndoableEdit;

/**
 * @author David Kaspar
 */
public class DocumentInterfaceImpl implements DocumentInterface {

    private DataObjectContext context;
//    private Project project;
    private UndoRedo.Manager undoRedoManager;
    private boolean enabled;

    public DocumentInterfaceImpl (DataObjectContext context, UndoRedo.Manager undoRedoManager) {
        this.context = context;
        this.undoRedoManager = undoRedoManager;
    }

    public String getProjectID () {
        return context.getProjectID ();
    }

    public String getProjectType () {
        return context.getProjectType ();
    }

    public void notifyModified () {
        if (enabled)
            context.notifyModified ();
    }

    public void undoableEditHappened (UndoableEdit edit) {
        if (enabled)
            undoRedoManager.addEdit (edit);
    }

    public void discardAllEdits () {
        if (enabled)
            undoRedoManager.discardAllEdits ();
    }

    public void enable () {
        enabled = true;
    }

}
