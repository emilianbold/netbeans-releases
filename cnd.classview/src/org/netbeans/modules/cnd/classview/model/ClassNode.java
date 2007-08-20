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
import org.netbeans.modules.cnd.api.model.*;
import org.openide.nodes.Children;

/**
 * @author Vladimir Kvasihn
 */
public class ClassNode extends ClassifierNode {
    
    public ClassNode(CsmClass cls, Children.Array key) {
        super(cls, key);
        init(cls);
    }
    
    private void init(CsmClass cls){
        String shortName = cls.isTemplate() ? ((CsmTemplate)cls).getDisplayName() : cls.getName(); // NOI18N
        String longName = cls.getQualifiedName() + (cls.isTemplate() ? "<>" : ""); // NOI18N
        setName(shortName);
        setDisplayName(shortName);
        setShortDescription(longName);
    }

    public void stateChanged(ChangeEvent e) {
        Object o = e.getSource();
        if (o instanceof CsmClass){
            CsmClass cls = (CsmClass)o;
            setObject(cls);
            init(cls);
            fireIconChange();
            fireOpenedIconChange();
        } else if (o != null) {
            System.err.println("Expected CsmClass. Actually event contains "+o.toString());
        }
    }
}
