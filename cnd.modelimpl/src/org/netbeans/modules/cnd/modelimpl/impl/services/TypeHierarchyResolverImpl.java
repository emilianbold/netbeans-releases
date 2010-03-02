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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.modelimpl.impl.services;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmInheritance;
import org.netbeans.modules.cnd.api.model.CsmInstantiation;
import org.netbeans.modules.cnd.api.model.CsmMember;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.api.model.services.CsmFileInfoQuery;
import org.netbeans.modules.cnd.api.model.services.CsmInheritanceUtilities;
import org.netbeans.modules.cnd.api.model.services.CsmSelect;
import org.netbeans.modules.cnd.api.model.services.CsmSelect.CsmFilter;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.util.UIDs;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceSupport;
import org.netbeans.modules.cnd.api.model.xref.CsmTypeHierarchyResolver;

/**
 *
 * @author Alexander Simon
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.cnd.api.model.xref.CsmTypeHierarchyResolver.class)
public final class TypeHierarchyResolverImpl extends CsmTypeHierarchyResolver {

    public TypeHierarchyResolverImpl() {
    }

    @Override
    public Collection<CsmReference> getSubTypes(CsmClass referencedClass, boolean directSubtypesOnly) {
        CsmFile file = referencedClass.getContainingFile();
        long fileVersion = CsmFileInfoQuery.getDefault().getFileVersion(file);
        CsmProject project = file.getProject();
        Map<CsmUID<CsmClass>,Set<CsmUID<CsmClass>>> fullMap = getOrCreateFullMap(project, fileVersion);
        HierarchyModelImpl model = new HierarchyModelImpl(fullMap, referencedClass, !directSubtypesOnly);
        CsmUID<CsmClass> referencedClassUID = UIDs.get(referencedClass);
        Set<CsmUID<CsmClass>> set = model.getModel().get(referencedClassUID);
        if (set != null){
            List<CsmReference> res = new ArrayList<CsmReference>();
            for (CsmUID<CsmClass> clsUID : set){
                CsmClass cls = clsUID.getObject();
                if (cls != null) {
                    res.add(CsmReferenceSupport.createObjectReference(cls));
                }
            }
            return res;
        }
        return Collections.<CsmReference>emptyList();
    }

    private static final class HierarchyModelImpl {
        private Map<CsmUID<CsmClass>,Set<CsmUID<CsmClass>>> myMap;
    
        public HierarchyModelImpl(Map<CsmUID<CsmClass>,Set<CsmUID<CsmClass>>> fullMap, CsmClass cls, boolean recursive) {
            CsmUID<CsmClass> clsUID = UIDs.get(cls);
            myMap = Collections.unmodifiableMap(fullMap);
            if (!recursive) {
                Set<CsmUID<CsmClass>> result = myMap.get(clsUID);
                if (result == null){
                    result = new HashSet<CsmUID<CsmClass>>();
                }
                myMap = new HashMap<CsmUID<CsmClass>,Set<CsmUID<CsmClass>>>();
                myMap.put(clsUID,result);
            }
            Set<CsmUID<CsmClass>> result = new HashSet<CsmUID<CsmClass>>();
            gatherList(clsUID, result, myMap);
            myMap = new HashMap<CsmUID<CsmClass>,Set<CsmUID<CsmClass>>>();
            myMap.put(clsUID,result);
        }

        public Map<CsmUID<CsmClass>,Set<CsmUID<CsmClass>>> getModel(){
            return myMap;
        }

        private static void gatherList(CsmUID<CsmClass> cls, Set<CsmUID<CsmClass>> result, final Map<CsmUID<CsmClass>,Set<CsmUID<CsmClass>>> map){
            Set<CsmUID<CsmClass>> set = map.get(cls);
            if (set == null) {
                return;
            }
            for(CsmUID<CsmClass> c : set){
                if (!result.contains(c)) {
                    result.add(c);
                    gatherList(c, result, map);
                }
            }
        }

        private Map<CsmUID<CsmClass>,Set<CsmUID<CsmClass>>> buildSuperHierarchy(CsmClass cls){
            HashMap<CsmUID<CsmClass>,Set<CsmUID<CsmClass>>> aMap = new HashMap<CsmUID<CsmClass>,Set<CsmUID<CsmClass>>>();
            buildSuperHierarchy(cls, aMap);
            return aMap;
        }
    
        private void buildSuperHierarchy(CsmClass cls, Map<CsmUID<CsmClass>,Set<CsmUID<CsmClass>>> map){
            CsmUID<CsmClass> clsUID = UIDs.get(cls);
            Set<CsmUID<CsmClass>> back = map.get(clsUID);
            if (back != null) {
                return;
            }
            back = new HashSet<CsmUID<CsmClass>>();
            map.put(clsUID, back);
            Collection<CsmInheritance> list = cls.getBaseClasses();
            if (list != null && list.size() >0){
                for(CsmInheritance inh : list){
                    CsmClass c = getClassDeclaration(inh);
                    if (c != null) {
                        CsmUID<CsmClass> cUID = UIDs.get(c);
                        back.add(cUID);
                        buildSuperHierarchy(c, map);
                    }
                }
            }
        }
    }

    private static Map<CsmUID<CsmProject>, Reference<Map<CsmUID<CsmClass>, Set<CsmUID<CsmClass>>>>> cache = new HashMap<CsmUID<CsmProject>, Reference<Map<CsmUID<CsmClass>, Set<CsmUID<CsmClass>>>>>();
    private static long lastVersion = -1;
    private synchronized Map<CsmUID<CsmClass>, Set<CsmUID<CsmClass>>> getOrCreateFullMap(CsmProject project, long version) {
        if (lastVersion != version) {
            cache.clear();
        }
        lastVersion = version;
        CsmUID<CsmProject> prjUID = UIDs.get(project);
        Reference<Map<CsmUID<CsmClass>, Set<CsmUID<CsmClass>>>> outRef = cache.get(prjUID);
        Map<CsmUID<CsmClass>, Set<CsmUID<CsmClass>>> out = (outRef == null) ? null : outRef.get();
        if (out == null) {
            out = new HashMap<CsmUID<CsmClass>,Set<CsmUID<CsmClass>>>();
            buildSubHierarchy(project.getGlobalNamespace(), out);
            cache.put(prjUID, new SoftReference<Map<CsmUID<CsmClass>, Set<CsmUID<CsmClass>>>>(out));
        }
        return out;
    }

    ///////// common /////////////////

    private static CsmFilter getClassFilter() {
        return CsmSelect.getFilterBuilder().createKindFilter(CsmDeclaration.Kind.CLASS, CsmDeclaration.Kind.STRUCT);
    }

    private static CsmClass getClassDeclaration(CsmInheritance inh){
        CsmClass c = CsmInheritanceUtilities.getCsmClass(inh);
        if (CsmKindUtilities.isInstantiation(c)) {
            CsmDeclaration d = ((CsmInstantiation)c).getTemplateDeclaration();
            if (CsmKindUtilities.isClass(d)){
                c = (CsmClass) d;
            }
        }
        return c;
    }
    
    ///////// sub hierarchy ///////////
    private static void buildSubHierarchy(CsmNamespace ns, Map<CsmUID<CsmClass>,Set<CsmUID<CsmClass>>> map){
        for(Iterator it = ns.getNestedNamespaces().iterator(); it.hasNext();){
            buildSubHierarchy((CsmNamespace)it.next(), map);
        }
        Iterator<CsmOffsetableDeclaration> declarations = CsmSelect.getDeclarations(ns, getClassFilter());
        while (declarations.hasNext()) {
            CsmClass decl = (CsmClass)declarations.next();
            buildSubHierarchy(map, decl);
        }
    }

    private static void buildSubHierarchy(final Map<CsmUID<CsmClass>, Set<CsmUID<CsmClass>>> map, final CsmClass cls) {
        Collection<CsmInheritance> list = cls.getBaseClasses();
        CsmUID<CsmClass> clsUID = UIDs.get(cls);
        if (list != null && list.size() >0){
            for(CsmInheritance inh : list){
                CsmClass c = getClassDeclaration(inh);
                if (c != null) {
                    CsmUID<CsmClass> cUID = UIDs.get(c);
                    Set<CsmUID<CsmClass>> back = map.get(cUID);
                    if (back == null){
                        back = new HashSet<CsmUID<CsmClass>>();
                        map.put(cUID,back);
                    }
                    back.add(clsUID);
                }
            }
        }
        Iterator<CsmMember> classMembers = CsmSelect.getClassMembers(cls, getClassFilter());
        while (classMembers.hasNext()) {
            CsmClass member = (CsmClass)classMembers.next();
            buildSubHierarchy(map, member);
        }
    }
}
