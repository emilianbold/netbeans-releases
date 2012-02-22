/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.modelimpl.csm.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.modules.cnd.antlr.collections.AST;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmInstantiation;
import org.netbeans.modules.cnd.api.model.CsmMacro;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.modelimpl.csm.DeclarationsContainer;
import org.netbeans.modules.cnd.modelimpl.csm.FunctionImplEx;
import org.netbeans.modules.cnd.modelimpl.csm.IncludeImpl;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;

/**
 * storage for file content. 
 * This object is passed to parser hooks to fill with objects created during parse phase.
 * Then it can be compared with other content to decide if signature of file
 * was changed significantly or not. If not then full reparse is not needed and 
 * this content can be used as new file content
 * @author Vladimir Voskresensky
 */
public final class FileImplContent implements DeclarationsContainer {

    private final FileImpl fileImpl;
    private final List<CsmUID<FunctionImplEx<?>>> fakeFunctionRegistrations = new CopyOnWriteArrayList<CsmUID<FunctionImplEx<?>>>();
    private final List<FileImpl.FakeIncludePair> fakeIncludeRegistrations = new CopyOnWriteArrayList<FileImpl.FakeIncludePair>();
    private int errorCount = 0;
    private final Set<ErrorDirectiveImpl> errors = createErrors();
    private final FileComponentDeclarations fileComponentDeclarations;
    private final FileComponentMacros fileComponentMacros;
    private final AtomicBoolean hasBrokenIncludes;
    private final FileComponentIncludes fileComponentIncludes;
    private final FileComponentInstantiations fileComponentInstantiations;
    private final FileComponentReferences fileComponentReferences;

    public FileImplContent(FileImpl fileImpl) {
        this.fileImpl = fileImpl;
        this.fileComponentDeclarations = new FileComponentDeclarations(fileImpl);
        this.fileComponentMacros = new FileComponentMacros(fileImpl);
        this.hasBrokenIncludes = new AtomicBoolean(false);
        this.fileComponentIncludes = new FileComponentIncludes(fileImpl);
        this.fileComponentInstantiations = new FileComponentInstantiations(fileImpl);
        this.fileComponentReferences = new FileComponentReferences(fileImpl);
    }

    public final int getErrorCount() {
        return errorCount;
    }

    public void setErrorCount(int errorCount) {
        this.errorCount = errorCount;
    }

    public final void onFakeRegisration(FunctionImplEx<?> decl, AST fakeRegistrationAst) {
        CsmUID<?> aUid = UIDCsmConverter.declarationToUID(decl);
        @SuppressWarnings("unchecked")
        CsmUID<FunctionImplEx<?>> uidDecl = (CsmUID<FunctionImplEx<?>>) aUid;
        fakeFunctionRegistrations.add(uidDecl);
        trackFakeFunctionAST(uidDecl, fakeRegistrationAst);
    }

    public final boolean onFakeIncludeRegistration(IncludeImpl include, CsmOffsetableDeclaration container) {
        if (include != null && container != null) {
            CsmUID<IncludeImpl> includeUid = UIDCsmConverter.identifiableToUID(include);
            CsmUID<CsmOffsetableDeclaration> containerUID = UIDCsmConverter.declarationToUID(container);
            if (includeUid != null && containerUID != null) {
                // extra check to track possible double registrations like
                // namespace AAA {
                //   namespace Inner {
                //        class B {
                // #include "classBody.h"
                //           class Inner {
                // #include "innerBody.h"
                //           }; end of class Inner
                //        }; end of class B
                //   } // end of namespace Inner
                // } // end of namespace AAA
                // 
                for (FileImpl.FakeIncludePair fakeIncludePair : fakeIncludeRegistrations) {
                    if (fakeIncludePair.includeUid.equals(includeUid)) {
                        // inner object always has higher priority
                        if (!fakeIncludePair.containerUid.equals(containerUID)) {
                            assert false : "trying to replace? " + include + " for container " + container + " was: " + fakeIncludePair;
                        }
                        return false;
                    }
                }
                fakeIncludeRegistrations.add(new FileImpl.FakeIncludePair(includeUid, containerUID));
                return true;
            }
        }
        return false;
    }

    public List<CsmUID<FunctionImplEx<?>>> getFakeFunctionRegistrations() {
        return fakeFunctionRegistrations;
    }

    List<FileImpl.FakeIncludePair> getFakeIncludeRegistrations() {
        return fakeIncludeRegistrations;
    }
    
    public void addError(ErrorDirectiveImpl error) {
        errors.add(error);
        fileImpl.addError(error);
    }
    
    public void addMacro(CsmMacro macro) {
        getFileMacros().addMacro(macro);
        fileImpl.addMacro(macro);
    }

    public void addDeclaration(CsmOffsetableDeclaration decl) {
        getFileDeclarations().addDeclaration(decl);
    }

    @Override
    public CsmOffsetableDeclaration findExistingDeclaration(int startOffset, int endOffset, CharSequence name) {
        return getFileDeclarations().findExistingDeclaration(startOffset, endOffset, name);
    }

    @Override
    public CsmOffsetableDeclaration findExistingDeclaration(int startOffset, CharSequence name, CsmDeclaration.Kind kind) {
        return getFileDeclarations().findExistingDeclaration(startOffset, name, kind);
    }

    public void addInclude(IncludeImpl includeImpl, boolean broken) {
        // addInclude can remove added one from list of broken includes =>
        boolean hasBroken = getFileIncludes().addInclude(includeImpl, broken);
        // update hasBrokenIncludes marker accordingly and store if changed
        if (hasBrokenIncludes.compareAndSet(!hasBroken, hasBroken)) {
//            RepositoryUtils.put(this);
        }
        fileImpl.addInclude(includeImpl, broken);
    }

    public void addInstantiation(CsmInstantiation inst) {
        getFileInstantiations().addInstantiation(inst);
    }

    public boolean addReference(CsmReference ref, CsmObject referencedObject) {
        return getFileReferences().addReference(ref, referencedObject);
    }

    public boolean addResolvedReference(CsmReference ref, CsmObject referencedObject) {
        return getFileReferences().addResolvedReference(ref, referencedObject);
    }

    @Override
    public String toString() {
        return "Content for " + fileImpl; // NOI18N
    }

    private Set<ErrorDirectiveImpl> createErrors() {
        return new TreeSet<ErrorDirectiveImpl>(FileImpl.START_OFFSET_COMPARATOR);
    }
    
    private FileComponentDeclarations getFileDeclarations() {
        return fileComponentDeclarations;
    }

    private FileComponentMacros getFileMacros() {
        return fileComponentMacros;
    }

    private FileComponentIncludes getFileIncludes() {
        return fileComponentIncludes;
    }

    private FileComponentReferences getFileReferences() {
        return fileComponentReferences;
    }

    private FileComponentInstantiations getFileInstantiations() {
        return fileComponentInstantiations;
    }
    /* collection to keep fake ASTs during parse phase */
    private final Map<CsmUID<FunctionImplEx<?>>, AST> fileASTs = new HashMap<CsmUID<FunctionImplEx<?>>, AST>();

    private void trackFakeFunctionAST(CsmUID<FunctionImplEx<?>> funUID, AST funAST) {
        if (funAST == null) {
            fileASTs.remove(funUID);
        } else {
            fileASTs.put(funUID, funAST);
        }
    }
}
