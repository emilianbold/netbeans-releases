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

package org.netbeans.modules.xml.xam.ui.undo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.AbstractDocument;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import org.netbeans.modules.xml.xam.Model;
import org.openide.awt.UndoRedo;

/**
 * A proxy for another UndoRedo.Manager instance which removes and then
 * adds the undoable edit listeners from a Swing document, each time the
 * undo and redo operations are invoked. Essentially, this allows the
 * client to avoid firing undo events to the listeners when doing nothing
 * more than undoing or redoing a change to the document.
 *
 * <p>To use this undo manager proxy with <code>CloneableEditorSupport</code>,
 * it is necessary to subclass <code>CloneableEditorSupport</code> and override
 * the <code>createUndoRedoManager()</code> method to return an instance of
 * this class, providing the return value of
 * <code>super.createUndoRedoManager()</code> to the
 * <code>QuietUndoManager</code> constructor. In this way, this proxy can
 * then intercept all new edits and properly manage its state. Note that
 * setting the document in <code>createUndoRedoManager()</code> will not be
 * possible since the document will not have been initialized yet. It will
 * have to happen at a later time.</p>
 *
 * @author  Nathan Fiedler
 */
public class QuietUndoManager extends CompoundUndoManager {
    /** silence compiler warnings */
    private static final long serialVersionUID = 1L;
    /** Undoable edit listeners are managed for this document; may be null. */
    private AbstractDocument document;
    /** If not null, the Model is sync'd after undo/redo. */
    private Model model;
    private List<Model> otherModels = new ArrayList<Model>();

    /**
     * Creates a new instance of QuietUndoManager.
     *
     * @param  original  UndoRedo.Manager to be proxied.
     */
    public QuietUndoManager(UndoRedo.Manager original) {
        super(original);
    }

    /**
     * Wrap up after performing an undo/redo operation.
     *
     * @param  listeners  undoable edit listeners to be added to document.
     */
    private void finish(UndoableEditListener[] listeners) {
        if (model != null) {
            // When model is non-null, add ourselves as a listener since
            // we removed us in the preparation step.
            model.addUndoableEditListener(this);
        }
        if (listeners != null && listeners.length > 0) {
            for (UndoableEditListener uel : listeners) {
                document.addUndoableEditListener(uel);
            }
        }
    }

    /**
     * Prepare to perform an undo/redo operation.
     *
     * @return  the set of undoable edit listeners removed from document.
     */
    private UndoableEditListener[] prepare() {
        if (model != null) {
            // If the model is set, that means we are to sync the model
            // after performing the undo/redo operation. That, however,
            // means we cannot be listening to the model lest we receive
            // additional undoable edits, which would be bad.
            model.removeUndoableEditListener(this);
        }
        if (document == null) {
            return null;
        }
        UndoableEditListener[] listeners = document.getUndoableEditListeners();
        if (listeners != null && listeners.length > 0) {
            for (UndoableEditListener uel : listeners) {
                document.removeUndoableEditListener(uel);
            }
        }
        return listeners;
    }

    /**
     * Set the document for which the undoable edit listeners will be
     * removed and then added back for each undo/redo operation.
     *
     * @param  document  the document whose listeners will be managed.
     */
    public void setDocument(AbstractDocument document) {
        this.document = document;
    }

    /**
     * Set the Model to be managed when the undo and redo operations
     * are performed. This should only be set when the model view is
     * being shown. Call this method with a value of <code>null</code>
     * when the model view is being hidden.
     *
     * @param  model  Model to be managed; if null, disables management.
     */
    public void setModel(Model model) {
        this.model = model;
    }

    /**
     * Sync the Model so it does not perform the auto-sync at some
     * later time, causing additional undoable edits from appearing
     * on the undo queue. That would be pointless anyway, since we
     * are simply undoing or redoing edits anyway.
     */
    private void syncModel() {
        if (model != null) {
            try {
                // Fortunately this method is efficient in that it knows
                // if the change that occurred was done by the model or
                // if the change is coming from the document (the model
                // has a document listener on the Swing document).
                model.sync();
                for(Model m: otherModels)
                    m.sync();
            } catch (IOException ioe) {
                // Ignore, nothing we can do about it.
            }
        }
    }

    public void redo() throws CannotRedoException {
        UndoableEditListener[] listeners = prepare();
        try {
            super.redo();
            syncModel();
        } finally {
            finish(listeners);
        }
    }

    public void undo() throws CannotUndoException {
        UndoableEditListener[] listeners = prepare();
        try {
            super.undo();
            syncModel();
        } finally {
            finish(listeners);
        }
    }
    
    /**
     * Allows other models to be synced after undo or redo.
     */
    public void addWrapperModel(Model model) {
        if(!otherModels.contains(model))
            otherModels.add(model);
    }
    
    /**
     * Removes other models that were added for sync.
     */
    public void removeWrapperModel(Model model) {
        otherModels.remove(model);
    }
}
