/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.websvc.rest.wadl.design.undo;

import javax.swing.event.ChangeListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;
import org.openide.awt.UndoRedo;

/**
 * A proxy for another UndoRedo.Manager instance. Unless otherwise noted,
 * all methods delegate to the original manager.
 *
 * @author  Nathan Fiedler
 */
public class FilterUndoManager extends UndoRedo.Manager {
    /** silence compiler warnings */
    private static final long serialVersionUID = 1L;
    /** The original undo/redo manager. */
    private UndoRedo.Manager original;

    public FilterUndoManager(UndoRedo.Manager original) {
        assert original != null;
        this.original = original;
    }

    /**
     * Returns the original UndoRedo.Manager instance.
     *
     * @return  original manager.
     */
    protected UndoRedo.Manager getOriginal() {
        return original;
    }

    public void addChangeListener(ChangeListener changeListener) {
        original.addChangeListener(changeListener);
    }

    public boolean addEdit(UndoableEdit anEdit) {
        return original.addEdit(anEdit);
    }

    public boolean canRedo() {
        return original.canRedo();
    }

    public boolean canUndo() {
        return original.canUndo();
    }

    public boolean canUndoOrRedo() {
        return original.canUndoOrRedo();
    }

    public void die() {
        original.die();
    }

    public void discardAllEdits() {
        original.discardAllEdits();
    }

    public void end() {
        original.end();
    }

    public int getLimit() {
        return original.getLimit();
    }

    public String getPresentationName() {
        return original.getPresentationName();
    }

    public String getRedoPresentationName() {
        return original.getRedoPresentationName();
    }

    public String getUndoOrRedoPresentationName() {
        return original.getUndoOrRedoPresentationName();
    }

    public String getUndoPresentationName() {
        return original.getUndoPresentationName();
    }

    public boolean isInProgress() {
        return original.isInProgress();
    }

    public boolean isSignificant() {
        return original.isSignificant();
    }

    public void redo() throws CannotRedoException {
        original.redo();
    }

    public void removeChangeListener(ChangeListener changeListener) {
        original.removeChangeListener(changeListener);
    }

    public boolean replaceEdit(UndoableEdit anEdit) {
        return original.replaceEdit(anEdit);
    }

    public void setLimit(int l) {
        original.setLimit(l);
    }

    public void undo() throws CannotUndoException {
        original.undo();
    }

    public void undoOrRedo() throws CannotRedoException, CannotUndoException {
        original.undoOrRedo();
    }

    public void undoableEditHappened(UndoableEditEvent event) {
        original.undoableEditHappened(event);
    }
}
