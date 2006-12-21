/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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
public class CsmModelAccessor {

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
	
	public Collection projects() {
	    return Collections.EMPTY_LIST;
	}
	
	public CsmProject getProject(Object id) {
	    return null;
	}
	
	public CsmFile findFile(String absPath) {
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
	
	public void enqueue(Runnable task, String name) {}

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
