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

/* $Id$ */

package org.netbeans.modules.form;

import org.openide.nodes.Node;

/** Cookie for Form operations.
 *
 * @author Ian Formanek
 * @version 0.10, May 26, 1998
 */
public interface FormCookie extends Node.Cookie {

    /** Focuses the source editor */
    public void gotoEditor();

    /** Focuses the form */
    public void gotoForm();

}
