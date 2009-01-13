/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.refactoring.codegen;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmMethod;
import org.netbeans.modules.cnd.api.model.CsmNamespaceDefinition;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.CsmScopeElement;
import org.netbeans.modules.cnd.api.model.util.CsmBaseUtilities;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;

/**
 *
 * @author Vladimir Voskresensky
 */
public final class CsmContext {
    private final CsmFile file;
    private final int offset;
    private List<CsmObject> path = null;
    private CsmClass enclosingClass = null;
    private CsmNamespaceDefinition enclosingNS = null;
    private CsmFunction enclosingFun = null;
    private CsmObject objectUnderOffset = null;
    /*package*/CsmContext(CsmFile file, int offset) {
        this.file = file;
        this.offset = offset;
    }

    public CsmFile getFile() {
        return file;
    }

    public List<CsmObject> getPath() {
        initPath();
        return path;
    }

    @Override
    public String toString() {
        return "context: [" + file + ":" + offset + "]"; // NOI18N
    }

    public CsmClass getEnclosingClass() {
        initPath();
        return enclosingClass;
    }

    public CsmFunction getEnclosingFunction() {
        initPath();
        return enclosingFun;
    }

    public CsmNamespaceDefinition getEnclosingNamespace() {
        initPath();
        return enclosingNS;
    }

    public CsmObject getObjectUnderOffset() {
        initPath();
        return objectUnderOffset;
    }

    private synchronized void initPath() {
        if (path != null) {
            return;
        }
        path = new ArrayList<CsmObject>(5);
        path.add(file);
        Collection<? extends CsmScopeElement> scopeElements = file.getDeclarations();
        boolean cont;
        do {
            cont = false;
            for (CsmScopeElement csmScopeElement : scopeElements) {
                if (CsmKindUtilities.isOffsetable(csmScopeElement)) {
                    CsmOffsetable elem = (CsmOffsetable) csmScopeElement;
                    // stop if element starts after offset
                    if (this.offset < elem.getStartOffset()) {
                        break;
                    } else if (this.offset < elem.getEndOffset()) {
                        // offset is in element
                        cont = true;
                        path.add(elem);
                        rememberObject(elem);
                        if (CsmKindUtilities.isScope(elem)) {
                            // deep diving
                            scopeElements = ((CsmScope)elem).getScopeElements();
                            break;
                        } else {
                            objectUnderOffset = elem;
                            cont = false;
                        }
                    }
                }
            }
        } while (cont);
    }

    private void rememberObject(CsmObject obj) {
        if (CsmKindUtilities.isNamespaceDefinition(obj)) {
            enclosingNS = (CsmNamespaceDefinition) obj;
        } else if (CsmKindUtilities.isClass(obj)) {
            enclosingClass = (CsmClass)obj;
        } else if (CsmKindUtilities.isFunction(obj)) {
            enclosingFun = (CsmFunction) obj;
            if (CsmKindUtilities.isMethod(enclosingFun)) {
                enclosingClass = ((CsmMethod)CsmBaseUtilities.getFunctionDeclaration(enclosingFun)).getContainingClass();
            }
        }
    }
}
