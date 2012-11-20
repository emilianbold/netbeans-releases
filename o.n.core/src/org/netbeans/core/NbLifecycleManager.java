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

package org.netbeans.core;

import java.awt.EventQueue;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.core.startup.ModuleLifecycleManager;
import org.openide.DialogDisplayer;
import org.openide.LifecycleManager;
import org.openide.NotifyDescriptor;
import org.openide.awt.StatusDisplayer;
import org.openide.cookies.SaveCookie;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * Default implementation of the lifecycle manager interface that knows
 * how to save all modified DataObject's, and to exit the IDE safely.
 */
@ServiceProvider(service=LifecycleManager.class, supersedes="org.netbeans.core.startup.ModuleLifecycleManager")
public final class NbLifecycleManager extends LifecycleManager {
    private final CountDownLatch onExit = new CountDownLatch(1);
    
    @Override
    public void saveAll() {
        ArrayList<DataObject> bad = new ArrayList<DataObject>();
        DataObject[] modifs = DataObject.getRegistry().getModified();
        if (modifs.length == 0) {
            // Do not show MSG_AllSaved
            return;
        }
        for (DataObject dobj : modifs) {
            try {
                SaveCookie sc = dobj.getLookup().lookup(SaveCookie.class);
                if (sc != null) {
                    StatusDisplayer.getDefault().setStatusText(
                            NbBundle.getMessage(NbLifecycleManager.class, "CTL_FMT_SavingMessage", dobj.getName()));
                    sc.save();
                }
            } catch (IOException ex) {
                Logger.getLogger(NbLifecycleManager.class.getName()).log(Level.WARNING, null, ex);
                bad.add(dobj);
            }
        }
        NotifyDescriptor descriptor;
        //recode this part to show only one dialog?
        for (DataObject badDO : bad) {
            descriptor = new NotifyDescriptor.Message(
                    NbBundle.getMessage(NbLifecycleManager.class, "CTL_Cannot_save", badDO.getPrimaryFile().getName()));
            DialogDisplayer.getDefault().notify(descriptor);
        }
        // notify user that everything is done
        StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(NbLifecycleManager.class, "MSG_AllSaved"));
    }

    @Override
    public void exit() {
        // #37160 So there is avoided potential clash between hiding GUI in AWT
        // and accessing AWTTreeLock from saving routines (winsys).
        exit(0);
    }

    @Override
    public void exit(int status) {
        NbLifeExit action = new NbLifeExit(0, status, onExit);
        Mutex.EVENT.readAccess(action);
        if (!EventQueue.isDispatchThread()) {
            try {
                onExit.await();
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
    
    public static boolean isExiting() {
        return NbLifeExit.isExiting();
    }

    @Override
    public void markForRestart() throws UnsupportedOperationException {
        new ModuleLifecycleManager().markForRestart();
    }
}
