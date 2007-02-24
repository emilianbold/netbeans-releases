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
 * DocumentationCookie.java
 *
 * Created on April 20, 2005, 1:35 PM
 */

package org.netbeans.modules.uml.project.ui.cookies;

import org.openide.nodes.Node;

/**
 * The documentation cookie is used to access the documentation for a node.
 * The documentation can be a JavaDoc comment, or a model element description.
 *
 * @author Trey Spiva
 */
public interface DocumentationCookie extends Node.Cookie
{
   /**
    * Retreive the documentation from the node.
    */
   public String getDocumentation();

   /**
    * Sets the documentation for the node.
    */
   public void setDocumentation(String retVal);
}
