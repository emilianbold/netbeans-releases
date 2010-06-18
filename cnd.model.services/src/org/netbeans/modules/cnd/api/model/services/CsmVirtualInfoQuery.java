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

package org.netbeans.modules.cnd.api.model.services;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmClassifier;
import org.netbeans.modules.cnd.api.model.CsmInheritance;
import org.netbeans.modules.cnd.api.model.CsmMember;
import org.netbeans.modules.cnd.api.model.CsmMethod;
import org.netbeans.modules.cnd.api.model.CsmParameter;
import org.netbeans.modules.cnd.api.model.CsmType;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.api.model.xref.CsmTypeHierarchyResolver;
import org.netbeans.modules.cnd.modelutil.AntiLoop;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.openide.util.CharSequences;
import org.openide.util.Lookup;

/**
 * API to query information about virtuality of method
 * @author Vladimir Voskresensky
 */
public abstract class CsmVirtualInfoQuery {
    public abstract boolean isVirtual(CsmMethod method);
    public abstract Collection<CsmMethod> getTopmostBaseDeclarations(CsmMethod method);
    public abstract Collection<CsmMethod> getFirstBaseDeclarations(CsmMethod method);
    public abstract Collection<CsmMethod> getAllBaseDeclarations(CsmMethod method);
    public abstract Collection<CsmMethod> getOverriddenMethods(CsmMethod method, boolean searchFromBase);
    private static final CsmVirtualInfoQuery EMPTY = new Empty();
    
    /** default instance */
    private static CsmVirtualInfoQuery defaultQuery;
    
    protected CsmVirtualInfoQuery() {
    }
    
    /** Static method to obtain the resolver.
     * @return the resolver
     */
    public static CsmVirtualInfoQuery getDefault() {
        /*no need for sync synchronized access*/
        if (defaultQuery != null) {
            return defaultQuery;
        }
        defaultQuery = Lookup.getDefault().lookup(CsmVirtualInfoQuery.class);
        return defaultQuery == null ? EMPTY : defaultQuery;
    }
    
    private static boolean methodEquals(CsmMethod toSearch, CsmMethod method) {
        if (!toSearch.getName().equals(method.getName())) {
            return false;
        }
        Collection<CsmParameter> list1 = toSearch.getParameters();
        Collection<CsmParameter> list2 = method.getParameters();
        if (list1.size() != list2.size()) {
            return false;
        }
        Iterator<CsmParameter> it2 = list2.iterator();
        for (CsmParameter p1 : list1) {
            CsmParameter p2 = it2.next();
            if (p1 != null && p2 != null) {
                if (p1.isVarArgs() && p2.isVarArgs()) {
                    continue;
                }
                CsmType type1 = p1.getType();
                CsmType type2 = p2.getType();
                if (type1 != null && type2 != null) {
                    CsmClassifier classifier1 = type1.getClassifier();
                    CsmClassifier classifier2 = type1.getClassifier();
                    if (classifier1 != null && classifier2 != null) {
                        if (!classifier1.equals(classifier2)) {
                            return false;
                        }
                        continue;
                    }
                    if (CharSequences.comparator().compare(type1.getText(), type2.getText()) != 0 ) {
                        return false;
                    }
                    continue;
                } else if (type1 == null && type2 == null) {
                    continue;
                }
            } else if (p1 == null && p2 == null) {
                continue;
            }
            return false;
        }
        return true;
    }

    //
    // Implementation of the default query
    //
    private static final class Empty extends CsmVirtualInfoQuery {
        private static enum Overridden {
            FIRST,
            TOP,
            ALL
        }

        private Empty() {
        }

        @Override
        public boolean isVirtual(CsmMethod method) {
            if (method.isVirtual()) {
                return true;
            }
            return processClass(method, method.getContainingClass(), new AntiLoop());
        }

        private boolean processClass(CsmMethod toSearch, CsmClass cls, AntiLoop antilLoop){
            if (cls == null || antilLoop.contains(cls)) {
                return false;
            }
            antilLoop.add(cls);
            for(CsmMember m : cls.getMembers()){
                if (CsmKindUtilities.isMethod(m)) {
                    CsmMethod met = (CsmMethod) m;
                    if (methodEquals(toSearch, met)) {
                        if (met.isVirtual()){
                            return true;
                        }
                        break;
                    }
                }
            }
            for(CsmInheritance inh : cls.getBaseClasses()){
                if (processClass(toSearch, CsmInheritanceUtilities.getCsmClass(inh), antilLoop)){
                    return true;
                }
            }
            return false;
        }

        @Override
        public Collection<CsmMethod> getTopmostBaseDeclarations(CsmMethod method) {
            return getBaseDeclaration(method, Overridden.TOP);
        }

        @Override
        public Collection<CsmMethod> getFirstBaseDeclarations(CsmMethod method) {
            return getBaseDeclaration(method, Overridden.FIRST);
        }

        @Override
        public Collection<CsmMethod> getAllBaseDeclarations(CsmMethod method) {
            return getBaseDeclaration(method, Overridden.ALL);
        }

        private Collection<CsmMethod> getBaseDeclaration(CsmMethod method, Overridden overridden) {
            Set<CharSequence> antilLoop = new HashSet<CharSequence>();
            Set<CsmMethod> result = new HashSet<CsmMethod>();
            CsmClass cls = method.getContainingClass();
            if (cls != null) {
                for(CsmInheritance inh : cls.getBaseClasses()) {
                    processMethod(method, CsmInheritanceUtilities.getCsmClass(inh), antilLoop,
                                null, null, result, overridden);
                }
            }
            return result;
        }

        /**
         * Searches for method with the given signature in the given class and its ancestors.
         * @param sig signature to search
         * @param cls class to start with
         * @param antilLoop prevents infinite loops
         * @param result if a method with the given signature is found, it's stored in result - even if it is not virtual.
         * @param first if true, returns first found method, otherwise the topmost one
         * @return true if method found and it is virtual, otherwise false
         */
        private void processMethod(CsmMethod toSearch, CsmClass cls, Set<CharSequence> antilLoop,
                CsmMethod firstFound, CsmMethod lastFound,
                Set<CsmMethod> result, Overridden overridden) {

            boolean theLastInHierarchy;
            if (cls == null || antilLoop.contains(cls.getQualifiedName())) {
                theLastInHierarchy = true;
            } else {

                antilLoop.add(cls.getQualifiedName());
                for(CsmMember member : cls.getMembers()) {
                    if (CsmKindUtilities.isMethod(member)) {
                        CsmMethod method = (CsmMethod) member;
                        if (methodEquals(toSearch, method)) {
                            if (firstFound == null) {
                                firstFound = method;
                            }
                            lastFound = method;
                            if (method.isVirtual()) {
                                switch (overridden) {
                                    case FIRST:
                                        result.add(firstFound);
                                        return;
                                    case ALL:
                                        result.add(method);
                                        break;
                                }
                            }
                        }
                    }
                }
                theLastInHierarchy = cls.getBaseClasses().isEmpty();
                for(CsmInheritance inh : cls.getBaseClasses()) {
                    processMethod(toSearch, CsmInheritanceUtilities.getCsmClass(inh), antilLoop, firstFound, lastFound, result, overridden);
                }

            }
            if (theLastInHierarchy) {
                CsmMethod m  = lastFound;
                if (m != null && m.isVirtual()) {
                    CndUtils.assertNotNull(firstFound, "last found != null && first found == null ?!"); //NOI18N
                    switch (overridden) {
                        case FIRST:
                            result.add(firstFound);
                            break;
                        case TOP:
                            result.add(m);
                            break;
                    }
                }
            }
        }

        @Override
        public Collection<CsmMethod> getOverriddenMethods(CsmMethod method, boolean searchFromBase) {
            Set<CsmMethod> res = new HashSet<CsmMethod>();
            CsmClass cls;
            if (searchFromBase) {
                Iterator<CsmMethod> it = getTopmostBaseDeclarations(method).iterator();
                if (it.hasNext()){
                    method = it.next();
                }
                res.add(method);
            }
            cls = method.getContainingClass();
            if (cls != null){
                for(CsmReference ref :CsmTypeHierarchyResolver.getDefault().getSubTypes(cls, false)){
                    CsmClass c = (CsmClass) ref.getOwner();
                    if (c != null) {
                        for(CsmMember m : c.getMembers()){
                            if (CsmKindUtilities.isMethod(m)) {
                                CsmMethod met = (CsmMethod) m;
                                if (methodEquals(met, method)){
                                    res.add(met);
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            return res;
        }
    }    
}
