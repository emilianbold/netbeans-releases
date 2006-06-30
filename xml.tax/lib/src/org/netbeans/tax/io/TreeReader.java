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
