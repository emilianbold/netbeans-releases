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

package org.netbeans.modules.cnd.execution41.org.openide.cookies;

import org.openide.nodes.Node;

// XXX huh? --jglick
/* This interface signals that this cookie is able to "start"
* itself. Any operations needed for the start must be implemented
* by this cookie itself.
* <P>
* This is the difference with ExecCookie that only provides information
* about which class to start and does not do any other actions. They
* are handled by execution.
* <P>
* The ExecAction should react to the ExecCookie and also to this StartCookie.
*
* @see ExecCookie
* @see StartCookie
*/
/**
 * Cookie for objects which may be executed.
*
* @author Jaroslav Tulach
* @version 0.10, Jul 27, 1998
*/
public interface ExecCookie extends Node.Cookie {
    /** Start execution.
    */
    public void start ();
}
