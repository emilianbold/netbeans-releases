/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
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
	StringWriter stringWriter = new StringWriter();
	XMLStringResult result = new XMLStringResult (stringWriter);
	TreeStreamResult.TreeStreamWriter writer = 
            (TreeStreamResult.TreeStreamWriter)result.getWriter (null);

	writer.writeNode (node);
	return stringWriter.toString();
    }

}
