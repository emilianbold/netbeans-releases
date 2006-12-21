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

package org.netbeans.modules.cnd.modelutil;

import java.lang.ref.WeakReference;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.openide.nodes.Children;
import org.openide.util.Lookup;

/**
 *
 * @author vk155633
 */
public class CsmNode extends AbstractCsmNode {
    
    private WeakReference<CsmObject> data = null;
    
    public CsmNode(Children children, Lookup lookup) {
        super(children, lookup);
    }

    public CsmNode(Children children) {
        this(children, null);
    }

    public void setData(CsmObject data) {
	if( data == null ) {
	    this.data = null;
	}
	else {
	    this.data = new WeakReference(data);
	}
    }
    
    public CsmObject getCsmObject() {
	return (data == null) ? null : data.get();
    }

}
