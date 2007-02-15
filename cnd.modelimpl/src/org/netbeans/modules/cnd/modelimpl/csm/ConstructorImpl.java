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

package org.netbeans.modules.cnd.modelimpl.csm;

import org.netbeans.modules.cnd.api.model.*;
import java.util.*;
import antlr.collections.AST;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;

/**
 * CsmConstructor implementation
 * @author Vladimir Kvasihn
 */
public class ConstructorImpl extends MethodImpl<CsmConstructor> implements CsmConstructor {

    public ConstructorImpl(AST ast, ClassImpl cls, CsmVisibility visibility) {
        super(ast, cls, visibility);
        setName(cls.getName());
    }

    public List getInitializerList() {
        return Collections.EMPTY_LIST;
    }
        
    public CsmType getReturnType() {
        return NoType.instance();
    }
        
}
