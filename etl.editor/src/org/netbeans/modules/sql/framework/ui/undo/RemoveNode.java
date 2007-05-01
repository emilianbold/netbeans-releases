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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.sql.framework.ui.undo;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.netbeans.modules.sql.framework.model.SQLObject;
import org.netbeans.modules.sql.framework.ui.model.SQLUIModel;

import com.sun.sql.framework.exception.BaseException;

/**
 * @author Ritesh Adval
 */
public class RemoveNode extends AbstractUndoableEdit {

    private SQLUIModel model;
    private SQLObject obj;

    public RemoveNode(SQLUIModel model, SQLObject obj) {
        this.model = model;
        this.obj = obj;
    }

    public void undo() throws CannotUndoException {
        try {
            model.addObjectIgnoreUndo(obj);
        } catch (BaseException ex) {
            ex.printStackTrace();
        }
    }

    public void redo() throws CannotRedoException {
        try {
            model.removeObjectIgnoreUndo(obj);
        } catch (BaseException ex) {
            ex.printStackTrace();
        }
    }

    public boolean canUndo() {
        return true;
    }

    public boolean canRedo() {
        return true;
    }

    public String getPresentationName() {
        return "RemoveNode";
    }
}

