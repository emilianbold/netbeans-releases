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
package org.netbeans.modules.php.dbgp.models.nodes;

import java.util.Set;

import org.netbeans.modules.php.dbgp.models.VariablesModelFilter.FilterType;
import org.netbeans.modules.php.dbgp.packets.Property;
import org.openide.util.NbBundle;


/**
 * @author ads
 *
 */
class ObjectVariableNode extends 
    org.netbeans.modules.php.dbgp.models.VariablesModel.AbstractVariableNode {
    protected static final String OBJECT_ICON =
        "org/netbeans/modules/debugger/resources/localsView/LocalVariablesGroup";//NOI18N

    private static final String TYPE_OBJECT = "TYPE_Object";                 // NOI18N
    
    private static final String THIS        = "$this";                       // NOI18N
    
    private static final String SELF        = "self";                        // NOI18N

    ObjectVariableNode( Property property , AbstractModelNode parent ) {
        super(property , parent );
    }

    @Override
    public String getType() {
        return NbBundle.getMessage( ObjectVariableNode.class, TYPE_OBJECT);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.php.dbgp.models.nodes.AbstractVariableNode#getName()
     */
    @Override
    public String getIconBase() {
        String name = getProperty().getName();
        if ( THIS.equals(name) || SELF.equals(name)) {
            return FIELD_ICON;
        }
        return OBJECT_ICON;
    }
    
    protected boolean isTypeApplied( Set<FilterType> filters ) {
        return filters.contains( FilterType.OBJECT );
    }
}
