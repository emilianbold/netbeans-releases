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

import java.io.*;

import org.netbeans.tax.TreeDocumentRoot;
import org.netbeans.tax.TreeException;
import org.netbeans.tax.io.TreeStreamResult;
import org.netbeans.tax.io.TreeWriter;

/**
 * Access tree content using input stream. The tree MUST NOT change until the
 * stream is closed. It is responsibility of caller.
 *
 * @author  Petr Kuzel
 * @version 0.9
 */
public class TreeInputStream extends InputStream {
    
    private final ByteArrayInputStream input;
    
    /**
     * Creates new TreeInputStream that is immediatelly ready for reading.
     */
    public TreeInputStream (TreeDocumentRoot doc) throws IOException {
        input = new ByteArrayInputStream (Convertors.treeToByteArray (doc));
    }
    
    public void close () throws IOException {
        if (input == null) throw new IOException (Util.getString ("EXC_null_input"));
        input.close ();
    }
    
    public int read () throws IOException {
        if (input == null) throw new IOException (Util.getString ("EXC_null_input"));
        int ch = input.read ();
        return ch;
    }
}
