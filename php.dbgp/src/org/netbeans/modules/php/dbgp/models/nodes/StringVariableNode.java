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
import org.netbeans.modules.php.dbgp.UnsufficientValueException;

import org.netbeans.modules.php.dbgp.models.VariablesModelFilter.FilterType;
import org.netbeans.modules.php.dbgp.packets.Property;
import org.openide.util.NbBundle;


/**
 * @author ads
 *
 */
class StringVariableNode extends 
    org.netbeans.modules.php.dbgp.models.VariablesModel.AbstractVariableNode 
{

    private static final String TYPE_STRING = "TYPE_String";        // NOI18N

    StringVariableNode( Property property , AbstractModelNode parent ) {
        super(property , parent );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.dbgp.models.nodes.AbstractVariableNode#getType()
     */
    @Override
    public String getType() {
        return NbBundle.getMessage( StringVariableNode.class , TYPE_STRING);
    }
    
    @Override
    protected boolean isTypeApplied( Set<FilterType> filters ) {
        return filters.contains( FilterType.SCALARS );
    }

    @Override
    public String getValue() throws UnsufficientValueException {
        StringBuilder sb = new StringBuilder();
        return sb.append("\"").append(super.getValue()).append("\"").toString(); //NOI18N
    }

}
