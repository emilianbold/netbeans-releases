/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.apache.tools.ant.module.api;

import org.w3c.dom.Element;

import org.openide.nodes.Node;

/** Represents something with an associated Document Object Model element.
 * @deprecated Not currently used in Ant module UI.
 */
public interface ElementCookie extends Node.Cookie {
    
    /** Get the associated DOM element.
     * @return the element
     */
    Element getElement ();
    
}
