/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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

import org.netbeans.modules.cnd.antlr.collections.AST;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.modelimpl.csm.core.Utils;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;

/**
 * A dummy class that is added to model in the case
 * there is a forward class declaration that does not refer to a class.
 * ForwardClass can be created i.e. for the following constructions:
 * enum Fwd1; // as ::Fwd1
 * namespace N {
 * class Outer {
 *      enum InnerFwd2; // as N::Outer::InnerFwd2
 * }
 * }
 * enum class N::Outer::InnerFwd2 {
 *      E1, E2
 * }
 */
public final class ForwardEnum extends EnumImpl {

    private ForwardEnum(CharSequence name, CharSequence qName, boolean stronglyTyped, CsmFile file, int start, int end) {
        super(name, qName, stronglyTyped, file, start, end);
    }

    public static boolean isForwardEnum(CsmObject cls) {
        return cls instanceof ForwardEnum;
    }

    public static ForwardEnum createIfNeeded(CharSequence name, CsmFile file, AST ast, int start, int end, CsmScope scope, boolean registerInProject) {
        ForwardEnum fwd = new ForwardEnum(name, name, EnumImpl.isStronglyTypedEnum(ast), file, start, end);
        fwd.initQualifiedName(scope);
        fwd.setTemplateDescriptor(TemplateDescriptor.createIfNeeded(ast, file, scope, registerInProject));
        if (fwd.getProject().findClassifier(fwd.getQualifiedName()) == null) {
            fwd.initScope(scope);
            if(registerInProject) {
                fwd.register(scope, false);
            } else {
                Utils.setSelfUID(fwd);
            }
            return fwd;
        }
        return null;
    }

    @Override
    public boolean shouldBeReplaced(CsmClassifier another) {
        if (another == null) {
            return true;
        } else if (another instanceof ForwardEnum) {
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
    public String toString() {
        return "DUMMY_FORWARD_ENUM " + super.toString(); // NOI18N
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // impl of SelfPersistent
    @Override
    public void write(RepositoryDataOutput output) throws IOException {
        super.write(output);
    }

    public ForwardEnum(RepositoryDataInput input) throws IOException {
        super(input);
    }

    @Override
    public Collection<CsmEnumerator> getEnumerators() {
        return Collections.emptyList();
    }

    private void setTemplateDescriptor(TemplateDescriptor createIfNeeded) {
        if (createIfNeeded != null) {
            throw new UnsupportedOperationException();
        }
    }
}
