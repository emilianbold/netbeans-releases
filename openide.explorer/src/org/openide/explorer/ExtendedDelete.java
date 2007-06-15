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

package org.openide.explorer;

import org.openide.nodes.Node;
import java.io.IOException;

/**
 * Register implementation of this interface into META-INF services
 * lookup if you want to intercept Node deletion in explorer.
 * If more instances are registered, they are invoked in order
 * until one of them claim to have performed the action
 * by returning true. 
 *
 * @author Jan Becicka
 * @since 6.10
 */
public interface ExtendedDelete {
    
    /**
     * handle delete of nodes
     * @param nodes nodes to delete
     * @return true if delete was handled 
     *         false if delete was not handled
     * @throws IOException to signal some problem while performing the delete.
     *         The exception also means that the instance tried to handle
     *         node deletion and no further processing on the nodes
     *         should be done.
     */ 
    boolean delete(Node[] nodes) throws IOException;
        
}
