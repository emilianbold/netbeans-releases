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

package org.netbeans.modules.cnd.modelimpl.csm;

import org.netbeans.modules.cnd.api.model.*;
import antlr.collections.AST;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * @author Vladimir Voskresensky
 */
public final class ConstructorDDImpl extends MethodDDImpl<CsmConstructor> implements CsmConstructor {

    public ConstructorDDImpl(AST ast, ClassImpl cls, CsmVisibility visibility) {
        super(ast, cls, visibility, true);
    }
 
    @Override
    public CsmType getReturnType() {
        return NoType.instance();
    }
    
    public List getInitializerList() {
        return Collections.EMPTY_LIST;
    }    
    ////////////////////////////////////////////////////////////////////////////
    // iml of SelfPersistent
    
    @Override
    public void write(DataOutput output) throws IOException {
        super.write(output);
    }
    
    public ConstructorDDImpl(DataInput input) throws IOException {
        super(input);
    }    
}
