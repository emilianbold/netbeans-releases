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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.modelimpl.csm;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.netbeans.modules.cnd.antlr.collections.AST;
import org.netbeans.modules.cnd.api.model.CsmClassifier;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmMember;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.CsmSpecializationParameter;
import org.netbeans.modules.cnd.api.model.CsmTemplate;
import org.netbeans.modules.cnd.api.model.services.CsmSelect.CsmFilter;
import org.netbeans.modules.cnd.modelimpl.csm.core.AstUtil;
import org.netbeans.modules.cnd.modelimpl.csm.core.Utils;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.modelimpl.textcache.NameCache;
import org.openide.util.CharSequences;

/**
 * Template function specialization container.
 *
 * @author Nikolay Krasilnikov (http://nnnnnk.name)
 */
public class ClassImplFunctionSpecialization extends ClassImplSpecialization implements CsmTemplate {

    private CharSequence qualifiedNameSuffix = "";

    private SpecializationDescriptor specializationDesctiptor;

    private ClassImplFunctionSpecialization(AST ast, NameHolder name, CsmFile file) {
        super(ast, name, file);
    }

    @Override
    protected void init(CsmScope scope, AST ast, boolean register) {
        // does not call super.init(), but copies super.init() with some changes:
        // it needs to initialize qualifiedNameSuffix
        // after rendering, but before calling initQualifiedName() and register()

        initScope(scope);
        temporaryRepositoryRegistration(register, this);
        render(ast, !register);

        AST qIdToken = AstUtil.findChildOfType(ast, CPPTokenTypes.CSM_QUALIFIED_ID);
        assert qIdToken != null;
        qualifiedNameSuffix = NameCache.getManager().getString(TemplateUtils.getSpecializationSuffix(qIdToken, getTemplateParameters()));
        initQualifiedName(scope);
        specializationDesctiptor = SpecializationDescriptor.createIfNeeded(ast, getContainingFile(), scope, register);

        if (register) {
            register(getScope(), false);
        }
    }

    public static ClassImplFunctionSpecialization create(AST ast, CsmScope scope, CsmFile file, boolean register, DeclarationsContainer container) {
        NameHolder nameHolder = NameHolder.createName(getClassName(ast));
        ClassImplFunctionSpecialization impl = new ClassImplFunctionSpecialization(ast, nameHolder, file);
        impl.init(scope, ast, false);
        ClassImplFunctionSpecialization clsImpl = findExistingClassImplClassImplFunctionSpecializationInProject(file, impl);
        if (clsImpl != null) {
            impl = clsImpl;
        } else {
            impl.init(scope, ast, register);
        }
        nameHolder.addReference(file, impl);
        return impl;
    }

    private static ClassImplFunctionSpecialization findExistingClassImplClassImplFunctionSpecializationInProject(CsmFile file, ClassImplFunctionSpecialization spec) {
        ClassImplFunctionSpecialization out = null;
        if (file != null) {
            CsmClassifier existing = file.getProject().findClassifier(spec.getQualifiedName());
            if (existing instanceof ClassImplFunctionSpecialization) {
                out = (ClassImplFunctionSpecialization) existing;
            }
        }
        return out;
    }

    private ClassImpl findBaseClassImplInProject() {
        ClassImpl out = null;
        CsmFile file = getContainingFile();
        if (file != null) {
            CsmClassifier base = file.getProject().findClassifier(super.getQualifiedNamePostfix());
            if (base instanceof ClassImpl) {
                out = (ClassImpl) base;
            }
        }
        return out;
    }

    @Override
    public boolean isTemplate() {
        return true;
    }

    @Override
    public void addMember(CsmMember member, boolean global) {
        super.addMember(member, global);
    }

    @Override
    public Collection<CsmMember> getMembers() {
        Collection<CsmMember> members = new ArrayList<CsmMember>();
        members.addAll(super.getMembers());
        ClassImpl base = findBaseClassImplInProject();
        if(base != null && base != this) {
            members.addAll(base.getMembers());
        }
        return members;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Iterator<CsmMember> getMembers(CsmFilter filter) {
        ClassImpl base = findBaseClassImplInProject();
        if(base != null && base != this) {
            return new MultiIterator<CsmMember>(super.getMembers(filter), base.getMembers(filter));
        } else {
            return super.getMembers(filter);
        }
    }

    @Override
    public boolean isSpecialization() {
        return true;
    }

    @Override
    public String getQualifiedNamePostfix() {
        return super.getQualifiedNamePostfix() + qualifiedNameSuffix.toString();
    }

    @Override
    public List<CsmSpecializationParameter> getSpecializationParameters() {
        return (specializationDesctiptor != null) ? specializationDesctiptor.getSpecializationParameters() : Collections.<CsmSpecializationParameter>emptyList();
    }

    private static String getClassName(AST ast) {
        CharSequence funName = CharSequences.create(AstUtil.findId(ast, CPPTokenTypes.RCURLY, true));
        return getClassNameFromFunctionSpecialicationName(funName.toString());
    }

    private static String getClassNameFromFunctionSpecialicationName(CharSequence functionName) {
        CharSequence[] nameParts = Utils.splitQualifiedName(functionName.toString());
        StringBuilder className = new StringBuilder("");
        for(int i = 0; i < nameParts.length - 1; i++) {
            className.append(nameParts[i]);
        }
        return className.toString();
    }

    ////////////////////////////////////////////////////////////////////////////
    // impl of SelfPersistent
    @Override
    public void write(DataOutput output) throws IOException {
        super.write(output);
        PersistentUtils.writeUTF(qualifiedNameSuffix, output);
        PersistentUtils.writeSpecializationDescriptor(specializationDesctiptor, output);
    }

    public ClassImplFunctionSpecialization(DataInput input) throws IOException {
        super(input);
        qualifiedNameSuffix = PersistentUtils.readUTF(input, NameCache.getManager());
        specializationDesctiptor = PersistentUtils.readSpecializationDescriptor(input);
    }

    @Override
    public String getDisplayName() {
        return getName() + qualifiedNameSuffix.toString();
    }

    private static class MultiIterator<T> implements Iterator<T> {

        private int currentIterator;
        private Iterator<T> iterators[];

        public MultiIterator(Iterator<T>... iterators) {
            this.iterators = iterators;
            this.currentIterator = 0;
        }

        @Override
        public T next() {
            while (currentIterator < iterators.length && !iterators[currentIterator].hasNext()) {
                currentIterator++;
            }
            return iterators[currentIterator].next();
        }

        @Override
        public boolean hasNext() {
            while (currentIterator < iterators.length && !iterators[currentIterator].hasNext()) {
                currentIterator++;
            }
            return currentIterator < iterators.length;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Not supported."); // NOI18N
        }
    }

}
