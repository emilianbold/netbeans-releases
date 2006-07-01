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
package org.openide.cookies;

import org.openide.nodes.Node;


/** Cookie for node groups which can somehow be filtered.
 * This would be applied to a subclass of {@link org.openide.nodes.Children Children}.
* @deprecated Use Looks instead.
* @author Jaroslav Tulach, Jan Jancura, Dafe Simonek
*/
public interface FilterCookie extends Node.Cookie {
    /** Get the declared filter (super-)class.
     * @return the class, or may be <code>null</code> if no filter is currently in use
    */
    public Class getFilterClass();

    /** Get the current filter.
     * @return the filter, or <code>null</code> if none is currently in use
    */
    public Object getFilter();

    /** Set the current filter.
    * @param filter the filter, or <code>null</code> if none should be used
    */
    public void setFilter(Object filter);
}
