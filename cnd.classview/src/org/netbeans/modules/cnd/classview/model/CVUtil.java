/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.classview.model;

import java.util.*;
import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.openide.nodes.*;

/**
 * Misc static utilitiy functions
 * @author Vladimir Kvasihn
 */
public class CVUtil {
    
    public static String getSignature(CsmFunction fun) {
        StringBuffer sb = new StringBuffer(fun.getName());
        sb.append('(');
        boolean addComma = false;
        for( Iterator iter = fun.getParameters().iterator(); iter.hasNext(); ) {
            CsmParameter par = (CsmParameter) iter.next();
            if( addComma ) {
                sb.append(", "); // NOI18N
            } else {
                addComma = true;
            }
            //sb.append(par.getText());
            CsmType type = par.getType();
            if( type != null ) {
                sb.append(type.getText());
                //sb.append(' ');
            } else if (par.isVarArgs()){
                sb.append("..."); // NOI18N
            }
            // Signature should't contain parameter name
            //sb.append(par.getName());
        }
        
        sb.append(')');
        return sb.toString();
    }
    
    public static Node createLoadingRoot() {
        Children.Array children = new Children.SortedArray();
        children.add(new Node[] { createLoadingNode() });
        AbstractNode root = new AbstractNode(children);
        return root;
    }
    
    public static Node createLoadingNode() {
        BaseNode node = new LoadingNode();
        return node;
    }
    
    public static final class ClassViewComparator implements Comparator {
        public ClassViewComparator() {
        }
        
        public int compare(Object o1, Object o2) {
            if (o1 == o2) {
                return 0;
            }
            if( (o1 instanceof BaseNode) || o2 instanceof BaseNode) {
                if( ! ( o2 instanceof BaseNode ) ) {
                    return -1;
                } else if( ! ( o1 instanceof BaseNode ) ) {
                    return +1;
                }
                int w1 = ((BaseNode)o1).getWeight();
                int w2 = ((BaseNode)o2).getWeight();
                if (w1!=w2){
                    return w1-w2;
                }
            }
             return compareName(o1, o2);
        }
        
        private int compareName(Object o1, Object o2){
            if( (o1 instanceof Node) && (o2 instanceof Node) ) {
                return ((Node) o1).getDisplayName().compareTo(((Node) o2).getDisplayName());
            }
            return 0;
        }
    }
    
    public static class FillingDone {
        boolean isFillingDone = false;
        public void setFillingDone(){
            isFillingDone = true;
        }
        public boolean isFillingDone(){
            return isFillingDone;
        }
    }
    
    public static class LazyNamespaceSorteddArray extends Children.SortedArray {
        private String id;
        private FillingDone inited;
        private CsmProject project;
        
        public LazyNamespaceSorteddArray(CsmProject prj, CsmNamespace obj, FillingDone init){
            project = prj;
            id = obj.getQualifiedName();
            inited = init;
        }
        
        protected Collection initCollection() {
            return init();
        }
        
        private Collection init(){
            synchronized (inited){
                inited.setFillingDone();
                CsmNamespace ns = lookup(id);
                List res = initNamespace(ns);
                Collections.sort(res);
                //if (ns != null) {
                //    System.out.println("Inited "+ns.getName()+" contains "+res.size()+" elements");
                //}
                return res;
            }
        }
        
        private CsmNamespace lookup(String key){
            return  project.findNamespace(key);
        }
        
        private List initNamespace(CsmNamespace namespace){
            List res = new LinkedList();
            if (namespace != null){
                for( Iterator/*<CsmNamespace>*/ iter = namespace.getNestedNamespaces().iterator(); iter.hasNext(); ) {
                    res.add(new NamespaceNode(project, (CsmNamespace) iter.next()));
                }
                Collection/*<CsmDeclaration>*/ decl = namespace.getDeclarations();
                if (decl != null) {
                    for( Iterator/*<CsmDeclaration>*/ iter = decl.iterator(); iter.hasNext(); ) {
                        CsmDeclaration d = (CsmDeclaration) iter.next();
                        ObjectNode declNode = NodeUtil.createNode(d);
                        if( declNode != null ) {
                            res.add(declNode);
                        }
                    }
                }
            }
            return res;
        }
    }
    
    
    public static class LazyClassifierSortedArray extends Children.SortedArray {
        private CsmProject project;
        private FillingDone inited;
        private String id;
        private CsmCompoundClassifier unnamedClassifier;
        
        public LazyClassifierSortedArray(CsmCompoundClassifier obj, FillingDone init){
            id = obj.getQualifiedName();
            project = obj.getContainingFile().getProject();
            inited = init;
            if (obj.getName().length()==0){
                unnamedClassifier = obj;
            }
        }
        
        protected Collection initCollection() {
            return init();
        }
        
        private Collection init(){
            synchronized (inited){
                inited.setFillingDone();
                List res = null;
                CsmClassifier cls = lookup(id);
                if (CsmKindUtilities.isClass(cls)){
                    res = initClass((CsmClass)cls);
                } else if (CsmKindUtilities.isEnum(cls)){
                    res = initEnum((CsmEnum)cls);
                } else {
                    return new ArrayList();
                }
                Collections.sort(res);
                //if (cls != null) {
                //    System.out.println("Inited "+cls.getName()+" contains "+res.size()+" elements");
                //}
                return res;
            }
        }
        
        private CsmClassifier lookup(String key){
            CsmClassifier res = project.findClassifier(key);
            if (res == null) {
                res = unnamedClassifier;
            }
            return res;
        }
        
        private List initClass(CsmClass cls){
            List nodes = new LinkedList();
            for( Iterator/*<CsmClass>*/ iter = cls.getMembers().iterator(); iter.hasNext(); ) {
                CsmMember member = (CsmMember) iter.next();
                ObjectNode declNode = null;
                if( CsmKindUtilities.isClass(member) ) {
                    declNode = new ClassNode((CsmClass) member);
                } else if( CsmKindUtilities.isEnum(member) ) {
                    declNode = new EnumNode((CsmEnum) member);
                } else {
                    declNode = new MemberNode(member);
                }
                if( declNode != null ) {
                    nodes.add(declNode);
                }
            }
            return nodes;
        }
        
        private List initEnum(CsmEnum en){
            List nodes = new LinkedList();
            for (Iterator iter = en.getEnumerators().iterator(); iter.hasNext();) {
                nodes.add(new EnumeratorNode((CsmEnumerator) iter.next()));
            }
            return nodes;
        }
    }
}
