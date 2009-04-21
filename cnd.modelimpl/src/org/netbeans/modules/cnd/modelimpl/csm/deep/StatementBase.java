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

package org.netbeans.modules.cnd.modelimpl.csm.deep;


import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.api.model.deep.*;

import antlr.collections.AST;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import java.util.logging.Level;
import org.netbeans.modules.cnd.modelimpl.csm.core.*;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;
import org.netbeans.modules.cnd.utils.CndUtils;

/**
 * Common ancestor for all statements
 * @author Vladimir Kvashin
 */
public abstract class StatementBase extends OffsetableBase implements CsmStatement {
    
    private final AST ast;
    
    private CsmScope scopeRef;
    private CsmUID<CsmScope> scopeUID;
    
    public StatementBase(AST ast, CsmFile file, CsmScope scope) {
        super(ast, file);
        CndUtils.assertTrue(file != null, "File can not be null", Level.WARNING);
        this.ast = ast;
        if( scope != null ) {
            setScope(scope);
        }
    }
    
    public CsmScope getScope() {
        CsmScope scope = this.scopeRef;
        if (scope == null) {
            scope = UIDCsmConverter.UIDtoScope(this.scopeUID);
            // this is possible situation when scope is already invalidated (see IZ#154264)
            // assert (scope != null || this.scopeUID == null) : "null object for UID " + this.scopeUID;
        }
        return scope;
    }
    
    protected void setScope(CsmScope scope) {
	// within bodies scope is a statement - it is not Identifiable
        if (scope instanceof CsmIdentifiable) {
            this.scopeUID = UIDCsmConverter.scopeToUID(scope);
            assert (scopeUID != null || scope == null);
        } else {
            this.scopeRef = scope;
        }
    }
    
    protected AST getAst() {
        return ast;
    }

    @Override
    public void dispose() {
        onDispose();
        super.dispose();
    }

    @Override
    public void write(DataOutput output) throws IOException {
        super.write(output);
        UIDObjectFactory.getDefaultFactory().writeUID(this.scopeUID, output);
    }
    
    protected StatementBase(DataInput input) throws IOException {
        super(input);
        this.ast = null;
        this.scopeUID = UIDObjectFactory.getDefaultFactory().readUID(input);
    }   

    @Override
    public String toString() {
        return "" + getKind() + ' ' + getOffsetString(); // NOI18N
    }

    private void onDispose() {
        // restore scope from it's UID
        if (this.scopeRef == null) {
            this.scopeRef = UIDCsmConverter.UIDtoScope(scopeUID);
            assert this.scopeRef != null : "no object for UID " + scopeUID;
        }
    }
}
