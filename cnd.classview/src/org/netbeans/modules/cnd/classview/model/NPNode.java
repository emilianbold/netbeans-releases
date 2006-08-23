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
        NodeUtil.fillNodeWithNamespaceContent(this, getNamespace(), true);
    }

    /** Implements AbstractCsmNode.getData() */
    public CsmObject getCsmObject() {
	return getNamespace();
    }
    
    
    public boolean update(CsmChangeEvent e) {
        
        boolean updated = false;
        
        if( ! e.getNewNamespaces().isEmpty() ) {
            List namespaces = new ArrayList();
            for( Iterator iter = e.getNewNamespaces().iterator(); iter.hasNext(); ) {
                CsmNamespace ns = (CsmNamespace) iter.next();
                if( isSubNamspace(ns) ) {
                    namespaces.add(new NamespaceNode(ns, false));
                }
            }
            if( ! namespaces.isEmpty() ) {
                NamespaceNode[] nsNodes = (NamespaceNode[]) namespaces.toArray(new NamespaceNode[0]);
                for (int i = 0; i < nsNodes.length; i++) {
                    nsNodes[i].update(e);
                };
                this.getChildren().add(nsNodes);
            }
        }

        for( Iterator iter = e.getNewDeclarations().iterator(); iter.hasNext(); ) {
            CsmDeclaration decl = (CsmDeclaration) iter.next();
            CsmDeclaration.Kind kind = decl.getKind();
            if( kind == CsmDeclaration.Kind.CLASS || kind == CsmDeclaration.Kind.STRUCT || kind == CsmDeclaration.Kind.UNION ) {
                if( ((CsmClass) decl).getContainingNamespace() == getNamespace() ) {
                    //fill();
                    //return true;
//                    if( "Cursor".equals(decl.getName()) ) {
//                        System.err.println("Cursor");
//                    }
                    addDeclaration(decl);
                    updated = true;
                }
            }
            else if( kind == CsmDeclaration.Kind.ENUM ) {
                if( ((CsmEnum) decl).getScope() == getNamespace() ) {
                    //fill();
                    //return true;
                    addDeclaration(decl);
                    updated = true;
                }
            }
            else if( kind == CsmDeclaration.Kind.FUNCTION || kind == CsmDeclaration.Kind.FUNCTION_DEFINITION ) {
                if( ((CsmFunction) decl).getScope() == getNamespace() ) {
                    //fill();
                    //return true;
                    addDeclaration(decl);
                    updated = true;
                }
            }
            else if( kind == CsmDeclaration.Kind.VARIABLE ) {
                if( ((CsmVariable) decl).getScope() == getNamespace() ) {
                    //fill();
                    //return true;
                    addDeclaration(decl);
                    updated = true;
                }
            }
            else if( kind == CsmDeclaration.Kind.TYPEDEF ) {
                if( ((CsmTypedef) decl).getScope() == getNamespace() ) {
                    //fill();
                    //return true;
                    addDeclaration(decl);
                    updated = true;
                }
            }
        }
        updated |= super.update(e);
        
        return updated;
    }

    protected void addDeclaration(CsmDeclaration decl) {
        ObjectNode node = NodeUtil.createNode(decl);
        if( node != null ) {
            this.getChildren().add(new Node[] {node});
        }
    }
   
}
