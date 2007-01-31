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
import org.netbeans.modules.cnd.apt.structure.APTConditionsBlock;

/**
 * implementation of preprocessor conditions block container
 * @author Vladimir Voskresensky
 */
public final class APTConditionsBlockNode extends APTContainerNode 
                                        implements APTConditionsBlock, 
                                        Serializable {
    private static final long serialVersionUID = 2285405035404696441L;
    transient private APT next;
    
    /** Copy constructor */
    /**package*/APTConditionsBlockNode(APTConditionsBlockNode orig) {
        super(orig);
        // clear tree structure information
        this.next = null;
    }
    
    /**
     * Creates a new instance of APTConditionsBlockNode
     */
    public APTConditionsBlockNode() {
    }

    public final int getType() {
        return APT.Type.CONDITION_CONTAINER;
    }
    
    public int getOffset() {
        return -1;
    }

    public int getEndOffset() {
        return -1;
    }
    
    public APT getNextSibling() {
        return next;
    }

    public String getText() {
        return "preprocessor condition branches container"; // NOI18N
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // implementation details

    public final void setNextSibling(APT next) {
        assert (next != null) : "null sibling, what for?"; // NOI18N
        assert (this.next == null) : "why do you change immutable APT?"; // NOI18N
        this.next = next;
    }
}
