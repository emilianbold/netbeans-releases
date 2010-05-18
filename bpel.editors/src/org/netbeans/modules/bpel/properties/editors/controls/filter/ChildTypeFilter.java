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
package org.netbeans.modules.bpel.properties.editors.controls.filter;

import org.netbeans.modules.bpel.editors.api.nodes.NodeType;

/**
 * Represents a filter which can be asked if the particular child Node
 * with the type childType is allowed at the particular parent node
 * with the type parentClass.
 *
 * The filter is generally intended to hide/show part of nodes dinamically.
 *
 * @author nk160297
 */
public interface ChildTypeFilter {

    boolean isPairAllowed(NodeType parentType, NodeType childType);
}
