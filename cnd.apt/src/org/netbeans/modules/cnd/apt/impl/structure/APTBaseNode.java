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

package org.netbeans.modules.cnd.apt.impl.structure;

import java.io.Serializable;
import org.netbeans.modules.cnd.apt.structure.APT;

/**
 * base APT node impl
 * @author Vladimir Voskresensky
 */
public abstract class APTBaseNode implements APT, Serializable {
    private static final long serialVersionUID = -7790789617759717719L;
    
    /** Copy constructor */
    /**package*/APTBaseNode(APTBaseNode orig) {
    }
    
    /** Creates a new instance of APTBaseNode */
    protected APTBaseNode() {
    }
    
    public abstract int getType();
    public abstract int getOffset();
    public abstract int getEndOffset();
    
    public void dispose() {
        // do nothing
    }
    
    public String toString() {
        return getText();
    }

    /**Add a node to the end of the child list for this node */
    protected final void addChild(APT node) {
        if (node == null) {
            return;
        }
        APT t = getFirstChild();
        if (t != null) {
            while (t.getNextSibling() != null) {
                t = t.getNextSibling();
            }
            ((APTBaseNode)t).setNextSibling(node);
        } else {
            setFirstChild(node);
        }
    }     
    
    ////////////////////////////////////////////////////////////////////////////
    // implementation details
    
    /** 
     * sets next sibling element
     */
    public abstract void setNextSibling(APT next);
    
    /** 
     * sets first child element
     */
    public abstract void setFirstChild(APT child);        
}
