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

import antlr.Token;
import java.io.Serializable;
import org.netbeans.modules.cnd.apt.structure.APT;
import org.netbeans.modules.cnd.apt.structure.APTIf;

/**
 * #if directive implementation
 * @author Vladimir Voskresensky
 */
public final class APTIfNode extends APTIfConditionBaseNode 
                            implements APTIf, Serializable {
    private static final long serialVersionUID = 5865452252000352917L;
    
    /** Copy constructor */
    /**package*/ APTIfNode(APTIfNode orig) {
        super(orig);
    }
    
    /** Constructor for serialization */
    protected APTIfNode() {
    }
    
    /** Creates a new instance of APTIfNode */
    public APTIfNode(Token token) {
        super(token);
    }
    
    public final int getType() {
        return APT.Type.IF;
    }
}
