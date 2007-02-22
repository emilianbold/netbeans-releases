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

package org.netbeans.modules.xml.schema.ui.nodes.categorized;

import org.netbeans.modules.xml.schema.model.GlobalType;

/**
 * A marker for child filter lists such that the client can explicitly
 * request that the primitive simple types node is included in the
 * node tree created by a node factory. Use this like any other child
 * filter (e.g. GlobalSimpleType.class), to include the Primitive
 * Simple Types node and its children. Since no schema components will
 * implement this interface, it has no other effect on the nodes created
 * by the factory.
 *
 * @author  Nathan Fiedler
 */
public interface PrimitiveSimpleType extends GlobalType {
}
