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
package org.netbeans.modules.cnd.modelimpl.content.file;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.modules.cnd.antlr.collections.AST;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmErrorDirective;
import org.netbeans.modules.cnd.api.model.CsmInclude;
import org.netbeans.modules.cnd.api.model.CsmInstantiation;
import org.netbeans.modules.cnd.api.model.CsmMacro;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmScopeElement;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.api.model.CsmValidable;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.modelimpl.cache.impl.WeakContainer;
import org.netbeans.modules.cnd.modelimpl.csm.FunctionImplEx;
import org.netbeans.modules.cnd.modelimpl.csm.IncludeImpl;
import org.netbeans.modules.cnd.modelimpl.csm.MutableDeclarationsContainer;
import org.netbeans.modules.cnd.modelimpl.csm.core.ErrorDirectiveImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.ProjectBase;
import org.netbeans.modules.cnd.modelimpl.repository.FileDeclarationsKey;
import org.netbeans.modules.cnd.modelimpl.repository.FileIncludesKey;
import org.netbeans.modules.cnd.modelimpl.repository.FileInstantiationsKey;
import org.netbeans.modules.cnd.modelimpl.repository.FileMacrosKey;
import org.netbeans.modules.cnd.modelimpl.repository.FileReferencesKey;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;
import org.openide.util.Union2;

/**
 * storage for file content. 
 * This object is passed to parser hooks to fill with objects created during parse phase.
 * Then it can be compared with other content to decide if signature of file
 * was changed significantly or not. If not then full reparse is not needed and 
 * this content can be used as new file content
 * @author Vladimir Voskresensky
 */
public final class FileContent implements MutableDeclarationsContainer {

    private final FileImpl fileImpl;
    private boolean persistent;
    private final List<FakeIncludePair> fakeIncludeRegistrations;
    private final List<CsmUID<FunctionImplEx<?>>> fakeFunctionRegistrations;
    private int parserErrorsCount;    
    private final Set<ErrorDirectiveImpl> errors;
    private final Union2<FileComponentDeclarations, WeakContainer<FileComponentDeclarations>> fileComponentDeclarations;
    /*FileComponentMacros or WeakContainer<FileComponentMacros>*/
    private final Union2<FileComponentMacros, WeakContainer<FileComponentMacros>> fileComponentMacros;
    /*FileComponentIncludes or WeakContainer<FileComponentIncludes>*/
    private final Union2<FileComponentIncludes, WeakContainer<FileComponentIncludes>> fileComponentIncludes;
    private final AtomicBoolean hasBrokenIncludes;
    /*FileComponentInstantiations or WeakContainer<FileComponentInstantiations>*/
    private final Union2<FileComponentInstantiations, WeakContainer<FileComponentInstantiations>> fileComponentInstantiations;
    /*FileComponentReferences or WeakContainer<FileComponentReferences>*/
    private final Union2<FileComponentReferences, WeakContainer<FileComponentReferences>> fileComponentReferences;

    public static FileContent createFileContent(FileImpl fileImpl, ProjectBase project) {
        return new FileContent(fileImpl, project, true,
                new FileComponentDeclarations(fileImpl, true),
                new FileComponentMacros(fileImpl, true),
                new FileComponentIncludes(fileImpl, true),
                false,
                new FileComponentInstantiations(fileImpl, true),
                new FileComponentReferences(fileImpl, true),
                createFakeIncludes(Collections.<FakeIncludePair>emptyList()),
                createFakeFunctions(Collections.<CsmUID<FunctionImplEx<?>>>emptyList()),
                createErrors(Collections.<ErrorDirectiveImpl>emptySet()), 0);
    }
    
    private FileContent(FileImpl fileImpl, ProjectBase project, boolean persistent,
            FileComponentDeclarations fcd, FileComponentMacros fcm,
            FileComponentIncludes fcinc, boolean hasBrokenIncludes,
            FileComponentInstantiations fcinst, FileComponentReferences fcr,
            List<FakeIncludePair> fakeIncludeRegistrations, 
            List<CsmUID<FunctionImplEx<?>>> fakeFunctionRegistrations,
            Set<ErrorDirectiveImpl> errors, int parserErrorsCount) {
        this.persistent = persistent;
        this.fileImpl = fileImpl;
        this.fileComponentDeclarations = asUnion(project, fcd, persistent);
        this.fileComponentMacros = asUnion(project, fcm, persistent);
        this.fileComponentIncludes = asUnion(project, fcinc, persistent);
        this.hasBrokenIncludes = new AtomicBoolean(hasBrokenIncludes);
        this.fileComponentInstantiations = asUnion(project, fcinst, persistent);
        this.fileComponentReferences = asUnion(project, fcr, persistent);
        this.fakeIncludeRegistrations = fakeIncludeRegistrations;
        this.fakeFunctionRegistrations = fakeFunctionRegistrations;
        this.errors = errors;
        this.parserErrorsCount = parserErrorsCount;
        if (persistent) {
            fcd.put();
            fcm.put();
            fcinc.put();
            fcinst.put();
            fcr.put();
        }
        checkValid();
    }
    
    public Collection<CsmScopeElement> getScopeElements() {
        List<CsmScopeElement> l = new ArrayList<CsmScopeElement>();
        l.addAll(getFileDeclarations().getStaticVariableDeclarations());
        l.addAll(getFileDeclarations().getStaticFunctionDeclarations());
        return l;
    }
    
    /**
     * returns copy with hard referenced collections. It can not be put into repository.
     * toWeakReferenceBasedCopy method should be used to release hard references.
     * @return hard reference based non-persistent copy of content
     */
    public static FileContent getHardReferenceBasedCopy(FileContent other, boolean emptyContent) {
        other.checkValid();
        final ProjectBase projectImpl = other.fileImpl.getProjectImpl(true);
        return new FileContent(other.fileImpl, projectImpl, false,
                new FileComponentDeclarations(other.getFileDeclarations(), emptyContent),
                new FileComponentMacros(other.getFileMacros(), emptyContent),
                new FileComponentIncludes(other.getFileIncludes(), emptyContent),
                (emptyContent ? false : other.hasBrokenIncludes()),
                new FileComponentInstantiations(other.getFileInstantiations(), emptyContent),
                new FileComponentReferences(other.getFileReferences(), emptyContent),
                createFakeIncludes(emptyContent ? Collections.<FakeIncludePair>emptyList() : other.fakeIncludeRegistrations),
                createFakeFunctions(emptyContent ? Collections.<CsmUID<FunctionImplEx<?>>>emptyList() : other.fakeFunctionRegistrations),
                createErrors(emptyContent ? Collections.<ErrorDirectiveImpl>emptySet() : other.errors), 
                emptyContent ? 0 : other.parserErrorsCount);
    }
    
    /**
     * content of this instance is converted to weak referenced one.
     * @return copy which reference internal containers by weak reference and repository keys
     */
    public FileContent toWeakReferenceBasedCopy() {
        final ProjectBase projectImpl = this.fileImpl.getProjectImpl(true);
        checkValid();
        try {
            // convert this instance as is into persistent copy
            // it not legal to use it later on for appending elements
            return new FileContent(this.fileImpl, projectImpl, true,
                    this.getFileDeclarations(), this.getFileMacros(),
                    this.getFileIncludes(), this.hasBrokenIncludes(),
                    this.getFileInstantiations(), this.getFileReferences(),
                    this.fakeIncludeRegistrations,
                    this.fakeFunctionRegistrations,
                    this.errors, this.parserErrorsCount);
        } finally {
            // mark object as no more usable
            this.parserErrorsCount = -1;
        }
    }
    
    public final int getErrorCount() {
        checkValid();
        return parserErrorsCount;
    }

    public void setErrorCount(int errorCount) {
        checkValid();
        this.parserErrorsCount = errorCount;
        checkValid();
    }

    public final void onFakeRegisration(FunctionImplEx<?> decl, AST fakeRegistrationAst) {
        checkValid();
        CsmUID<?> aUid = UIDCsmConverter.declarationToUID(decl);
        @SuppressWarnings("unchecked")
        CsmUID<FunctionImplEx<?>> uidDecl = (CsmUID<FunctionImplEx<?>>) aUid;
        fakeFunctionRegistrations.add(uidDecl);
        trackFakeFunctionAST(uidDecl, fakeRegistrationAst);
    }

    public final boolean onFakeIncludeRegistration(IncludeImpl include, CsmOffsetableDeclaration container) {
        checkValid();
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
                for (FakeIncludePair fakeIncludePair : fakeIncludeRegistrations) {
                    if (fakeIncludePair.includeUid.equals(includeUid)) {
                        // inner object always has higher priority
                        if (!fakeIncludePair.containerUid.equals(containerUID)) {
                            assert false : "trying to replace? " + include + " for container " + container + " was: " + fakeIncludePair;
                        }
                        return false;
                    }
                }
                fakeIncludeRegistrations.add(new FakeIncludePair(includeUid, containerUID));
                return true;
            }
        }
        return false;
    }

    public List<CsmUID<FunctionImplEx<?>>> getFakeFunctionRegistrations() {
        checkValid();
        return fakeFunctionRegistrations;
    }

    public List<FakeIncludePair> getFakeIncludeRegistrations() {
        checkValid();
        return fakeIncludeRegistrations;
    }
    
    public void addError(ErrorDirectiveImpl error) {
        checkValid();
        errors.add(error);
    }
    
    public void addMacro(CsmMacro macro) {
        checkValid();
        getFileMacros().addMacro(macro);
    }       

    @Override
    public void addDeclaration(CsmOffsetableDeclaration decl) {
        checkValid();
        getFileDeclarations().addDeclaration(decl);
//        fileImpl.addDeclaration(decl);
    }

    @Override
    public CsmOffsetableDeclaration findExistingDeclaration(int startOffset, int endOffset, CharSequence name) {
        checkValid();
        return getFileDeclarations().findExistingDeclaration(startOffset, endOffset, name);
    }

    @Override
    public CsmOffsetableDeclaration findExistingDeclaration(int startOffset, CharSequence name, CsmDeclaration.Kind kind) {
        checkValid();
        return getFileDeclarations().findExistingDeclaration(startOffset, name, kind);
    }

    public Collection<CsmInclude> getIncludes() {
        checkValid();
        return getFileIncludes().getIncludes();
    }
    
    public void addInclude(IncludeImpl includeImpl, boolean broken) {
        checkValid();
        // addInclude can remove added one from list of broken includes =>
        hasBrokenIncludes.set(getFileIncludes().addInclude(includeImpl, broken));
    }

    public void addInstantiation(CsmInstantiation inst) {
        checkValid();
        getFileInstantiations().addInstantiation(inst);
    }

    public boolean addReference(CsmReference ref, CsmObject referencedObject) {
        checkValid();
        return getFileReferences().addReference(ref, referencedObject);
    }

    public boolean addResolvedReference(CsmReference ref, CsmObject referencedObject) {
        checkValid();
        return getFileReferences().addResolvedReference(ref, referencedObject);
    }

    @Override
    public String toString() {
        return ((parserErrorsCount < 0) ? "INVALID " :"") + (persistent ? "PERSISTENT " :"") + "File Content for " + fileImpl; // NOI18N
    }

    static private List<CsmUID<FunctionImplEx<?>>> createFakeFunctions(List<CsmUID<FunctionImplEx<?>>> in) {
        return new CopyOnWriteArrayList<CsmUID<FunctionImplEx<?>>>(in);
    }
    
    private static List<FakeIncludePair> createFakeIncludes(List<FakeIncludePair> in) {
        return new CopyOnWriteArrayList<FakeIncludePair>(in);
    }
    
    static private Set<ErrorDirectiveImpl> createErrors(Set<ErrorDirectiveImpl> in) {
        Set<ErrorDirectiveImpl> out = new TreeSet<ErrorDirectiveImpl>(FileImpl.START_OFFSET_COMPARATOR);
        out.addAll(in);
        return out;
    }
    
    public FileComponentDeclarations getFileDeclarations() {
        checkValid();
        return getFileComponent(fileComponentDeclarations);
    }

    public Set<? extends CsmErrorDirective> getErrors() {
        checkValid();
        return Collections.unmodifiableSet(errors);
    }

    public FileComponentMacros getFileMacros() {
        checkValid();
        return getFileComponent(fileComponentMacros);
    }

    public FileComponentIncludes getFileIncludes() {
        checkValid();
        return getFileComponent(fileComponentIncludes);
    }

    public boolean hasBrokenIncludes() {
        checkValid();
        return hasBrokenIncludes.get();
    }

    public FileComponentReferences getFileReferences() {
        checkValid();
        return getFileComponent(fileComponentReferences);
    }

    public FileComponentInstantiations getFileInstantiations() {
        checkValid();
        return getFileComponent(fileComponentInstantiations);
    }
    
    private <T extends FileComponent> T getFileComponent(Union2<T, WeakContainer<T>> ref) {
        if (ref.hasFirst()) {
            assert !persistent : "non persistent must have hard reference";
            return ref.first();
        } else {
            assert persistent : "persistent must have weak reference";
            return ref.second().getContainer();
        }
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

    public void write(RepositoryDataOutput output) throws IOException {
        checkValid();
        assert persistent : "only persistent content can be put into repository";
        FileDeclarationsKey fileDeclarationsKey = (FileDeclarationsKey) getFileComponent(fileComponentDeclarations).getKey();
        assert fileDeclarationsKey != null : "file declaratios key can not be null";
        fileDeclarationsKey.write(output);
        FileIncludesKey fileIncludesKey = (FileIncludesKey) getFileComponent(fileComponentIncludes).getKey();
        assert fileIncludesKey != null : "file includes key can not be null";
        fileIncludesKey.write(output);
        output.writeBoolean(hasBrokenIncludes.get());
        FileMacrosKey fileMacrosKey = (FileMacrosKey) getFileComponent(fileComponentMacros).getKey();
        assert fileMacrosKey != null : "file macros key can not be null";
        fileMacrosKey.write(output);
        FileReferencesKey fileReferencesKey = (FileReferencesKey) getFileComponent(fileComponentReferences).getKey();
        assert fileReferencesKey != null : "file referebces key can not be null";
        fileReferencesKey.write(output);
        FileInstantiationsKey fileInstantiationsKey = (FileInstantiationsKey) getFileComponent(fileComponentInstantiations).getKey();
        assert fileInstantiationsKey != null : "file instantiation references key can not be null";
        fileInstantiationsKey.write(output);
        
        PersistentUtils.writeErrorDirectives(this.errors, output);
        output.writeInt(parserErrorsCount);
        
        FakeIncludePair.write(fakeIncludeRegistrations, output);
        UIDObjectFactory.getDefaultFactory().writeUIDCollection(this.fakeFunctionRegistrations, output, false);
    }
    
    public FileContent(FileImpl file, ProjectBase project, RepositoryDataInput input) throws IOException {
        this.fileImpl = file;
        this.persistent = true;
        FileDeclarationsKey fileDeclarationsKey = new FileDeclarationsKey(input);
        assert fileDeclarationsKey != null : "file declaratios key can not be null";
        fileComponentDeclarations = Union2.createSecond(new WeakContainer<FileComponentDeclarations>(project, fileDeclarationsKey));

        FileIncludesKey fileIncludesKey = new FileIncludesKey(input);
        assert fileIncludesKey != null : "file includes key can not be null";
        fileComponentIncludes = Union2.createSecond(new WeakContainer<FileComponentIncludes>(project, fileIncludesKey));
        hasBrokenIncludes = new AtomicBoolean(input.readBoolean());

        FileMacrosKey fileMacrosKey = new FileMacrosKey(input);
        assert fileMacrosKey != null : "file macros key can not be null";
        fileComponentMacros = Union2.createSecond(new WeakContainer<FileComponentMacros>(project, fileMacrosKey));

        FileReferencesKey fileReferencesKey = new FileReferencesKey(input);
        assert fileReferencesKey != null : "file referebces key can not be null";
        fileComponentReferences = Union2.createSecond(new WeakContainer<FileComponentReferences>(project, fileReferencesKey));

        FileInstantiationsKey fileInstantiationsKey = new FileInstantiationsKey(input);
        assert fileInstantiationsKey != null : "file instantiation references key can not be null";
        fileComponentInstantiations = Union2.createSecond(new WeakContainer<FileComponentInstantiations>(project, fileInstantiationsKey));
        
        this.errors = createErrors(Collections.<ErrorDirectiveImpl>emptySet());
        PersistentUtils.readErrorDirectives(this.errors, input);
        parserErrorsCount = input.readInt();
        
        this.fakeIncludeRegistrations = createFakeIncludes(Collections.<FakeIncludePair>emptyList());
        FakeIncludePair.read(this.fakeIncludeRegistrations, input);
        
        this.fakeFunctionRegistrations = createFakeFunctions(Collections.<CsmUID<FunctionImplEx<?>>>emptyList());
        UIDObjectFactory.getDefaultFactory().readUIDCollection(this.fakeFunctionRegistrations, input);
        
        checkValid();
    }

    private <T extends FileComponent> Union2<T, WeakContainer<T>> asUnion(CsmValidable stateOwner, T fc, boolean persistent) {
        if (persistent) {
            return Union2.createSecond(new WeakContainer<T>(stateOwner, fc.getKey()));
        } else {
            return Union2.createFirst(fc);
        }
    }

    private void checkValid() {
        assert this.parserErrorsCount >= 0 : "invalid object " + parserErrorsCount;
    }

    @Override
    public void removeDeclaration(CsmOffsetableDeclaration declaration) {
        getFileDeclarations().removeDeclaration(declaration);
    }

    @Override
    public Collection<CsmOffsetableDeclaration> getDeclarations() {
        return getFileDeclarations().getDeclarations();
    }

    public void cleanOther() {
        getFileIncludes().clean();
        getFileMacros().clean();
        getFileReferences().clean();
        getFileInstantiations().clean();
    }

    public Collection<CsmUID<CsmOffsetableDeclaration>> cleanDeclarations() {
        Collection<CsmUID<CsmOffsetableDeclaration>> uids = getFileDeclarations().clean();
        return uids;
    }
    
    public void put() {
        getFileIncludes().put();
        getFileMacros().put();
        getFileReferences().put();
        getFileInstantiations().put();
        getFileDeclarations().put();
    }

    public FileImpl getFile() {
        return this.fileImpl;
    }

    public Map<CsmUID<FunctionImplEx<?>>, AST> getFakeASTs() {
        return this.fileASTs;
    }
}
