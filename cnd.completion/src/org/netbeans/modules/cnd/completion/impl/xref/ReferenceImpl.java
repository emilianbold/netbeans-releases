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
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.completion.cplusplus.utils.Token;

/**
 *
 * @author Vladimir Voskresensky
 */
public class ReferenceImpl extends DocOffsetableImpl implements CsmReference {
    private final Token token;
    private CsmObject target = null;
    
    public ReferenceImpl(CsmFile file, Token token, BaseDocument doc) {
        super(doc, file, token.getStartOffset());
        this.token = token;
    }

    public CsmObject getReferencedObject() {
        synchronized (this) {
            if (target == null) {
                target = ReferencesSupport.findReference(super.getContainingFile(), super.getDocument(), super.getStartOffset(), token);
            }
        }
        return target;
    }

    public CsmOffsetable getOwner() {
        return null;
    }

    public String getText() {
        return token.getText();
    }
    
    public String toString() {
        return "'" + org.netbeans.editor.EditorDebug.debugString(getText()) // NOI18N
               + "', tokenID=" + token.getTokenID() 
               + ", offset=" + getStartOffset(); // NOI18N
    }    
}
