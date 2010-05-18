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
package org.netbeans.modules.visualweb.insync;

import org.netbeans.api.project.Project;
import org.openide.filesystems.FileObject;

import org.netbeans.modules.visualweb.project.jsf.api.JsfProjectUtils;
//NB60 import org.netbeans.modules.visualweb.insync.faces.refactoring.MdrInSyncSynchronizer;

/**
 * General Model abstraction. A Model is a wrapper for one or more Units and serves to bootstrap the
 * units and coordinate them with Project and its Items.
 *
 * @author cquinn
 */
public abstract class Model implements SourceUnitListener {

    public static final Model[] EMPTY_ARRAY = {};

    // EAT! TODO
    // We need to do some work on straightening out this circular sync issue, but
    // not time now :(
    public static final boolean CHECK_CIRCULAR_SYNC = false;

    // For debugging, capturing who is doing the sync
    protected Exception syncInProgress;

    /**
     * Manager tracking source unit updates for this model and coordinating undo/redo operations.
     */
    protected UndoManager undoManager = new UndoManager();

    /**
     */
    public interface Factory {

        /**
         * Possibly create an instance of the associated model iff it would be appropriate for the
         * given file object.
         *
         * @param set Owning ModelSet
         * @param file FileObject to create Model for
         * @return The new Model or null if N/A
         */
        public Model newInstance(ModelSet set, FileObject file);
    }

    protected static Model getInstance(FileObject file, Class modelSetType, boolean forceCreate) {
        ModelSet models = ModelSet.getInstance(file, modelSetType);
        if (models != null)
            return models.getModel(file);
        return null;
    }

    protected static Model getModel(FileObject file) {
        ModelSet modelSet = ModelSet.getModelSet(file);
        if(modelSet != null) {
            return modelSet.getModel(file);
        }
        return null;
    }

    //--------------------------------------------------------------------------------- Construction

    /** NB and Project coordination */
    protected ModelSet owner;
    protected FileObject file;

    /**
     * Construct a new model under a given owner and for a given NB file
     *
     * @param owner
     * @param file
     */
    protected Model(ModelSet owner, FileObject file) {
        assert file != null;
        this.owner = owner;
        this.file = file;
    }

    /**
     * Destroy this model causing it to clean up all its references. Called when the project is
     * closing, or the underlying file is deleted. Models must never be used after destroy is
     * called.
     */
    public void destroy() {
        owner = null;
        file = null;
    }

    /**
     * ONLY called when my parent is being destroy'ed, in order to make sure there are no contexts
     * active while client views are updating during event notification of models being destroyed.
     *
     */
    public void resetOwner() {
        owner = null;
    }

    //------------------------------------------------------------------------------------ Accessors

    /**
     * Get the owning ModelSet for this Model.
     * @return the owning ModelSet for this Model.
     */
    public ModelSet getOwner() {
        return owner;
    }

    public abstract ParserAnnotation[] getErrors();

    /**
     * Get the primary FileObject that this Model is abstracting.
     *
     * @return the primary FileObject that this Model is abstracting
     */
    public FileObject getFile() {
        return file;
    }

    /**
     * Get the undo manager associated with this model.
     *
     * @return the undo manager associated with this model
     */
    public UndoManager getUndoManager() {
        return undoManager;
    }

    //----------------------------------------------------------------- Project & DataObject Helpers

    public Project getProject() {
        if (owner == null)
            return null;
        return owner.getProject();
    }

    /**
     * Get the folder within the owning Project that holds the web source files.
     * 
     * @return the web source folder. 
     */
    public FileObject getWebFolder() {
        return JsfProjectUtils.getDocumentRoot(getProject());
    }

    /**
     * Lock the entire model for write access. 
     * @param description The human readable description of the operation to be performed during
     *            lock.
     * @return The undo event object that will be populated with state needed to undo what is done
     *         during the lock.
     */
    public abstract UndoEvent writeLock(String description);

    /**
     * Unlock a previously locked model. Unlock should <b>always </b> be called exactly once after
     * lock, and so should be placed in a finally block.
     * 
     * @param event The undo event object that was returned from writeLock().
     */
    public abstract void writeUnlock(UndoEvent event);

    /**
     * @return
     */
    public abstract boolean isWriteLocked();

    protected void beginMdrTransation() {
    }
    
    protected void endMdrTransaction() {
    }

    /**
     * Cause the model to synchronize its state with the underlying source buffers as needed. Will
     * do nothing if the model is already in a sync'd state.
     */
    public void sync() {
        syncImpl();
    }
    
    protected abstract void syncImpl();

    public void fireModelChanged() {
        getOwner().fireModelChanged(this);
    }
    
    public abstract void saveUnits();
    
    /**
     * Cause the model to flush any model changes down to the underlying souce buffers as needed.
     * Will do nothing if the source buffers are already up to date.
     * 
     * @see writeLock,writeUnlock for the prefered way to commit changes to source.
     */
    public void flush() {
        flushImpl();
    }
    
    public abstract void flushImpl();

    /**
     * Should only be used if you are certain this will not cause a problem.
     * At moment this is only used by refactoring mechanism.
     * See the caller in ModelSet.plannedChange for more information.
     *
     */
    public void flushNonJavaUnits() {
        flushNonJavaUnitsImpl();
    }
    
    public abstract void flushNonJavaUnitsImpl();
    /**
     * Called when a file in the project is renamed by the user. Model implementations should handle
     * this by updating all internal references.
     * 
     * @param oldName  The old name of the renamed file.
     * @param newName  The new name of the renamed file.
     * @param ext  The file extension which can be used to determine the type of file.
     * @param file  The actual NB file object.
     * @param remove Indicates if the model needs to be removed
     */
    public void fileRenamed(String oldName, String newName, String ext, FileObject file, boolean remove) {
    }
    
    /**
     * Provide for an easier string to be displayed when this object is seen in an object inspector.
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("["); // NOI18N
        toString(buffer);
        buffer.append("]"); // NOI18N
        return super.toString() + buffer.toString();
    }
    
    /**
     * Subclasses should override this to add extra information desired for the string to be displayed when this object is seen in an object inspector.
     * @param buffer
     */
    public void toString(StringBuffer buffer) {
//        buffer.append(getClass().getName().substring(getClass().getPackage().getName().length() + 1));
        if (file == null)
            buffer.append("null"); // NOI18N
        else {
            buffer.append(" file: ");
            buffer.append(file.getNameExt());
        }
    }

    public void sourceUnitSaved(final SourceUnit unit) {
    }
    
    public void sourceUnitModelDirtied(SourceUnit unit) {
    }

    protected  boolean needSyncing = true;

    public void sourceUnitSourceDirtied(SourceUnit unit) {
        if(isValid()) {
            needSyncing = true;
            getOwner().addToModelsToSync(this);
        }
    }
    
    /**
     * Return true ONLY if I have an owner and all my file objects are valid.
     * I had to add this in order to handle issues with threading, queue on different threads,
     * during refactoring.  We can run into a case where a file has been removed, but the
     * model is still around.  This is not a good situation, so now we check for isValid.
     * 
     * @return
     */
    public boolean isValid() {
        if (getOwner() == null)
            return false;
        return true;
    }
    
}
