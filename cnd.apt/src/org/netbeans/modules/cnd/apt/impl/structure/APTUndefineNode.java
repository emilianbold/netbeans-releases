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
import org.netbeans.modules.cnd.apt.structure.APTUndefine;

/**
 * #undef directive implementation
 * @author Vladimir Voskresensky
 */
public final class APTUndefineNode extends APTMacroBaseNode 
                                    implements APTUndefine, Serializable {
    private static final long serialVersionUID = 3929923839413486096L;
    
    /** Copy constructor */
    /**package*/ APTUndefineNode(APTUndefineNode orig) {
        super(orig);
    }
    
    /** constructor for serialization **/
    protected APTUndefineNode() {
    }
    
    /** Creates a new instance of APTUndefineNode */
    public APTUndefineNode(Token token) {
        super(token);
    }
    
    public final int getType() {
        return APT.Type.UNDEF;
    }    
}
