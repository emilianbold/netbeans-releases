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

package org.netbeans.modules.compapp.casaeditor.nodes;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaComponent;
import org.openide.nodes.Children;

/**
 *
 * @author Josh Sandusky
 */
public abstract class CasaNodeChildren<T> extends Children.Keys<T> {
    
    protected CasaNodeFactory mNodeFactory;
    private WeakReference mDataReference;
    private Object mHardInitializationReference;
    
    
    public CasaNodeChildren(Object data, CasaNodeFactory factory) {
        super();
        mNodeFactory = factory;
        
        // for casual data references, use a weak reference which allows our
        // reference to be garbage collected when no longer needed
        mDataReference = new WeakReference<Object>(data);
        
        // for initialization purposes, use a hard reference. we manually
        // control when this reference is set to null. initialization needs
        // a preserved handle to the data, even though the model may not.
        mHardInitializationReference = data;
    }
    
    
    protected Object getData() {
        if (mDataReference != null) {
            Object ref = mDataReference.get();
            if (ref instanceof CasaComponent) {
                if (!((CasaComponent) ref).isInDocumentModel()) {
                    return null;
                }
            }
            return ref;
        }
        return null;
    }
    
    public Object getChildKeys(Object data) {
        List children = null;
        if (data instanceof CasaComponent) {
            children = ((CasaComponent) data).getChildren();
        } else if (data instanceof List) {
            children = (List) data;
        }
        return children;
    }

    @Override
    protected void addNotify() {
        super.addNotify();
        
        // lazy initialization
        initialize();
    }
    
    @Override
    protected void removeNotify() {
        super.removeNotify();
        
        mHardInitializationReference = null;
    }
    
    private void initialize()  {
        Object data = mHardInitializationReference;
        if (data == null) {
            return;
        }
        Object children = getChildKeys(data);
        if (children instanceof Collection) {
            setKeys((Collection<T>) children);
        } else if (children instanceof Object[]) {
            T[] x = (T[]) children; // ?
            setKeys(x);
        } else {
            List<T> keys = Collections.emptyList();
            setKeys(keys);
        }
        // We initialized, so we don't need the initialization reference anymore.
        mHardInitializationReference = null;
    }
}
