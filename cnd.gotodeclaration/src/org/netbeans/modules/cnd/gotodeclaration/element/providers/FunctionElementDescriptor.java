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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.gotodeclaration.element.providers;

import javax.swing.Icon;
import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.gotodeclaration.element.spi.ElementDescriptor;
import org.netbeans.modules.cnd.gotodeclaration.util.ContextUtil;

import org.netbeans.modules.cnd.modelutil.CsmImageLoader;


/**
 * An ElementDescriptor for variables (CsmVariable)
 * @author Vladimir Kvashin
 */

/* package */ 
class FunctionElementDescriptor extends BaseElementDescriptor implements ElementDescriptor {

    private final CsmUID<CsmFunction> uid;
    private String displayName = null;
    private String contextName = null;
    private boolean isForward;
    
    @SuppressWarnings("unchecked")
    FunctionElementDescriptor(CsmFunction func) {
	uid = func.getUID();
	displayName = func.getSignature().toString();
	contextName = ContextUtil.getContextName(func);
        isForward = ! func.equals(func.getDefinition());
    }
    
    protected final CsmFunction getFunction() {
	return uid.getObject();
    }
    
    protected CsmOffsetable getElement() {
	return getFunction();
    }

    protected String getContextNameImpl() {
	return contextName;
    }

    public String getDisplayName() {
	return displayName;
    }

    public Icon getIcon() {
	return CsmImageLoader.getIcon(isForward ? 
            CsmDeclaration.Kind.FUNCTION : 
            CsmDeclaration.Kind.FUNCTION_DEFINITION, 0);
    }

}
