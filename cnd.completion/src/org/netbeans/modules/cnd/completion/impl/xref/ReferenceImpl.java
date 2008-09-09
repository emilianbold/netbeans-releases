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

package org.netbeans.modules.cnd.completion.impl.xref;

import org.netbeans.api.lexer.Token;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceKind;
import org.netbeans.modules.cnd.utils.cache.TextCache;

/**
 *
 * @author Vladimir Voskresensky
 */
public class ReferenceImpl extends DocOffsetableImpl implements CsmReference {
    private final Token token;
    private CsmObject target = null;
    private CsmObject owner = null;
    private boolean findDone  = false;
    private final int offset;
    private CsmReferenceKind kind;
    private FileReferencesContext fileReferencesContext;
    
    public ReferenceImpl(CsmFile file, BaseDocument doc, int offset, Token token, CsmReferenceKind kind) {
        super(doc, file, offset);
        this.token = token;
        this.offset = offset;
        // could be null or known kind like CsmReferenceKind.DIRECT_USAGE or CsmReferenceKind.AFTER_DEREFERENCE_USAGE
        this.kind = kind; 
    }

    public CsmObject getReferencedObject() {
        if (!findDone && isValid()) {
            target = ReferencesSupport.instance().findReferencedObject(super.getContainingFile(), super.getDocument(),
                                       this.offset, token, fileReferencesContext);
            findDone = true;
        }
        return target;
    }

    public CsmObject getOwner() {
        if (owner == null && isValid()) {
            owner = ReferencesSupport.findOwnerObject(super.getContainingFile(), super.getDocument(), this.offset, token);
        }
        return owner;
    }

    @Override
    public CharSequence getText() {
        CharSequence cs = token.text();
        if (cs == null) {
            // Token.text() can return null if the token has been removed.
            // We want to avoid NPE (see IZ#143591).
            return ""; // NOI18N
        } else {
            return TextCache.getString(cs);
        }
    }
    
    @Override
    public String toString() {
        return "'" + org.netbeans.editor.EditorDebug.debugString(getText().toString()) // NOI18N
               + "', tokenID=" + this.token.id().toString().toLowerCase() // NOI18N
               + ", offset=" + this.offset + " [" + super.getStartPosition() + "-" + super.getEndPosition() + "]"; // NOI18N
    }    
    
    /*package*/ final void setTarget(CsmObject target) {
        this.target = target;
    }
    
    /*package*/ final CsmObject getTarget() {
        return this.target;
    }
    
    /*package*/ final int getOffset() {
        return this.offset;
    }
    
    /*package*/ final Token getToken() {
        return this.token;
    }

    /*package*/ final CsmReferenceKind getKindImpl() {
        return this.kind;
    }

    public CsmReferenceKind getKind() {
        if (this.kind == null) {
            CsmReferenceKind curKind = ReferencesSupport.getReferenceKind(this);
            this.kind = curKind;
        }
        return this.kind;
    }
    void setFileReferencesContext(FileReferencesContext fileReferencesContext) {
        this.fileReferencesContext = fileReferencesContext;
    }
}
