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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.completion.impl.xref;

import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.completion.cplusplus.utils.Token;

/**
 *
 * @author Vladimir Voskresensky
 */
public class ReferenceImpl extends DocOffsetableImpl implements CsmReference {
    private final Token token;
    private CsmObject target = null;
    private CsmObject owner = null;
    private final int offset;
    
    public ReferenceImpl(CsmFile file, BaseDocument doc, int offset, Token token) {
        super(doc, file, offset);
        this.token = token;
        this.offset = offset;
    }

    public CsmObject getReferencedObject() {
        if (target == null) {
            target = ReferencesSupport.findReferencedObject(super.getContainingFile(), super.getDocument(), this.offset, token);
        }
        return target;
    }

    public CsmObject getOwner() {
        if (owner == null) {
            owner = ReferencesSupport.findOwnerObject(super.getContainingFile(), super.getDocument(), this.offset, token);
        }
        return owner;
    }

    public String getText() {
        return token.getText();
    }
    
    public String toString() {
        return "'" + org.netbeans.editor.EditorDebug.debugString(getText()) // NOI18N
               + "', tokenID=" + this.token.getTokenID() 
               + ", offset=" + this.offset + " [" + super.getStartPosition() + "-" + super.getEndPosition() + "]"; // NOI18N
    }    
}
