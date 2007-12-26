/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
import java.util.Iterator;
import org.netbeans.modules.cnd.api.model.util.WeakList;
import org.openide.util.Lookup;

/**
 * utility class to access Csm model
 * @author Vladimir Voskresensky
 */
public final class CsmModelAccessor {

    public interface CsmModelEx extends CsmModel {
	public Iterator<CsmModelStateListener> getModelStateListeners();
	public Iterator<CsmProgressListener> getProgressListeners();
	public Iterator<CsmModelListener> getModelListeners();
    }
    
    // singleton instance of model
    private static CsmModel model;
    private static CsmModel dummy;
    
    private static WeakList<CsmModelStateListener> modelStateListeners = new WeakList<CsmModelStateListener>();
    private static WeakList<CsmModelListener> modelListeners = new WeakList<CsmModelListener>();
    private static WeakList<CsmProgressListener> progressListeners = new WeakList<CsmProgressListener>();
    
    private static CsmModelStateListener stateListener = new CsmModelStateListener() {
        public void modelStateChanged(CsmModelState newState, CsmModelState oldState) {
            if( newState == CsmModelState.UNLOADED ) {
                model.removeModelStateListener(stateListener);
		saveListeners();
                model = null;
            }
        }
    };
    
    private static void saveListeners() {
	
	if( model instanceof CsmModelEx ) {
	    
	    CsmModelEx modelEx = (CsmModelEx) model;
	    
	    modelStateListeners.clear();
	    modelStateListeners.addAll(modelEx.getModelStateListeners());
	    
	    modelListeners.clear();
	    modelListeners.addAll(modelEx.getModelListeners());
	    
	    progressListeners.clear();
	    progressListeners.addAll(modelEx.getProgressListeners());
	}
    }
    
    private static void restoreListeners() {
	
	for( CsmProgressListener l : progressListeners ) {
	    model.addProgressListener(l);
	}
	progressListeners.clear();
	
	for( CsmModelListener l : modelListeners ) {
	    model.addModelListener(l);
	}
	modelListeners.clear();
	
	for( CsmProgressListener l : progressListeners ) {
	    model.addProgressListener(l);
	}
    }
    
    private static class ModelStub implements CsmModel {
	
	public Collection<CsmProject> projects() {
	    return Collections.<CsmProject>emptyList();
	}
	
	public CsmProject getProject(Object id) {
	    return null;
	}
	
	public CsmFile findFile(CharSequence absPath) {
	    return null;
	}
        
        public CsmModelState getState() {
            return CsmModelState.UNLOADED;
        }

	public void enqueue(Runnable task) {}
	
	public void removeModelListener(CsmModelListener listener) {}
	
	public void addModelListener(CsmModelListener listener) {}
	
	
	public void removeProgressListener(CsmProgressListener listener) {}
	
	public void addProgressListener(CsmProgressListener listener) {}
	
	public void enqueue(Runnable task, CharSequence name) {}

        public void removeModelStateListener(CsmModelStateListener listener) {}

        public void addModelStateListener(CsmModelStateListener listener) {}
    }
    
    /** Creates a new instance of CsmModelAccessor */
    private CsmModelAccessor() {
    }

    /**
     * Gets CsmModel using Lookup
     */
    public static CsmModel getModel() {
        if( model == null ) {
            synchronized(CsmModel.class ) {
                if( model == null ) {
                    model = (CsmModel) Lookup.getDefault().lookup(CsmModel.class);
		    if( model == null ) {
			return getStub();
		    }
                    else {
                        model.addModelStateListener(stateListener);
			restoreListeners();
                    }
                }
            }
        }
        return model;
    }    

    private static CsmModel getStub() {
	if( dummy == null ) {
	    dummy = new ModelStub();
	}
	return dummy;
    }
}
