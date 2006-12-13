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

import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.classview.Diagnostic;
import org.netbeans.modules.cnd.api.model.util.CsmSortUtilities;
import java.awt.Image;
import java.util.*;
import org.openide.nodes.*;
import org.openide.util.Utilities;

import  org.netbeans.modules.cnd.api.model.*;


/**
 * @author Vladimir Kvasihn
 */
public class ClassNode extends ObjectNode {

    public ClassNode(CsmClass cls) {
        super(cls, new Children.SortedArray());
        ((Children.SortedArray) getChildren()).setComparator(new CVUtil.MemberNodesComparator());
        String shortName = cls.getName() + (cls.isTemplate() ? "<>" : "");
        String longName = cls.getQualifiedName() + (cls.isTemplate() ? "<>" : "");
        setName(shortName);
        setDisplayName(shortName);
        setShortDescription(longName);
        fill();
    }
    
    public CsmClass getClazz() {
        return (CsmClass) getObject();
    }
    
    private void fill() {
        
        CsmClass cls = getClazz();
        
        List/*<CsmMember>*/ members =cls.getMembers();
        
        members = CsmSortUtilities.sortMembers(members, true);
        
        List nodes = new LinkedList();
        for( Iterator/*<CsmClass>*/ iter = cls.getMembers().iterator(); iter.hasNext(); ) {
	    CsmMember member = (CsmMember) iter.next();
	    if( CsmKindUtilities.isClass(member) ) {
		nodes.add(new ClassNode((CsmClass) member));
	    }
	    else if( CsmKindUtilities.isEnum(member) ) {
		nodes.add(new EnumNode((CsmEnum) member));
	    }
	    else {
		nodes.add(new MemberNode(member));
	    }
        }
	getChildren().remove(getChildren().getNodes());
        getChildren().add( (Node[]) nodes.toArray(new Node[nodes.size()]) );
    }

    protected void objectChanged() {
	fill();
    }
    
    public boolean update(CsmChangeEvent e) {
        if (!isDismissed()) {
            if( super.update(e) ) {
                return true;
            }
            else {
                for( Iterator iter = e.getNewDeclarations().iterator(); iter.hasNext(); ) {
                    CsmDeclaration decl = (CsmDeclaration) iter.next();
                    if( decl instanceof CsmMember ) {
                        if( getClazz().equals(((CsmMember) decl).getContainingClass()) ) {
                            objectChanged();
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
    
}
