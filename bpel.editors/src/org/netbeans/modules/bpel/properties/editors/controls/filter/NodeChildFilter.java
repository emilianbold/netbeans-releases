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

import org.openide.nodes.Node;

/**
 * Represents a filter which can be asked if the particular child Node
 * is allowed at the particular parent node.
 *
 * The filter is generally intended to show part of model at a TreeView.
 * For example, BPEL Process has many different children like Variables,
 * Activities, Handlers. But in a particular situation it's necessary to
 * show only Variables. Sometimes it's necessary to show not all variables,
 * but only of a particular type. 
 *
 * @author nk160297
 */
public interface NodeChildFilter {
    
    boolean isPairAllowed(Node parentNode, Node childNode);
}
