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
package org.netbeans.modules.vmd.api.model;

import org.openide.ErrorManager;
import org.openide.util.Mutex;

import javax.swing.undo.*;
import java.util.Collection;

/**
 * This class manages a transactional access to a document. If you want execute a code which reads or writes to
 * a document or its components than you have to use this manager and call readAccess or writeAccess. A parameter is
 * a Runnable which will be executed with read/write access.
 * <p>
 * It uses the NetBeans MUTEX class. Therefore it allows multiple reads and just a single write access at the same time.
 * <p>
 * When a read or a write access is granted, it automatically waits for a read access on descriptor registry.
 *
 * @author David Kaspar
 */
public final class TransactionManager {

    private final DesignDocument document;
    private final DescriptorRegistry descriptorRegistry;
    private final ListenerManager listenerManager;
    private final Mutex mutex = new Mutex ();
    private boolean notRootLevelWriteAccess = false;
    private boolean assertEventAllowed = false;
    private boolean rollback = false;
    private boolean useUndoManager = false;
    private boolean discardAllEdits = false;
    private TransactionEdit transactionEdit;

    TransactionManager (DesignDocument document, DescriptorRegistry descriptorRegistry, ListenerManager listenerManager) {
        assert Debug.isFriend (DesignDocument.class, "<init>"); // NOI18N
        this.document = document;
        this.descriptorRegistry = descriptorRegistry;
        this.listenerManager = listenerManager;
    }

    /**
     * Executes a Runnable.run method with read access.
     * @param runnable the runnable
     */
    public void readAccess (final Runnable runnable) {
        descriptorRegistry.readAccess (new Runnable() {
            public void run () {
                mutex.readAccess (runnable);
            }
        });
    }

    /**
     * Executes a Runnable.run method with write access.
     * @param runnable the runnable
     * @return the event id at the end of the write transaction
     */
    public long writeAccess (final Runnable runnable) {
        final long eventID[] = new long[] { 0 };
        descriptorRegistry.readAccess (new Runnable() {
            public void run () {
                mutex.writeAccess (new Runnable () {
                    public void run () {
                        writeAccessCore (runnable);
                        eventID[0] = listenerManager.getEventID ();
                    }
                });
            }
        });
        return eventID[0];
    }

    private void writeAccessCore (Runnable runnable) {
        boolean rootLevel = ! notRootLevelWriteAccess;
        notRootLevelWriteAccess = true;

        if (rootLevel)
            writeAccessRootBegin ();

        try {
            runnable.run ();
        } finally {
            if (rootLevel)
                writeAccessRootEnd ();
        }
    }

    private void writeAccessRootBegin () {
        assertEventAllowed = true;
        rollback = false;
        useUndoManager = true;
        discardAllEdits = false;
        transactionEdit = null;
    }

    private void writeAccessRootEnd () {
        assertEventAllowed = false;
        DesignEvent event = null;
        try {
            if (rollback)
                rollbackCore ();
            event = listenerManager.fireEvent ();
        } finally {
            try {
                if (useUndoManager) {
                    if (discardAllEdits)
                        document.getDocumentInterface ().discardAllEdits ();
                    else if (transactionEdit != null) {
                        transactionEdit.end ();
                        document.getDocumentInterface ().undoableEditHappened (transactionEdit);
                    }
                }
            } finally {
                notRootLevelWriteAccess = false;
                if (event != null  &&  event.isStructureChanged ())
                    document.getDocumentInterface ().notifyModified ();    
            }
        }
    }

    /**
     * Checks whether the current thread has a read or a write access granted.
     * @return true if a read or a write access is granted
     */
    public boolean isAccess () {
        return mutex.isReadAccess ()  ||  mutex.isWriteAccess ();
    }

    /**
     * Checks whether the current thread has a write access granted.
     * @return true if a write access is granted
     */
    public boolean isWriteAccess () {
        return mutex.isWriteAccess ();
    }

    /**
     * Notifies the manager to rollback the transaction at the end of the of write access.
     * The rollback is not asure to happen when a model was changed the way that cannot be undone.
     * <p>
     * Even through the rollback is performed, the created component will be still presented in the document
     * and the listener manager will fire an event with all changes. If it is rollback-able, then the old and new state in the DesignEvent will be the same.
     * <p>
     * Note: Use this method in corner cases only. It is not asured that the rollback happens or happens correctly.
     * <p>
     * Note: Supported undoable changes are Component.addComponent, Component.removeComponent, Component.writeProperty and Document.setSelectedComponents only.
     * <p>
     * Note: Rejecting operations are: DescriptorRegistry changed, Document.setRootComponent, custom UndoableEdit.
     */
    public void rollback () {
        assert assertEventAllowed;
        rollback = true;
        useUndoManager = false;
    }

    private void rollbackCore () {
        if (discardAllEdits) {
            ErrorManager.getDefault ().log (ErrorManager.ERROR, "Cannot rollback operation"); // NOI18N
            return;
        }
        // TODO - implement rollback
        ErrorManager.getDefault ().log (ErrorManager.ERROR, "Rollback is not supported"); // NOI18N
    }

    void rootChangeHappened (DesignComponent root) {
        assert Debug.isFriend (DesignDocument.class, "setRootComponent"); // NOI18N
        assert assertEventAllowed;
        listenerManager.addAffectedComponentHierarchy (root);
        discardAllEdits = true;
    }

    void componentDescriptorChangeHappened (DesignComponent component, Collection<? extends Presenter> presentersToRemove, Collection<Presenter> presentersToAdd, boolean useUndo) {
        assert Debug.isFriend (DesignComponent.class, "setComponentDescriptor"); // NOI18N
        assert assertEventAllowed;
        listenerManager.addComponentDescriptorChanged (component, presentersToRemove, presentersToAdd);
        if (useUndo)
            discardAllEdits = true;
    }

    void parentChangeHappened (DesignComponent previousParent, DesignComponent parent, DesignComponent child) {
        assert Debug.isFriend (DesignComponent.class, "addComponent")  || Debug.isFriend (DesignComponent.class, "removeComponent"); // NOI18N
        assert assertEventAllowed;
        listenerManager.addAffectedComponentHierarchy (previousParent);
        listenerManager.addAffectedComponentHierarchy (parent);
        listenerManager.addAffectedComponentHierarchy (child);
        undoableEditHappened (new SetParentEdit (previousParent, parent,  child));
    }

    void writePropertyHappened (DesignComponent component, String propertyName, PropertyValue oldValue, PropertyValue newValue) {
        assert Debug.isFriend (DesignComponent.class, "writeProperty"); // NOI18N
        assert assertEventAllowed;
        listenerManager.addAffectedDesignComponent (component, propertyName, oldValue);
        undoableEditHappened (new WritePropertyEdit (component, propertyName, oldValue, newValue));
    }

    void selectComponentsHappened (Collection<DesignComponent> oldSelection, Collection<DesignComponent> newSelection) {
        assert Debug.isFriend (DesignDocument.class, "setSelectedComponents"); // NOI18N
        assert assertEventAllowed;
        listenerManager.setSelectionChanged ();
        undoableEditHappened (new SelectionEdit (document, oldSelection, newSelection));
    }

    /**
     * Adds an undoable edit into a undo-redo queue.
     * <p>
     * Note: use this to add an additional undoable edit that cannot be produces by the model directly.
     * <p>
     * @param edit the edit; for whole edit instance lifecycle, it has to: edit.isSignificant must return false, edit.canUndo and edit.canRedo must return true, edit.undo and edit.redo must not throw any exception.
     */
    public void undoableEditHappened (UndoableEdit edit) {
        assert isWriteAccess ();
        assert ! edit.isSignificant ();
        if (transactionEdit == null)
            transactionEdit = new TransactionEdit ();
        transactionEdit.addEdit (edit);
    }

    private class TransactionEdit extends CompoundEdit {

        @Override
        public boolean isSignificant () {
            return true;
        }

        @Override
        public void undo () throws CannotUndoException {
            final boolean[] error = new boolean[1];
            writeAccess (new Runnable () {
                public void run () {
                    useUndoManager = false;
                    try {
                        TransactionEdit.super.undo ();
                    } catch (CannotUndoException e) {
                        error[0] = true;
                        ErrorManager.getDefault ().notify (ErrorManager.ERROR, e);
                    }
                }
            });
            if (error[0])
                throw new CannotUndoException ();
        }

        @Override
        public void redo () throws CannotRedoException {
            final boolean[] error = new boolean[1];
            writeAccess (new Runnable() {
                public void run () {
                    useUndoManager = false;
                    try {
                        TransactionEdit.super.redo ();
                    } catch (CannotRedoException e) {
                        error[0] = true;
                        ErrorManager.getDefault ().notify (ErrorManager.ERROR, e);
                    }
                }
            });
            if (error[0])
                throw new CannotRedoException ();
        }

    }

    public class SetParentEdit extends AbstractUndoableEdit {

        private DesignComponent oldParent;
        private DesignComponent newParent;
        private DesignComponent child;

        public SetParentEdit (DesignComponent oldParent, DesignComponent newParent, DesignComponent child) {
            this.oldParent = oldParent;
            this.newParent = newParent;
            this.child = child;
        }

        @Override
        public boolean isSignificant () {
            return false;
        }

        @Override
        public void undo () throws CannotUndoException {
            super.undo ();
            if (newParent != null)
                newParent.removeComponent (child);
            if (oldParent != null)
                oldParent.addComponent (child);
        }

        @Override
        public void redo () throws CannotRedoException {
            super.redo ();
            if (oldParent != null)
                oldParent.removeComponent (child);
            if (newParent != null)
                newParent.addComponent (child);
        }

    }

    public class WritePropertyEdit extends AbstractUndoableEdit {

        private DesignComponent component;
        private String propertyName;
        private PropertyValue oldValue;
        private PropertyValue newValue;

        public WritePropertyEdit (DesignComponent component, String propertyName, PropertyValue oldValue, PropertyValue newValue) {
            this.component = component;
            this.propertyName = propertyName;
            this.oldValue = oldValue;
            this.newValue = newValue;
        }

        @Override
        public boolean isSignificant () {
            return false;
        }

        @Override
        public void undo () throws CannotUndoException {
            super.undo ();
            component.writeProperty (propertyName, oldValue);
        }

        @Override
        public void redo () throws CannotRedoException {
            super.redo ();
            component.writeProperty (propertyName, newValue);
        }

    }

    public class SelectionEdit extends AbstractUndoableEdit {

        private DesignDocument document;
        private Collection<DesignComponent> oldSelection;
        private Collection<DesignComponent> newSelection;

        public SelectionEdit (DesignDocument document, Collection<DesignComponent> oldSelection, Collection<DesignComponent> newSelection) {
            this.document = document;
            this.oldSelection = oldSelection;
            this.newSelection = newSelection;
        }

        @Override
        public boolean isSignificant () {
            return false;
        }

        @Override
        public void undo () throws CannotUndoException {
            super.undo ();
            document.setSelectedComponents (null, oldSelection);
        }

        @Override
        public void redo () throws CannotRedoException {
            super.redo ();
            document.setSelectedComponents (null, newSelection);
        }

    }

}
