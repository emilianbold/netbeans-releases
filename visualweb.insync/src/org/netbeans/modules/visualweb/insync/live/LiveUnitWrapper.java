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
package org.netbeans.modules.visualweb.insync.live;

import java.io.PrintWriter;

import org.openide.filesystems.FileObject;

import org.netbeans.modules.visualweb.insync.ParserAnnotation;
import org.netbeans.modules.visualweb.insync.UndoEvent;
import org.netbeans.modules.visualweb.insync.Unit;
import org.netbeans.modules.visualweb.insync.beans.BeansUnit;
import org.netbeans.modules.visualweb.insync.models.FacesModel;

/**
 * This is a class that wrapps a LiveUnit.  This wrapper is to allow FacesModel to delay
 * the resurrect operation on a LiveUnit.  Since it would be very onerous to change
 * every method in LiveUnit to ensure a resurrect had occured prior to computing there
 * result, I create a wrapper class which holds the actual LiveUnit, and attempts to
 * delay the requirement of the resurect for as long as possible.
 * We lazy initialize liveUnit due to issue with possible circular sync issue
 * Image that a new ModelSet is instantiated.  This model set will have sync all of its
 * units on creation.  The process of syncing used to created the LiveUnit right then
 * and there.  Well, creating a LiveUnit ends up create LiveProperty's that have their
 * value initialized.  At design time, there is a DesignTimeVariableResolver which turns
 * around and does a sync on the models its will attempt to resolve with.
 * Danger Will Robinson, by definition one of those models is the one in process of
 * syncing !!!  I have also added a check in Model.sync() to make sure we detect
 * these circular sync issues in the future.
 *
 * TODO
 * More work needs to be done to make this class thread safe, however there are a number
 * of issues with thread safety ness already within insync, that we can tackle this problem
 * then in a more general fashion.
 *
 * @author eric
 *
 */
public class LiveUnitWrapper implements Unit {
    protected FacesModel model;
    protected BeansUnit sourceUnit;
    protected FileObject file;
    protected LiveUnit liveUnit;

    public LiveUnitWrapper(FacesModel model, BeansUnit sourceUnit, FileObject file) {
        this.model = model;
        this.sourceUnit = sourceUnit;
        this.file = file;
    }

    public void destroy() {
        if (liveUnit != null) {
            liveUnit.destroy();
            liveUnit = null;
        }
        liveUnit = null;
    }

    public void dumpTo(PrintWriter w) {
        if (liveUnit == null)
            return;
        liveUnit.dumpTo(w);
    }

    protected LiveUnit getCurrentLiveUnit() {
        return liveUnit;
    }

    public ParserAnnotation[] getErrors() {
        return sourceUnit.getErrors();
    }

    public LiveUnit getLiveUnit() {
        if (liveUnit == null) {
            liveUnit = new LiveUnit(model, sourceUnit, file);
        }
        liveUnit.sync();
        return liveUnit;
    }

    public State getState() {
        return sourceUnit.getState();
    }

    public boolean isLiveUnitInstantiated() {
        return liveUnit != null;
    }

    public boolean isWriteLocked() {
        if (liveUnit == null)
            return sourceUnit.isWriteLocked();
        return liveUnit.isWriteLocked();
    }

    public void readLock() {
        if (liveUnit == null) {
            sourceUnit.readLock();
            return;
        }
        liveUnit.readLock();
    }

    public void readUnlock() {
        if (liveUnit == null) {
            sourceUnit.readUnlock();
            return;
        }
        liveUnit.readUnlock();
    }

    public boolean sync() {
        boolean synced;
        if (liveUnit == null)
            synced = sourceUnit.sync();
        else
            synced = liveUnit.sync();
        return synced;
    }

    public void writeLock(UndoEvent event) {
        if (liveUnit == null) {
            sourceUnit.writeLock(event);
            return;
        }
        liveUnit.writeLock(event);
    }

    public boolean writeUnlock(UndoEvent event) {
        if (liveUnit == null)
            return sourceUnit.writeUnlock(event);
        return liveUnit.writeUnlock(event);
    }

}
