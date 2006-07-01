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
import org.openide.text.Line;


/** Cookie for data objects that want to provide support for accessing
* lines in a document.
* Lines may change absolute position as changes are made around them in a document.
*
* @see Line
* @see org.openide.text.Line.Set
*
* @author Jaroslav Tulach
*/
public interface LineCookie extends Node.Cookie {
    /** Creates new line set.
    *
    * @return line set for current state of the node
    */
    public Line.Set getLineSet();
}
