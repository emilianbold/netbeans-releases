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

package org.netbeans.modules.cnd.modelimpl.impl.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmClassifier;
import org.netbeans.modules.cnd.api.model.CsmInheritance;
import org.netbeans.modules.cnd.api.model.CsmMember;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmVisibility;
import org.netbeans.modules.cnd.api.model.services.CsmInheritanceUtilities;
import org.netbeans.modules.cnd.api.model.services.CsmSelect;
import org.netbeans.modules.cnd.api.model.util.CsmBaseUtilities;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.util.CsmSortUtilities;
import org.netbeans.modules.cnd.modelimpl.csm.core.Resolver;
import org.netbeans.modules.cnd.modelimpl.csm.core.ResolverFactory;

/**
 *
 * @author Alexander Simon
 */
public final class MemberResolverImpl {
    private final Resolver resolver;

    public MemberResolverImpl(Resolver resolver){
        this.resolver = resolver;
    }
    
    public Iterator<CsmMember> getDeclarations(CsmClassifier cls, CharSequence name) {
        if (CsmKindUtilities.isOffsetable(cls)) {
            cls = ResolverFactory.createResolver((CsmOffsetable) cls, resolver).getOriginalClassifier(cls);
            if (CsmKindUtilities.isClass(cls)){
                List<CsmMember> res = new ArrayList<CsmMember>();
                getClassMembers((CsmClass)cls, name, res);
                getSuperClasses((CsmClass)cls, name, res, new HashSet<CharSequence>());
                return res.iterator();
            }
        }
        return Collections.<CsmMember>emptyList().iterator();
    }       

    private void getClassMembers(CsmClass cls, CharSequence name, List<CsmMember> res){
        Iterator<CsmMember> it = CsmSelect.getDefault().getClassMembers(cls,
                    CsmSelect.getDefault().getFilterBuilder().createNameFilter(name.toString(), true, true, false));
        while(it.hasNext()){
            CsmMember m = it.next();
            if (CsmSortUtilities.matchName(m.getName(), name, true, true)){
                res.add(m);
            }
        }
    }

    private void getSuperClasses(CsmClass cls, CharSequence name, List<CsmMember> res, Set<CharSequence> antiLoop){
        if (antiLoop.contains(cls.getQualifiedName())){
            return;
        }
        antiLoop.add(cls.getQualifiedName());
        for(CsmInheritance inh : cls.getBaseClasses()){
            CsmVisibility v = inh.getVisibility();
            switch (v){
                case PRIVATE:
                    break;
                default:
                    CsmClass base = CsmInheritanceUtilities.getCsmClass(inh);
                    if (base != null) {
                        getClassMembers(base, name, res);
                        getSuperClasses(base, name, res, antiLoop);
                    }
            }
        }
    }
    
    public Iterator<CsmClassifier> getNestedClassifiers(CsmClassifier cls, CharSequence name) {
        Iterator<CsmMember> it =  getDeclarations(cls, name);
        List<CsmClassifier> res = new ArrayList<CsmClassifier>();
        while(it.hasNext()){
            CsmMember m = it.next();
            if (CsmKindUtilities.isClassifier(m)){
                res.add((CsmClassifier) m);
            }
        }
        return res.iterator();
    }
}
