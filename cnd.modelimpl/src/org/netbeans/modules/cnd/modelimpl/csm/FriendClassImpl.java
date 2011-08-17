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

import org.netbeans.modules.cnd.modelimpl.csm.resolver.Resolver;
import org.netbeans.modules.cnd.modelimpl.csm.resolver.ResolverFactory;
import java.io.IOException;
import java.util.List;
import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.antlr.collections.AST;
import java.util.Collections;
import org.netbeans.modules.cnd.api.model.services.CsmInstantiationProvider;
import org.netbeans.modules.cnd.api.model.util.CsmBaseUtilities;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.util.UIDs;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;
import org.netbeans.modules.cnd.modelimpl.csm.core.*;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.modelimpl.textcache.NameCache;
import org.netbeans.modules.cnd.modelimpl.textcache.QualifiedNameCache;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;
import org.netbeans.modules.cnd.modelimpl.uid.UIDUtilities;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;
import org.openide.util.CharSequences;

/**
 *
 * @author Vladimir Kvasihn
 */
public final class FriendClassImpl extends OffsetableDeclarationBase<CsmFriendClass> implements CsmFriendClass, CsmTemplate {
    private final CharSequence name;
    private final CharSequence[] nameParts;
    private final CsmUID<CsmClass> parentUID;
    private final CsmUID<CsmClassForwardDeclaration> classForwardUID;
    private CsmUID<CsmClass> friendUID;
    private TemplateDescriptor templateDescriptor = null;
    private SpecializationDescriptor specializationDesctiptor;
    private int lastParseCount = -1;    
    private int lastFileID = -1;
    
    private FriendClassImpl(AST ast, AST qid, CsmClassForwardDeclaration cfd, FileImpl file, CsmClass parent, boolean register) throws AstRendererException {
        super(file, getStartOffset(ast), getEndOffset(ast));
        this.parentUID = UIDs.get(parent);
        qid = (qid != null) ? qid : AstUtil.findSiblingOfType(ast, CPPTokenTypes.CSM_QUALIFIED_ID);
        if (qid == null) {
            throw new AstRendererException(file, getStartOffset(), "Invalid friend class declaration."); // NOI18N
        }
        name = QualifiedNameCache.getManager().getString(AstRenderer.getQualifiedName(qid));
        nameParts = initNameParts(qid);
        classForwardUID = UIDCsmConverter.declarationToUID(cfd);
        AST templateParams = AstUtil.findSiblingOfType(ast, CPPTokenTypes.LITERAL_template);
        if (templateParams != null) {
            List<CsmTemplateParameter> params = TemplateUtils.getTemplateParameters(templateParams, file, parent, register);
            final String classSpecializationSuffix = TemplateUtils.getClassSpecializationSuffix(templateParams, null);
            String fullName = "<" + classSpecializationSuffix + ">"; // NOI18N
            setTemplateDescriptor(params, fullName, !classSpecializationSuffix.isEmpty(), register);
        }
        specializationDesctiptor = SpecializationDescriptor.createIfNeeded(ast, getContainingFile(), parent, register);
    }

    public static FriendClassImpl create(AST ast, AST qid, CsmClassForwardDeclaration cfd, FileImpl file, CsmClass parent, boolean register) throws AstRendererException {
        FriendClassImpl friendClassImpl = new FriendClassImpl(ast, qid, cfd, file, parent, register);
        postObjectCreateRegistration(register, friendClassImpl);
        return friendClassImpl;
    }

    @Override
    protected boolean registerInProject() {
        CsmProject project = getContainingFile().getProject();
        if( project instanceof ProjectBase ) {
            return ((ProjectBase) project).registerDeclaration(this);
        }
        return false;
    }

    @Override
    public CsmClass getContainingClass() {
        return parentUID.getObject();
    }

    @Override
    public CsmScope getScope() {
        return getContainingClass();
    }

    @Override
    public CharSequence getName() {
        return name;
    }

    @Override
    public CharSequence getQualifiedName() {
        CsmClass cls = getContainingClass();
        CharSequence clsQName = cls.getQualifiedName();
	if( clsQName != null && clsQName.length() > 0 ) {
            return CharSequences.create(clsQName.toString() + "::" + getQualifiedNamePostfix()); // NOI18N
	}
        return getName();
    }

    @Override
    public CsmDeclaration.Kind getKind() {
        return CsmDeclaration.Kind.CLASS_FRIEND_DECLARATION;
    }

    private boolean needRecount(int newParseCount, Resolver currentResolver) {
        if (lastParseCount != newParseCount) {
            return true;
        }
        CsmFile startFile = null;
        if (currentResolver != null) {
            startFile = currentResolver.getStartFile();
        }
        if (startFile == null) {
            startFile = getContainingFile();
        }
        int fileID = UIDUtilities.getFileID(UIDs.get(startFile));
        if (lastFileID != fileID) {
            return true;
        }
        return false;
    }

    private void updateCache(int newParseCount, Resolver currentResolver) {
        lastParseCount = newParseCount;
        CsmFile startFile = null;
        if (currentResolver != null) {
            startFile = currentResolver.getStartFile();
        }
        if (startFile == null) {
            startFile = getContainingFile();
        }
        int fileID = UIDUtilities.getFileID(UIDs.get(startFile));
        lastFileID = fileID;
    }
    
    @Override
    public CsmClass getReferencedClass() {
        CsmClass cls = UIDCsmConverter.UIDtoClass(friendUID);
        int newParseCount = FileImpl.getParseCount();
        Resolver currentResolver = ResolverFactory.getCurrentResolver();
        if (needRecount(newParseCount, currentResolver)) {
            if(!CsmBaseUtilities.isValid(cls)|| ForwardClass.isForwardClass(cls)) {
                cls = null;
                CsmClassForwardDeclaration cfd = UIDCsmConverter.UIDtoCsmObject(classForwardUID);
                if(CsmBaseUtilities.isValid(cfd)) {
                    cls = cfd.getCsmClass();
                }
                friendUID = UIDCsmConverter.declarationToUID(cls);
            }
            if (!CsmBaseUtilities.isValid(cls) || ForwardClass.isForwardClass(cls)) {
                CsmObject o = resolve();
                if (CsmKindUtilities.isClass(o)) {
                    cls = (CsmClass) o;
                    friendUID = UIDCsmConverter.objectToUID(cls);
                }
            }
            if(CsmKindUtilities.isTemplate(cls) && specializationDesctiptor != null) {
                CsmInstantiationProvider instProvider = CsmInstantiationProvider.getDefault();            
                CsmObject o = instProvider.instantiate((CsmTemplate)cls, specializationDesctiptor.getSpecializationParameters());
                while(CsmKindUtilities.isInstantiation(o) ) {
                    o = ((CsmInstantiation)o).getTemplateDeclaration();
                }
                if(CsmKindUtilities.isClass(o)) {
                    cls = (CsmClass) o;
                    friendUID = UIDCsmConverter.objectToUID(cls);
                }
            }            
            updateCache(newParseCount, currentResolver);
        //} else {
        //    System.err.println("cache hit FriendClassImpl");
        }
        return cls;
    }
    
    private CharSequence[] initNameParts(AST qid) {
        if( qid != null ) {
            return AstRenderer.getNameTokens(qid);
        }
        return new CharSequence[0];
    }
    
    private CsmObject resolve() {
        CsmObject result = null;
        Resolver aResolver = ResolverFactory.createResolver(this);
        try {
            result = aResolver.resolve(nameParts, Resolver.CLASS);
        } finally {
            ResolverFactory.releaseResolver(aResolver);
        }
        if (result == null) {
            result = ((ProjectBase) getContainingFile().getProject()).getDummyForUnresolved(nameParts, getContainingFile(), getStartOffset());
        }
        return result;
    }

    @Override
    public void dispose() {
        super.dispose();
        unregisterInProject();
    }

    private void unregisterInProject() {
        CsmClassForwardDeclaration cfd = UIDCsmConverter.UIDtoCsmObject(classForwardUID);
        if (cfd instanceof ClassForwardDeclarationImpl) {
            ((ClassForwardDeclarationImpl) cfd).dispose();
        }
        ((ProjectBase) getContainingFile().getProject()).unregisterDeclaration(this);
        this.cleanUID();
    }

    private void setTemplateDescriptor(List<CsmTemplateParameter> params, String name, boolean specialization, boolean global) {
        templateDescriptor = new TemplateDescriptor(params, name, specialization, global);
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
    // iml of SelfPersistent

    @Override
    public void write(RepositoryDataOutput output) throws IOException {
        super.write(output);
        assert this.name != null;
        PersistentUtils.writeUTF(name, output);
        PersistentUtils.writeStrings(this.nameParts, output);
        UIDObjectFactory.getDefaultFactory().writeUID(this.parentUID, output);    
        UIDObjectFactory.getDefaultFactory().writeUID(this.friendUID, output);
        PersistentUtils.writeTemplateDescriptor(templateDescriptor, output);
        UIDObjectFactory.getDefaultFactory().writeUID(this.classForwardUID, output);
        PersistentUtils.writeSpecializationDescriptor(specializationDesctiptor, output);
    }


    public FriendClassImpl(RepositoryDataInput input) throws IOException {
        super(input);
        this.name = PersistentUtils.readUTF(input, QualifiedNameCache.getManager());
        assert this.name != null;
        this.nameParts = PersistentUtils.readStrings(input, NameCache.getManager());
        this.parentUID = UIDObjectFactory.getDefaultFactory().readUID(input);
        this.friendUID = UIDObjectFactory.getDefaultFactory().readUID(input);
        this.templateDescriptor = PersistentUtils.readTemplateDescriptor(input);
        this.classForwardUID = UIDObjectFactory.getDefaultFactory().readUID(input);
        this.specializationDesctiptor = PersistentUtils.readSpecializationDescriptor(input);
    }
}
