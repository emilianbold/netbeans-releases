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
import java.util.*;
import org.openide.nodes.*;

import  org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.classview.model.CVUtil.FillingDone;


/**
 * @author Vladimir Kvasihn
 */
public class ClassNode extends ClassifierNode {
    
    public ClassNode(CsmClass cls) {
        super(cls, new FillingDone());
        String shortName = cls.getName() + (cls.isTemplate() ? "<>" : ""); // NOI18N
        String longName = cls.getQualifiedName() + (cls.isTemplate() ? "<>" : ""); // NOI18N
        setName(shortName);
        setDisplayName(shortName);
        setShortDescription(longName);
    }
    
    protected void objectChanged() {
        final List nodes = new LinkedList();
        for( Iterator/*<CsmClass>*/ iter = ((CsmClass) getObject()).getMembers().iterator(); iter.hasNext(); ) {
            CsmMember member = (CsmMember) iter.next();
            if( CsmKindUtilities.isClass(member) ) {
                nodes.add(new ClassNode((CsmClass) member));
            } else if( CsmKindUtilities.isEnum(member) ) {
                nodes.add(new EnumNode((CsmEnum) member));
            } else {
                nodes.add(new MemberNode(member));
            }
        }
        final Children children = getChildren();
        children.MUTEX.writeAccess(new Runnable(){
            public void run() {
                children.remove(getChildren().getNodes());
                children.add( (Node[]) nodes.toArray(new Node[nodes.size()]) );
            }
        });
    }
}
