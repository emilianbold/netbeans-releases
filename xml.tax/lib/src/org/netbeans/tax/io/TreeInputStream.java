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
        if (input == null) throw new IOException (Util.THIS.getString ("EXC_null_input"));
        input.close ();
    }
    
    public int read () throws IOException {
        if (input == null) throw new IOException (Util.THIS.getString ("EXC_null_input"));
        int ch = input.read ();
        return ch;
    }
}
