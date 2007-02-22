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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.xam.ui.customizer;

import org.netbeans.modules.xml.xam.Model;
import org.openide.nodes.Node;

/**
 * Nodes within ExternalReferenceCustomizer should implement this interface
 * to provide information about the data they represent.
 *
 * @author  Nathan Fiedler
 */
public interface ExternalReferenceNode {

    /**
     * Get a cookie for this node.
     *
     * @param  type  the representation class of the cookie.
     * @return  a cookie assignable to that class, or null if this node
     *          has no such cookie.
     */
    <T extends Node.Cookie> T getCookie(Class<T> type);

    /**
     * Returns the Model associated with this node, if it has one.
     *
     * @return  XAM model, or null if none or invalid.
     * @see #hasModel
     */
    Model getModel();

    /**
     * Returns the namespace for this node. If the node represents a file,
     * the value is the namespace for that file. If the node represents a
     * namespace, the value is that namespace.
     *
     * @return  namespace for this node, or null if none.
     */
    String getNamespace();

    /**
     * Indicates if this node represents a file that has a model. Even if
     * the model is not valid (i.e. the file is not well formed), this will
     * return true.
     *
     * @return  true if node represents a model, false otherwise.
     */
    boolean hasModel();
}
