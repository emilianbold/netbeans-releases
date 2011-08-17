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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.modelimpl.csm;

import org.netbeans.modules.cnd.antlr.collections.AST;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmClassifierBasedTemplateParameter;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmTemplateParameter;
import org.netbeans.modules.cnd.modelimpl.csm.core.CsmIdentifiable;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.CsmTemplate;
import org.netbeans.modules.cnd.api.model.CsmType;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.modelimpl.csm.core.OffsetableDeclarationBase;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.modelimpl.textcache.NameCache;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;
import org.netbeans.modules.cnd.repository.support.SelfPersistent;
import org.openide.util.CharSequences;

/**
 *
 * @author eu155513
 */
public final class TemplateParameterImpl<T> extends OffsetableDeclarationBase<T> implements CsmClassifierBasedTemplateParameter, CsmTemplate, SelfPersistent {
    private final CharSequence name;
    private CsmUID<CsmScope> scope;

    private CsmType defaultValue = null;

    private TemplateDescriptor templateDescriptor = null;
    
    public TemplateParameterImpl(AST ast, CharSequence name, CsmFile file, CsmScope scope, boolean global) {
        super(file, getStartOffset(ast), getEndOffset(ast));
        // TODO what about explicite type in ast?
        this.name = NameCache.getManager().getString(name);
        templateDescriptor = TemplateDescriptor.createIfNeeded(ast, file, scope, global);
        if ((scope instanceof CsmIdentifiable)) {
            this.scope = UIDCsmConverter.scopeToUID(scope);
        }
    }

    public TemplateParameterImpl(AST ast, CharSequence name, CsmFile file, CsmScope scope, boolean global, AST defaultValue) {
        this(ast, name, file, scope, global);
        this.defaultValue = TypeFactory.createType(defaultValue, file, null, 0);
    }
    
    @Override
    public CharSequence getName() {
        return name;
    }

    @Override
    public CsmObject getDefaultValue() {
        return defaultValue;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TemplateParameterImpl) {
            if (this.getName().equals(((TemplateParameterImpl)obj).getName())){
                if(scope != null) {
                    return scope.equals(((TemplateParameterImpl)obj).scope);
                } else {
                    return ((TemplateParameterImpl)obj).scope == null;
                }
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean isTemplate() {
        return templateDescriptor != null;
    }

    @Override
    public boolean isSpecialization() {
        return false;
    }

    @Override
    public boolean isExplicitSpecialization() {
        return false;
    }

    @Override
    public List<CsmTemplateParameter> getTemplateParameters() {
        return (templateDescriptor != null) ? templateDescriptor.getTemplateParameters() : Collections.<CsmTemplateParameter>emptyList();
    }

    @Override
    public CharSequence getDisplayName() {
        return (templateDescriptor != null) ? CharSequences.create((getName().toString() + templateDescriptor.getTemplateSuffix())) : getName();
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // impl of SelfPersistent

    @Override
    public void write(RepositoryDataOutput output) throws IOException {
        super.write(output); 
        PersistentUtils.writeUTF(name, output);
        UIDObjectFactory.getDefaultFactory().writeUID(scope, output);
        PersistentUtils.writeType(defaultValue, output);
        PersistentUtils.writeTemplateDescriptor(templateDescriptor, output);
    }
    
    public TemplateParameterImpl(RepositoryDataInput input) throws IOException {
        super(input);
        this.name = PersistentUtils.readUTF(input, NameCache.getManager());
        this.scope = UIDObjectFactory.getDefaultFactory().readUID(input);
        this.defaultValue = PersistentUtils.readType(input);
        this.templateDescriptor = PersistentUtils.readTemplateDescriptor(input);
    }
    
    @Override
    public CsmScope getScope() {
        return scope == null? null : scope.getObject();
    }

    @Override
    public CsmDeclaration.Kind getKind() {
        return CsmDeclaration.Kind.TEMPLATE_PARAMETER;
    }

    @Override
    public CharSequence getQualifiedName() {
        CsmScope s = getScope();
        if (CsmKindUtilities.isFunction(s)) {
            return CharSequences.create(((CsmFunction)s).getQualifiedName()+"::"+name); // NOI18N
        } else if (CsmKindUtilities.isClass(s)) {
            return CharSequences.create(((CsmClass)s).getQualifiedName()+"::"+name); // NOI18N
        }
        return name;
    }

    @Override
    public String toString() {
        return getQualifiedName().toString() + getPositionString();
    }
}
