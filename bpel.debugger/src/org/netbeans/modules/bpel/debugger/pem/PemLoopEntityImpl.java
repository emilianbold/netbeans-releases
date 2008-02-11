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

package org.netbeans.modules.bpel.debugger.pem;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.bpel.debugger.api.psm.PsmEntity;

/**
 *
 * @author Alexander Zgursky
 */
public class PemLoopEntityImpl extends PemEntityImpl {
    private List<PemEntityImpl> myChildren;
    
    /** Creates a new instance of PemLoopEntity */
    protected PemLoopEntityImpl(ProcessExecutionModelImpl model,
            PsmEntity psmEntity, String branchId, boolean isReceivingEvents)
    {
        super(model, psmEntity, branchId, isReceivingEvents);
    }
    
    public PemEntityImpl[] getChildren() {
        if (myChildren != null) {
            return myChildren.toArray(new PemEntityImpl[myChildren.size()]);
        } else {
            return new PemEntityImpl[0];
        }
    }
    
    public int getChildrenCount() {
        if (myChildren != null) {
            return myChildren.size();
        } else {
            return 0;
        }
    }

    public boolean hasChildren() {
        return myChildren != null;
    }

    public PemEntityImpl[] getChildren(PsmEntity psmEntity) {
        if (psmEntity != getPsmEntity().getLoopChild()) {
            return new PemEntityImpl[0];
        } else {
            return getChildren();
        }
    }

    public int getChildrenCount(PsmEntity psmEntity) {
        if (psmEntity != getPsmEntity().getLoopChild()) {
            return 0;
        } else {
            return getChildrenCount();
        }
    }

    public boolean hasChildren(PsmEntity psmEntity) {
        if (psmEntity != getPsmEntity().getLoopChild()) {
            return false;
        } else {
            return hasChildren();
        }
    }
    
    protected void addChild(PemEntityImpl child) {
        if (myChildren == null) {
            myChildren = new ArrayList<PemEntityImpl>();
        }
        
        myChildren.add(child);
        child.setParent(this);
        child.setIndex(myChildren.size());
    }
}
