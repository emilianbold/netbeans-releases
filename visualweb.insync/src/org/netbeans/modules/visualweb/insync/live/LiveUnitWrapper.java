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
            liveUnit.sync();
        }
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
