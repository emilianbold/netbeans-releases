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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.List;
import org.netbeans.modules.cnd.api.model.CsmClassifier;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmOffsetable.Position;
import org.netbeans.modules.cnd.api.model.CsmTemplateParameter;
import org.netbeans.modules.cnd.api.model.CsmTemplateParameterType;
import org.netbeans.modules.cnd.api.model.CsmType;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.repository.support.SelfPersistent;

/**
 *
 * @author eu155513
 */
public class TemplateParameterTypeImpl implements CsmType, CsmTemplateParameterType, SelfPersistent {
    private final CsmType type;
    private final CsmTemplateParameter parameter;
    
    public TemplateParameterTypeImpl(CsmType type, CsmTemplateParameter parameter) {
        this.type = type;
        this.parameter = parameter;
    }

    public CsmTemplateParameter getParameter() {
        return parameter;
    }

    public CsmType getTemplateType() {
        return type;
    }

    public CsmFile getContainingFile() {
        return type.getContainingFile();
    }

    public int getEndOffset() {
        return type.getEndOffset();
    }

    public Position getEndPosition() {
        return type.getEndPosition();
    }

    public CharSequence getClassifierText() {
        return type.getClassifierText();
    }

    public int getStartOffset() {
        return type.getStartOffset();
    }

    public Position getStartPosition() {
        return type.getStartPosition();
    }

    public CharSequence getText() {
        return type.getText();
    }

    public int getArrayDepth() {
        return type.getArrayDepth();
    }

    public CharSequence getCanonicalText() {
        return type.getCanonicalText();
    }

    public CsmClassifier getClassifier() {
        return (CsmClassifier)parameter;
    }

    public int getPointerDepth() {
        return type.getPointerDepth();
    }

    public boolean isBuiltInBased(boolean resolveTypeChain) {
        return type.isBuiltInBased(resolveTypeChain);
    }

    public boolean isConst() {
        return type.isConst();
    }

    public boolean isPointer() {
        return type.isPointer();
    }

    public boolean isReference() {
        return type.isReference();
    }

    public List<CsmType> getInstantiationParams() {
        return type.getInstantiationParams();
    }

    public boolean isInstantiation() {
        return type.isInstantiation();
    }

    public boolean isTemplateBased() {
        return true;
    }

    // package
    CharSequence getOwnText() {
        if (type instanceof TypeImpl) {
            return ((TypeImpl) type).getOwnText();
        } else if (type instanceof TemplateParameterTypeImpl) {
            return ((TemplateParameterTypeImpl) type).getOwnText();
        } else {
            return "";
        }
    }
    
    @Override
    public String toString() {
        return "TEMPLATE PARAMETER TYPE " + getText()  + "[" + getStartOffset() + "-" + getEndOffset() + "]"; // NOI18N;
    }    
    
    ////////////////////////////////////////////////////////////////////////////
    // impl of SelfPersistent
    
    public void write(DataOutput output) throws IOException {
        PersistentUtils.writeType(type, output);
        PersistentUtils.writeTemplateParameter(parameter, output);
    }  
    
    public TemplateParameterTypeImpl(DataInput input) throws IOException {
        type = PersistentUtils.readType(input);
        parameter = PersistentUtils.readTemplateParameter(input);
    }
}
