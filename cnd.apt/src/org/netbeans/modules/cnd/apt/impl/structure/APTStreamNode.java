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
import org.netbeans.modules.cnd.apt.structure.APTStream;
import org.netbeans.modules.cnd.apt.utils.APTUtils;

/**
 * implementation of APTStream
 * @author Vladimir Voskresensky
 */
public final class APTStreamNode extends APTStreamBaseNode 
                                    implements APTStream, Serializable {
    private static final long serialVersionUID = -6247236916195389448L;
    
    /** Copy constructor */
    /**package*/ APTStreamNode(APTStreamNode orig) {
        super(orig);
        assert (false) : "are you sure it's correct to make copy of stream node?"; // NOI18N
    }
    
    /** Constructor for serialization **/
    protected APTStreamNode() {
    }
    
    /** Creates a new instance of APTStreamNode */
    public APTStreamNode(Token token) {
        super(token);
        assert (validToken(token)) : "must init only from valid tokens"; // NOI18N
    }
    
    public final int getType() {
        return APT.Type.TOKEN_STREAM;
    }    
    
    protected boolean validToken(Token t) {
        if (t == null) {
            return false;
        }
        int ttype = t.getType();
        assert (!APTUtils.isEOF(ttype)) : "EOF must be handled in callers"; // NOI18N
        return !APTUtils.isPreprocessorToken(ttype);
    }    
}
