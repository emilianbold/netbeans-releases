/*
 *                         Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License Version
 * 1.0 (the "License"). You may not use this file except in compliance with 
 * the License. A copy of the License is available at http://www.sun.com/
 *
 * The Original Code is the Ant module
 * The Initial Developer of the Original Code is Jayme C. Edwards.
 * Portions created by Jayme C. Edwards are Copyright (c) 2000.
 * All Rights Reserved.
 *
 * Contributor(s): Jesse Glick.
 */
 
package org.apache.tools.ant.module.api;

import org.w3c.dom.Element;

import org.openide.nodes.Node;

/** Represents something with an associated Document Object Model element.
 */
public interface ElementCookie extends Node.Cookie {
    
    /** Get the associated DOM element.
     * @return the element
     */
    Element getElement ();
    
}
