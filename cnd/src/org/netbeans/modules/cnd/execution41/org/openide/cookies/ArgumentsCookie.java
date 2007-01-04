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

/** Provides ability for a data object or node to be passed runtime arguments.
* This is useful for, e.g., execution of a standalone class.
* @see org.openide.util.Utilities#parseParameters
* @author  Ian Formanek
* @version 1.00, Sep 04, 1998
*/
public interface ArgumentsCookie extends Node.Cookie {
    /** Get the arguments.
     * @return the arguments. May be empty but not <code>null</code>.
     */
    public String[] getArguments ();

    /** Set the arguments.
     * @param args the arguments. May be empty but not <code>null</code>.
     * @throws IOException if the arguments could not be set
     */
    public void setArguments (String[] args) throws java.io.IOException;
}
