/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.modelimpl.csm;

import antlr.collections.AST;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.cnd.api.model.CsmClassifier;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFriend;
import org.netbeans.modules.cnd.api.model.CsmInheritance;
import org.netbeans.modules.cnd.api.model.CsmMember;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.CsmScopeElement;

/**
 * A dummy class that is added to model in the case
 * there is a forward class declaration that does not refer to a class
 * @author Vladimir Kvashin
 */
public class ForwardClass extends ClassImpl {

    protected ForwardClass(String name, CsmFile file, AST ast) {
        super(ast, file);
    }

    public static ForwardClass create(String name, CsmFile file, AST ast, CsmScope scope, boolean registerInProject) {
        ForwardClass fwd = new ForwardClass(name, file, ast);
        fwd.initQualifiedName(scope, ast);
        if (fwd.getProject().findClassifier(fwd.getQualifiedName()) == null) {
            fwd.initScope(scope, ast);
            if(registerInProject) {
                fwd.register(scope, false);
            }
            return fwd;
        }
        return null;
    }
    
    @Override
    public boolean shouldBeReplaced(CsmClassifier another) {
        if (another == null) {
            return true;
        } else if (another instanceof ForwardClass) {
            return false;
        } else {
            return true;
        }
    }
    
    @Override
    public Collection<CsmScopeElement> getScopeElements() {
        return Collections.emptyList();
    }

    @Override
    public List<CsmInheritance> getBaseClasses() {
        return Collections.emptyList();
    }

    @Override
    public Collection<CsmFriend> getFriends() {
        return Collections.emptyList();
    }

    @Override
    public int getLeftBracketOffset() {
        return getEndOffset() - 1;
    }

    @Override
    public Collection<CsmMember> getMembers() {
        return Collections.emptyList();
    }

    ////////////////////////////////////////////////////////////////////////////
    // impl of SelfPersistent
    @Override
    public void write(DataOutput output) throws IOException {
        super.write(output);
    }

    public ForwardClass(DataInput input) throws IOException {
        super(input);
    }
}
