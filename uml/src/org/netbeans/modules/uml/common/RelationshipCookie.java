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

/*
 * RelationshipCookie.java
 *
 * Created on July 29, 2005, 5:26 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.netbeans.modules.uml.common;

import org.openide.nodes.Node;

/* this class was added to distinguish relationship node from regular model node,
 * ideally, it should be placed in UMLProject module, however, UMLProject module
 * dependes on core module, it would not be accessible from core, so I have to put
 * it here as a temporary solution, I tend to think UMLProject module and core module 
 * are interdependent, maybe they should be in one module?
 */

public class RelationshipCookie implements Node.Cookie{
	
	public RelationshipCookie()
	{
	}
	
}
