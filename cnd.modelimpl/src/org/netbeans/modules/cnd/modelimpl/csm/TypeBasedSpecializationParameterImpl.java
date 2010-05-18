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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.List;
import org.netbeans.modules.cnd.api.model.CsmClassifier;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmSpecializationParameter;
import org.netbeans.modules.cnd.api.model.CsmType;
import org.netbeans.modules.cnd.api.model.CsmTypeBasedSpecializationParameter;
import org.netbeans.modules.cnd.modelimpl.csm.core.OffsetableBase;
import org.netbeans.modules.cnd.modelimpl.csm.core.Resolver;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.repository.support.SelfPersistent;

/**
 * Template specialization parameter based on type implementation.
 *
 * @author Nick Krasilnikov
 */
public class TypeBasedSpecializationParameterImpl extends OffsetableBase implements CsmTypeBasedSpecializationParameter, SelfPersistent, Resolver.SafeClassifierProvider {

    private final CsmType type;

    public TypeBasedSpecializationParameterImpl(CsmType type, CsmFile file, int start, int end) {
        super(file, start, end);
        this.type = type;
    }

    public TypeBasedSpecializationParameterImpl(CsmType type) {
        super(type.getContainingFile(), type.getStartOffset(), type.getEndOffset());
        this.type = type;
    }

    public CsmType getType() {
        return type;
    }

    public CsmClassifier getClassifier() {
        return getClassifier(null);
    }

    public CsmClassifier getClassifier(Resolver resolver) {
        if (type instanceof Resolver.SafeClassifierProvider) {
            return ((Resolver.SafeClassifierProvider) type).getClassifier(resolver);
        } else {
            return type.getClassifier();
        }
    }

    public CharSequence getClassifierText() {
        return type.getClassifierText();
    }

    public boolean isInstantiation() {
        return type.isInstantiation();
    }

    public List<CsmSpecializationParameter> getInstantiationParams() {
        return type.getInstantiationParams();
    }

    public int getArrayDepth() {
        return type.getArrayDepth();
    }

    public boolean isPointer() {
        return type.isPointer();
    }

    public int getPointerDepth() {
        return type.getPointerDepth();
    }

    public boolean isReference() {
        return type.isReference();
    }

    public boolean isConst() {
        return type.isConst();
    }

    public boolean isBuiltInBased(boolean resolveTypeChain) {
        return type.isBuiltInBased(resolveTypeChain);
    }

    public boolean isTemplateBased() {
        return type.isTemplateBased();
    }

    public CharSequence getCanonicalText() {
        return type.getCanonicalText();
    }

    @Override
    public CharSequence getText() {
        return type.getText();
    }

    @Override
    public String toString() {
        return type.toString();
    }

    ////////////////////////////////////////////////////////////////////////////
    // impl of SelfPersistent

    @Override
    public void write(DataOutput output) throws IOException {
        super.write(output);
        PersistentUtils.writeType(type, output);
    }

    public TypeBasedSpecializationParameterImpl(DataInput input) throws IOException {
        super(input);
        this.type = PersistentUtils.readType(input);
    }

}
