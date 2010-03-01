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
package org.netbeans.modules.cnd.api.model;

import java.util.Collection;
import java.util.Collections;
import org.openide.util.Cancellable;
import org.openide.util.Lookup;

/**
 * utility class to access Csm model
 * @author Vladimir Voskresensky
 */
public final class CsmModelAccessor {

    // singleton instance of model
    private static CsmModel model;
    private static CsmModel dummy;
    private static CsmModelStateListener stateListener = new CsmModelStateListener() {

        public void modelStateChanged(CsmModelState newState, CsmModelState oldState) {
            if (newState == CsmModelState.OFF) {
                CsmListeners.getDefault().removeModelStateListener(stateListener);
                model = null;
            }
        }
    };

    public static CsmModelState getModelState() {
        CsmModel aModel = model;
        return (aModel == null) ? CsmModelState.OFF : aModel.getState();
    }

    private static class ModelStub implements CsmModel {

        public Collection<CsmProject> projects() {
            return Collections.<CsmProject>emptyList();
        }

        public CsmProject getProject(Object id) {
            return null;
        }

        public CsmFile findFile(CharSequence absPath, boolean snapShot) {
            return null;
        }

        public CsmModelState getState() {
            return CsmModelState.OFF;
        }

        public Cancellable enqueue(Runnable task, CharSequence name) {
            return cancellableStub;
        }
        
        public void scheduleReparse(Collection<CsmProject> projects) {
        }
    }
    private static final Cancellable cancellableStub = new Cancellable() {

        public boolean cancel() {
            return true;
        }
    };

    /** Creates a new instance of CsmModelAccessor */
    private CsmModelAccessor() {
    }
    private static final boolean TRACE_GET_MODEL = Boolean.getBoolean("trace.get.model");

    /**
     * Gets CsmModel using Lookup
     */
    public static CsmModel getModel() {
        if (TRACE_GET_MODEL) {
            Thread.dumpStack();
        }
        if (model == null) {
            synchronized (CsmModel.class) {
                if (model == null) {
                    model = Lookup.getDefault().lookup(CsmModel.class);
                    if (model == null) {
                        return getStub();
                    } else {
                        CsmListeners.getDefault().addModelStateListener(stateListener);
                    }
                }
            }
        }
        return model;
    }

    private static CsmModel getStub() {
        if (dummy == null) {
            dummy = new ModelStub();
        }
        return dummy;
    }
}
