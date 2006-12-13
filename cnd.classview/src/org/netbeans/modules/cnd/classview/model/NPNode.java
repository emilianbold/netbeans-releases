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

import java.awt.Image;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import org.netbeans.modules.cnd.api.model.util.CsmSortUtilities;
import org.openide.nodes.*;
import org.openide.util.Utilities;

import  org.netbeans.modules.cnd.api.model.*;


/**
 * Common functinality for ProjectNode and NamespaceNode
 * @author vk155633
 */
public abstract class NPNode extends BaseNode  {

    protected NPNode() {
        super(new Children.SortedArray());
        ((Children.SortedArray) getChildren()).setComparator(getComparator());
    }

    protected abstract CsmNamespace getNamespace();
    protected abstract Comparator getComparator();
    protected abstract boolean isSubNamspace(CsmNamespace ns);

    protected void fill() {
        CsmNamespace ns = getNamespace();
        if( ns != null ) {
            NodeUtil.fillNodeWithNamespaceContent(this, ns, true);
        }
    }

    /** Implements AbstractCsmNode.getData() */
    public CsmObject getCsmObject() {
	return getNamespace();
    }
    

    public boolean update(CsmChangeEvent e) {
        
        boolean updated = false;

        if (!isDismissed()) {
            if( ! e.getNewNamespaces().isEmpty() ) {
                List namespaces = new ArrayList();
                for( Iterator iter = e.getNewNamespaces().iterator(); iter.hasNext(); ) {
                    CsmNamespace ns = (CsmNamespace) iter.next();
                    if( isSubNamspace(ns) ) {
                        namespaces.add(ns);
                    }
                }
                if( ! namespaces.isEmpty() ) {
                    addDeclarations(namespaces, true);
                    updated = true;
                }
            }
            List decls = new ArrayList();
            for( Iterator iter = e.getNewDeclarations().iterator(); iter.hasNext(); ) {
                if(isDismissed()){
                    break;
                }
                CsmDeclaration decl = (CsmDeclaration) iter.next();
                CsmDeclaration.Kind kind = decl.getKind();
                if( kind == CsmDeclaration.Kind.CLASS || kind == CsmDeclaration.Kind.STRUCT || kind == CsmDeclaration.Kind.UNION ) {
                    if( ((CsmClass) decl).getContainingNamespace() == getNamespace() ) {
                        //fill();
                        //return true;
//                      if( "Cursor".equals(decl.getName()) ) {
//                            System.err.println("Cursor");
//                        }
                        //addDeclaration(decl);
                        decls.add(decl);
                        updated = true;
                    }
                }
                else if( kind == CsmDeclaration.Kind.ENUM ) {
                    if( ((CsmEnum) decl).getScope() == getNamespace() ) {
                        //fill();
                        //return true;
                        //addDeclaration(decl);
                        decls.add(decl);
                        updated = true;
                    }
                }
                else if( kind == CsmDeclaration.Kind.FUNCTION || kind == CsmDeclaration.Kind.FUNCTION_DEFINITION ) {
                    if( ((CsmFunction) decl).getScope() == getNamespace() ) {
                        //fill();
                        //return true;
                        //addDeclaration(decl);
                        decls.add(decl);
                        updated = true;
                    }
                }
                else if( kind == CsmDeclaration.Kind.VARIABLE/* || kind == CsmDeclaration.Kind.VARIABLE_DEFINITION*/ ) {
                    if( ((CsmVariable) decl).getScope() == getNamespace() ) {
                        //fill();
                        //return true;
                        //addDeclaration(decl);
                        decls.add(decl);
                        updated = true;
                    }
                }
                else if( kind == CsmDeclaration.Kind.TYPEDEF ) {
                    if( ((CsmTypedef) decl).getScope() == getNamespace() ) {
                        //fill();
                        //return true;
                        //addDeclaration(decl);
                        decls.add(decl);
                        updated = true;
                    }
                }
            }
            addDeclarations(decls, false);
            decls = new ArrayList();
            for( Iterator iter = e.getChangedDeclarations().iterator(); iter.hasNext(); ) {
                if(isDismissed()){
                    break;
                }
                CsmDeclaration decl = (CsmDeclaration) iter.next();
                CsmDeclaration.Kind kind = decl.getKind();
                if( kind == CsmDeclaration.Kind.CLASS || kind == CsmDeclaration.Kind.STRUCT ||
                    kind == CsmDeclaration.Kind.UNION || kind == CsmDeclaration.Kind.ENUM ) {
                    if( ((CsmCompoundClassifier) decl).getContainingNamespace() == getNamespace() ) {
                        decls.add(decl);
                        updated = true;
                    }
                }
            }
            changeDeclarations(decls);
            updated |= super.update(e);
        }
        return updated;
    }
    
    private void changeDeclarations(List decls) {
       if (!isDismissed() && decls.size()>0){
            final List nodes = new ArrayList();
            for( int i = 0; i < decls.size(); i++) {
                BaseNode node = NodeUtil.createNode((CsmClassifier)decls.get(i));
                if (node != null){
                    nodes.add(node);
                }
            }
            if (nodes.size()>0){
                final Children children = getChildren();
                children.MUTEX.writeAccess(new Runnable(){
                    public void run() {
                        HashMap map = new HashMap();
                        Node[] arr = children.getNodes();
                        for(int i = 0; i < arr.length; i++){
                            CsmObject x = ((BaseNode)arr[i]).getCsmObject();
                            if (x instanceof CsmClassifier){
                                CsmClassifier cls = (CsmClassifier) ((BaseNode)arr[i]).getCsmObject();
                                map.put(((CsmClassifier)x).getQualifiedName(),arr[i]);
                            }
                        }
                        List toRemove = new ArrayList();
                        for(int i = 0; i < nodes.size(); i++){
                            BaseNode n = (BaseNode)nodes.get(i);
                            CsmClassifier x  = (CsmClassifier)n.getCsmObject();
                            Node oldNode = (Node) map.get(x.getQualifiedName());
                            if (oldNode != null){
                                toRemove.add(oldNode);
                                //System.out.println("Replace duplicated node "+oldNode.getDisplayName());
                            }
                        }
                        if (toRemove.size()>0){
                            children.remove((Node[])toRemove.toArray(new Node[toRemove.size()]));
                        }
                        children.add((Node[])nodes.toArray(new Node[nodes.size()]));
                    }
                });
            }
       }
    }

    private void addDeclarations(List decls, boolean isNamespaces) {
       if (!isDismissed() && decls.size()>0){
            final List nodes = new ArrayList();
            for( int i = 0; i < decls.size(); i++) {
                BaseNode node;
                if (isNamespaces) {
                    node = new NamespaceNode((CsmNamespace)decls.get(i), false);
                } else {
                    node = NodeUtil.createNode((CsmDeclaration)decls.get(i));
                }
                if (node != null){
                    nodes.add(node);
                }
            }
            if (nodes.size()>0){
                final Children children = getChildren();
                children.MUTEX.writeAccess(new Runnable(){
                    public void run() {
                        HashMap map = new HashMap();
                        Node[] arr = children.getNodes();
                        for(int i = 0; i < arr.length; i++){
                            CsmObject obj = ((BaseNode)arr[i]).getCsmObject();
                            map.put(obj,arr[i]);
                            if (obj instanceof CsmDeclaration){
                                CsmDeclaration.Kind kind = ((CsmDeclaration)obj).getKind();
                                if (kind == CsmDeclaration.Kind.FUNCTION_DEFINITION){
                                    CsmFunctionDefinition def = (CsmFunctionDefinition)obj;
                                    CsmFunction func = def.getDeclaration();
                                    if (func != null){
                                        map.put(func,arr[i]);
                                    }
                                }
                            }
                        }
                        List toRemove = new ArrayList();
                        for(int i = 0; i < nodes.size(); i++){
                            BaseNode n = (BaseNode)nodes.get(i);
                            Node oldNode = (Node) map.get(n.getCsmObject());
                            if (oldNode != null){
                                toRemove.add(oldNode);
                                //System.out.println("Replace duplicated node "+oldNode.getDisplayName());
                            }
                        }
                        if (toRemove.size()>0){
                            children.remove((Node[])toRemove.toArray(new Node[toRemove.size()]));
                        }
                        children.add((Node[])nodes.toArray(new Node[nodes.size()]));
                    }
                });
            }
       }
    }
}
