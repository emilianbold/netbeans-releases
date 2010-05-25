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
import org.netbeans.modules.cnd.api.model.CsmClassifier;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmInheritance;
import org.netbeans.modules.cnd.api.model.CsmInstantiation;
import org.netbeans.modules.cnd.api.model.CsmMember;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.CsmScope;
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
        if (true) {
            return getSubTypes2(referencedClass, directSubtypesOnly);
        } else {
            return getSubTypes1(referencedClass, directSubtypesOnly);
        }
    }

    public Collection<CsmReference> getSubTypes1(CsmClass referencedClass, boolean directSubtypesOnly) {
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

    private Collection<CsmReference> getSubTypes2(CsmClass referencedClass, boolean directSubtypesOnly) {
        CsmFile file = referencedClass.getContainingFile();
        long fileVersion = CsmFileInfoQuery.getDefault().getFileVersion(file);
        CsmProject project = file.getProject();
        Map<CsmUID<CsmClass>,Set<CsmUID<CsmClass>>> fullMap = getOrCreateFullMap2(project, fileVersion);
        Collection<CsmReference> res = new ArrayList<CsmReference>();
        for(CsmUID<CsmClass> cls : getSubTypes2(referencedClass, fullMap, directSubtypesOnly)) {
            res.add(CsmReferenceSupport.createObjectReference(cls.getObject()));
        }
        return res;
    }

    private  Set<CsmUID<CsmClass>> getSubTypes2(CsmClass referencedClass, Map<CsmUID<CsmClass>,Set<CsmUID<CsmClass>>> map, boolean directSubtypesOnly) {
        if (directSubtypesOnly) {
            return getSubTypes2(referencedClass, map);
        }
        Set<CsmUID<CsmClass>> antiLoop = new HashSet<CsmUID<CsmClass>>();
        Set<CsmUID<CsmClass>> res = new HashSet<CsmUID<CsmClass>>(getSubTypes2(referencedClass, map));
        antiLoop.add(UIDs.get(referencedClass));
        while(true) {
            int size = res.size();
            Set<CsmUID<CsmClass>> step = new HashSet<CsmUID<CsmClass>>();
            for(CsmUID<CsmClass> reference : res) {
                if (!antiLoop.contains(reference)) {
                    CsmClass cls = reference.getObject();
                    for(CsmUID<CsmClass> increment : getSubTypes2(cls, map)) {
                        if (!antiLoop.contains(increment)) {
                            step.add(increment);
                        }
                    }
                    antiLoop.add(reference);
                }
            }
            res.addAll(step);
            if (res.size() == size) {
                break;
            }
        }
        return res;
    }

    private Set<CsmUID<CsmClass>> getSubTypes2(CsmClass referencedClass, Map<CsmUID<CsmClass>,Set<CsmUID<CsmClass>>> map) {
        CsmUID<CsmClass> referencedClassUID = UIDs.get(referencedClass);
        Set<CsmUID<CsmClass>> res = map.get(referencedClassUID);
        if (res != null) {
            return res;
        }
        CsmFile file = referencedClass.getContainingFile();
        CsmProject project = file.getProject();
        res = new HashSet<CsmUID<CsmClass>>();
        for (CsmInheritance inh : project.findInheritances(referencedClass.getName())){
            CsmClassifier classifier = inh.getClassifier();
            if (classifier != null) {
                if (CsmKindUtilities.isInstantiation(classifier)) {
                    CsmOffsetableDeclaration template = ((CsmInstantiation)classifier).getTemplateDeclaration();
                    if (CsmKindUtilities.isClassifier(template)) {
                        classifier = (CsmClassifier) template;
                    }
                }
                CsmUID<CsmClassifier> classifierUID = UIDs.get(classifier);
                if (referencedClassUID.equals(classifierUID)) {
                    CsmScope scope = inh.getScope();
                    if (CsmKindUtilities.isClass(scope)) {
                        res.add(UIDs.get((CsmClass)scope));
                    }
                }
            }
        }
        map.put(referencedClassUID, res);
        return res;
    }

    private synchronized Map<CsmUID<CsmClass>, Set<CsmUID<CsmClass>>> getOrCreateFullMap2(CsmProject project, long version) {
        if (lastVersion != version) {
            cache.clear();
        }
        lastVersion = version;
        CsmUID<CsmProject> prjUID = UIDs.get(project);
        Reference<Map<CsmUID<CsmClass>, Set<CsmUID<CsmClass>>>> outRef = cache.get(prjUID);
        Map<CsmUID<CsmClass>, Set<CsmUID<CsmClass>>> out = (outRef == null) ? null : outRef.get();
        if (out == null) {
            out = new HashMap<CsmUID<CsmClass>,Set<CsmUID<CsmClass>>>();
            cache.put(prjUID, new SoftReference<Map<CsmUID<CsmClass>, Set<CsmUID<CsmClass>>>>(out));
        }
        return out;
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
        for(CsmNamespace nns : ns.getNestedNamespaces()){
            buildSubHierarchy(nns, map);
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
