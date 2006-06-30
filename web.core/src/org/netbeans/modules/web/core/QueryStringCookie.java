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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.core;

import org.openide.nodes.Node;

/** Provides ability for a data object or node to be passed a query string.
* This is useful for, e.g., execution of internet objects and servlets.
* Empty marker interface, the real functionality is in WebExecSupport.
* Can be implemented by a DataObject or an Executor.
* @author  Petr Jiricka
* @version 1.00, Jun 03, 1999
*/
public interface QueryStringCookie extends Node.Cookie {

    public void setQueryString (String params) throws java.io.IOException;
}
