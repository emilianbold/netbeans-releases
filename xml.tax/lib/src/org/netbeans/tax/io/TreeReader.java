/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.tax.io;

import java.io.*;

import org.netbeans.tax.TreeDocumentRoot;
import org.netbeans.tax.TreeException;
import org.netbeans.tax.io.TreeStreamResult;
import org.netbeans.tax.io.TreeWriter;

/**
 * Access tree content using reader. The tree MUST NOT change until the
 * reader is closed. It is responsibility of caller.
 *
 * @author  Petr Kuzel
 * @version 0.9
 */
public class TreeReader extends Reader {
    
    private final StringReader reader;
    
    /**
     * Creates new TreeReader that is immediatelly ready for reading.
     */
    public TreeReader (TreeDocumentRoot doc) throws IOException {
        
        reader = new StringReader (Convertors.treeToString (doc));
        
    }
    
    public void close () throws IOException {
        if (reader == null) throw new IOException (Util.THIS.getString ("EXC_null_reader"));
        reader.close ();
    }
    
    public int read (char[] cbuf, int off, int len) throws IOException {
        if (reader == null) throw new IOException (Util.THIS.getString ("EXC_null_reader"));
        return reader.read (cbuf, off, len);
    }
}
