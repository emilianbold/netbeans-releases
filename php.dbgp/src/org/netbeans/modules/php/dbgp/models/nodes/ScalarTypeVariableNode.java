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
class ScalarTypeVariableNode extends 
    org.netbeans.modules.php.dbgp.models.VariablesModel.AbstractVariableNode 
{
    
    private static final String TYPE_FLOAT      = "TYPE_Float";     // NOI18N

    private static final String TYPE_INT        = "TYPE_Int";       // NOI18N

    private static final String TYPE_BOOLEAN    = "TYPE_Boolean";   // NOI18N

    public static final String BOOLEAN          = "boolean";        // NOI18N 
    
    public static final String BOOL             = "bool";           // NOI18N
    
    public static final String INTEGER          = "integer";        // NOI18N
    
    public static final String INT              = "int";            // NOI18N
    
    public static final String FLOAT            = "float";          // NOI18N

    ScalarTypeVariableNode( Property property , AbstractModelNode parent ) {
        super(property , parent );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.php.dbgp.models.nodes.AbstractVariableNode#getType()
     */
    @Override
    public String getType() {
        String type = super.getType();
        String bundleKey;
        if ( BOOLEAN.equals(type) || BOOL.equals( type )) {
            bundleKey = TYPE_BOOLEAN;
        }
        else if ( INTEGER.equals( type ) || INT.equals( type )) {
            bundleKey = TYPE_INT;
        }
        else if ( FLOAT.equals( type )) {
            bundleKey = TYPE_FLOAT;
        }
        else {
            assert false;
            bundleKey = null;
        }
        return NbBundle.getMessage( ScalarTypeVariableNode.class , bundleKey);
    }
    
    @Override
    protected boolean isTypeApplied( Set<FilterType> filters ) {
        return filters.contains( FilterType.SCALARS );
    }

}
