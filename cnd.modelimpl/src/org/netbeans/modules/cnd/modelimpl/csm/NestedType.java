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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.cnd.api.model.CsmClassifier;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmSpecializationParameter;
import org.netbeans.modules.cnd.api.model.CsmTemplate;
import org.netbeans.modules.cnd.api.model.CsmType;
import org.netbeans.modules.cnd.api.model.services.CsmInstantiationProvider;
import org.netbeans.modules.cnd.api.model.util.CsmBaseUtilities;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.modelimpl.csm.core.Resolver;
import org.netbeans.modules.cnd.modelimpl.csm.core.Resolver.SafeTemplateBasedProvider;
import org.netbeans.modules.cnd.modelimpl.csm.core.ResolverFactory;
import org.netbeans.modules.cnd.modelimpl.impl.services.InstantiationProviderImpl;
import org.netbeans.modules.cnd.modelimpl.impl.services.MemberResolverImpl;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;

/**
 * Class for types B and C in the compound type A::B::C
 * @author eu155513
 */
public class NestedType extends TypeImpl {
    private final CsmType parentType;

    public NestedType(CsmType parent, CsmFile file, int pointerDepth, boolean reference, int arrayDepth, boolean _const, int startOffset, int endOffset) {
        super(file, pointerDepth, reference, arrayDepth, _const, startOffset, endOffset);
        this.parentType = parent;
    }
    
    public NestedType(CsmType parent, CsmType type) {
        super(type);
        this.parentType = parent;
    }

    // package-local - for facory only
    NestedType(NestedType type, int pointerDepth, boolean reference, int arrayDepth, boolean _const) {
        super(type, pointerDepth, reference, arrayDepth, _const);
        this.parentType = type.parentType;
    }

    // package-local - for facory only
    NestedType(NestedType type, List<CsmSpecializationParameter> instantiationParams) {
        super(type, instantiationParams);
        this.parentType = type.parentType;
    }

    @Override
    public CsmClassifier getClassifier(Resolver parent) {
        CsmClassifier classifier = _getClassifier();
        if (CsmBaseUtilities.isValid(classifier)) {
            // skip
        } else {
            _setClassifier(null);
            if (parentType != null) {
                CsmClassifier parentClassifier;
                if (parentType instanceof Resolver.SafeClassifierProvider) {
                    parentClassifier = ((Resolver.SafeClassifierProvider)parentType).getClassifier(parent);
                } else {
                    parentClassifier = parentType.getClassifier();
                }
                if (CsmBaseUtilities.isValid(parentClassifier)) {
                    MemberResolverImpl memberResolver = new MemberResolverImpl(parent);
                    classifier = getNestedClassifier(memberResolver, parentClassifier, getOwnText());
                }
            }
            if (!CsmBaseUtilities.isValid(classifier)) {
                // try to resolve qualified name, not through the parent classifier
                List<CharSequence> fqn = getFullQName();
                classifier = renderClassifier(fqn.toArray(new CharSequence[fqn.size()]), parent);
            }
            _setClassifier(classifier);
        }
        if (isInstantiation() && CsmKindUtilities.isTemplate(classifier) && !((CsmTemplate)classifier).getTemplateParameters().isEmpty()) {
            CsmInstantiationProvider ip = CsmInstantiationProvider.getDefault();
            CsmObject obj= null;
            if (ip instanceof InstantiationProviderImpl) {
                Resolver resolver = ResolverFactory.createResolver(getContainingFile(), getStartOffset(), parent);
                if (!resolver.isRecursionOnResolving(Resolver.INFINITE_RECURSION)) {
                    obj = ((InstantiationProviderImpl) ip).instantiate((CsmTemplate) classifier, getInstantiationParams(), this, getContainingFile(), resolver, getStartOffset());
                }
            } else {
                obj = ip.instantiate((CsmTemplate) classifier, getInstantiationParams(), this, getContainingFile(), getStartOffset());
            }
            if (CsmKindUtilities.isClassifier(obj)) {
                classifier = (CsmClassifier) obj;
            }
        }
        return classifier;
    }

    private List<CharSequence> getFullQName() {
        List<CharSequence> res = new ArrayList<CharSequence>();
        if (parentType instanceof NestedType) {
            res.addAll(((NestedType)parentType).getFullQName());
        } else if (parentType instanceof TypeImpl) {
            res.add(((TypeImpl)parentType).getOwnText());
        } else if (parentType instanceof TemplateParameterTypeImpl) {
            res.add(((TemplateParameterTypeImpl)parentType).getOwnText());
        }
        res.add(getOwnText());
        return res;
    }

    /*package local*/ CsmType getParent() {
        return parentType;
    }
    
    /*
     * Classifier text should contain specialization of the parent classifier
     */
    @Override
    public CharSequence getClassifierText() {
        if (parentType != null) {
            return parentType.getClassifierText().toString() + getInstantiationText(parentType) + "::" + classifierText; // NOI18N
        } else {
            return "::" + classifierText; // NOI18N
        }
    }

    @Override
    public boolean isInstantiation() {
        return (parentType != null && parentType.isInstantiation()) || super.isInstantiation();
    }

    @Override
    public boolean isTemplateBased() {
        return isTemplateBased(new HashSet<CsmType>());
    }

    @Override
    public boolean isTemplateBased(Set<CsmType> visited) {
        if (parentType instanceof SafeTemplateBasedProvider) {
            if (visited.contains(this)) {
                return false;
            }
            // Fixed IZ#155112 : False positive error highlighting errors on inner types of templates
            // Check for isTemplateBased parent type and then this
            HashSet<CsmType> t = new HashSet<CsmType>(visited);
            t.add(this);
            boolean result = ((SafeTemplateBasedProvider)parentType).isTemplateBased(t);
            if (!result) {
                result = super.isTemplateBased(visited);
            }
            visited.add(this);
            return result;
        } else if (parentType != null && parentType.isTemplateBased()) {
            return true;
        } else {
            return super.isTemplateBased(visited);
        }
    }


    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        CsmType otherParent = ((NestedType)obj).parentType;
        if (parentType == null) {
            if(otherParent == null) {
                return super.equals(obj);
            } else {
                return false;
            }
        } else {
            if(otherParent == null) {
                return false;
            } else {
                return super.equals(obj) && parentType.equals(otherParent);
            }
        }
    }

    @Override
    public int hashCode() {
        int hash = super.hashCode();
        if(parentType != null) {
            hash = 47 * hash + parentType.hashCode();
        }
        return hash;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // impl of persistent

    @Override
    public void write(DataOutput output) throws IOException {
        super.write(output);
        PersistentUtils.writeType(parentType, output);
    }

    public NestedType(DataInput input) throws IOException {
        super(input);
        parentType = PersistentUtils.readType(input);
    }

    /*package*/ static CsmClassifier getNestedClassifier(MemberResolverImpl memberResolver, CsmClassifier parentClassifier, CharSequence ownText) {
        CsmClassifier classifier = null;
        Iterator<CsmClassifier> iter = memberResolver.getNestedClassifiers(parentClassifier, ownText);
        while (iter.hasNext()) {
            classifier = iter.next();
            // stop on the first not class forward classifier
            if (!CsmKindUtilities.isClassForwardDeclaration(classifier)) {
                break;
            }
        }
        return classifier;
    }
}
