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
