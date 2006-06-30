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
package org.netbeans.tax.io;

import java.io.StringWriter;

import org.netbeans.tax.TreeException;
import org.netbeans.tax.TreeNode;

/**
 * Converts any arbitrary node and its subnodes to their String representation.
 *
 * @author Libor Kramolis
 * @version 0.1
 */
public final class XMLStringResult extends TreeStreamResult {

    //
    // init
    //

    /** Creates new XMLStringResult. */
    private XMLStringResult (StringWriter stringWriter) {
        super (stringWriter);
    }


    //
    // static utils
    //

    /**
     * @param node to be ddeply converted to its String representation.
     */
    public static final String toString (TreeNode node) throws TreeException {
        StringWriter stringWriter = new StringWriter ();
        XMLStringResult result = new XMLStringResult (stringWriter);
        TreeStreamResult.TreeStreamWriter writer =
        (TreeStreamResult.TreeStreamWriter)result.getWriter (null);
        
        writer.writeNode (node);
        return stringWriter.toString ();
    }
    
}
