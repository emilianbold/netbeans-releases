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

import org.netbeans.tax.TreeDocumentRoot;
import org.netbeans.tax.TreeException;

/**
 *
 * @author Libor Kramolis
 * @version 0.1
 */
public interface TreeWriter {  //!!! the name is really confusing!
    
    /**
     * @param document (I would introduce it instead of calling
     *                 getWriter(Document) )
     */
    public void writeDocument () throws TreeException;
    
}
