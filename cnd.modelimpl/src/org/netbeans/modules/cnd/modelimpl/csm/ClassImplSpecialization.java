/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.cnd.modelimpl.csm;

import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.antlr.collections.AST;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;
import org.netbeans.modules.cnd.modelimpl.csm.core.*;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.modelimpl.repository.RepositoryUtils;
import org.netbeans.modules.cnd.modelimpl.textcache.NameCache;

/**
 * Implements 
 * @author Vladimir Kvashin
 */
public final class ClassImplSpecialization extends ClassImpl implements CsmTemplate {

    private CharSequence qualifiedNameSuffix = "";

    private SpecializationDescriptor specializationDesctiptor;

    private ClassImplSpecialization(AST ast, CsmFile file) {
        super(null, ast, file);
    }

    @Override
    protected void init(CsmScope scope, AST ast, boolean register) {
        // does not call super.init(), but copies super.init() with some changes:
        // it needs to initialize qualifiedNameSuffix
        // after rendering, but before calling initQualifiedName() and register()

        initScope(scope, ast);
        if (register) {
            RepositoryUtils.hang(this); // "hang" now and then "put" in "register()"
        } else {
            Utils.setSelfUID(this);
        }
        render(ast, !register);

        AST qIdToken = AstUtil.findChildOfType(ast, CPPTokenTypes.CSM_QUALIFIED_ID);
        assert qIdToken != null;
        qualifiedNameSuffix = NameCache.getManager().getString(TemplateUtils.getSpecializationSuffix(qIdToken, getTemplateParameters()));
        initQualifiedName(scope, ast);
        specializationDesctiptor = SpecializationDescriptor.createIfNeeded(ast, getContainingFile(), scope, register);

        if (register) {
            register(getScope(), false);
        }
    }

    public static ClassImplSpecialization create(AST ast, CsmScope scope, CsmFile file, boolean register, DeclarationsContainer container) {
        ClassImpl clsImpl = findExistingClassImplInContainer(container, ast);
        ClassImplSpecialization impl = null;
        if (clsImpl instanceof ClassImplSpecialization) {
            // not our instance
            impl = (ClassImplSpecialization) clsImpl;
        }
        if (impl == null) {
            impl = new ClassImplSpecialization(ast, file);
        }
        impl.init(scope, ast, register);
        return impl;
    }

    @Override
    public boolean isTemplate() {
        return true;
    }

    public boolean isSpecialization() {
        return true;
    }

//    public String getTemplateSignature() {
//	return qualifiedNameSuffix;
//    }
// This does not work since the method is called from base class' constructor    
//    protected String getQualifiedNamePostfix() {
//	String qName = super.getQualifiedNamePostfix();
//	if( isSpecialization() ) {
//	    qName += qualifiedNameSuffix;
//	}
//	return qName;
//    }
    @Override
    public String getQualifiedNamePostfix() {
        return super.getQualifiedNamePostfix() + qualifiedNameSuffix.toString();
    }

    public List<CsmSpecializationParameter> getSpecializationParameters() {
        return (specializationDesctiptor != null) ? specializationDesctiptor.getSpecializationParameters() : Collections.<CsmSpecializationParameter>emptyList();
    }

    ////////////////////////////////////////////////////////////////////////////
    // impl of SelfPersistent
    @Override
    public void write(DataOutput output) throws IOException {
        super.write(output);
        PersistentUtils.writeUTF(qualifiedNameSuffix, output);
        PersistentUtils.writeSpecializationDescriptor(specializationDesctiptor, output);
    }

    public ClassImplSpecialization(DataInput input) throws IOException {
        super(input);
        qualifiedNameSuffix = PersistentUtils.readUTF(input, NameCache.getManager());
        specializationDesctiptor = PersistentUtils.readSpecializationDescriptor(input);
    }

    @Override
    public String getDisplayName() {
        return getName() + qualifiedNameSuffix.toString();
    }
}
