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

/*
 * CasaNodeChildren.java
 *
 * Created on November 2, 2006, 9:21 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.compapp.casaeditor.nodes;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.compapp.casaeditor.CasaDataObject;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaComponent;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaModel;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaWrapperModel;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 *
 * @author Josh
 */
public abstract class CasaNodeChildren extends Children.Keys {
    
    protected Lookup mLookup;
    private WeakReference mDataReference;
    
    
    public CasaNodeChildren(Object data, Lookup lookup) {
        super();
        mDataReference = new WeakReference(data);
        mLookup = lookup;
        reloadFrom(data);
    }
    
    
    protected abstract Node[] createNodes(Object key);
    
    
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
    
    protected CasaWrapperModel getCasaModel(CasaComponent comp) {
        CasaDataObject dataObject = (CasaDataObject) getDataObject(comp);
        if (dataObject != null) {
            return dataObject.getEditorSupport().getModel();
        }
        return null;
    }
    
    public Object getChildKeys(Object data) {
        List<CasaComponent> children = null;
        if (data instanceof CasaComponent) {
            children = ((CasaComponent) data).getChildren();
        } else if (data instanceof List) {
            children = (List<CasaComponent>) data;
        }
        return children;
    }
    
    public void reloadFrom(Object data)  {
        if (data == null) {
            return;
        }
        Object children = getChildKeys(data);
        if (children instanceof Collection) {
            setKeys((Collection) children);
        } else if (children instanceof Object[]) {
            setKeys((Object[]) children);
        } else {
            setKeys(Collections.emptyList());
        }
    }
    
    private static DataObject getDataObject(CasaComponent comp) {
        try {
            CasaModel model = comp.getModel();
            if (model != null) {
                FileObject fobj = (FileObject) model.getModelSource().
                        getLookup().lookup(FileObject.class);
                if (fobj != null) {
                    return DataObject.find(fobj);
                }
            }
        } catch (DataObjectNotFoundException donfe) {
            // fall through to return null
        }
        return null;
    }
}
