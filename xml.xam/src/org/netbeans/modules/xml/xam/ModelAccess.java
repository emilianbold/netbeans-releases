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

package org.netbeans.modules.xml.xam;

import java.io.IOException;
import javax.swing.event.UndoableEditListener;

/**
 * Access to the underlying structure of the model.
 *
 * @author Nam Nguyen
 */

public abstract class ModelAccess {
    
    public abstract void addUndoableEditListener(UndoableEditListener listener);
    public abstract void removeUndoableEditListener(UndoableEditListener listener);
    
    public abstract void prepareForUndoRedo();
    public abstract void finishUndoRedo();
    
    public void prepareSync() { }
    public abstract Model.State sync() throws IOException;
    
    public abstract void flush();


    private boolean autoSync = true;
    public boolean isAutoSync() {
        return autoSync;
    }
    public void setAutoSync(boolean value) {
        autoSync = value;
    }
    
    /**
     * Returns length in milliseconds since last edit if the model source buffer 
     * is dirty, or 0 if the model source is not dirty.  Class of domain model 
     * implementations should provide override.
     */
    public long dirtyIntervalMillis() {
        return 0;
    }
    /**
     * Unset mark for dirty source buffer.
     */
    public void unsetDirty() {
        // subclass need to override
    }
    
}
