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

package org.netbeans.modules.cnd.classview.model;

import javax.swing.event.ChangeEvent;
import org.openide.nodes.*;

import  org.netbeans.modules.cnd.api.model.*;

/**
 * @author Vladimir Kvasihn
 */
public class TypedefNode extends ObjectNode {

    public TypedefNode(CsmTypedef typedef) {
	super(typedef, Children.LEAF);
        init(typedef);
    }

    public TypedefNode(CsmTypedef typedef, Children.Array key) {
	super(typedef, key);
        init(typedef);
    }
    
    private void init(CsmTypedef typedef){
        String shortName = typedef.getName();
        String longName = typedef.getQualifiedName();
        setName(shortName);
        setDisplayName(shortName);
        setShortDescription(longName);
    }

    public void stateChanged(ChangeEvent e) {
        Object o = e.getSource();
        if (o instanceof CsmTypedef){
            CsmTypedef cls = (CsmTypedef)o;
            setObject(cls);
            init(cls);
            fireIconChange();
            fireOpenedIconChange();
        } else if (o != null) {
            System.err.println("Expected CsmMember. Actually event contains "+o.toString());
        }
    }
}
