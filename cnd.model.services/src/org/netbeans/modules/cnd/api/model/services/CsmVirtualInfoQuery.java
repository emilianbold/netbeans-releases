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

package org.netbeans.modules.cnd.api.model.services;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmInheritance;
import org.netbeans.modules.cnd.api.model.CsmMember;
import org.netbeans.modules.cnd.api.model.CsmMethod;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.api.model.xref.CsmTypeHierarchyResolver;
import org.netbeans.modules.cnd.utils.cache.CharSequenceKey;
import org.openide.util.Lookup;

/**
 * API to query information about virtuality of method
 * @author Vladimir Voskresensky
 */
public abstract class CsmVirtualInfoQuery {
    public abstract boolean isVirtual(CsmMethod method);
    public abstract Collection<CsmMethod> getBaseDeclaration(CsmMethod method);
    public abstract Collection<CsmMethod> getOverridenMethods(CsmMethod method, boolean searchFromBase);
    private static final CsmVirtualInfoQuery EMPTY = new Empty();
    
    /** default instance */
    private static CsmVirtualInfoQuery defaultQuery;
    
    protected CsmVirtualInfoQuery() {
    }
    
    /** Static method to obtain the resolver.
     * @return the resolver
     */
    public static synchronized CsmVirtualInfoQuery getDefault() {
        if (defaultQuery != null) {
            return defaultQuery;
        }
        defaultQuery = Lookup.getDefault().lookup(CsmVirtualInfoQuery.class);
        return defaultQuery == null ? EMPTY : defaultQuery;
    }
    
    //
    // Implementation of the default query
    //
    private static final class Empty extends CsmVirtualInfoQuery {
        private Empty() {
        }

        @Override
        public boolean isVirtual(CsmMethod method) {
            if (method.isVirtual()) {
                return true;
            }
            return processClass(method.getSignature(), method.getContainingClass(), new HashSet<CharSequence>());
        }

        private boolean processClass(CharSequence sig, CsmClass cls, Set<CharSequence> antilLoop){
            if (cls == null || antilLoop.contains(cls.getQualifiedName())) {
                return false;
            }
            antilLoop.add(cls.getQualifiedName());
            for(CsmMember m : cls.getMembers()){
                if (CsmKindUtilities.isMethod(m)) {
                    CsmMethod met = (CsmMethod) m;
                    if (CharSequenceKey.Comparator.compare(sig, met.getSignature())==0){
                        if (met.isVirtual()){
                            return true;
                        }
                        break;
                    }
                }
            }
            for(CsmInheritance inh : cls.getBaseClasses()){
                if (processClass(sig, CsmInheritanceUtilities.getCsmClass(inh), antilLoop)){
                    return true;
                }
            }
            return false;
        }
        
        @Override
        public Collection<CsmMethod> getBaseDeclaration(CsmMethod method) {
            Set<CharSequence> antilLoop = new HashSet<CharSequence>();
            CharSequence sig = method.getSignature();
            CsmMethod met = processMethod(sig, method.getContainingClass(), antilLoop);
            if (met != null) {
                CsmMethod next = met;
                while(next != null) {
                    met = next;
                    CsmClass base = next.getContainingClass();
                    next = null;
                    if (base != null) {
                        for(CsmInheritance inh : base.getBaseClasses()){
                            CsmMethod m = processMethod(sig, CsmInheritanceUtilities.getCsmClass(inh), antilLoop);
                            if (m != null) {
                                next = m;
                                break;
                            }
                        }
                    }
                }
            }
            if (met == null) {
                return Collections.<CsmMethod>emptyList();
            }
            return Collections.<CsmMethod>singleton(met);
        }

        private CsmMethod processMethod(CharSequence sig, CsmClass cls, Set<CharSequence> antilLoop){
            if (cls == null || antilLoop.contains(cls.getQualifiedName())) {
                return null;
            }
            antilLoop.add(cls.getQualifiedName());
            for(CsmMember m : cls.getMembers()){
                if (CsmKindUtilities.isMethod(m)) {
                    CsmMethod met = (CsmMethod) m;
                    if (CharSequenceKey.Comparator.compare(sig, met.getSignature())==0
                        && met.isVirtual()){
                        return met;
                    }
                }
            }
            for(CsmInheritance inh : cls.getBaseClasses()){
                CsmMethod met = processMethod(sig, CsmInheritanceUtilities.getCsmClass(inh), antilLoop);
                if (met != null) {
                    return met;
                }
            }
            return null;
        }

        @Override
        public Collection<CsmMethod> getOverridenMethods(CsmMethod method, boolean searchFromBase) {
            Set<CsmMethod> res = new HashSet<CsmMethod>();
            CsmClass cls;
            if (searchFromBase) {
                Iterator<CsmMethod> it = getBaseDeclaration(method).iterator();
                if (it.hasNext()){
                    method = it.next();
                }
                res.add(method);
            }
            cls = method.getContainingClass();
            if (cls != null){
                CharSequence sig = method.getSignature();
                for(CsmReference ref :CsmTypeHierarchyResolver.getDefault().getSubTypes(cls, false)){
                    CsmClass c = (CsmClass) ref.getOwner();
                    for(CsmMember m : c.getMembers()){
                        if (CsmKindUtilities.isMethod(m)) {
                            CsmMethod met = (CsmMethod) m;
                            if (CharSequenceKey.Comparator.compare(sig, met.getSignature())==0){
                                res.add(met);
                                break;
                            }
                        }
                    }
                }
            }
            return res;
        }
    }    
}
