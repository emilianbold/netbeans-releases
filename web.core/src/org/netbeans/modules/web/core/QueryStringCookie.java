/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
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

}
