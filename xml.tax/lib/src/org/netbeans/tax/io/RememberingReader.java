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

/**
 * This class can remember what was read from underlayng Reader.
 * <p>
 * It is not a good idea to plug under a buffered Reader because
 * it would remember whole buffer or miss whole buffer if above
 * buffer will match.
 *
 * @author  Petr Kuzel
 * @version 
 */
public class RememberingReader extends Reader {

    private final Reader peer;
    private StringBuffer memory;
    
    /** Creates new RememberingReader */
    public RememberingReader(Reader peer) {
        this.peer = peer;        
    }

    /**
     * All subsequent reads
     */
    public void startRemembering() {
        memory = new StringBuffer();
    }
    
    public StringBuffer stopRemembering() {
        StringBuffer toret = memory;
        memory = null;
        return toret;
    }
    
    public void close() throws java.io.IOException {
        peer.close();
    }    
    
    public int read(char[] values, int off, int len) throws java.io.IOException {
        int toret = peer.read(values, off, len);
        if (memory != null && toret > 0) memory.append(values, off, toret);
        return toret;
    }
    
}
