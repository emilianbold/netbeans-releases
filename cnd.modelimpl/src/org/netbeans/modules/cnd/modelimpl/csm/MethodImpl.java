/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.cnd.modelimpl.csm;

import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.antlr.collections.AST;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;

/**
 * CsmFunction + CsmMember implementation
 * @param T
 * @author Vladimir Kvashin
 */
public class MethodImpl<T> extends FunctionImpl<T> implements CsmMethod {

    private final CsmVisibility visibility;
    private static final byte ABSTRACT = 1 << (FunctionImpl.LAST_USED_FLAG_INDEX+1);
    private static final byte VIRTUAL = 1 << (FunctionImpl.LAST_USED_FLAG_INDEX+2);
    private static final byte EXPLICIT = (byte)(1 << (FunctionImpl.LAST_USED_FLAG_INDEX+3));

    public MethodImpl(AST ast, ClassImpl cls, CsmVisibility visibility) throws AstRendererException {
        this(ast, cls, visibility, true, true);
    }

    protected MethodImpl(AST ast, ClassImpl cls, CsmVisibility visibility, boolean register, boolean global) throws AstRendererException {
        super(ast, cls.getContainingFile(), cls, false, global);
        this.visibility = visibility;
        //this(cls, visibility, AstUtil.findId(ast), 0, 0);
        //setAst(ast);
        for( AST token = ast.getFirstChild(); token != null; token = token.getNextSibling() ) {
            switch( token.getType() ) {
                case CPPTokenTypes.LITERAL_static:
                    setStatic(true);
                    break;
                case CPPTokenTypes.LITERAL_virtual:
                    setVirtual(true);
                    break;
                case CPPTokenTypes.LITERAL_explicit:
                    setExplicit(true);
                    break;
            }
        }
        if (register) {
            registerInProject();
        }
    }

    public CsmClass getContainingClass() {
        return (CsmClass) getScope();
    }

    public CsmVisibility getVisibility() {
        return visibility;
    }

    public boolean isAbstract() {
        return hasFlags(ABSTRACT);
    }

    public void setAbstract(boolean _abstract) {
        setFlags(ABSTRACT, _abstract);
    }

    private void setVirtual(boolean _virtual) {
        setFlags(VIRTUAL, _virtual);
    }

    private void setExplicit(boolean _explicit) {
        setFlags(EXPLICIT, _explicit);
    }

    public boolean isExplicit() {
        return hasFlags(EXPLICIT);
    }

    public boolean isVirtual() {
        //TODO: implement!
        // returns direct "virtual" keyword presence
        return hasFlags(VIRTUAL);
    }

    @Override
    public boolean isConst() {
        return super.isConst();
    }

////////////////////////////////////////////////////////////////////////////
    // iml of SelfPersistent

    @Override
    public void write(DataOutput output) throws IOException {
        super.write(output);
        PersistentUtils.writeVisibility(this.visibility, output);
    }

    public MethodImpl(DataInput input) throws IOException {
        super(input);
        this.visibility = PersistentUtils.readVisibility(input);
    }
}

