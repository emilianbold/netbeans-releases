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

package org.netbeans.modules.cnd.navigation.classhierarchy;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.modelutil.AbstractCsmNode;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.openide.nodes.Children;

/**
 * Hierarchy Tree node.
 */
public class HierarchyNode extends AbstractCsmNode{
    private CsmClass object;
    public HierarchyNode(CsmClass element, HierarchyModel model) {
        this(element, new HierarchyChildren(element, model));
    }
    
    public HierarchyNode(CsmClass element, Children children) {
        super(children);
        setName(element.getName());
        object = element;
    }
    
    public CsmObject getCsmObject() {
        return object;
    }
    
    @Override
    public Action getPreferredAction() {
        if (object.isValid()) {
            if (CsmKindUtilities.isOffsetable(object)){
                return new AbstractAction(){
                    public void actionPerformed(ActionEvent e) {
                        CsmUtilities.openSource((CsmOffsetable)object);
                    }
                };
            }
        }
        return super.getPreferredAction();
    }
}