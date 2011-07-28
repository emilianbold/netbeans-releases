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

import org.netbeans.modules.php.dbgp.ModelNode;
import org.openide.text.Line;



/**
 * @author ads
 *
 */
public interface VariableNode extends ModelNode {
    
    public static final String LOCAL_VARIABLE_ICON =
        "org/netbeans/modules/debugger/resources/localsView/LocalVariable"; // NOI18N
    
    /**
     * Differs from {@link #getName()}. Returns full name together with 
     * parent mention. F. e. for variable $arr['key'] string "$arr['key']"
     * will be returned, for variable $var : "$var". 
     * @return full name of variable
     */
    String getFullName();
    
    /**
     * Returns only short name of variable.
     * F.e. for $var : "var", for $arr['key'] : "key".
     * @return short name of variable or memeber.
     */
    @Override
    String getName();

    Line findDeclarationLine();

}
