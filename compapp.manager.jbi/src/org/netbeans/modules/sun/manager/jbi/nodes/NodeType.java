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

package org.netbeans.modules.sun.manager.jbi.nodes;

import org.openide.util.NbBundle;

/**
 * Enumeration of node types.
 * 
 * @author jqian
 */
public enum NodeType { 
    JBI,
    SERVICE_ENGINES, 
    BINDING_COMPONENTS, 
    SHARED_LIBRARIES,
    SERVICE_ASSEMBLIES, 
    SERVICE_ENGINE, 
    BINDING_COMPONENT, 
    SHARED_LIBRARY, 
    SERVICE_ASSEMBLY, 
    SERVICE_UNIT;
    
    public String getDisplayName() {
        return NbBundle.getMessage(NodeType.class, this.toString());
    }
    
    public String getShortDescription() {
        return NbBundle.getMessage(NodeType.class, this.toString() + "_SHORT_DESC"); // NOI18N
    }    
}
            

