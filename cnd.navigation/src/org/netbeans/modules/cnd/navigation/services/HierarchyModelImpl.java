/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.navigation.services;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.Action;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmInheritance;
import org.netbeans.modules.cnd.api.model.CsmMember;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;

/**
 *
 * @author Alexander Simon
 */
/*package-local*/ class HierarchyModelImpl implements HierarchyModel {
    private Map<CsmClass,Set<CsmClass>> myMap;
    private Action[] actions;
    private Action close;
       
    /** Creates a new instance of HierarchyModel */
    public HierarchyModelImpl(CsmClass cls, Action[] actions, boolean subDirection, boolean plain, boolean recursive) {
        this.actions = actions;
        if (subDirection) {
            myMap = buildSubHierarchy(cls);
        } else {
            myMap = buildSuperHierarchy(cls);
        }
        if (!recursive) {
            Set<CsmClass> result = myMap.get(cls);
            if (result == null){
                result = new HashSet<CsmClass>();
            }
            myMap = new HashMap<CsmClass,Set<CsmClass>>();
            myMap.put(cls,result);
        }
        if (plain) {
            Set<CsmClass> result = new HashSet<CsmClass>();
            gatherList(cls, result, myMap);
            myMap = new HashMap<CsmClass,Set<CsmClass>>();
            myMap.put(cls,result);
        }
    }
    
    public Map<CsmClass,Set<CsmClass>> getModel(){
        return myMap;
    }

    private void gatherList(CsmClass cls, Set<CsmClass> result, Map<CsmClass,Set<CsmClass>> map){
        Set<CsmClass> set = map.get(cls);
        if (set == null) {
            return;
        }
        for(CsmClass c : set){
            if (!result.contains(c)) {
                result.add(c);
                gatherList(c, result, map);
            }
        }
    }

    private Map<CsmClass,Set<CsmClass>> buildSuperHierarchy(CsmClass cls){
        HashMap<CsmClass,Set<CsmClass>> aMap = new HashMap<CsmClass,Set<CsmClass>>();
        buildSuperHierarchy(cls, aMap);
        return aMap;
    }
    
    private void buildSuperHierarchy(CsmClass cls, Map<CsmClass,Set<CsmClass>> map){
        Set<CsmClass> back = map.get(cls);
        if (back != null) {
            return;
        }
        back = new HashSet<CsmClass>();
        map.put(cls, back);
        List list = cls.getBaseClasses();
        if (list != null && list.size() >0){
            for(int i = 0; i < list.size(); i++){
                CsmInheritance inh = (CsmInheritance)list.get(i);
                CsmClass c = inh.getCsmClass();
                if (c != null) {
                    back.add(c);
                    buildSuperHierarchy(c, map);
                }
            }
        }
    }
    
    private Map<CsmClass,Set<CsmClass>> buildSubHierarchy(CsmClass cls){
        HashMap<CsmClass,Set<CsmClass>> aMap = new HashMap<CsmClass,Set<CsmClass>>();
        CsmProject prj = cls.getContainingFile().getProject();
        buildSubHierarchy(prj.getGlobalNamespace(), aMap);
        return aMap;
    }
    
    private void buildSubHierarchy(CsmNamespace ns, Map<CsmClass,Set<CsmClass>> map){
        for(Iterator it = ns.getNestedNamespaces().iterator(); it.hasNext();){
            buildSubHierarchy((CsmNamespace)it.next(), map);
        }
        for(Iterator it = ns.getDeclarations().iterator(); it.hasNext();){
            CsmDeclaration decl = (CsmDeclaration)it.next();
            if (CsmKindUtilities.isClass(decl)){
                buildSubHierarchy(map, (CsmClass)decl);
            }
        }
    }

    private void buildSubHierarchy(final Map<CsmClass, Set<CsmClass>> map, final CsmClass cls) {
        List list = cls.getBaseClasses();
        if (list != null && list.size() >0){
            for(int i = 0; i < list.size(); i++){
                CsmInheritance inh = (CsmInheritance)list.get(i);
                CsmClass c = inh.getCsmClass();
                if (c != null) {
                    Set<CsmClass> back = map.get(c);
                    if (back == null){
                        back = new HashSet<CsmClass>();
                        map.put(c,back);
                    }
                    back.add(cls);
                }
            }
        }
        for(CsmMember member : cls.getMembers()){
            if (CsmKindUtilities.isClass(member)){
                buildSubHierarchy(map, (CsmClass)member);
            }
        }
    }

    public Action[] getDefaultActions() {
        return actions;
    }

    public Action getCloseWindowAction() {
        return close;
    }

    public void setCloseWindowAction(Action close) {
        this.close = close;
    }
}
